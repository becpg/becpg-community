package fr.becpg.repo.toxicology;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>IngToxPolicy class.</p>
 *
 * @author matthieu
 */
public class IngToxPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy {

	private static final String ING_UPDATED_KEY = "IngToxPolicy.ingUpdated";
	private static final String TOX_UPDATED_KEY = "IngToxPolicy.toxUpdated";

	private ToxicologyService toxicologyService;

	/**
	 * <p>Setter for the field <code>toxicologyService</code>.</p>
	 *
	 * @param toxicologyService a {@link fr.becpg.repo.toxicology.ToxicologyService} object
	 */
	public void setToxicologyService(ToxicologyService toxicologyService) {
		this.toxicologyService = toxicologyService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_ING, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_TOX, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, PLMModel.TYPE_TOX, new JavaBehaviour(this, "beforeDeleteNode"));
	}
	
	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		QName type = nodeService.getType(nodeRef);
		if (PLMModel.TYPE_TOX.equals(type)) {
			toxicologyService.removeToxFromIngredients(nodeRef);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		QName type = nodeService.getType(nodeRef);
		if (PLMModel.TYPE_ING.equals(type)) {
			if (before.containsKey(PLMModel.PROP_ING_TOX_DATA) && (boolean) before.get(PLMModel.PROP_ING_TOX_DATA)) {
				return;
			}
			if (after.containsKey(PLMModel.PROP_ING_TOX_DATA) && (boolean) after.get(PLMModel.PROP_ING_TOX_DATA)) {
				queueNode(ING_UPDATED_KEY, nodeRef);
			}
		} else if (PLMModel.TYPE_TOX.equals(type)) {
			queueNode(TOX_UPDATED_KEY, nodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (ING_UPDATED_KEY.equals(key)) {
			for (NodeRef ingNodeRef : pendingNodes) {
				toxicologyService.updateIngredient(ingNodeRef);
				nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DATA, false);
			}
			return true;
		} else if (TOX_UPDATED_KEY.equals(key)) {
			for (NodeRef toxNodeRef : pendingNodes) {
				if (nodeService.exists(toxNodeRef)) {
					toxicologyService.updateIngredientsFromTox(toxNodeRef);
				}
			}
			return true;
		}
		return false;
	}
	
}
