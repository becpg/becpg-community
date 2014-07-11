package fr.becpg.repo.ecm.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * 
 * @author matthieu Add automatic change order
 */
public class AutomaticECOPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private AutomaticECOService automaticECOService;

	private ECOService ecoService;

	private static Log logger = LogFactory.getLog(AutomaticECOPolicy.class);

	private boolean isEnable = true;

	JavaBehaviour onUpdatePropertiesBehaviour;

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public void setAutomaticECOService(AutomaticECOService automaticECOService) {
		this.automaticECOService = automaticECOService;
	}

	public void setEcoService(ECOService ecoService) {
		this.ecoService = ecoService;
	}

	@Override
	public void doInit() {
		onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_PRODUCT, onUpdatePropertiesBehaviour);
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (isEnable && before.containsKey(ContentModel.PROP_MODIFIED) && after.containsKey(ContentModel.PROP_MODIFIED)
				&& !before.get(ContentModel.PROP_MODIFIED).equals(after.get(ContentModel.PROP_MODIFIED))) {

			if (L2CacheSupport.isThreadLockEnable()) {
				logger.debug("Entity is locked by ECM :" + nodeRef);
				return;
			}

			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) && !isBeCPGVersion(nodeRef)) {
				onUpdatePropertiesBehaviour.disable();
				try {
					ChangeOrderData changeOrderData = automaticECOService.getCurrentUserChangeOrderData();
					if (automaticECOService.addAutomaticChangeEntry(nodeRef, changeOrderData) && changeOrderData != null) {
						logger.debug("Creating new version for nodeRef : " + nodeRef);
						ecoService.createNewProductVersion(nodeRef, VersionType.MINOR, changeOrderData);
					}
				} finally {
					onUpdatePropertiesBehaviour.enable();
				}
			}

		}
	}

}
