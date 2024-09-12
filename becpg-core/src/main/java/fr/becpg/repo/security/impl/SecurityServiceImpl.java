/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.security.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.PermissionContext;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.plugins.SecurityServicePlugin;

/**
 * Security Service : is in charge to compute acls by node Type. And provide
 * permission on properties
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
@Service("securityService")
public class SecurityServiceImpl implements SecurityService {

	private static final String ACLS_CACHE_KEY = "ACLS_CACHE_KEY";
	private static final String LOCAL_ACLS_CACHE_KEY = "LOCAL_ACLS_CACHE_KEY";
	private static final String USER_ROLE_CACHE_KEY = "USER_ROLE_CACHE_KEY";
	private static final Log logger = LogFactory.getLog(SecurityServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ACLGroupData> alfrescoRepository;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private EntityDictionaryService dictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private SecurityServicePlugin[] securityPlugins;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	/** {@inheritDoc} */
	@Override
	public int computeAccessMode(NodeRef nodeRef, QName nodeType, QName propName) {
		return computeAccessMode(nodeRef, nodeType, dictionaryService.toPrefixString(propName));
	}

	/** {@inheritDoc} */
	@Override
	public int computeAccessMode(NodeRef nodeRef, QName nodeType, String propName) {

		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}
		try {
			
			int accesMode = SecurityService.WRITE_ACCESS;

			if (!isAdmin() && !isEntityTemplate(nodeRef)) {
				PermissionContext permissionContext = getPermissionContext(nodeRef, nodeType, propName);
				
				if (Boolean.TRUE.equals(permissionContext.isDefaultReadOnly())) {
					accesMode = SecurityService.READ_ACCESS;
				}
				
				List<PermissionModel> permissions = permissionContext.getPermissions();
				if (!permissions.isEmpty()) {
					accesMode = computeAccessMode(nodeRef, nodeType, permissions);
				}
			}

			return accesMode;
		} finally {
			if (logger.isDebugEnabled() && (stopWatch != null)) {
				stopWatch.stop();
				logger.debug("Compute Access Mode takes : " + stopWatch.getTotalTimeSeconds() + "s");
			}

		}
	}
	
	private boolean isEntityTemplate(NodeRef nodeRef) {
		return nodeRef != null && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
	}

	private int computeAccessMode(NodeRef nodeRef, QName nodeType, List<PermissionModel> permissions) {
		int accessMode = SecurityService.NONE_ACCESS;
		
		boolean hasReadAccess = true;
		
		for (PermissionModel permission : permissions) {
			
			if (isInGroup(nodeRef, nodeType, permission)) {
				if (permission.isWrite()) {
					return SecurityService.WRITE_ACCESS;
				} else if (permission.isExclusiveRead()) {
					accessMode = SecurityService.READ_ACCESS;
				}
			} else {
				if (permission.isExclusiveRead()) {
					hasReadAccess = false;
				}
			}
			
		}
		
		if ((accessMode == SecurityService.NONE_ACCESS) && hasReadAccess) {
			accessMode = SecurityService.READ_ACCESS;
		}
		
		return accessMode;
	}

	/** {@inheritDoc} */
	@Override
	public PermissionContext getPermissionContext(NodeRef nodeRef, QName nodeType, String propName) {
		
		PermissionContext permissionContext = new PermissionContext();
		
		String cacheKey = computeCacheKey(nodeRef);
		
		Map<String, Boolean> readOnlyMap = getReadOnlyCachedMap(cacheKey);
		
		String nodeTypeKey = computeNodeTypeKey(nodeType);
		if (readOnlyMap.containsKey(nodeTypeKey)) {
			permissionContext.setIsDefaultReadOnly(readOnlyMap.get(nodeTypeKey));
		}
		
		Map<String, List<PermissionModel>> permissionMap = getPermissionCachedMap(cacheKey);
		
		String nodeTypePropKey = computeNodeTypePropKey(nodeType, propName);
		if (permissionMap.containsKey(nodeTypePropKey)) {
			permissionContext.setPermissions(permissionMap.get(nodeTypePropKey));
		}
		
		return permissionContext;

	}

	private String computeCacheKey(NodeRef nodeRef) {
		String cacheKey = ACLS_CACHE_KEY;
		if ((nodeRef != null) && nodeService.hasAspect(nodeRef, SecurityModel.ASPECT_SECURITY)) {
			NodeRef aclGroupNodeRef = associationService.getTargetAssoc(nodeRef, SecurityModel.ASSOC_SECURITY_REF);

			if (aclGroupNodeRef != null) {
				cacheKey = LOCAL_ACLS_CACHE_KEY + "_" + aclGroupNodeRef.getId();
			}
		}
		return cacheKey;
	}

	private Map<String, Boolean> getReadOnlyCachedMap(String cacheKey) {
		return beCPGCacheService.getFromCache(SecurityService.class.getName(), cacheKey + ".readOnly", () -> {
			Map<String, Boolean> readOnlyMap = new HashMap<>();
			StopWatch stopWatch = null;
			if (logger.isDebugEnabled()) {
				stopWatch = new StopWatch();
				stopWatch.start();
			}

			List<NodeRef> aclGroups = null;

			if (cacheKey.startsWith(LOCAL_ACLS_CACHE_KEY)) {
				aclGroups = Arrays.asList(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, cacheKey.split("_")[4]));

			} else {
				aclGroups = findAllAclGroups();
			}

			if (aclGroups != null) {

				for (NodeRef aclGroupNodeRef : aclGroups) {
					ACLGroupData aclGrp = alfrescoRepository.findOne(aclGroupNodeRef);
					QName aclGrpType = QName.createQName(aclGrp.getNodeType(), namespaceService);
					
					if (aclGrpType != null) {
						Boolean isDefaultReadOnly = aclGrp.getIsDefaultReadOnly();
						if (isDefaultReadOnly != null) {
							String isDefaultReadOnlyKey = computeNodeTypeKey(aclGrpType);
							readOnlyMap.put(isDefaultReadOnlyKey, isDefaultReadOnly);
						}
					}
				}
			}

			if (logger.isDebugEnabled() && (stopWatch != null)) {
				stopWatch.stop();
				logger.debug("Compute default permissions takes : " + stopWatch.getTotalTimeSeconds() + "s");
			}

			return readOnlyMap;
		});
	}

	private String computeNodeTypeKey(QName nodeType) {
		return nodeType.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void refreshAcls() {
		beCPGCacheService.clearCache(SecurityService.class.getName());
	}

	private Map<String, List<PermissionModel>> getPermissionCachedMap(String cacheKey) {
		return beCPGCacheService.getFromCache(SecurityService.class.getName(), cacheKey, () -> {
			Map<String, List<PermissionModel>> permissionMap = new HashMap<>();
			StopWatch stopWatch = null;
			if (logger.isDebugEnabled()) {
				stopWatch = new StopWatch();
				stopWatch.start();
			}

			List<NodeRef> aclGroups = null;

			if (cacheKey.startsWith(LOCAL_ACLS_CACHE_KEY)) {
				aclGroups = Arrays.asList(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, cacheKey.split("_")[4]));

			} else {
				aclGroups = findAllAclGroups();
			}

			if (aclGroups != null) {

				for (NodeRef aclGroupNodeRef : aclGroups) {
					ACLGroupData aclGrp = alfrescoRepository.findOne(aclGroupNodeRef);
					QName nodeType = QName.createQName(aclGrp.getNodeType(), namespaceService);
					List<ACLEntryDataItem> aclEntries = aclGrp.getAcls();
					if (aclEntries != null) {
						for (ACLEntryDataItem aclEntry : aclEntries) {
							if(aclEntry.getPropName()!=null) {
								String key = computeNodeTypePropKey(nodeType, aclEntry.getPropName());
								permissionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(new PermissionModel(aclEntry.getAclPermission(), aclEntry.getGroupsAssignee(), aclEntry.getIsEnforceACL()));
							} else {
								logger.warn("Acl has no propName: "+aclEntry.toString());
							}
						}
					}
				}
			}

			if (logger.isDebugEnabled() && (stopWatch != null)) {
				stopWatch.stop();
				logger.debug("Compute ACLs takes : " + stopWatch.getTotalTimeSeconds() + "s");
			}

			return permissionMap;
		});
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCurrentUserAllowed(String securityGroup) {
		if (!isAdmin()) {
			Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.USER, PermissionService.GROUP_PREFIX + securityGroup,
					true);
			if (!authorities.isEmpty() && !authorities.contains(AuthenticationUtil.getFullyAuthenticatedUser())) {
				return false;
			}

		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getUserSecurityRoles() {

		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

		return beCPGCacheService.getFromCache(SecurityService.class.getName(), USER_ROLE_CACHE_KEY + "_" + currentUser, () -> {

			List<String> ret = new ArrayList<>();

			try {
				Set<String> authorities = authorityService.getContainedAuthorities(AuthorityType.GROUP,
						PermissionService.GROUP_PREFIX + SystemGroup.SecurityRole.toString(), true);
				boolean isAdmin = isAdmin();
				for (String authority : authorities) {

					if (isAdmin || authorityService.getAuthoritiesForUser(currentUser).contains(authority)) {
						ret.add(authority.replace(PermissionService.GROUP_PREFIX, ""));
						if (logger.isDebugEnabled()) {
							logger.debug("Adding authority :" + authority + " for user" + currentUser);
						}
					}

				}
			} catch (UnknownAuthorityException e) {
				logger.warn("You need to run initRepo and refresh cache", e);
			}

			return ret;
		});
	}

	private boolean isAdmin() {
		return authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
	}

	/**
	 * Check if current user is in corresponding group or role
	 */
	private boolean isInGroup(NodeRef nodeRef, QName nodeType, PermissionModel permissionModel) {

		for (SecurityServicePlugin plugin : securityPlugins) {
			if (plugin.accept(nodeType) && plugin.checkIsInSecurityGroup(nodeRef, permissionModel)) {
				return true;
			}
		}

		return false;
	}

	private String computeNodeTypePropKey(QName nodeType, String propName) {
		return nodeType.toString() + "_" + propName;
	}

	private List<NodeRef> findAllAclGroups() {
		List<NodeRef>  ret = new ArrayList<>();
		
		for(NodeRef aclGroupNodeRef : BeCPGQueryBuilder.createQuery().ofType(SecurityModel.TYPE_ACL_GROUP).inDB().list()) {
			Boolean isLocalPermission = (Boolean) nodeService.getProperty(aclGroupNodeRef, SecurityModel.PROP_ACL_GROUP_IS_LOCAL_PERMISSION);
			if(!Boolean.TRUE.equals(isLocalPermission)) {
				ret.add(aclGroupNodeRef);
			}
			
		}
		
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getAvailablePropNames() {
		List<String> ret = new ArrayList<>();

			for (NodeRef aclGroupNodeRef : findAllAclGroups()) {

				ACLGroupData aclGroup = alfrescoRepository.findOne(aclGroupNodeRef);

				TypeDefinition typeDefinition = dictionaryService.getType(QName.createQName(aclGroup.getNodeType(), namespaceService));

				if ((typeDefinition != null) && (typeDefinition.getProperties() != null)) {
					for (Map.Entry<QName, PropertyDefinition> properties : typeDefinition.getProperties().entrySet()) {
						if (isViewableProperty(properties.getKey())) {
							appendPropName(typeDefinition, properties, ret);
						}
					}

					List<AspectDefinition> aspects = typeDefinition.getDefaultAspects();

					if (aspects == null) {
						aspects = new ArrayList<>();
					}

					for (AspectDefinition aspect : aspects) {
						if ((aspect != null) && (aspect.getProperties() != null)) {
							for (Map.Entry<QName, PropertyDefinition> properties : aspect.getProperties().entrySet()) {
								if (isViewableProperty(properties.getKey())) {
									appendPropName(typeDefinition, properties, ret);
								}
							}
						}

					}

				}
		}

		return ret;
	}

	private void appendPropName(TypeDefinition typeDefinition, Entry<QName, PropertyDefinition> properties, List<String> ret) {
		String key = properties.getKey().toPrefixString(namespaceService);
		String label = dictionaryService.getTitle(properties.getValue(), typeDefinition.getName());

		if (!ret.contains(key + "|" + typeDefinition.getTitle(dictionaryService) + " - " + label) && (label != null)) {
			ret.add(key + "|" + typeDefinition.getTitle(dictionaryService) + " - " + label);
		}

	}

	/**
	 * Test if the property should be show
	 *
	 * @param qName
	 *            the q name
	 * @return true, if is viewable property
	 */
	private boolean isViewableProperty(QName qName) {

		return !(qName.equals(ContentModel.PROP_NODE_REF) || qName.equals(ContentModel.PROP_NODE_DBID) || qName.equals(ContentModel.PROP_NODE_UUID)
				|| qName.equals(ContentModel.PROP_STORE_IDENTIFIER) || qName.equals(ContentModel.PROP_STORE_NAME)
				|| qName.equals(ContentModel.PROP_STORE_PROTOCOL) || qName.equals(ContentModel.PROP_CONTENT)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION) || qName.equals(ContentModel.PROP_AUTO_VERSION_PROPS) ||
				// do not compare frozen properties and version properties
				qName.equals(BeCPGModel.PROP_VERSION_LABEL) ||
				// system properties
				qName.equals(BeCPGModel.PROP_PARENT_LEVEL) || qName.equals(ContentModel.PROP_NAME) || qName.equals(ContentModel.PROP_CREATOR)
				|| qName.equals(ContentModel.PROP_CREATED) || qName.equals(ContentModel.PROP_ACCESSED) || qName.equals(ContentModel.PROP_MODIFIER)
				|| qName.equals(ContentModel.PROP_MODIFIED));
	}

}
