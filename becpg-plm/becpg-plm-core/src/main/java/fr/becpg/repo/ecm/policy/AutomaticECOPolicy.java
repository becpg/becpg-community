package fr.becpg.repo.ecm.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * 
 * @author matthieu Add automatic change order
 */
public class AutomaticECOPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private AutomaticECOService automaticECOService;

	private static Log logger = LogFactory.getLog(AutomaticECOPolicy.class);

	public void setAutomaticECOService(AutomaticECOService automaticECOService) {
		this.automaticECOService = automaticECOService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_PRODUCT, new JavaBehaviour(this,
				"onUpdateProperties"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (before.containsKey(ContentModel.PROP_MODIFIED) && after.containsKey(ContentModel.PROP_MODIFIED)
				&& !before.get(ContentModel.PROP_MODIFIED).equals(after.get(ContentModel.PROP_MODIFIED))) {

			if (L2CacheSupport.isThreadLockEnable()) {
				logger.debug("Entity is locked by ECM :" + nodeRef);
				return;
			}
			queueNode(nodeRef);
		}

	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) && !isBeCPGVersion(nodeRef)) {
				automaticECOService.addAutomaticChangeEntry(nodeRef);
			}
		}
	}

}
