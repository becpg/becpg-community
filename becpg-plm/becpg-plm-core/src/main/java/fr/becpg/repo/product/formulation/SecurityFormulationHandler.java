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
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.DataListModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.PermissionContext;
import fr.becpg.repo.security.data.PermissionModel;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SecurityFormulationHandler class.</p>
 *
 * @author Evelyne Ing
 * @version $Id: $Id
 */
public class SecurityFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(SecurityFormulationHandler.class);
	public static final String VIEW_DOCUMENTS = "View-documents";
	private static final String EXTERNAL_USER_GROUP_NAME = "GROUP_ExternalUser";

	private NodeService nodeService;
	private SecurityService securityService;
	private PermissionService permissionService;
	private EntityListDAO entityListDAO;
	private AuthorityDAO authorityDAO;
	private SiteService siteService;
	private AssociationService associationService;
	private FileFolderService fileFolderService;
	private SystemConfigurationService systemConfigurationService;
	private BeCPGCacheService beCPGCacheService;

	// Record for template folder data
	private record TemplateFolderData(NodeRef nodeRef, Set<AccessPermission> permissions) {
	}

	// Record for batch permission operations
	private record PermissionUpdate(NodeRef nodeRef, Map<String, String> toAdd, Set<String> toRemove, boolean setInheritance) {
	}

	// Optimized permission context cache key
	private record PermissionContextKey(NodeRef nodeRef, String nodeType, String itemType) {
	}

	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public EntityListDAO getEntityListDAO() {
		return entityListDAO;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public AuthorityDAO getAuthorityDAO() {
		return authorityDAO;
	}

	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
		this.authorityDAO = authorityDAO;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	private boolean enforceACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.enforceACL"));
	}

	private boolean forceResetACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.forceResetACL"));
	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {
		if (!L2CacheSupport.isCacheOnlyEnable() && isSecurityApplicable(productData)) {

			updateSecurityRuleFromTemplate(productData);

			// Initialize shared caches
			final Map<NodeRef, Set<AccessPermission>> permissionsCache = new ConcurrentHashMap<>();
			final Map<NodeRef, Boolean> inheritanceCache = new ConcurrentHashMap<>();
			final Map<PermissionContextKey, PermissionContext> contextCache = new ConcurrentHashMap<>();

			// Pre-warm caches with critical data
			preWarmCaches(productData, permissionsCache, inheritanceCache, contextCache);

			if (enforceACL() || forceResetACL()) {
				processDatalistPermissions(productData, inheritanceCache, permissionsCache, contextCache);
			}

			processDocumentPermissions(productData, inheritanceCache, permissionsCache, contextCache);
		}
		return true;
	}

	/**
	 * Pre-warm all caches with data we know we'll need
	 */
	private void preWarmCaches(ProductData productData, Map<NodeRef, Set<AccessPermission>> permissionsCache, Map<NodeRef, Boolean> inheritanceCache,
			Map<PermissionContextKey, PermissionContext> contextCache) {

		NodeRef productDataNodeRef = productData.getNodeRef();
		String nodeType = nodeService.getType(productDataNodeRef).toString();

		if (enforceACL() || forceResetACL()) {
			// Cache product permissions and inheritance
			getCachedPermissions(productDataNodeRef, permissionsCache);
			getCachedInheritance(productDataNodeRef, inheritanceCache);
		}

		// Pre-cache common permission contexts
		getCachedPermissionContext(productDataNodeRef, nodeType, VIEW_DOCUMENTS, contextCache);

	}

	/**
	 * Batch process datalist permissions with optimized grouping
	 */
	private void processDatalistPermissions(ProductData productData, Map<NodeRef, Boolean> inheritanceCache,
			Map<NodeRef, Set<AccessPermission>> permissionsCache, Map<PermissionContextKey, PermissionContext> contextCache) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(productData.getNodeRef());
		List<NodeRef> datalists = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

		if (datalists.isEmpty()) {
			return;
		}

		String nodeType = nodeService.getType(productData.getNodeRef()).toString();

		// Batch get all datalist item types in one operation
		Map<NodeRef, String> nodeToItemType = datalists.stream().collect(Collectors.toConcurrentMap(nodeRef -> nodeRef,
				nodeRef -> (String) nodeService.getProperty(nodeRef, DataListModel.PROP_DATALISTITEMTYPE)));

		// Group by permission context key to minimize context lookups
		Map<String, List<NodeRef>> datalistsByItemType = nodeToItemType.entrySet().stream().collect(
				Collectors.groupingBy(Map.Entry::getValue, ConcurrentHashMap::new, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

		// Batch collect all permission updates
		List<PermissionUpdate> updates = datalistsByItemType.entrySet().stream().flatMap(entry -> {
			String itemType = entry.getKey();
			PermissionContext context = getCachedPermissionContext(productData.getNodeRef(), nodeType, itemType, contextCache);

			return entry.getValue().stream().map(dataListNodeRef -> preparePermissionUpdate(productData, dataListNodeRef, context.getPermissions(), 
					inheritanceCache, permissionsCache));
		}).toList();

		// Apply all updates in batch
		applyBatchPermissionUpdates(updates, permissionsCache, inheritanceCache);
	}

	/**
	 * Process document permissions with optimized template handling
	 */
	private void processDocumentPermissions(ProductData productData, Map<NodeRef, Boolean> inheritanceCache,
			Map<NodeRef, Set<AccessPermission>> permissionsCache, Map<PermissionContextKey, PermissionContext> contextCache) {

		List<FileInfo> folders = fileFolderService.listFolders(productData.getNodeRef());
		if (folders.isEmpty()) {
			return;
		}

		NodeRef productDataNodeRef = productData.getNodeRef();
		String nodeType = nodeService.getType(productDataNodeRef).toString();

		// Get cached template folder data
		final Map<String, TemplateFolderData> templateFolderData = beCPGCacheService.getFromCache(SecurityFormulationHandler.class.getName(),
				"templateFolderData", () -> precomputeTemplateFolderData(productData.getEntityTpl(), permissionsCache, inheritanceCache));
		// Ensure we have a non-null map even if cache returns null
		final Map<String, TemplateFolderData> safeFolderData = templateFolderData != null ? templateFolderData : Map.of();

		final PermissionContext viewDocumentsContext = getCachedPermissionContext(productDataNodeRef, nodeType, VIEW_DOCUMENTS, contextCache);

		// Batch collect all permission updates
		List<PermissionUpdate> updates = folders.stream().map(folder -> {
			String folderName = folder.getName();
			NodeRef folderNodeRef = folder.getNodeRef();
			TemplateFolderData templateData = safeFolderData.get(folderName);

			if (templateData != null) {
				return prepareTemplatePermissionUpdate(folderNodeRef, templateData, inheritanceCache, permissionsCache);
			} else {
				return preparePermissionUpdate(productData, folderNodeRef, viewDocumentsContext.getPermissions(), inheritanceCache,
						permissionsCache);
			}
		}).toList();

		// Apply all updates in batch
		applyBatchPermissionUpdates(updates, permissionsCache, inheritanceCache);
	}

	/**
	 * Cached permission context lookup
	 */
	private PermissionContext getCachedPermissionContext(NodeRef nodeRef, String nodeType, String itemType,
			Map<PermissionContextKey, PermissionContext> contextCache) {
		PermissionContextKey key = new PermissionContextKey(nodeRef, nodeType, itemType);
		return contextCache.computeIfAbsent(key,
				k -> securityService.getPermissionContext(k.nodeRef(), nodeService.getType(k.nodeRef()), k.itemType()));
	}

	/**
	 * Prepare permission update without immediately applying it
	 */
	private PermissionUpdate preparePermissionUpdate(ProductData productData, NodeRef nodeRef, List<PermissionModel> permissionModels,
			 Map<NodeRef, Boolean> inheritanceCache, Map<NodeRef, Set<AccessPermission>> permissionsCache) {

		boolean hasParentPermissions = getCachedInheritance(nodeRef, inheritanceCache);
		Set<AccessPermission> specificPermissionsSet = hasParentPermissions ? Set.<AccessPermission> of()
				: getCachedPermissions(nodeRef, permissionsCache);

		Map<String, String> specificPermissions = specificPermissionsSet.stream()
				.collect(Collectors.toMap(AccessPermission::getAuthority, AccessPermission::getPermission, (a, b) -> a));

		Map<String, String> parentPermissions = getCachedPermissions(nodeService.getPrimaryParent(nodeRef).getParentRef(), permissionsCache).stream()
				.collect(Collectors.toMap(AccessPermission::getAuthority, AccessPermission::getPermission, (a, b) -> a));

		Map<String, String> toAdd = new HashMap<>();
		Set<String> toRemove = new HashSet<>();
		boolean setInheritance = false;

		if ((permissionModels != null) && !permissionModels.isEmpty() ) {
			SiteInfo siteInfo = siteService.getSite(productData.getNodeRef());
			List<PermissionModel> copyPermissions = copyPermissionsForSuppliers(productData, permissionModels);
			computePermissions(siteInfo, parentPermissions, specificPermissions, copyPermissions, toAdd, toRemove);
			setInheritance = hasParentPermissions;
		} else {
			// Clear all specific permissions
			toRemove.addAll(specificPermissions.keySet());
			setInheritance = !hasParentPermissions;
		}

		return new PermissionUpdate(nodeRef, toAdd, toRemove, setInheritance);
	}

	/**
	 * Prepare template-based permission update
	 */
	private PermissionUpdate prepareTemplatePermissionUpdate(NodeRef folderNodeRef, TemplateFolderData templateData,
			Map<NodeRef, Boolean> inheritanceCache, Map<NodeRef, Set<AccessPermission>> permissionsCache) {

		boolean folderInherits = getCachedInheritance(folderNodeRef, inheritanceCache);
		Set<AccessPermission> templatePermissions = templateData.permissions();
		Set<AccessPermission> folderPermissions = getCachedPermissions(folderNodeRef, permissionsCache);

		Map<String, String> toAdd = new HashMap<>();
		Set<String> toRemove = new HashSet<>();

		if (!templatePermissions.equals(folderPermissions)) {
			// Clear existing permissions
			toRemove.addAll(folderPermissions.stream().map(AccessPermission::getAuthority).collect(Collectors.toSet()));

			// Add template permissions
			templatePermissions.forEach(permission -> toAdd.put(permission.getAuthority(), permission.getPermission()));
		}

		return new PermissionUpdate(folderNodeRef, toAdd, toRemove, folderInherits);
	}

	/**
	 * Apply batch permission updates efficiently
	 */
	private void applyBatchPermissionUpdates(List<PermissionUpdate> updates, Map<NodeRef, Set<AccessPermission>> permissionsCache,
			Map<NodeRef, Boolean> inheritanceCache) {

		// Group operations by type for maximum efficiency
		Map<Boolean, List<PermissionUpdate>> groupedByInheritance = updates.stream()
				.collect(Collectors.partitioningBy(PermissionUpdate::setInheritance));

		// Process inheritance changes first (batch by true/false)
		groupedByInheritance.get(true).forEach(update -> {
			permissionService.setInheritParentPermissions(update.nodeRef(), false);
			inheritanceCache.put(update.nodeRef(), false);
		});

		groupedByInheritance.get(false).stream().filter(update -> !update.toAdd().isEmpty() || !update.toRemove().isEmpty()).forEach(update -> {
			permissionService.setInheritParentPermissions(update.nodeRef(), true);
			inheritanceCache.put(update.nodeRef(), true);
		});

		// Batch permission operations
		updates.stream().forEach(update -> {
			applyPermissionChanges(update.nodeRef(), update.toAdd(), update.toRemove(), permissionsCache);
		});
	}

	/**
	 * Optimized permission changes application
	 */
	private void applyPermissionChanges(NodeRef nodeRef, Map<String, String> toAdd, Set<String> toRemove,
			Map<NodeRef, Set<AccessPermission>> permissionsCache) {

		// Batch clear operations
		if (!toRemove.isEmpty()) {
			toRemove.forEach(authority -> {
				if (logger.isDebugEnabled()) {
					logger.debug("clearing permission: " + authority + " on node: " + nodeRef);
				}
				permissionService.clearPermission(nodeRef, authority);
			});
		}

		// Batch add operations
		if (!toAdd.isEmpty()) {
			toAdd.entrySet().forEach(entry -> {
				String authority = entry.getKey();
				String permission = entry.getValue();
				if (logger.isDebugEnabled()) {
					logger.debug("adding permission: " + authority + ";" + permission + " on node: " + nodeRef);
				}
				permissionService.setPermission(nodeRef, authority, permission, true);
			});

			// Update permissions cache efficiently
			Set<AccessPermission> newPermissions = toAdd.entrySet().stream()
					.map(entry -> new AccessPermissionImpl(entry.getKey(), null, entry.getValue(), 0)).collect(Collectors.toSet());
			permissionsCache.put(nodeRef, newPermissions);
		} else if (!toRemove.isEmpty()) {
			// Clear cache if only removals
			permissionsCache.put(nodeRef, Set.of());
		}
	}

	/**
	 * Optimized template folder data computation with better parallel processing
	 */
	private Map<String, TemplateFolderData> precomputeTemplateFolderData(ProductData entityTpl, Map<NodeRef, Set<AccessPermission>> permissionsCache,
			Map<NodeRef, Boolean> inheritanceCache) {

		if (entityTpl == null) {
			return Map.of();
		}

		List<FileInfo> templateFolders = fileFolderService.listFolders(entityTpl.getNodeRef());

		// Pre-filter and batch process inheritance checks
		Map<NodeRef, Boolean> batchInheritanceResults = templateFolders.stream()
				.collect(Collectors.toConcurrentMap(FileInfo::getNodeRef, folder -> getCachedInheritance(folder.getNodeRef(), inheritanceCache)));

		return templateFolders.stream().filter(folder -> !batchInheritanceResults.get(folder.getNodeRef()))
				.collect(Collectors.toConcurrentMap(FileInfo::getName, folder -> {
					NodeRef folderRef = folder.getNodeRef();
					Set<AccessPermission> permissions = getCachedPermissions(folderRef, permissionsCache);
					return new TemplateFolderData(folderRef, permissions);
				}));
	}

	private Set<AccessPermission> getCachedPermissions(NodeRef nodeRef, Map<NodeRef, Set<AccessPermission>> permissionsCache) {
		return permissionsCache.computeIfAbsent(nodeRef, permissionService::getAllSetPermissions);
	}

	private boolean getCachedInheritance(NodeRef nodeRef, Map<NodeRef, Boolean> inheritanceCache) {
		return inheritanceCache.computeIfAbsent(nodeRef, permissionService::getInheritParentPermissions);
	}

	private boolean isSecurityApplicable(ProductData productData) {
		return (productData.getNodeRef() != null) && !productData.isEntityTemplate();
	}

	private void updateSecurityRuleFromTemplate(ProductData productData) {
		ProductData entityTpl = productData.getEntityTpl();
		if ((entityTpl != null) && (entityTpl.getSecurityRef() != null)) {
			if (logger.isDebugEnabled()) {
				logger.debug("update sec:securityRef assoc from template on node: " + productData.getNodeRef());
			}
			productData.setSecurityRef(entityTpl.getSecurityRef());
		}
	}

	/**
	 * Optimized supplier permissions copying with batch processing
	 */
	private List<PermissionModel> copyPermissionsForSuppliers(ProductData productData, List<PermissionModel> permissionModels) {
		// Check if any model has external user group first
		boolean hasExternalUserGroup = permissionModels.stream().anyMatch(
				model -> model.getGroups().stream().anyMatch(groupRef -> EXTERNAL_USER_GROUP_NAME.equals(authorityDAO.getAuthorityName(groupRef))));

		if (!hasExternalUserGroup && (productData.getSuppliers() == null)) {
			return permissionModels; // No processing needed
		}

		// Batch load supplier accounts once
		List<NodeRef> supplierAccounts = productData.getSuppliers().stream()
				.flatMap(supplierRef -> associationService.getTargetAssocs(supplierRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS).stream()).toList();

		if (supplierAccounts.isEmpty()) {
			return permissionModels; // No supplier accounts to process
		}

		return permissionModels.stream().map(model -> {
			NodeRef externalUserGroup = model.getGroups().stream()
					.filter(groupRef -> EXTERNAL_USER_GROUP_NAME.equals(authorityDAO.getAuthorityName(groupRef))).findFirst().orElse(null);

			if (externalUserGroup != null) {
				List<NodeRef> newGroups = new ArrayList<>(model.getGroups());
				newGroups.remove(externalUserGroup);
				newGroups.addAll(supplierAccounts);
				return new PermissionModel(model.getPermission(), newGroups, model.getIsEnforceACL());
			}
			return model;
		}).toList();
	}

	/**
	 * Optimized permission computation with reduced iterations
	 */
	private void computePermissions(SiteInfo siteInfo, Map<String, String> parentPermissions, Map<String, String> specificPermissions,
			List<PermissionModel> permissionModels, Map<String, String> toAdd, Set<String> toRemove) {

		if ((permissionModels == null) || permissionModels.isEmpty()) {
			return;
		}

		// Pre-compute all authority names in one batch
		Set<String> allAuthorities = permissionModels.stream().flatMap(pm -> pm.getGroups().stream()).map(authorityDAO::getAuthorityName)
				.collect(Collectors.toSet());

		// Batch compute site roles for all authorities
		Map<String, String> authorityToSiteRole = new HashMap<>();
		if (siteInfo != null) {
			allAuthorities.stream().forEach(authority -> {
				String siteRole = siteService.getMembersRole(siteInfo.getShortName(), authority);
				if (siteRole != null) {
					authorityToSiteRole.put(authority, siteRole);
				}
			});
		}

		// Process permission models
		for (PermissionModel permissionModel : permissionModels) {
			String basePermission = PermissionModel.READ_ONLY.equals(permissionModel.getPermission()) ? PermissionService.CONSUMER
					: PermissionService.CONTRIBUTOR;

			List<String> permissionAuthorities = permissionModel.getGroups().stream().map(authorityDAO::getAuthorityName).toList();

			for (String authority : permissionAuthorities) {
				boolean enforceACL = Boolean.TRUE.equals(permissionModel.getIsEnforceACL());
				String targetPermission = basePermission;

				// Optimize permission adaptation
				if (parentPermissions.containsKey(authority) && !enforceACL) {
					String currentPermission = parentPermissions.get(authority);
					if (PermissionService.CONSUMER.equals(currentPermission) || PermissionService.READ.equals(currentPermission)) {
						targetPermission = PermissionService.CONSUMER;
					}
				}

				// Use pre-computed site role
				String siteRole = authorityToSiteRole.get(authority);
				targetPermission = adaptPermissionToSite(targetPermission, siteRole, enforceACL);
				addPermission(authority, targetPermission, toAdd, toRemove);
			}

			// Handle read-for-others logic
			if (PermissionModel.READ_WRITE.equals(permissionModel.getPermission())) {
				parentPermissions.keySet().forEach(authority -> addPermission(authority, PermissionService.READ, toAdd, toRemove));
			}
		}

		// Remove specific permissions not in toAdd
		specificPermissions.keySet().stream().filter(authority -> !toAdd.containsKey(authority)).forEach(toRemove::add);
	}

	private String adaptPermissionToSite(String targetPermission, String sitePermission, boolean enforceACL) {
		if (sitePermission != null) {
			if (PermissionService.CONSUMER.equals(targetPermission) || sitePermission.contains(targetPermission)) {
				return targetPermission;
			}
			if (SiteModel.SITE_COLLABORATOR.equals(sitePermission) || SiteModel.SITE_MANAGER.equals(sitePermission)) {
				return PermissionService.COORDINATOR;
			}
			if (SiteModel.SITE_CONSUMER.equals(sitePermission) && !enforceACL) {
				return PermissionService.CONSUMER;
			}
		}
		return targetPermission;
	}

	private void addPermission(String authority, String permission, Map<String, String> toAdd, Set<String> toRemove) {
		if ((PermissionService.READ.equals(permission) || PermissionService.CONSUMER.equals(permission)) && toAdd.containsKey(authority)) {
			return;
		}
		toAdd.put(authority, permission);
		toRemove.remove(authority);
	}
}