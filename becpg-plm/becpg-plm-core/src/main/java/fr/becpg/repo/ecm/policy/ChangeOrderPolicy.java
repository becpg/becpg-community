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

/**
 * <p>ChangeOrderPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ChangeOrderPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	JavaBehaviour onUpdatePropertiesBehaviour;
	
	private ECOService ecoService;
	
	/**
	 * <p>Setter for the field <code>ecoService</code>.</p>
	 *
	 * @param ecoService a {@link fr.becpg.repo.ecm.ECOService} object
	 */
	public void setEcoService(ECOService ecoService) {
		this.ecoService = ecoService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void doInit() {
		onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ECMModel.TYPE_ECO, onUpdatePropertiesBehaviour);
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (before.containsKey(ECMModel.PROP_ECO_STATE) && after.containsKey(ECMModel.PROP_ECO_STATE)
				&& !before.get(ECMModel.PROP_ECO_STATE).equals(after.get(ECMModel.PROP_ECO_STATE))) {
			
			if (ECOState.Cancelled.toString().equals(after.get(ECMModel.PROP_ECO_STATE))) {
				queueNode(nodeRef);
			}
			
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			ecoService.closeECO(nodeRef, new ArrayList<>());
		}
		
		return true;
	}

}
