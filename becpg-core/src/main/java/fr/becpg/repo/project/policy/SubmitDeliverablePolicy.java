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
import fr.becpg.repo.project.data.projectList.DeliverableState;

/**
 * The Class SubmitDeliverablePolicy.
 * 
 * @author querephi
 */
@Service
public class SubmitDeliverablePolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SubmitDeliverablePolicy.class);

	private ProjectService projectService;

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init SubmitDeliverablePolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ProjectModel.TYPE_DELIVERABLE_LIST, new JavaBehaviour(this, "onUpdateProperties"));

	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			
			String dlState = (String) nodeService.getProperty(nodeRef, ProjectModel.PROP_DL_STATE);
			
			if (dlState != null){
				if(dlState.equals(DeliverableState.Completed.toString())) {
					projectService.submitDeliverable(nodeRef);
				}
				else if(dlState.equals(DeliverableState.InProgress.toString())){
					projectService.openDeliverable(nodeRef);
				}
			}				
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		String beforeState = (String)before.get(ProjectModel.PROP_DL_STATE);
		String afterState = (String)after.get(ProjectModel.PROP_DL_STATE);
		if(beforeState != null && afterState != null){
			if(beforeState.equals(DeliverableState.InProgress.toString())&&afterState.equals(DeliverableState.Completed.toString())){
				queueNode(nodeRef);
			}
			else if(beforeState.equals(DeliverableState.Completed.toString())&&afterState.equals(DeliverableState.InProgress.toString())){
				queueNode(nodeRef);
			}
		}
						
	}
}
