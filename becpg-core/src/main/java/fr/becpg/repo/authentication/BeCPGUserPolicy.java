package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class BeCPGUserPolicy extends AbstractBeCPGPolicy implements OnAddAspectPolicy, OnUpdatePropertiesPolicy {

	private BeCPGUserAccountService beCPGUserAccountService;

	public void setBeCPGUserAccountService(BeCPGUserAccountService beCPGUserAccountService) {
		this.beCPGUserAccountService = beCPGUserAccountService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_USER_AUTHENTICATION, new JavaBehaviour(this, "onAddAspect"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (before.containsKey(BeCPGModel.PROP_IS_SSO_USER) && (boolean) before.get(BeCPGModel.PROP_IS_SSO_USER)) {
			return;
		}
		if (after.containsKey(BeCPGModel.PROP_IS_SSO_USER)	&& (boolean) after.get(BeCPGModel.PROP_IS_SSO_USER)) {
			queueNode(nodeRef);
		}
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_SSO_USER) != null
				&& (boolean) nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_SSO_USER)) {
			queueNode(nodeRef);
		}
	}

	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef pendingNode : pendingNodes) {
			beCPGUserAccountService.synchronizeSsoUser((String) nodeService.getProperty(pendingNode, ContentModel.PROP_USERNAME));
		}
		return true;
	}
}
