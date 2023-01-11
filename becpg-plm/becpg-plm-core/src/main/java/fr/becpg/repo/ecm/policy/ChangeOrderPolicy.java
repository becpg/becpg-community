package fr.becpg.repo.ecm.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ECMModel;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class ChangeOrderPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	JavaBehaviour onUpdatePropertiesBehaviour;
	
	private ECOService ecoService;
	
	public void setEcoService(ECOService ecoService) {
		this.ecoService = ecoService;
	}
	
	@Override
	public void doInit() {
		onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ECMModel.TYPE_ECO, onUpdatePropertiesBehaviour);
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (before.containsKey(ECMModel.PROP_ECO_STATE) && after.containsKey(ECMModel.PROP_ECO_STATE)
				&& !before.get(ECMModel.PROP_ECO_STATE).equals(after.get(ECMModel.PROP_ECO_STATE))) {
			
			if (ECOState.Cancelled.toString().equals(after.get(ECMModel.PROP_ECO_STATE))) {
				queueNode(nodeRef);
			}
			
		}
	}

	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			ecoService.closeECO(nodeRef, new ArrayList<>());
		}
		
		return true;
	}

}
