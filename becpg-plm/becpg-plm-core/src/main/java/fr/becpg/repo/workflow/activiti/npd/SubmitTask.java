package fr.becpg.repo.workflow.activiti.npd;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.project.ProjectService;
import fr.becpg.util.ApplicationContextHelper;

/**
 * Submit a workflow task
 * 
 * @author quere
 * 
 */
public class SubmitTask extends ScriptTaskListener {

	private static final long serialVersionUID = 8018666006871621151L;

	private final static Log logger = LogFactory.getLog(SubmitTask.class);
	
	private NodeService nodeService;
	
	private ProjectService projectService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public void notify(final DelegateTask task) {

		nodeService = getServiceRegistry().getNodeService();		
		projectService = (ProjectService)ApplicationContextHelper.getApplicationContext().getBean("projectService");
		
		/**
		 *  update task
		 */
		String action = (String) task.getVariable("npdwf_npdAction");
		
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
