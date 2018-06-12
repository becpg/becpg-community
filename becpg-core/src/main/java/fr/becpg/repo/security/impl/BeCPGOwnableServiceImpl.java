package fr.becpg.repo.security.impl;

import java.util.Collections;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.ownable.impl.OwnableServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.EntityDictionaryService;

public class BeCPGOwnableServiceImpl extends OwnableServiceImpl {

	private boolean disableOwner = false;

	public void setDisableOwner(boolean disableOwner) {
		this.disableOwner = disableOwner;
	}

	
	private EntityDictionaryService entityDictionaryService;
	
	
	
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}


	private NodeService nodeService;
	private SimpleCache<NodeRef, String> nodeOwnerCache;
	private TenantService tenantService;
	private Set<String> storesToIgnorePolicies = Collections.emptySet();
	private RenditionService renditionService;
	
	

	public BeCPGOwnableServiceImpl() {
		super();
	}

	// IOC

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
		super.setNodeService(nodeService);
	}


	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
		super.setTenantService(tenantService);
	}

	public void setStoresToIgnorePolicies(Set<String> storesToIgnorePolicies) {
		this.storesToIgnorePolicies = storesToIgnorePolicies;
		super.setStoresToIgnorePolicies(storesToIgnorePolicies);
	}

	/**
	 * @param ownerCache
	 *            a transactionally-safe cache of node owners
	 */
	public void setNodeOwnerCache(SimpleCache<NodeRef, String> ownerCache) {
		this.nodeOwnerCache = ownerCache;
		super.setNodeOwnerCache(ownerCache);
	}

	/**
	 * @param renditionService
	 *            the renditionService to set
	 */
	public void setRenditionService(RenditionService renditionService) {
		this.renditionService = renditionService;
		super.setRenditionService(renditionService);
	}


	// OwnableService implementation

	@Override
	public String getOwner(NodeRef nodeRef) {
		String userName = nodeOwnerCache.get(nodeRef);

		if (userName == null) {
			
			if (userName == null) {

				// If ownership is not explicitly set then we fall back to the
				// creator
				if (isRendition(nodeRef)) {
					userName = getOwner(nodeService.getPrimaryParent(nodeRef).getParentRef());
				} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE)) {
					userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER));
				} else { 
					QName type = nodeService.getType(nodeRef);
					if(disableOwner && !entityDictionaryService.isSubClass(type,  ContentModel.TYPE_PERSON) 
						&& !DownloadModel.DOWNLOAD_MODEL_1_0_URI.equals(type.getNamespaceURI()) 
						&& !NamespaceService.SYSTEM_MODEL_1_0_URI.equals(type.getNamespaceURI())) {
						userName = OwnableService.NO_OWNER;
					} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE) ) {
						userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
					} 
				}
			}
			cacheOwner(nodeRef, userName);
		}

		return userName;
	}




	private void cacheOwner(NodeRef nodeRef, String userName) {
		// do not cache owners of nodes that are from stores that ignores
		// policies
		// to prevent mess in nodeOwnerCache
		if (!storesToIgnorePolicies.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString())) {
			nodeOwnerCache.put(nodeRef, userName);
		}
	}

	private boolean isRendition(final NodeRef node) {
		return AuthenticationUtil.runAs(() -> renditionService.isRendition(node), AuthenticationUtil.getSystemUserName());
	}

}
