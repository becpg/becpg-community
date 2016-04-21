/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;

/**
 * Security Service : is in charge to compute acls by node Type. And provide
 * permission on properties
 * 
 * @author "Matthieu Laborie"
 */
@Service("securityService")
public class SecurityServiceImpl implements SecurityService {

	private static final String ACLS_CACHE_KEY = "ACLS_CACHE_KEY";

	private final static Log logger = LogFactory.getLog(SecurityServiceImpl.class);

	@Autowired
	private AlfrescoRepository<ACLGroupData> alfrescoRepository;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private NamespaceService namespaceService;

    @Autowired
	private BeCPGCacheService beCPGCacheService;
	
	
	@Override
	public int computeAccessMode(QName nodeType, String propName) {
		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}
		try {

			String key = computeAclKey(nodeType, propName);
			logger.debug("Compute acl for: " + key);

			if (getAcls().containsKey(key)) {

				List<ACLEntryDataItem.PermissionModel> perms = getAcls().get(key);
				int ret = SecurityService.WRITE_ACCESS;
				if (!isAdmin()) {

					// Rule to override if one of the rule says that is has a
					// better right
					for (PermissionModel permissionModel : perms) {

						if (permissionModel.isReadOnly() && isInGroup(permissionModel)) {
							ret = SecurityService.READ_ACCESS;
							// Continue we can get better;
						} else if (permissionModel.isReadOnly()) {
							ret = SecurityService.NONE_ACCESS;
						}

						if (permissionModel.isWrite() && !isInGroup(permissionModel) && ret != SecurityService.NONE_ACCESS) {
							ret = SecurityService.READ_ACCESS;
							// Continue we can get better;
						} else if (permissionModel.isWrite()) {
							return SecurityService.WRITE_ACCESS;
							// return we cannot get better
						}
					}

				} 

				return ret;
			}

			return SecurityService.WRITE_ACCESS;
		} finally {
			if (logger.isDebugEnabled()) {
				stopWatch.stop();
				logger.debug("Compute Access Mode takes : " + stopWatch.getTotalTimeSeconds() + "s");
			}

		}
	}

	@Override
	public void refreshAcls() {
		beCPGCacheService.removeFromCache(SecurityService.class.getName(), ACLS_CACHE_KEY);
	}

	private Map<String, List<ACLEntryDataItem.PermissionModel>> getAcls() {
		return beCPGCacheService.getFromCache(SecurityService.class.getName(), ACLS_CACHE_KEY,
				new BeCPGCacheDataProviderCallBack<Map<String, List<ACLEntryDataItem.PermissionModel>>>() {

					@Override
					public Map<String, List<ACLEntryDataItem.PermissionModel>> getData() {
						Map<String, List<ACLEntryDataItem.PermissionModel>> acls = new HashMap<>();
						StopWatch stopWatch = null;
						if (logger.isDebugEnabled()) {
							stopWatch = new StopWatch();
							stopWatch.start();
						}

						List<NodeRef> aclGroups = findAllAclGroups();
						if (aclGroups != null) {
							for (NodeRef aclGroupNodeRef : aclGroups) {
								ACLGroupData aclGrp = alfrescoRepository.findOne(aclGroupNodeRef);
								QName aclGrpType= QName.createQName(aclGrp.getNodeType(),namespaceService);
								List<ACLEntryDataItem> aclEntries = aclGrp.getAcls();
								if (aclEntries != null) {
									for (ACLEntryDataItem aclEntry : aclEntries) {
										String key = computeAclKey(aclGrpType, aclEntry.getPropName());
										List<PermissionModel> perms = new ArrayList<>();
										perms.add(aclEntry.getPermissionModel());
										if (acls.containsKey(key)) {
											perms.addAll(acls.get(key));
										}
										acls.put(key, perms);
									}
								}

							}
						}

						if (logger.isDebugEnabled()) {
							stopWatch.stop();
							logger.debug("Compute ACLs takes : " + stopWatch.getTotalTimeSeconds() + "s");
						}

						return acls;
					}
				});
	}

	private boolean isAdmin() {
		return authorityService.hasAdminAuthority() || AuthenticationUtil.isRunAsUserTheSystemUser();
	}

	/**
	 * Check if current user is in corresponding group
	 */
	private boolean isInGroup(PermissionModel permissionModel) {

		
		for (String currAuth : authorityService.getAuthorities()) {
			if(permissionModel.getGroups().contains(authorityService.getAuthorityNodeRef(currAuth))){
				return true;
			}
		}

		return false;
	}

	private String computeAclKey(QName nodeType, String propName) {
		return nodeType.toString() + "_" + propName;
	}

	private List<NodeRef> findAllAclGroups() {
		return BeCPGQueryBuilder.createQuery().ofType(SecurityModel.TYPE_ACL_GROUP).inDB().list();
	}

	@Override
	public List<String> getAvailablePropNames() {
		List<String> ret = new ArrayList<>();

		List<NodeRef> aclGroups = findAllAclGroups();
		if (aclGroups != null) {
			for (NodeRef aclGroupNodeRef : aclGroups) {

				ACLGroupData aclGroup = alfrescoRepository.findOne(aclGroupNodeRef);

				TypeDefinition typeDefinition = dictionaryService.getType( QName.createQName(aclGroup.getNodeType(),namespaceService));

				if (typeDefinition != null && typeDefinition.getProperties() != null) {
					for (Map.Entry<QName, PropertyDefinition> properties : typeDefinition.getProperties().entrySet()) {
						if (isViewableProperty(properties.getKey())) {
							appendPropName(typeDefinition, properties, ret);
						}
					}

					List<AspectDefinition> aspects = typeDefinition.getDefaultAspects();

					if (aspects == null) {
						aspects = new ArrayList<>();
					}

//					for (QName aspect : aclGroup.getNodeAspects()) {
//						AspectDefinition aspectDefinition = dictionaryService.getAspect(aspect);
//						aspects.add(aspectDefinition);
//					}

					for (AspectDefinition aspect : aspects) {
						if (aspect != null && aspect.getProperties() != null) {
							for (Map.Entry<QName, PropertyDefinition> properties : aspect.getProperties().entrySet()) {
								if (isViewableProperty(properties.getKey())) {
									appendPropName(typeDefinition, properties, ret);
								}
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
		String label = properties.getValue().getTitle(dictionaryService);

		if (!ret.contains(key + "|" + typeDefinition.getTitle(dictionaryService) + " - " + label) && label != null) {
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

		if (qName.equals(ContentModel.PROP_NODE_REF) || qName.equals(ContentModel.PROP_NODE_DBID) || qName.equals(ContentModel.PROP_NODE_UUID)
				|| qName.equals(ContentModel.PROP_STORE_IDENTIFIER) || qName.equals(ContentModel.PROP_STORE_NAME)
				|| qName.equals(ContentModel.PROP_STORE_PROTOCOL)
				|| qName.equals(ContentModel.PROP_CONTENT)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION_PROPS)
				||
				// do not compare frozen properties and version properties
				qName.equals(BeCPGModel.PROP_VERSION_LABEL)
				||
				// system properties
				qName.equals(BeCPGModel.PROP_PARENT_LEVEL) || qName.equals(ContentModel.PROP_NAME) || qName.equals(ContentModel.PROP_CREATOR)
				|| qName.equals(ContentModel.PROP_CREATED) || qName.equals(ContentModel.PROP_ACCESSED) || qName.equals(ContentModel.PROP_MODIFIER)
				|| qName.equals(ContentModel.PROP_MODIFIED)) {

			return false;
		}

		return true;
	}

}
