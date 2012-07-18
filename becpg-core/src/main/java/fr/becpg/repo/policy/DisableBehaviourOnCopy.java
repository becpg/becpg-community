package fr.becpg.repo.policy;

import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class DisableBehaviourOnCopy implements CopyServicePolicies.OnCopyNodePolicy,
CopyServicePolicies.OnCopyCompletePolicy {

	private QName type;
	
	private BehaviourFilter policyBehaviourFilter;
	
	
	public DisableBehaviourOnCopy(QName type, BehaviourFilter policyBehaviourFilter) {
		super();
		this.type = type;
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return new EntityCopyBehaviourCallback();
	}

	private class EntityCopyBehaviourCallback extends DefaultCopyBehaviourCallback {

		@Override
		public boolean getMustCopy(QName classQName, CopyDetails copyDetails) {
			NodeRef targetNodeRef = copyDetails.getTargetNodeRef();
			policyBehaviourFilter.disableBehaviour(targetNodeRef, type);

			// Always copy
			return true;
		}
	}

	/**
	 * Re-enable aspect behaviour for the source node
	 */
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

		policyBehaviourFilter.enableBehaviour(destinationRef, type);

	}

	
}
