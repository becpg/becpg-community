package fr.becpg.repo.security.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.springframework.util.StopWatch;

import fr.becpg.model.SecurityModel;
import fr.becpg.repo.BeCPGDao;
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
public class SecurityServiceImpl implements SecurityService {

	/*
	 * ACL keys / Group List
	 */
	private Map<String, List<ACLEntryDataItem.PermissionModel>> acls = new HashMap<String, List<ACLEntryDataItem.PermissionModel>>();

	private static Log logger = LogFactory.getLog(SecurityServiceImpl.class);

	private BeCPGDao<ACLGroupData> aclGroupDao;

	private AuthorityService authorityService;

	private BeCPGSearchService beCPGSearchService;

	private DictionaryService dictionaryService;

	private NamespacePrefixResolver namespacePrefixResolver;

	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAclGroupDao(BeCPGDao<ACLGroupData> aclGroupDao) {
		this.aclGroupDao = aclGroupDao;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	/**
	 * Compute access mode for the given field name on a specific type
	 * @param nodeType
	 * @param name
	 * @return Access Mode status
	 */
	public int computeAccessMode(QName nodeType, String propName) {
		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}
		try {

			String key = computeAclKey(nodeType, propName);
			if (acls.containsKey(key)) {

				List<ACLEntryDataItem.PermissionModel> perms = acls.get(key);
				int ret = SecurityService.WRITE_ACCESS;
				if (!isAdmin()) {

					// Rule to override if one of the rule says that is has a
					// better right
					for (PermissionModel permissionModel : perms) {

						if (permissionModel.isReadOnly()
								&& isInGroup(permissionModel)) {
							ret = SecurityService.READ_ACCESS;
							// Continue we can get better;
						} else if (permissionModel.isReadOnly()
								&& ret == SecurityService.WRITE_ACCESS) {
							ret = SecurityService.NONE_ACCESS;

						}

						if (permissionModel.isWrite()
								&& !isInGroup(permissionModel)) {
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
				logger.debug("Compute Access Mode takes : "
						+ stopWatch.getTotalTimeSeconds() + "s");
			}

		}
	}

	private boolean isAdmin() {
		return authorityService.hasAdminAuthority();
	}

	/**
	 * Check if current user is in corresponding group
	 */
	private boolean isInGroup(PermissionModel permissionModel) {

		for (String currAuth : authorityService.getAuthorities()) {
			for (String checkedAuth : permissionModel.getGroups()) {
				if (currAuth != null && currAuth.equals(checkedAuth)) {
					return true;
				}
			}

		}

		return false;
	}

	private String computeAclKey(QName nodeType, String propName) {
		return nodeType.toString() + "_" + propName;
	}

	public void init() {
		logger.info("Init SecurityService");
		computeAcls();
	}

	public void computeAcls() {
		acls.clear();
		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}

		List<NodeRef> aclGroups = findAllAclGroups();
		if (aclGroups != null) {
			for (NodeRef aclGroupNodeRef : aclGroups) {
				ACLGroupData aclGrp = aclGroupDao.find(aclGroupNodeRef);
				List<ACLEntryDataItem> aclEntries = aclGrp.getAcls();
				if (aclEntries != null) {
					for (ACLEntryDataItem aclEntry : aclEntries) {
						String key = computeAclKey(aclGrp.getNodeType(),
								aclEntry.getPropName());
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
			logger.debug("Compute ACLs takes : "
					+ stopWatch.getTotalTimeSeconds() + "s");
		}

	}

	private List<NodeRef> findAllAclGroups() {
		String runnedQuery = "+TYPE:\""
				+ SecurityModel.TYPE_ACL_GROUP.toString() + "\"";
		return beCPGSearchService.unProtLuceneSearch(runnedQuery);
	}

	@Override
	public List<String> getAvailablePropNames() {
		List<String> ret = new ArrayList<String>();

		List<NodeRef> aclGroups = findAllAclGroups();
		if (aclGroups != null) {
			for (NodeRef aclGroupNodeRef : aclGroups) {

				ACLGroupData aclGroup = aclGroupDao.find(aclGroupNodeRef);

				TypeDefinition typeDefinition = dictionaryService
						.getType(aclGroup.getNodeType());

				if (typeDefinition != null
						&& typeDefinition.getProperties() != null) {
					for (Map.Entry<QName, PropertyDefinition> properties : typeDefinition
							.getProperties().entrySet()) {

						appendPropName(typeDefinition,properties,ret);
					}

					List<AspectDefinition> aspects = typeDefinition
							.getDefaultAspects();
					if (aspects != null) {
						for (AspectDefinition aspect : aspects) {
							if (aspect != null
									&& aspect.getProperties() != null) {
								for (Map.Entry<QName, PropertyDefinition> properties : aspect
										.getProperties().entrySet()) {
									appendPropName(typeDefinition,properties,ret);
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
		String key = properties.getKey()
				.toPrefixString(namespacePrefixResolver);
		String label = properties.getValue().getTitle();

		if(!ret.contains(key + "|" + typeDefinition.getTitle() + " - " + label)){
			ret.add(key + "|" + typeDefinition.getTitle() + " - " + label);
		}

	}

}
