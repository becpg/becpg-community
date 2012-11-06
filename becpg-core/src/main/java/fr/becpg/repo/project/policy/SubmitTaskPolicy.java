/*
 * 
 */
package fr.becpg.repo.project.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * The Class SubmitTaskPolicy.
 * 
 * @author querephi
 */
@Service
public class SubmitTaskPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SubmitTaskPolicy.class);

	private ProjectService projectService;

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init SubmitTaskPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onUpdateProperties"));

	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			projectService.startNextTasks(nodeRef);
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String afterState = (String) after.get(ProjectModel.PROP_TL_STATE);		

		if (afterState != null && afterState.equals(TaskState.Completed.toString())) {
			queueNode(nodeRef);
		}
	}
}
