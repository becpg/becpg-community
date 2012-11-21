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

/**
 * The Class TaskDatesPolicy.
 * 
 * @author querephi
 */
@Service
public class TaskDatesPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(TaskDatesPolicy.class);

	private ProjectService projectService;

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init TaskDatesPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_TASK_LIST, new JavaBehaviour(this, "onUpdateProperties"));

	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			try{
				policyBehaviourFilter.disableBehaviour(ProjectModel.TYPE_TASK_LIST);
				projectService.calculateTaskDates(nodeRef);
			}
			finally{
				policyBehaviourFilter.enableBehaviour(ProjectModel.TYPE_TASK_LIST);
			}
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_DURATION);
		isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_START);
		isPropChanged(nodeRef, before, after, ProjectModel.PROP_TL_END);		
	}
	
	private void isPropChanged(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after, QName propertyQName){
		Serializable beforeProp = before.get(propertyQName);
		Serializable afterProp = after.get(propertyQName);
		if(afterProp != null && !afterProp.equals(beforeProp)){
			logger.debug("beforeProp: " + beforeProp + " - afterProp: " + afterProp);
			queueNode(nodeRef);					
		}
	}
}
