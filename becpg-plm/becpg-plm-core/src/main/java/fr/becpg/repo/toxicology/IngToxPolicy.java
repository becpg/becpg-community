package fr.becpg.repo.toxicology;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>IngToxPolicy class.</p>
 *
 * @author matthieu
 */
public class IngToxPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy {

	/** Constant <code>ING_UPDATED_KEY="IngToxPolicy.ingUpdated"</code> */
	private static final String ING_UPDATED_KEY = "IngToxPolicy.ingUpdated";
	/** Constant <code>TOX_UPDATED_KEY="IngToxPolicy.toxUpdated"</code> */
	private static final String TOX_UPDATED_KEY = "IngToxPolicy.toxUpdated";

	/** Tox properties read by the ingredient tox computation; only their changes trigger a recalculation. */
	private static final List<QName> TOX_COMPUTATION_PROPS = List.of(PLMModel.PROP_TOX_VALUE, PLMModel.PROP_TOX_CALCULATE_SYSTEMIC,
			PLMModel.PROP_TOX_CALCULATE_MAX, PLMModel.PROP_TOX_TYPES, PLMModel.PROP_TOX_ABSORPTION_TYPE, PLMModel.PROP_TOX_BODY_WEIGHT,
			BeCPGModel.PROP_IS_DELETED, BeCPGModel.PROP_CHARACT_NAME);

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
	
	/**
	 * {@inheritDoc}
	 *
	 * <p>For a tox, the ingredients are recalculated only when a property read by the computation
	 * changes (see {@link #isToxComputationChanged(Map, Map)}).</p>
	 */
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
		} else if (PLMModel.TYPE_TOX.equals(type) && isToxComputationChanged(before, after)) {
			queueNode(TOX_UPDATED_KEY, nodeRef);
		}
	}

	/**
	 * Tells whether a property read by the ingredient tox computation changed. On creation the
	 * before map is empty, so any set property counts as changed.
	 *
	 * @param before the tox properties before the update
	 * @param after the tox properties after the update
	 * @return true if at least one of the computation properties changed
	 */
	private boolean isToxComputationChanged(Map<QName, Serializable> before, Map<QName, Serializable> after) {
		for (QName propName : TOX_COMPUTATION_PROPS) {
			if (isPropChanged(before, after, propName)) {
				return true;
			}
		}
		return false;
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
