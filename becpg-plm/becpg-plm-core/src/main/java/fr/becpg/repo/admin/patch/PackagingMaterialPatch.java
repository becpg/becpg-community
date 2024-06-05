package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>PackagingMaterialPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PackagingMaterialPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(PackagingMaterialPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.packagingMaterialPatch.result";

	private BehaviourFilter policyBehaviourFilter;
	private LockService lockService;


	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		try {
			policyBehaviourFilter.disableBehaviour();

			logger.info("Migrating packaging material");

			NodeRef parentNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
					"/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:pmMaterials/.");

			if (parentNodeRef != null) {

				List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
						"/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:pmMaterials/*");
			
				logger.info("Update datalistType");
				nodeService.setProperty(parentNodeRef, DataListModel.PROP_DATALISTITEMTYPE,
						PackModel.TYPE_PACKAGING_MATERIAL.toPrefixString(namespaceService));
			
				for (NodeRef nodeRef : nodeRefs) {
					if (lockService.isLocked(nodeRef)) {
						lockService.unlock(nodeRef);
					}
					logger.info("Migrate : "+ nodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_VALUE));
					nodeService.setType(nodeRef, PackModel.TYPE_PACKAGING_MATERIAL);
				}

			}
		} finally {
			policyBehaviourFilter.enableBehaviour();
		}
		return I18NUtil.getMessage(MSG_SUCCESS);

	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

}
