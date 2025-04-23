package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionSupportUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.authentication.provider.IdentityServiceAccountProvider;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>BeCPGUserPolicy class.</p>
 *
 * @author matthieu
 */
public class BeCPGUserPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy {

	private static final String KEY_GENERATE_PASSWORD = "generatePassword";

	private static final String KEY_UPDATED_USER = "updatedUser";
	
	private BeCPGUserAccountService beCPGUserAccountService;
	
	private IdentityServiceAccountProvider identityServiceAccountProvider;
	
	/**
	 * <p>Setter for the field <code>identityServiceAccountProvider</code>.</p>
	 *
	 * @param identityServiceAccountProvider a {@link fr.becpg.repo.authentication.provider.IdentityServiceAccountProvider} object
	 */
	public void setIdentityServiceAccountProvider(IdentityServiceAccountProvider identityServiceAccountProvider) {
		this.identityServiceAccountProvider = identityServiceAccountProvider;
	}

	/**
	 * <p>Setter for the field <code>beCPGUserAccountService</code>.</p>
	 *
	 * @param beCPGUserAccountService a {@link fr.becpg.repo.authentication.BeCPGUserAccountService} object
	 */
	public void setBeCPGUserAccountService(BeCPGUserAccountService beCPGUserAccountService) {
		this.beCPGUserAccountService = beCPGUserAccountService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, ContentModel.TYPE_PERSON, new JavaBehaviour(this, "beforeDeleteNode"));
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		Boolean isIdsUser = (Boolean) nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_SSO_USER);
		if (isIdsUser != null && isIdsUser.booleanValue() && Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
			identityServiceAccountProvider.deleteAccount((String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (Boolean.TRUE.equals(TransactionSupportUtil.getResource("BeCPGUserPolicy.disable"))) {
			return;
		}
		if (isNewTrueProperty(before, after, BeCPGModel.PROP_IS_SSO_USER)) {
			beCPGUserAccountService.synchronizeWithIDS((String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
		}
		if (isNewTrueProperty(before, after, BeCPGModel.PROP_GENERATE_PASSWORD)) {
			queueNode(KEY_GENERATE_PASSWORD, nodeRef);
		}
		if (hasChanged(before, after, ContentModel.PROP_EMAIL) || hasChanged(before, after, ContentModel.PROP_FIRSTNAME)
				|| hasChanged(before, after, ContentModel.PROP_LASTNAME)) {
			queueNode(KEY_UPDATED_USER, nodeRef);
		}
	}

	private boolean hasChanged(Map<QName, Serializable> before, Map<QName, Serializable> after, QName prop) {
		return after.get(prop) != null && !after.get(prop).equals(before.get(prop));
	}

	private boolean isNewTrueProperty(Map<QName, Serializable> before, Map<QName, Serializable> after, QName prop) {
		return !isPropertyTrue(before, prop) && isPropertyTrue(after, prop);
	}

	private boolean isPropertyTrue(Map<QName, Serializable> props, QName prop) {
		return props.containsKey(prop) && (boolean) props.get(prop);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (KEY_GENERATE_PASSWORD.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				if (nodeService.exists(pendingNode)) {
					beCPGUserAccountService.generatePassword((String) nodeService.getProperty(pendingNode, ContentModel.PROP_USERNAME), true);
					nodeService.removeProperty(pendingNode, BeCPGModel.PROP_GENERATE_PASSWORD);
				}
			}
		} else if (KEY_UPDATED_USER.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				if (nodeService.exists(pendingNode)) {
					Boolean isIdsUser = (Boolean) nodeService.getProperty(pendingNode, BeCPGModel.PROP_IS_SSO_USER);
					if (isIdsUser != null && isIdsUser.booleanValue() && Boolean.TRUE.equals(identityServiceAccountProvider.isEnabled())) {
						BeCPGUserAccount account = new BeCPGUserAccount();
						account.setUserName((String) nodeService.getProperty(pendingNode, ContentModel.PROP_USERNAME));
						account.setFirstName((String) nodeService.getProperty(pendingNode, ContentModel.PROP_FIRSTNAME));
						account.setLastName((String) nodeService.getProperty(pendingNode, ContentModel.PROP_LASTNAME));
						account.setEmail((String) nodeService.getProperty(pendingNode, ContentModel.PROP_EMAIL));
						identityServiceAccountProvider.updateUser(account);
					}
				}
			}
		}
		return true;
	}
}
