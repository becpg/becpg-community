package fr.becpg.repo.copy;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>BeCPGCopyPolicy class.</p>
 *
 * @author matthieu
 */
public class BeCPGCopyPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy {

	private CopyRestrictionService copyRestrictionService;

	/**
	 * <p>Setter for the field <code>copyRestrictionService</code>.</p>
	 *
	 * @param copyRestrictionService a {@link fr.becpg.repo.copy.CopyRestrictionService} object
	 */
	public void setCopyRestrictionService(CopyRestrictionService copyRestrictionService) {
		this.copyRestrictionService = copyRestrictionService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCopyComplete"));
	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		copyRestrictionService.handlePropertiesToReset(classRef, sourceNodeRef, targetNodeRef);
	}

}
