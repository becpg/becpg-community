/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.ProjectWorkflowService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.impl.ProjectHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * The Class ProjectPolicy.
 * 
 * @author querephi
 */
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, EntityVersionPlugin {

	private static final Log logger = LogFactory.getLog(ProjectPolicy.class);

	protected static final String KEY_DELETED_TASK_LIST_ITEM = "DeletedTaskListItem";

	protected static final String KEY_PROJECT_ITEM = "ProjectItem";

	protected ProjectService projectService;
	protected ProjectActivityService projectActivityService;
	protected AlfrescoRepository<ProjectData> alfrescoRepository;
	protected ProjectWorkflowService projectWorkflowService;
	protected EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProjectWorkflowService(ProjectWorkflowService projectWorkflowService) {
		this.projectWorkflowService = projectWorkflowService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init ProjectPolicy...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ProjectModel.TYPE_PROJECT, new JavaBehaviour(this,
				"onUpdateProperties"));

		// disable otherwise, impossible to copy project that has a template
		super.disableOnCopyBehaviour(ProjectModel.TYPE_PROJECT);
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		boolean formulateProject = false;
		String beforeState = (String) before.get(ProjectModel.PROP_PROJECT_STATE);
		String afterState = (String) after.get(ProjectModel.PROP_PROJECT_STATE);

		// change state
		if (afterState != null && !afterState.equals(beforeState)) {

			projectActivityService.postProjectStateChangeActivity(nodeRef, beforeState, afterState);

			if (afterState.equals(ProjectState.InProgress.toString())) {
				logger.debug("onUpdateProperties:start project");
				Date startDate = ProjectHelper.removeTime(new Date());
				nodeService.setProperty(nodeRef, ProjectModel.PROP_PROJECT_START_DATE, startDate);
				ProjectData projectData = alfrescoRepository.findOne(nodeRef);
				for (TaskListDataItem taskListDataItem : ProjectHelper.getNextTasks(projectData, null)) {
					nodeService.setProperty(taskListDataItem.getNodeRef(), ProjectModel.PROP_TL_START, startDate);
				}
				formulateProject = true;
			} else if (afterState.equals(ProjectState.Cancelled.toString())) {
				logger.debug("onUpdateProperties:cancel project");
				projectService.cancel(nodeRef);
			}
		}

		// change startdate, duedate
		if (isPropChanged(before, after, ProjectModel.PROP_PROJECT_START_DATE) || isPropChanged(before, after, ProjectModel.PROP_PROJECT_DUE_DATE)) {
			formulateProject = true;
		}

		if (formulateProject) {
			queueNode(nodeRef);
		}
	}

	protected void queueListItem(NodeRef listItemNodeRef) {
		NodeRef projectNodeRef = entityListDAO.getEntity(listItemNodeRef);
		if (projectNodeRef != null) {
			queueNode(projectNodeRef);
		}
	}

	@Override
	protected void queueNode(NodeRef nodeRef) {
		super.queueNode(KEY_PROJECT_ITEM, nodeRef);
	}

	@Override
	protected void doBeforeCommit(String key, final Set<NodeRef> pendingNodes) {

		if (logger.isDebugEnabled()) {
			logger.debug("doBeforeCommit key: " + key + " size: " + pendingNodes.size());
		}
		if (KEY_DELETED_TASK_LIST_ITEM.equals(key)) {
			for (NodeRef wfIds : pendingNodes) {
				projectWorkflowService.deleteWorkflowById(wfIds.getId());
			}
		} else {
			// TODO Move that to afterCommit ?
			// Unsure is the last one
			// Case a rule is execute after #1446
			AlfrescoTransactionSupport.bindListener(new TransactionListener() {

				@Override
				public void beforeCommit(boolean readOnly) {
					for (final NodeRef projectNodeRef : pendingNodes) {
						if (logger.isDebugEnabled()) {
							logger.debug("doBeforeCommit last - formulate project");
						}
						if (nodeService.exists(projectNodeRef)) {

							AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

								@Override
								public NodeRef doWork() throws Exception {

									try {
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_PROJECT);
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_SCORE_LIST);
										policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
										policyBehaviourFilter.disableBehaviour(ProjectModel.ASPECT_BUDGET);
										projectService.formulate(projectNodeRef);
									} catch (FormulateException e) {
										logger.error(e, e);
									} finally {
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_LOG_TIME_LIST);
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_PROJECT);
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_SCORE_LIST);
										policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_BUDGET_LIST);
										policyBehaviourFilter.enableBehaviour(ProjectModel.ASPECT_BUDGET);
									}

									return null;
								}
							}, AuthenticationUtil.SYSTEM_USER_NAME);
						}

					}

				}

				@Override
				public void flush() {
				}

				@Override
				public void beforeCompletion() {
				}

				@Override
				public void afterRollback() {
				}

				@Override
				public void afterCommit() {
				}
			});
		}

	}

	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);
		queueNode(workingCopyNodeRef);		
	}

	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);
	}

	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		queueNode(origNodeRef);		
	}

}
