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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
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


/**
 * <p>SecurityFormulationHandler class.</p>
 * @author Evelyne Ing
 */
public class SecurityFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(SecurityFormulationHandler.class);

	private static final String VIEW_DOCUMENTS= "View-documents";

	private NodeService nodeService;

	private SecurityService securityService;

	private PermissionService permissionService;

	private EntityListDAO entityListDAO;

	private AuthorityDAO authorityDAO;

	private SiteService siteService;
	
	private AssociationService associationService;
	
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

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
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
				updatePermissions(siteInfo, dataListNodeRef, permissionContext.getPermissions());
			}

			//Set document permissions
			PermissionContext permissionContext = securityService.getPermissionContext(productDataNodeRef, nodeService.getType(productDataNodeRef), VIEW_DOCUMENTS);
			List<ChildAssociationRef> folders = nodeService.getChildAssocs(productDataNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef folder : folders) {
				updatePermissions(siteInfo, folder.getChildRef(), permissionContext.getPermissions());
			}
		}
		return true;
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
					associationService.update(productData.getNodeRef(), SecurityModel.ASSOC_SECURITY_REF, tplSecurityRef);
				}
			}
		}
	}

	private void updatePermissions(SiteInfo siteInfo, NodeRef nodeRef, List<PermissionModel> permissionModels) {
		
		boolean hasParentPermissions = permissionService.getInheritParentPermissions(nodeRef);
		
		Map<String, String> specificPermissions = new HashMap<>();
		
		if (!hasParentPermissions) {
			for (AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
				specificPermissions.put(permission.getAuthority(), permission.getPermission());
			}
			permissionService.setInheritParentPermissions(nodeRef, true);
		}
		
		Map<String, String> parentPermissions = new HashMap<>();
		for (AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
			if (!specificPermissions.containsKey(permission.getAuthority())) {
				parentPermissions.put(permission.getAuthority(), permission.getPermission());
			}
		}
		
		HashMap<String, String> toAdd = new HashMap<>();
		Set<String> toRemove = new HashSet<>();
		
		if (visitPermissions(siteInfo, parentPermissions, specificPermissions, permissionModels, toAdd, toRemove)) {
			for (Entry<String, String> entry : toAdd.entrySet()) {
				String authority = entry.getKey();
				String permission = entry.getValue();
				if (!specificPermissions.containsKey(authority) || !specificPermissions.get(entry.getKey()).equals(permission)) {
					permissionService.clearPermission(nodeRef, authority);
					permissionService.setPermission(nodeRef, authority, permission, true);
				}
			}
			for (String authority : toRemove) {
				permissionService.clearPermission(nodeRef, authority);
			}
			permissionService.setInheritParentPermissions(nodeRef, false);
		} else {
			for (String authority : specificPermissions.keySet()) {
				permissionService.clearPermission(nodeRef, authority);
			}
			if (!permissionService.getInheritParentPermissions(nodeRef)) {
				permissionService.setInheritParentPermissions(nodeRef, true);
			}
		}
	}
	
	private boolean visitPermissions(SiteInfo siteInfo, Map<String, String> parentPermissions, Map<String, String> specificPermissions, List<PermissionModel> permissionModels, HashMap<String, String> toAdd, Set<String> toRemove) {
		
		if (permissionModels == null || permissionModels.isEmpty()) {
			return false;
		}
		
		for (PermissionModel permissionModel : permissionModels) {
			String basePermission = PermissionModel.READ_ONLY.equals(permissionModel.getPermission()) ? PermissionService.CONSUMER : PermissionService.CONTRIBUTOR;
			
			List<String> permissionAuthorities = permissionModel.getGroups().stream()
					.map(n -> authorityDAO.getAuthorityName(n))
					.collect(Collectors.toList());
			
			for (String authority : permissionAuthorities) {
				String permission = extractPermission(basePermission, siteInfo, authority);
				addPermission(authority, permission, toAdd, toRemove);
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
		
		return true;
	}
	
	private String extractPermission(String basePermission, SiteInfo siteInfo, String authorityName) {
		if (siteInfo != null) {
			String sitePermission = siteService.getMembersRole(siteInfo.getShortName(), authorityName);
			if (sitePermission != null) {		
				if (PermissionService.CONSUMER.equals(basePermission) || sitePermission.contains(basePermission)) {
					return basePermission;
				}
				if (SiteModel.SITE_COLLABORATOR.equals(sitePermission) || SiteModel.SITE_MANAGER.equals(sitePermission)) {
					return PermissionService.COORDINATOR;
				} 
				if(SiteModel.SITE_CONSUMER.equals(sitePermission)) {
					return PermissionService.CONSUMER;
				}
			}
		}
		
		return basePermission;
	}


	private void addPermission(String authority, String permission, HashMap<String, String> toAdd, Set<String> toRemove) {
		if (PermissionService.READ.equals(permission) && toAdd.containsKey(authority)) {
			return;
		}
		toAdd.put(authority, permission);
		if (toRemove.contains(authority)) {
			toRemove.remove(authority);
		}
	}
	
}
