package fr.becpg.repo.ecm.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>AutomaticECOPolicy class.</p>
 *
 * @author matthieu Add automatic change order
 * @version $Id: $Id
 */
public class AutomaticECOPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private AutomaticECOService automaticECOService;

	private EntityVersionService entityVersionService;

	private static final Log logger = LogFactory.getLog(AutomaticECOPolicy.class);

	private boolean isEnable = true;
	
	private String automaticRecordVersionType = VersionType.MAJOR.toString();

	JavaBehaviour onUpdatePropertiesBehaviour;
	
	/**
	 * <p>Setter for the field <code>automaticRecordVersionType</code>.</p>
	 *
	 * @param automaticRecordVersionType a {@link java.lang.String} object.
	 */
	public void setAutomaticRecordVersionType(String automaticRecordVersionType) {
		this.automaticRecordVersionType = automaticRecordVersionType;
	}

	/**
	 * <p>setEnable.</p>
	 *
	 * @param isEnable a boolean.
	 */
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	/**
	 * <p>Setter for the field <code>automaticECOService</code>.</p>
	 *
	 * @param automaticECOService a {@link fr.becpg.repo.ecm.AutomaticECOService} object.
	 */
	public void setAutomaticECOService(AutomaticECOService automaticECOService) {
		this.automaticECOService = automaticECOService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		onUpdatePropertiesBehaviour = new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_PRODUCT, onUpdatePropertiesBehaviour);
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (isEnable && before.containsKey(ContentModel.PROP_MODIFIED) && after.containsKey(ContentModel.PROP_MODIFIED)
				&& !before.get(ContentModel.PROP_MODIFIED).equals(after.get(ContentModel.PROP_MODIFIED)) && !AuthenticationUtil.isRunAsUserTheSystemUser()) {

			if (L2CacheSupport.isThreadLockEnable()) {
				if(logger.isDebugEnabled()){
					logger.debug("Entity ["+Thread.currentThread().getName()+"] is locked by ECM :" + nodeRef);
				}
				return;
			}

			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) && !isBeCPGVersion(nodeRef) && !isEntityTemplate(nodeRef) ) {
				onUpdatePropertiesBehaviour.disable();
				try {
					ChangeOrderData changeOrderData = automaticECOService.getCurrentUserChangeOrderData();
					if (automaticECOService.addAutomaticChangeEntry(nodeRef, changeOrderData) && changeOrderData != null) {
						logger.debug("Creating new version for nodeRef : " + nodeRef);
						entityVersionService.createInitialVersion(nodeRef);
						queueNode(nodeRef);
					}
				} finally {
					onUpdatePropertiesBehaviour.enable();
				}
			}

		}
	}



	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) && !isBeCPGVersion(nodeRef)) {
				onUpdatePropertiesBehaviour.disable();
				try {
					ChangeOrderData changeOrderData = automaticECOService.getCurrentUserChangeOrderData();
					Map<String, Serializable> properties = new HashMap<>();
					properties.put(VersionModel.PROP_VERSION_TYPE, VersionType.valueOf(automaticRecordVersionType));
					properties.put(Version.PROP_DESCRIPTION, I18NUtil.getMessage("plm.ecm.apply.version.label", changeOrderData.getCode()+" - "+changeOrderData.getName()));

					entityVersionService.createVersion(nodeRef, properties);
				} finally {
					onUpdatePropertiesBehaviour.enable();
				}
			}
		}
		return true;
	}

}
