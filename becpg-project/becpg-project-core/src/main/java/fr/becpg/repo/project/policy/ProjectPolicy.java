/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
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
public class ProjectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, CopyServicePolicies.OnCopyNodePolicy {

	private static Log logger = LogFactory.getLog(ProjectPolicy.class);

	private ProjectService projectService;
	private ProjectActivityService projectActivityService;
	private AlfrescoRepository<ProjectData> alfrescoRepository;

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

		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, ProjectModel.TYPE_PROJECT, new JavaBehaviour(this,
				"getCopyCallback"));

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

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (final NodeRef projectNodeRef : pendingNodes) {
			if (nodeService.exists(projectNodeRef) && isNotLocked(projectNodeRef)) {
				AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

					@Override
					public NodeRef doWork() throws Exception {
						try {
							policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
							policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
							policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_PROJECT);
							projectService.formulate(projectNodeRef);
						} catch (FormulateException e) {
							logger.error(e, e);
						} finally {
							policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_DELIVERABLE_LIST);
							policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
							policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_PROJECT);
						}

						return null;
					}

				}, AuthenticationUtil.SYSTEM_USER_NAME);

			}
		}

	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		super.getCopyCallback(classRef, copyDetails);
		return new ProjectCopyBehaviourCallback();
	}

	private class ProjectCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		private ProjectCopyBehaviourCallback() {
		}

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			return true;
		}

		@Override
		public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties) {

			if (ProjectModel.TYPE_PROJECT.equals(classQName)) {
				if (properties.containsKey(ProjectModel.PROP_PROJECT_STATE)) {
					properties.put(ProjectModel.PROP_PROJECT_STATE, ProjectState.Planned);
				}
				if (properties.containsKey(ProjectModel.PROP_PROJECT_START_DATE)) {
					properties.remove(ProjectModel.PROP_PROJECT_START_DATE);
				}
				if (properties.containsKey(ProjectModel.PROP_PROJECT_DUE_DATE)) {
					properties.remove(ProjectModel.PROP_PROJECT_DUE_DATE);
				}
				if (properties.containsKey(ProjectModel.PROP_PROJECT_COMPLETION_DATE)) {
					properties.remove(ProjectModel.PROP_PROJECT_COMPLETION_DATE);
				}
			}

			return properties;
		}
	}

}
