package fr.becpg.repo.workflow.activiti.npd;

import java.util.ArrayList;
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

import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.SystemState;
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

		/**
		 * update task
		 */
		String action = (String) task.getVariable("npdwf_npdAction");

		// for compatibility with existing workflow (not the best) we need this
		// for old instances, otherwise endDate is not filled

		NodeRef taskNodeRef = null;
		ActivitiScriptNode taskNode = (ActivitiScriptNode) task.getVariable("pjt_workflowTask");
		if (taskNode != null) {
			logger.debug("taskNode exist " + taskNode.getNodeRef());
			taskNodeRef = taskNode.getNodeRef();
		}

		String taskComment = (String) task.getVariable("bpm_comment");

		if ((action == null) || action.equals("submitTask")) {
			if (taskNodeRef != null) {
				if (nodeService.exists(taskNodeRef)) {
					projectService.submitTask(taskNodeRef, taskComment);
				}
			}

			@SuppressWarnings("unchecked")
			List<ActivitiScriptNode> selectedNodes = (List<ActivitiScriptNode>) task.getVariable("npdwf_npdSelectedProducts");
			NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

			if (selectedNodes != null) {
				updateSelectedNodes(pkgNodeRef, selectedNodes);
			}

		} else if (action.equals("refused")) {
			projectService.refusedTask(taskNodeRef, taskComment);
		}

	}

	private void updateSelectedNodes(final NodeRef pkgNodeRef, final List<ActivitiScriptNode> selectedNodes) {
		if ((selectedNodes != null) && !selectedNodes.isEmpty()) {

			NodeRef projectNodeRef = null;

			List<NodeRef> toRemoveNodes = new ArrayList<>();
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (nodeService.getType(childAssoc.getChildRef()).equals(ProjectModel.TYPE_PROJECT)) {
					projectNodeRef = childAssoc.getChildRef();
				} else if (nodeService.getType(childAssoc.getChildRef()).equals(PLMModel.TYPE_FINISHEDPRODUCT)) {
					boolean toRemove = true;
					for (ActivitiScriptNode selectedProduct : selectedNodes) {
						if (selectedProduct.getNodeRef().equals(childAssoc.getChildRef())) {
							toRemove = false;
							break;
						}
					}
					if (toRemove) {
						toRemoveNodes.add(childAssoc.getChildRef());
						nodeService.setProperty(childAssoc.getChildRef(), PLMModel.PROP_PRODUCT_STATE, SystemState.Refused);
						nodeService.removeChildAssociation(childAssoc);
					}
				}
			}

			if (projectNodeRef != null) {
				for (NodeRef toRemoveNodeRef : toRemoveNodes) {
					nodeService.removeAssociation(projectNodeRef, toRemoveNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY);
				}
			}

			for (ActivitiScriptNode selectedProduct : selectedNodes) {
				nodeService.setProperty(selectedProduct.getNodeRef(), PLMModel.PROP_PRODUCT_STATE, SystemState.ToValidate);
			}
		}

	}
}
