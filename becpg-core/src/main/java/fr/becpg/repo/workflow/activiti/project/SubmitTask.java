/*
 * 
 */
package fr.becpg.repo.workflow.activiti.project;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.CommentsService;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.util.ApplicationContextHelper;

/**
 * Submit a workflow task
 * 
 * @author quere
 * 
 */
public class SubmitTask extends ScriptTaskListener {

	private final static Log logger = LogFactory.getLog(SubmitTask.class);
	
	private NodeService nodeService;
	
	private ProjectActivityService projectActivityService;
	
	private CommentsService commentsService;
	
	private ProjectService projectService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	public void setCommentsService(CommentsService commentsService) {
		this.commentsService = commentsService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public void notify(final DelegateTask task) {

		nodeService = getServiceRegistry().getNodeService();
		projectActivityService = (ProjectActivityService)ApplicationContextHelper.getApplicationContext().getBean("projectActivityService");
		commentsService = (CommentsService)ApplicationContextHelper.getApplicationContext().getBean("commentsService");
		projectService = (ProjectService)ApplicationContextHelper.getApplicationContext().getBean("projectService");
		

		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		/**
		 * Add submitted comment on project
		 */
		String comment = (String) task.getVariable("bpm_comment");

		if (comment != null && !comment.isEmpty()) {

			NodeRef projectNodeRef = null;

			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef,
					WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (nodeService.getType(childAssoc.getChildRef()).equals(ProjectModel.TYPE_PROJECT)) {
					projectNodeRef = childAssoc.getChildRef();
					break;
				}
			}

			if (projectNodeRef != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Add comment '" + comment + "' on project " + projectNodeRef);
				}

				commentsService.createComment(projectNodeRef, "", comment, false);
				projectActivityService.postProjectCommentCreatedActivity(projectNodeRef, comment);
			}			
		}
		
		/**
		 *  update task
		 */
		String action = (String) task.getVariable("pjtwf_npdAction");
		
		// for compatibility with existing workflow (not the best) we need this for old instances, otherwise endDate is not filled 
		if(action == null || action.equals("submitTask")){
			ActivitiScriptNode taskNode = (ActivitiScriptNode) task.getVariable("pjt_workflowTask");
			if (taskNode != null) {
				logger.debug("taskNode exist " + taskNode.getNodeRef());
				NodeRef taskNodeRef = taskNode.getNodeRef();
				if(nodeService.exists(taskNodeRef)){	            	
	            	projectService.submitTask(taskNodeRef);
				}
			}
		}
			
	}
}
