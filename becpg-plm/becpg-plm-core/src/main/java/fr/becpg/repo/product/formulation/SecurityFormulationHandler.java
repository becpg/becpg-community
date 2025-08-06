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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.repo.security.authority.AuthorityDAO;
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
import fr.becpg.model.SecurityModel;
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

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(SecurityFormulationHandler.class);

	public static final String VIEW_DOCUMENTS= "View-documents";
	
	private static final String CACHE_KEY = SecurityFormulationHandler.class.getName();

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
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}
	
	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Getter for the field <code>entityListDAO</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	public EntityListDAO getEntityListDAO() {
		return entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Getter for the field <code>authorityDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.security.authority.AuthorityDAO} object
	 */
	public AuthorityDAO getAuthorityDAO() {
		return authorityDAO;
	}

	/**
	 * <p>Setter for the field <code>authorityDAO</code>.</p>
	 *
	 * @param authorityDAO a {@link org.alfresco.repo.security.authority.AuthorityDAO} object
	 */
	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
		this.authorityDAO = authorityDAO;
	}

	/**
	 * <p>Getter for the field <code>securityService</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.security.SecurityService} object
	 */
	public SecurityService getSecurityService() {
		return securityService;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Getter for the field <code>nodeService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public NodeService getNodeService() {
		return nodeService;
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
	 * <p>Getter for the field <code>permissionService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public PermissionService getPermissionService() {
		return permissionService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	private boolean enforceACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.enforceACL"));
	}
	
	private boolean forceResetACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.forceResetACL"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) throws FormulateException {
		if (!L2CacheSupport.isCacheOnlyEnable() && isSecurityApplicable(productData)) {
			
			updateSecurityRuleFromTemplate(productData);
			
			NodeRef productDataNodeRef = productData.getNodeRef();
			SiteInfo siteInfo = siteService.getSite(productDataNodeRef);
			
			if (enforceACL() || forceResetACL()) {
				NodeRef listContainerNodeRef = entityListDAO.getListContainer(productDataNodeRef);
				List<NodeRef> datalists = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);
				
				if (datalists != null && !datalists.isEmpty()) {
					Map<String, String> containerPermissions = new HashMap<>();
					for (AccessPermission permission : permissionService.getAllSetPermissions(listContainerNodeRef)) {
						containerPermissions.put(permission.getAuthority(), permission.getPermission());
					}
					Map<NodeRef, Map<String, String>> containerPermissionsMap = Map.of(listContainerNodeRef, containerPermissions);
					//Set datalist permissions
					for(NodeRef dataListNodeRef : datalists) {
						String dataListQName = (String)nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
						PermissionContext permissionContext = securityService.getPermissionContext(productDataNodeRef, nodeService.getType(productDataNodeRef), dataListQName);
						updatePermissions(productDataNodeRef, siteInfo, dataListNodeRef, permissionContext.getPermissions(), false, containerPermissionsMap);
					}
				}
			}
			
			Map<NodeRef, Map<String, String>> parentPermissionsCache = new HashMap<>();

			Map<String, Set<AccessPermission>> tplCachedPermissions = new HashMap<>();
			if (productData.getEntityTpl() != null) {
				tplCachedPermissions = getTemplateCachedPermissions(productData.getEntityTpl().getNodeRef());
			}
			
			//Set document permissions
			for (FileInfo folder : fileFolderService.listFolders(productDataNodeRef)) {
				if (tplCachedPermissions.containsKey(folder.getName())) {
					updatePermissionsFromTemplateFolder(folder.getNodeRef(), tplCachedPermissions.get(folder.getName()));
				} else {
					PermissionContext permissionContext = securityService.getPermissionContext(productDataNodeRef, nodeService.getType(productDataNodeRef), VIEW_DOCUMENTS);
					updatePermissions(productDataNodeRef, siteInfo, folder.getNodeRef(), permissionContext.getPermissions(), true, parentPermissionsCache);
				}
			}
		}
		return true;
	}
	
	private Map<String, Set<AccessPermission>> getTemplateCachedPermissions(NodeRef tplNodeRef) {
		return beCPGCacheService.getFromCache(CACHE_KEY, "templateFoldersPermissions." + tplNodeRef, () -> {
			List<FileInfo> tplFolders = null;
			Map<String, Set<AccessPermission>> tplPermissions = new HashMap<>();
			if (tplNodeRef != null) {
				tplFolders = fileFolderService.listFolders(tplNodeRef);
			}
			if (tplFolders != null) {
				for (FileInfo tplFolder : tplFolders) {
					NodeRef tplFolderNodeRef = tplFolder.getNodeRef();
					if (!permissionService.getInheritParentPermissions(tplFolderNodeRef)) {
						tplPermissions.put(tplFolder.getName(), permissionService.getAllSetPermissions(tplFolderNodeRef));
					}
				}
			}
			return tplPermissions;
		});
	}

	private void updatePermissionsFromTemplateFolder(NodeRef folderNodeRef, Set<AccessPermission> templateFolderPermissions) {
		if (permissionService.getInheritParentPermissions(folderNodeRef)) {
			permissionService.setInheritParentPermissions(folderNodeRef, false);
		}
		Set<AccessPermission> folderPermissions = permissionService.getAllSetPermissions(folderNodeRef);
		if (!templateFolderPermissions.equals(folderPermissions)) {
			folderPermissions.forEach(p -> permissionService.clearPermission(folderNodeRef, p.getAuthority()));
			templateFolderPermissions.forEach(p -> 
			permissionService.setPermission(folderNodeRef,p.getAuthority(), p.getPermission(), true));
		}
	}

	private boolean isSecurityApplicable(ProductData productData) {
		return productData.getNodeRef() != null && !productData.isEntityTemplate();
	}

	private void updateSecurityRuleFromTemplate(ProductData productData) {
		if(productData.getEntityTpl()!=null) {
			NodeRef tplNodeRef = productData.getEntityTpl().getNodeRef();
			if (tplNodeRef != null && nodeService.exists(tplNodeRef)) {
				NodeRef tplSecurityRef = associationService.getTargetAssoc(tplNodeRef, SecurityModel.ASSOC_SECURITY_REF);
				if (tplSecurityRef != null && nodeService.exists(tplSecurityRef)) {
					if (logger.isDebugEnabled()) {
						logger.debug("update sec:securityRef assoc from template on node: " + productData.getNodeRef());
					}
					associationService.update(productData.getNodeRef(), SecurityModel.ASSOC_SECURITY_REF, tplSecurityRef);
				}
			}
		}
	}

	private void updatePermissions(NodeRef productDataNodeRef, SiteInfo siteInfo, NodeRef nodeRef, List<PermissionModel> permissionModels,
			boolean areDocuments, Map<NodeRef, Map<String, String>> parentPermissionsMap) {
		if (forceResetACL()) {
			resetPermissions(nodeRef);
			return;
		}
		if (areDocuments || enforceACL()) {
			boolean hasParentPermissions = permissionService.getInheritParentPermissions(nodeRef);
			Map<String, String> specificPermissions = new HashMap<>();
			if (!hasParentPermissions) {
				for (AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
					specificPermissions.put(permission.getAuthority(), permission.getPermission());
				}
			}
			if (permissionModels != null && !permissionModels.isEmpty()) {
				HashMap<String, String> toAdd = new HashMap<>();
				Set<String> toRemove = new HashSet<>();
						
				List<PermissionModel> copyPermissions = copyPermissionsForSuppliers(productDataNodeRef, permissionModels);
				
				Map<String, String> parentPermissions = areDocuments ? 
						parentPermissionsMap.computeIfAbsent(nodeService.getPrimaryParent(nodeRef).getParentRef(), parentRef -> {
							Map<String, String> perm = new HashMap<>();
							for (AccessPermission permission : permissionService.getAllSetPermissions(parentRef)) {
								perm.put(permission.getAuthority(), permission.getPermission());
							}
							return perm;
						})
						: parentPermissionsMap.values().iterator().next();
				
				if (logger.isDebugEnabled()) {
					logger.debug("specificPermissions: " + specificPermissions + " on node: " + nodeRef);
					logger.debug("parentPermissions: " + parentPermissions + " on node: " + nodeRef);
					logger.debug("permissionModels to be applied: " + copyPermissions + " on node: " + nodeRef);
				}
				computePermissions(siteInfo, parentPermissions, specificPermissions, copyPermissions, toAdd, toRemove);
				for (Entry<String, String> entry : toAdd.entrySet()) {
					String authority = entry.getKey();
					String permission = entry.getValue();
					if (!specificPermissions.containsKey(authority) || !specificPermissions.get(authority).equals(permission)) {
						if (logger.isDebugEnabled()) {
							logger.debug("adding permission: " + authority + ";" + permission + " on node: " + nodeRef);
						}
						permissionService.clearPermission(nodeRef, authority);
						permissionService.setPermission(nodeRef, authority, permission, true);
					}
				}
				for (String authority : toRemove) {
					if (logger.isDebugEnabled()) {
						logger.debug("clearing permission: " + authority + " on node: " + nodeRef);
					}
					permissionService.clearPermission(nodeRef, authority);
				}
				if (hasParentPermissions) {
					permissionService.setInheritParentPermissions(nodeRef, false);
				}
			} else {
				for (String authority : specificPermissions.keySet()) {
					if (logger.isDebugEnabled()) {
						logger.debug("clearing permission: " + authority + " on node: " + nodeRef);
					}
					permissionService.clearPermission(nodeRef, authority);
				}
				if (!hasParentPermissions) {
					permissionService.setInheritParentPermissions(nodeRef, true);
				}
			}
		}
	}

	private void resetPermissions(NodeRef nodeRef) {
		boolean hasParentPermissions = permissionService.getInheritParentPermissions(nodeRef);
		Map<String, String> specificPermissions = new HashMap<>();
		if (!hasParentPermissions) {
			for (AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
				specificPermissions.put(permission.getAuthority(), permission.getPermission());
			}
		}
		for (String authority : specificPermissions.keySet()) {
			if (logger.isDebugEnabled()) {
				logger.debug("clearing permission: " + authority + " on node: " + nodeRef);
			}
			permissionService.clearPermission(nodeRef, authority);
		}
		if (!hasParentPermissions) {
			permissionService.setInheritParentPermissions(nodeRef, true);
		}
	}
	
	private List<PermissionModel> copyPermissionsForSuppliers(NodeRef productDataNodeRef, List<PermissionModel> permissionModels) {
		List<PermissionModel> copyPermissions = new ArrayList<>();
		
		List<NodeRef> supplierAccountNodeRefs = associationService.getTargetAssocs(productDataNodeRef, PLMModel.ASSOC_SUPPLIERS).stream()
				.flatMap(s -> associationService.getTargetAssocs(s, PLMModel.ASSOC_SUPPLIER_ACCOUNTS).stream()).toList();
		for (PermissionModel permissionModel : permissionModels) {
			NodeRef externalUserGroup = permissionModel.getGroups().stream()
					.filter(n -> "GROUP_ExternalUser".equals(authorityDAO.getAuthorityName(n))).findFirst().orElse(null);
			if (externalUserGroup != null) {
				List<NodeRef> newGroups = new ArrayList<>(permissionModel.getGroups());
				newGroups.addAll(supplierAccountNodeRefs);
				newGroups.remove(externalUserGroup);
				PermissionModel permissionModelCopy = new PermissionModel(permissionModel.getPermission(), newGroups, permissionModel.getIsEnforceACL());
				copyPermissions.add(permissionModelCopy);
			} else {
				copyPermissions.add(permissionModel);
			}
		}
		return copyPermissions;
	}

	private void computePermissions(SiteInfo siteInfo, Map<String, String> parentPermissions, Map<String, String> specificPermissions, List<PermissionModel> permissionModels, HashMap<String, String> toAdd, Set<String> toRemove) {
		for (PermissionModel permissionModel : permissionModels) {
			String targetPermission = PermissionModel.READ_ONLY.equals(permissionModel.getPermission()) ? PermissionService.CONSUMER : PermissionService.CONTRIBUTOR;
			
			List<String> permissionAuthorities = permissionModel.getGroups().stream()
					.map(n -> authorityDAO.getAuthorityName(n))
					.collect(Collectors.toList());
			
			for (String authority : permissionAuthorities) {
				boolean enforceACL = Boolean.TRUE.equals(permissionModel.getIsEnforceACL());
				if (parentPermissions.containsKey(authority) && !enforceACL) {
					String currentPermission = parentPermissions.get(authority);
					if (currentPermission.equals(PermissionService.CONSUMER) || currentPermission.equals(PermissionService.READ)) {
						targetPermission = PermissionService.CONSUMER;
					}
				}
				targetPermission = adaptPermissionToSite(targetPermission, siteInfo, authority, enforceACL);
				addPermission(authority, targetPermission, toAdd, toRemove);
			}
			
			// set read to parent permissions as business logic is "read for others"
			if (PermissionModel.READ_WRITE.equals(permissionModel.getPermission())) {
				for (String authority : parentPermissions.keySet()) {
					addPermission(authority, PermissionService.READ, toAdd, toRemove);
				}
			}
			
			for (String authority : specificPermissions.keySet()) {
				if (!toAdd.containsKey(authority)) {
					toRemove.add(authority);
				}
			}
		}
	}
	
	private String adaptPermissionToSite(String targetPermission, SiteInfo siteInfo, String authorityName, boolean enforceACL) {
		if (siteInfo != null) {
			String sitePermission = siteService.getMembersRole(siteInfo.getShortName(), authorityName);
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
		}
		
		return targetPermission;
	}


	private void addPermission(String authority, String permission, HashMap<String, String> toAdd, Set<String> toRemove) {
		if ((PermissionService.READ.equals(permission) || PermissionService.CONSUMER.equals(permission)) && toAdd.containsKey(authority)) {
			return;
		}
		toAdd.put(authority, permission);
		if (toRemove.contains(authority)) {
			toRemove.remove(authority);
		}
	}
	
}
