package fr.becpg.repo.ecm.policy;

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * 
 * @author matthieu
 * Add automatic change order
 */
public class AutomaticECOPolicy extends AbstractBeCPGPolicy implements  NodeServicePolicies.OnUpdateNodePolicy {

	private AutomaticECOService automaticECOService;
	

	//TODO type product ?????
	
	public void setAutomaticECOService(AutomaticECOService automaticECOService) {
		this.automaticECOService = automaticECOService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, PLMModel.TYPE_PRODUCT, new JavaBehaviour(this,	"onUpdateNode"));
	}
	
	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		queueNode(nodeRef);
	}

	
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) ) {
				automaticECOService.addAutomaticChangeEntry(nodeRef);
			}
		}		
	}
	

}
