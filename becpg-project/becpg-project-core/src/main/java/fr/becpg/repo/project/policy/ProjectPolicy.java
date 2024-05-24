/*
 *
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * The Class ProjectPolicy.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, EntityVersionPlugin {

	private static final Log logger = LogFactory.getLog(ProjectPolicy.class);

	/** Constant <code>KEY_DELETED_TASK_LIST_ITEM="DeletedTaskListItem"</code> */
	protected static final String KEY_DELETED_TASK_LIST_ITEM = "DeletedTaskListItem";

	/** Constant <code>KEY_PROJECT_ITEM="ProjectItem"</code> */
	protected static final String KEY_PROJECT_ITEM = "ProjectItem";

	protected ProjectService projectService;
	protected AlfrescoRepository<ProjectData> alfrescoRepository;
	protected ProjectWorkflowService projectWorkflowService;
	protected EntityListDAO entityListDAO;

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>projectWorkflowService</code>.</p>
	 *
	 * @param projectWorkflowService a {@link fr.becpg.repo.project.ProjectWorkflowService} object.
	 */
	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init ProjectPolicy...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_PROJECT,
				new JavaBehaviour(this, "onUpdateProperties"));

		// disable otherwise, impossible to copy project that has a template
		super.disableOnCopyBehaviour(ProjectModel.TYPE_PROJECT);
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if(policyBehaviourFilter.isEnabled(BeCPGModel.TYPE_SYSTEM_ENTITY)) {

			Set<NodeRef> toReformulates = new HashSet<>();
			String beforeState = (String) before.get(ProjectModel.PROP_PROJECT_STATE);
			String afterState = (String) after.get(ProjectModel.PROP_PROJECT_STATE);
	
			// change state
			if ((afterState != null) && !afterState.equals(beforeState)) {
				toReformulates.addAll( projectService.updateProjectState(nodeRef, beforeState, afterState));
			}
	
			// change startdate, duedate
			if (isPropChanged(before, after, ProjectModel.PROP_PROJECT_START_DATE) || isPropChanged(before, after, ProjectModel.PROP_PROJECT_DUE_DATE)) {
				toReformulates.add(nodeRef);
			}
	
			if (!toReformulates.isEmpty()) {
				for(NodeRef n : toReformulates) {
					queueNode(n);
				}
			}
		}
	}

	/**
	 * <p>queueListItem.</p>
	 *
	 * @param listItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void queueListItem(NodeRef listItemNodeRef) {
		NodeRef projectNodeRef = entityListDAO.getEntity(listItemNodeRef);
		if (projectNodeRef != null) {
			queueNode(projectNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void queueNode(NodeRef nodeRef) {
		super.queueNode(KEY_PROJECT_ITEM, nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, final Set<NodeRef> pendingNodes) {

		if (KEY_DELETED_TASK_LIST_ITEM.equals(key)) {
			for (NodeRef wfIds : pendingNodes) {
				projectWorkflowService.deleteWorkflowById(wfIds.getId());
			}
		} else {

			for (final NodeRef projectNodeRef : pendingNodes) {
				if (nodeService.exists(projectNodeRef)) {
					logger.debug("doBeforeCommit formulate project : "+projectNodeRef);
					try {
						projectService.formulate(projectNodeRef);
					} catch(FormulateException e) {
						logger.error("Cannot formulate project: "+projectNodeRef, e);
					}
				}
			}

		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);
		queueNode(workingCopyNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effetiveDate) {
		// Empty
	}

}
