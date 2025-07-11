package fr.becpg.repo.security.impl;

import java.util.Collections;
import java.util.HashSet;
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
import fr.becpg.repo.helper.AuthorityHelper;

/**
 * <p>BeCPGOwnableServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGOwnableServiceImpl extends OwnableServiceImpl {

	private boolean disableOwner = false;

	/**
	 * <p>Setter for the field <code>disableOwner</code>.</p>
	 *
	 * @param disableOwner a boolean.
	 */
	public void setDisableOwner(boolean disableOwner) {
		this.disableOwner = disableOwner;
	}

	private EntityDictionaryService entityDictionaryService;

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	private NodeService nodeService;
	private SimpleCache<NodeRef, String> nodeOwnerCache;
	private TenantService tenantService;
	private Set<String> storesToIgnorePolicies = Collections.emptySet();
	@SuppressWarnings("deprecation")
	private RenditionService renditionService;

	/**
	 * <p>Constructor for BeCPGOwnableServiceImpl.</p>
	 */
	public BeCPGOwnableServiceImpl() {
		super();
	}

	// IOC

	/** {@inheritDoc} */
	@Override
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
		super.setNodeService(nodeService);
	}

	/** {@inheritDoc} */
	@Override
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
		super.setTenantService(tenantService);
	}

	/** {@inheritDoc} */
	@Override
	public void setStoresToIgnorePolicies(Set<String> storesToIgnorePolicies) {
		this.storesToIgnorePolicies = storesToIgnorePolicies;
		super.setStoresToIgnorePolicies(storesToIgnorePolicies);
	}

	/** {@inheritDoc} */
	@Override
	public void setNodeOwnerCache(SimpleCache<NodeRef, String> ownerCache) {
		this.nodeOwnerCache = ownerCache;
		super.setNodeOwnerCache(ownerCache);
	}

	/** {@inheritDoc} */
	@Override
	public void setRenditionService(@SuppressWarnings("deprecation") RenditionService renditionService) {
		this.renditionService = renditionService;
		super.setRenditionService(renditionService);
	}

	private static final  Set<String> URI_TO_EXCLUDES = new HashSet<>();
	static {
		URI_TO_EXCLUDES.add(DownloadModel.DOWNLOAD_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.SYSTEM_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.FORUMS_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.WORKFLOW_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.EMAILSERVER_MODEL_URI);
		URI_TO_EXCLUDES.add(NamespaceService.APP_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.BPM_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.DICTIONARY_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.SECURITY_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.WEBDAV_MODEL_1_0_URI);
		URI_TO_EXCLUDES.add(NamespaceService.LINKS_MODEL_1_0_URI);
	}

	// OwnableService implementation

	/** {@inheritDoc} */
	@Override
	public String getOwner(NodeRef nodeRef) {
		
		// case of ExternalUser
		String currentUser = AuthenticationUtil.getRunAsUser();
		if (currentUser != null && AuthorityHelper.isExternalUser(currentUser) && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE)) {
			String creator = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
			if (AuthorityHelper.isExternalUser(creator)) {
				return currentUser;
			}
		}
		
		String userName = nodeOwnerCache.get(nodeRef);

		if (userName == null) {

			// If ownership is not explicitly set then we fall back to the
			// creator
			if (localIsRendition(nodeRef)) {
				userName = getOwner(nodeService.getPrimaryParent(nodeRef).getParentRef());
			} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE)) {
				userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_OWNER));
			} else {
				QName type = nodeService.getType(nodeRef);
				if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE)) {
					userName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
				}

				if (disableOwner && !URI_TO_EXCLUDES.contains(type.getNamespaceURI())
						&& !entityDictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {

					if (!((userName != null) && AuthorityHelper.isExternalUser(userName))) {
						userName = OwnableService.NO_OWNER;
					}

				}
			}
			localCacheOwner(nodeRef, userName);
		}

		return userName;
	}

	private void localCacheOwner(NodeRef nodeRef, String userName) {
		// do not cache owners of nodes that are from stores that ignores
		// policies
		// to prevent mess in nodeOwnerCache
		if (!storesToIgnorePolicies.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString())) {
			nodeOwnerCache.put(nodeRef, userName);
		}
	}

	@SuppressWarnings("deprecation")
	private boolean localIsRendition(final NodeRef node) {
		return AuthenticationUtil.runAs(() -> renditionService.isRendition(node), AuthenticationUtil.getSystemUserName());
	}

}
