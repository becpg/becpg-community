package fr.becpg.repo.web.scripts.entity.datalist;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.AbstractWebScript;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>Abstract AbstractEntityDataListWebScript class.</p>
 *
 * @author matthieu
 */
public abstract class AbstractEntityDataListWebScript extends AbstractWebScript {
	
	protected enum Access {
		
		NONE,
		READ,
		WRITE;
		
		public boolean canWrite() {
			return this == WRITE;
		}
		
		public boolean canRead() {
			return this == READ || canWrite();
		}
		
		static Access valueOf(boolean read, boolean write) {
			return write ? WRITE : read ? READ : NONE;
		}
	}

	protected NamespaceService namespaceService;
	
	protected NodeService nodeService;

	protected SecurityService securityService;
	
	protected LockService lockService;
	
	protected BeCPGLicenseManager becpgLicenseManager;
	
	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}
	
	/**
	 * <p>Setter for the field <code>becpgLicenseManager</code>.</p>
	 *
	 * @param becpgLicenseManager a {@link fr.becpg.repo.license.BeCPGLicenseManager} object
	 */
	public void setBecpgLicenseManager(BeCPGLicenseManager becpgLicenseManager) {
		this.becpgLicenseManager = becpgLicenseManager;
	}
	
	/**
	 * <p>getAccess.</p>
	 *
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object
	 * @param versionFilter a boolean
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListName a {@link java.lang.String} object
	 * @param extractor a {@link fr.becpg.repo.entity.datalist.DataListExtractor} object
	 * @return a {@link fr.becpg.repo.web.scripts.entity.datalist.AbstractEntityDataListWebScript.Access} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	protected Access getAccess(QName dataType, NodeRef entityNodeRef,
			boolean versionFilter, NodeRef parentNodeRef, String dataListName, DataListExtractor extractor) {
		boolean hasWriteAccess = !versionFilter;
		boolean hasReadAccess = true;
		if(entityNodeRef!=null  && dataType != null) {
			QName entityNodeRefType = nodeService.getType(entityNodeRef);
			
			int accessMode = securityService.computeAccessMode(entityNodeRef, entityNodeRefType, dataType.toPrefixString());
			hasReadAccess = accessMode != SecurityService.NONE_ACCESS;
			
			if (hasReadAccess && hasWriteAccess) {

				hasWriteAccess = (extractor == null || extractor.hasWriteAccess()) && !nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_CHECKED_OUT)
						&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
						&& (lockService.getLockStatus(entityNodeRef) == LockStatus.NO_LOCK)
						&& (accessMode == SecurityService.WRITE_ACCESS)
						&& becpgLicenseManager.hasWriteLicense()
						&& isExternalUserAllowed(parentNodeRef);

				if (hasWriteAccess && parentNodeRef != null && !dataType.getLocalName().equals(dataListName)) {
					String dataListType = (String) nodeService.getProperty(parentNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

					if ((dataListType != null) && !dataListType.isEmpty()) {
						QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
						hasWriteAccess = securityService.computeAccessMode(entityNodeRef, entityNodeRefType, dataListTypeQName) == SecurityService.WRITE_ACCESS;
					}
				}
			}
		}
		return Access.valueOf(hasReadAccess, hasWriteAccess);
	}
	


	private boolean isExternalUserAllowed(NodeRef parentNodeRef) {
		if (parentNodeRef != null && nodeService.exists(parentNodeRef)
				&& nodeService.hasAspect(parentNodeRef, BeCPGModel.ASPECT_ENTITYLIST_STATE)
				&& SystemState.Valid.toString().equals(nodeService.getProperty(parentNodeRef, BeCPGModel.PROP_ENTITYLIST_STATE))
				&& AuthorityHelper.isCurrentUserExternal()) {
			return false;

		}
		return true;
	}
}
