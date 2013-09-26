package fr.becpg.repo.security.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGSearchService;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;

/**
 * Security Service : is in charge to compute acls by node Type. And provide
 * permission on properties
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 */
@Service
public class SecurityServiceImpl implements SecurityService {

	private static final String ACLS_CACHE_KEY = "ACLS_CACHE_KEY";

	private static Log logger = LogFactory.getLog(SecurityServiceImpl.class);

	private AlfrescoRepository<ACLGroupData> alfrescoRepository;

	private AuthorityService authorityService;

	private BeCPGSearchService beCPGSearchService;

	private DictionaryService dictionaryService;

	private NamespacePrefixResolver namespacePrefixResolver;

	private BeCPGCacheService beCPGCacheService;

	public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	
	public void setAlfrescoRepository(AlfrescoRepository<ACLGroupData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

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
						Map<String, List<ACLEntryDataItem.PermissionModel>> acls = new HashMap<String, List<PermissionModel>>();
						StopWatch stopWatch = null;
						if (logger.isDebugEnabled()) {
							stopWatch = new StopWatch();
							stopWatch.start();
						}

						List<NodeRef> aclGroups = findAllAclGroups();
						if (aclGroups != null) {
							for (NodeRef aclGroupNodeRef : aclGroups) {
								ACLGroupData aclGrp = alfrescoRepository.findOne(aclGroupNodeRef);
								QName aclGrpType= QName.createQName(aclGrp.getNodeType(),namespacePrefixResolver);
								List<ACLEntryDataItem> aclEntries = aclGrp.getAcls();
								if (aclEntries != null) {
									for (ACLEntryDataItem aclEntry : aclEntries) {
										String key = computeAclKey(aclGrpType, aclEntry.getPropName());
										List<PermissionModel> perms = new ArrayList<ACLEntryDataItem.PermissionModel>();
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
		return authorityService.hasAdminAuthority();
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
		String runnedQuery = "+TYPE:\"" + SecurityModel.TYPE_ACL_GROUP.toString() + "\"";
		return beCPGSearchService.luceneSearch(runnedQuery);
	}

	@Override
	public List<String> getAvailablePropNames() {
		List<String> ret = new ArrayList<String>();

		List<NodeRef> aclGroups = findAllAclGroups();
		if (aclGroups != null) {
			for (NodeRef aclGroupNodeRef : aclGroups) {

				ACLGroupData aclGroup = alfrescoRepository.findOne(aclGroupNodeRef);

				TypeDefinition typeDefinition = dictionaryService.getType( QName.createQName(aclGroup.getNodeType(),namespacePrefixResolver));

				if (typeDefinition != null && typeDefinition.getProperties() != null) {
					for (Map.Entry<QName, PropertyDefinition> properties : typeDefinition.getProperties().entrySet()) {
						if (isViewableProperty(properties.getKey())) {
							appendPropName(typeDefinition, properties, ret);
						}
					}

					List<AspectDefinition> aspects = typeDefinition.getDefaultAspects();

					if (aspects == null) {
						aspects = new ArrayList<AspectDefinition>();
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
		String key = properties.getKey().toPrefixString(namespacePrefixResolver);
		String label = properties.getValue().getTitle();

		if (!ret.contains(key + "|" + typeDefinition.getTitle() + " - " + label) && label != null) {
			ret.add(key + "|" + typeDefinition.getTitle() + " - " + label);
		}

	}

	/**
	 * Test if the property should be show
	 * 
	 * @param qName
	 *            the q name
	 * @return true, if is viewable property
	 */
	@SuppressWarnings("deprecation")
	private boolean isViewableProperty(QName qName) {

		if (qName.equals(ContentModel.PROP_NODE_REF) || qName.equals(ContentModel.PROP_NODE_DBID) || qName.equals(ContentModel.PROP_NODE_UUID)
				|| qName.equals(ContentModel.PROP_STORE_IDENTIFIER) || qName.equals(ContentModel.PROP_STORE_NAME)
				|| qName.equals(ContentModel.PROP_STORE_PROTOCOL)
				|| qName.equals(ContentModel.PROP_CONTENT)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION)
				|| qName.equals(ContentModel.PROP_AUTO_VERSION_PROPS)
				||
				// do not compare frozen properties and version properties
				qName.equals(BeCPGModel.PROP_VERSION_DESCRIPTION) || qName.equals(BeCPGModel.PROP_VERSION_LABEL) || qName.equals(BeCPGModel.PROP_FROZEN_NODE_DBID)
				|| qName.equals(BeCPGModel.PROP_FROZEN_NODE_REF) || qName.equals(BeCPGModel.PROP_FROZEN_ACCESSED) || qName.equals(BeCPGModel.PROP_FROZEN_CREATOR)
				|| qName.equals(BeCPGModel.PROP_FROZEN_CREATED) || qName.equals(BeCPGModel.PROP_FROZEN_MODIFIER)
				|| qName.equals(BeCPGModel.PROP_FROZEN_MODIFIED)
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
