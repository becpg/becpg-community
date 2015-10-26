package fr.becpg.repo.workflow.activiti.project;

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

	@Override
	public void notify(final DelegateTask task) {

		nodeService = getServiceRegistry().getNodeService();
		projectService = (ProjectService) ApplicationContextHelper.getApplicationContext().getBean("projectService");

		ActivitiScriptNode taskNode = (ActivitiScriptNode) task.getVariable("pjt_workflowTask");
		if (taskNode != null) {

			logger.debug("taskNode exist " + taskNode.getNodeRef());
			NodeRef taskNodeRef = taskNode.getNodeRef();

			if (nodeService.exists(taskNodeRef)) {

				String taskComment = (String) task.getVariable("bpm_comment");

				String transition = (String) task.getVariable("pjt_worflowTransition");
				if ((transition != null) && "refused".equals(transition)) {
					projectService.refusedTask(taskNodeRef, taskComment);
				} else {
					projectService.submitTask(taskNodeRef, taskComment);
				}
			}

		}
	}
}
