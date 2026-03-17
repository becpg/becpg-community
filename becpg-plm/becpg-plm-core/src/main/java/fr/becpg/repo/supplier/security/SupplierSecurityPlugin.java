package fr.becpg.repo.supplier.security;

import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.plugins.SecurityServicePlugin;
import fr.becpg.repo.security.filter.SecurityContextHelper;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * Security plugin for supplier portal access control.
 *
 * <p>Determines write/read access for external supplier users based on
 * workflow task assignment. The supplier node is included in the workflow
 * package at start time, so {@code getWorkflowIdsForContent(supplierNodeRef)}
 * correctly finds the active workflow when the wizard is opened with the
 * supplier nodeRef directly.</p>
 *
 * @author matthieu
 */
@Service
public class SupplierSecurityPlugin implements SecurityServicePlugin {

	private static final Log logger = LogFactory.getLog(SupplierSecurityPlugin.class);

	@Autowired
	@Lazy
	private SupplierPortalService supplierPortalService;

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	@Autowired
	@Qualifier("workflowPackageImpl")
	private WorkflowPackageComponent workflowPackageComponent;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	private static final String SUPPLIER_WIZARD_PREFIX = "/share/page/wizard?id=supplier-";

	/** {@inheritDoc} */
	@Override
	public boolean checkIsInSecurityGroup(NodeRef entityNodeRef, List<NodeRef> groups) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(QName nodeType) {
		return AuthorityHelper.isCurrentUserExternal() && entityDictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITY_V2);
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxAccessMode(NodeRef entityNodeRef) {
		if (entityNodeRef == null) {
			return SecurityService.WRITE_ACCESS;
		}

		NodeRef supplierNodeRef = supplierPortalService.getSupplierNodeRef(entityNodeRef);
		if ((supplierNodeRef == null) || !supplierPortalService.isCurrentUserInSupplierGroup(supplierNodeRef)) {
			return SecurityService.WRITE_ACCESS;
		}

		List<String> contentWorkflowIds = findWorkflowIds(entityNodeRef, supplierNodeRef);
		if (contentWorkflowIds.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No workflows found for entity " + entityNodeRef + " or supplier " + supplierNodeRef);
			}
			return SecurityService.READ_ACCESS;
		}

		Boolean cachedTaskResult = SecurityContextHelper.getUserHasAssignedTask();
		if (cachedTaskResult != null) {
			return cachedTaskResult ? SecurityService.WRITE_ACCESS : SecurityService.READ_ACCESS;
		}

		if (hasMatchingTask(contentWorkflowIds)) {
			return SecurityService.WRITE_ACCESS;
		}

		return SecurityService.READ_ACCESS;
	}

	/**
	 * Find workflow IDs for the entity, falling back to the supplier node
	 * when the entity itself is not in any workflow package.
	 *
	 * @param entityNodeRef the entity being accessed
	 * @param supplierNodeRef the supplier associated with the entity
	 * @return list of workflow instance IDs
	 */
	private List<String> findWorkflowIds(NodeRef entityNodeRef, NodeRef supplierNodeRef) {
		List<String> workflowIds = workflowPackageComponent.getWorkflowIdsForContent(entityNodeRef);
		if (!workflowIds.isEmpty()) {
			return workflowIds;
		}

		if (!entityNodeRef.equals(supplierNodeRef)) {
			workflowIds = workflowPackageComponent.getWorkflowIdsForContent(supplierNodeRef);
			if (logger.isDebugEnabled()) {
				logger.debug("Fallback to supplier node " + supplierNodeRef + " workflows: " + workflowIds);
			}
		}

		return workflowIds;
	}

	/**
	 * Check if the current user has an assigned or pooled task matching the given workflow IDs.
	 *
	 * @param contentWorkflowIds workflow instance IDs to match against
	 * @return true if user has a matching task
	 */
	private boolean hasMatchingTask(List<String> contentWorkflowIds) {
		String supplierAccount = AuthenticationUtil.getFullyAuthenticatedUser();

		List<WorkflowTask> assignedTasks = workflowService.getAssignedTasks(supplierAccount, WorkflowTaskState.IN_PROGRESS);
		boolean matching = assignedTasks.stream()
				.anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId())
						&& hasSupplierWizardDeliverable(task));

		if (!matching) {
			List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(supplierAccount);
			matching = pooledTasks.stream()
					.anyMatch(task -> contentWorkflowIds.contains(task.getPath().getInstance().getId())
							&& hasSupplierWizardDeliverable(task));
		}

		return matching;
	}

	private boolean hasSupplierWizardDeliverable(WorkflowTask task) {
		NodeRef projectNodeRef = (NodeRef) task.getProperties().get(BeCPGModel.ASSOC_WORKFLOW_ENTITY);
		if (projectNodeRef == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No project node found on workflow task " + task.getId());
			}
			return false;
		}

		ProjectData projectData = alfrescoRepository.findOne(projectNodeRef);
		if (projectData == null || projectData.getDeliverableList() == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No deliverables for project " + projectNodeRef + " on task " + task.getId());
			}
			return false;
		}

		NodeRef workflowTaskNodeRef = (NodeRef) task.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK);

		for (DeliverableListDataItem deliverable : projectData.getDeliverableList()) {
			String url = deliverable.getUrl();
			if ((url != null) && url.startsWith(SUPPLIER_WIZARD_PREFIX)) {
				if (workflowTaskNodeRef == null) {
					return true;
				}
				List<NodeRef> deliverableTasks = deliverable.getTasks();
				if ((deliverableTasks != null) && deliverableTasks.contains(workflowTaskNodeRef)) {
					return true;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("No supplier wizard deliverable found for task " + task.getId() + " in project " + projectNodeRef);
		}
		return false;
	}

}
