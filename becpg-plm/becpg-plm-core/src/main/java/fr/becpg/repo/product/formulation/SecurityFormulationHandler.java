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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
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
 * @author Evelyne Ing
 */
public class SecurityFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(SecurityFormulationHandler.class);

	private static final String VIEW_DOCUMENTS= "View-documents";

	private NodeService nodeService;

	private SecurityService securityService;

	private PermissionService permissionService;

	private EntityListDAO entityListDAO;

	private AuthorityDAO authorityDAO;

	private SiteService siteService;
	
	private AssociationService associationService;
	
	private FileFolderService fileFolderService;
	
	private SystemConfigurationService systemConfigurationService;
	
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
	
	private boolean enforceACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.enforceACL"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) throws FormulateException {
		if (!L2CacheSupport.isCacheOnlyEnable() && isSecurityApplicable(productData)) {
			
			updateSecurityRuleFromTemplate(productData);
			
			NodeRef productDataNodeRef = productData.getNodeRef();
			
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productDataNodeRef);
			List<NodeRef> datalists = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

			SiteInfo siteInfo = siteService.getSite(productDataNodeRef);
			
			//Set datalist permissions
			for(NodeRef dataListNodeRef : datalists) {
				String dataListQName = (String)nodeService.getProperty(dataListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				PermissionContext permissionContext = securityService.getPermissionContext(productDataNodeRef, nodeService.getType(productDataNodeRef), dataListQName);
				updateSupplierPortalPermissionContext(permissionContext, productDataNodeRef);
				updatePermissions(siteInfo, dataListNodeRef, permissionContext.getPermissions(), false);
			}

			//Set document permissions
			for (FileInfo folder : fileFolderService.listFolders(productDataNodeRef)) {
				NodeRef templateFolderWithSpecificPermissions = findTemplateFolderWithSpecificPermissions(folder.getNodeRef(), productData.getEntityTpl());
				if (templateFolderWithSpecificPermissions != null) {
					updatePermissionsFromTemplateFolder(folder.getNodeRef(), templateFolderWithSpecificPermissions);
				} else {
					PermissionContext permissionContext = securityService.getPermissionContext(productDataNodeRef, nodeService.getType(productDataNodeRef), VIEW_DOCUMENTS);
					updateSupplierPortalPermissionContext(permissionContext, productDataNodeRef);
					updatePermissions(siteInfo, folder.getNodeRef(), permissionContext.getPermissions(), true);
				}
			}
		}
		return true;
	}

	private void updateSupplierPortalPermissionContext(PermissionContext permissionContext, NodeRef productDataNodeRef) {
		List<NodeRef> supplierAccountNodeRefs = associationService.getTargetAssocs(productDataNodeRef, PLMModel.ASSOC_SUPPLIERS).stream()
				.flatMap(s -> associationService.getTargetAssocs(s, PLMModel.ASSOC_SUPPLIER_ACCOUNTS).stream()).collect(Collectors.toList());
		for (PermissionModel permissionModel : permissionContext.getPermissions()) {
			NodeRef externalUserGroup = permissionModel.getGroups().stream()
					.filter(n -> "GROUP_ExternalUser".equals(authorityDAO.getAuthorityName(n))).findFirst().orElse(null);
			if (externalUserGroup != null) {
				permissionModel.getGroups().remove(externalUserGroup);
				permissionModel.getGroups().addAll(supplierAccountNodeRefs);
			}
		}
	}

	private NodeRef findTemplateFolderWithSpecificPermissions(NodeRef folderNodeRef, ProductData entityTpl) {
		List<FileInfo> templateFolders = null;
		if (entityTpl != null) {
			templateFolders = fileFolderService.listFolders(entityTpl.getNodeRef());
		}
		if (templateFolders != null) {
			String folderName = (String) nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME);
			for (FileInfo folder : templateFolders) {
				if (folder.getName().equals(folderName) && !permissionService.getInheritParentPermissions(folder.getNodeRef())) {
					return folder.getNodeRef();
				}
			}
		}
		return null;
	}

	private void updatePermissionsFromTemplateFolder(NodeRef folderNodeRef, NodeRef templateFolder) {
		if (permissionService.getInheritParentPermissions(folderNodeRef)) {
			permissionService.setInheritParentPermissions(folderNodeRef, false);
		}
		Set<AccessPermission> templateFolderPermissions = permissionService.getAllSetPermissions(templateFolder);
		Set<AccessPermission> folderPermissions = permissionService.getAllSetPermissions(folderNodeRef);
		if (!templateFolderPermissions.equals(folderPermissions)) {
			folderPermissions.forEach(p -> permissionService.clearPermission(folderNodeRef, p.getAuthority()));
			templateFolderPermissions.forEach(p -> 
			permissionService.setPermission(folderNodeRef,p.getAuthority(), p.getPermission(), true));
		}
	}

	private boolean isSecurityApplicable(ProductData productData) {
		return !productData.isEntityTemplate();
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

	private void updatePermissions(SiteInfo siteInfo, NodeRef nodeRef, List<PermissionModel> permissionModels, boolean areDocuments) {
		
		boolean hasParentPermissions = permissionService.getInheritParentPermissions(nodeRef);
		Map<String, String> specificPermissions = new HashMap<>();
		
		if (!hasParentPermissions) {
			for (AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
				specificPermissions.put(permission.getAuthority(), permission.getPermission());
			}
		}
		
		Map<String, String> parentPermissions = new HashMap<>();
		for (AccessPermission permission : permissionService.getAllSetPermissions(nodeService.getPrimaryParent(nodeRef).getParentRef())) {
			parentPermissions.put(permission.getAuthority(), permission.getPermission());
		}
		
		HashMap<String, String> toAdd = new HashMap<>();
		Set<String> toRemove = new HashSet<>();
		
		if (permissionModels != null && !permissionModels.isEmpty() && (areDocuments || enforceACL())) {
			if (logger.isDebugEnabled()) {
				logger.debug("specificPermissions: " + specificPermissions + " on node: " + nodeRef);
				logger.debug("parentPermissions: " + parentPermissions + " on node: " + nodeRef);
				logger.debug("permissionModels to be applied: " + permissionModels + " on node: " + nodeRef);
			}
			computePermissions(siteInfo, parentPermissions, specificPermissions, permissionModels, toAdd, toRemove);
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
