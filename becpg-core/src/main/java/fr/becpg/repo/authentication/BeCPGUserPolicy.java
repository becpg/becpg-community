package fr.becpg.repo.authentication;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>BeCPGUserPolicy class.</p>
 *
 * @author matthieu
 */
public class BeCPGUserPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy {

	private static final String KEY_GENERATE_PASSWORD = "generatePassword";
	
	private BeCPGUserAccountService beCPGUserAccountService;

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
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (isNewProperty(before, after, BeCPGModel.PROP_IS_SSO_USER)) {
			beCPGUserAccountService.synchronizeWithIDS((String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
		}
		if (isNewProperty(before, after, BeCPGModel.PROP_GENERATE_PASSWORD)) {
			queueNode(KEY_GENERATE_PASSWORD, nodeRef);
		}
	}

	private boolean isNewProperty(Map<QName, Serializable> before, Map<QName, Serializable> after, QName prop) {
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
				beCPGUserAccountService.generatePassword((String) nodeService.getProperty(pendingNode, ContentModel.PROP_USERNAME), true);
				nodeService.removeProperty(pendingNode, BeCPGModel.PROP_GENERATE_PASSWORD);
			}
		}
		return true;
	}
}
