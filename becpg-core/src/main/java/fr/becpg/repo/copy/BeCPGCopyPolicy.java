package fr.becpg.repo.copy;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class BeCPGCopyPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy {

	private CopyRestrictionService copyRestrictionService;

	public void setCopyRestrictionService(CopyRestrictionService copyRestrictionService) {
		this.copyRestrictionService = copyRestrictionService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, ContentModel.TYPE_CMOBJECT,
				new JavaBehaviour(this, "onCopyComplete"));
	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		copyRestrictionService.handlePropertiesToReset(classRef, sourceNodeRef, targetNodeRef);
	}

}
