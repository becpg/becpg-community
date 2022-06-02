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
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem.PermissionModel;


/**
 * <p>SecurityFormulationHandler class.</p>
 *
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
		if (!L2CacheSupport.isCacheOnlyEnable()) {
			NodeRef productDataNodeRef = productData.getNodeRef();
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(productDataNodeRef);
			List<NodeRef> datalists = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);

			//Set datalist permissions
			for(NodeRef dataList : datalists) {
				String dataListQName = (String)nodeService.getProperty(dataList, DataListModel.PROP_DATALISTITEMTYPE);

				List<ACLEntryDataItem.PermissionModel> perms = securityService.getNodeACLPermissions(productDataNodeRef, nodeService.getType(productDataNodeRef), dataListQName);
				if (perms != null) {
					setAclPermissions(productDataNodeRef, dataList, perms);
				}
			}

			//Set document permissions
			List<ACLEntryDataItem.PermissionModel> perms = securityService.getNodeACLPermissions(productDataNodeRef, nodeService.getType(productDataNodeRef), VIEW_DOCUMENTS);
			if (perms != null) {
				List<ChildAssociationRef> folders = nodeService.getChildAssocs(productDataNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef folder : folders) {
					setAclPermissions(productDataNodeRef, folder.getChildRef(), perms);
				}
			}
		}
		return true;
	}

	private void setAclPermissions(NodeRef productDataNodeRef, NodeRef nodeRef, List<ACLEntryDataItem.PermissionModel> perms) {

		for (ACLEntryDataItem.PermissionModel permissionModel : perms) {

			String permissionToSet = null;
			boolean setOtherGroupPermissions = false;
			List<String> authorityPermissionGroup = new ArrayList<>();

			if (PermissionModel.READ_ONLY.equals(permissionModel.getPermission())) {
				permissionToSet = PermissionService.CONSUMER;
			} else if (PermissionModel.READ_READANDWRITE.equals(permissionModel.getPermission())) {
				permissionToSet = PermissionService.CONTRIBUTOR;
			} else if (PermissionModel.READ_WRITE.equals(permissionModel.getPermission())) {
				permissionToSet = PermissionService.CONTRIBUTOR;
				setOtherGroupPermissions = true;
			}


			for(NodeRef authorityNodeRef : permissionModel.getGroups()) {
				String authorityName = authorityDAO.getAuthorityName(authorityNodeRef);
				authorityPermissionGroup.add(authorityName);
				if (siteService.getSite(productDataNodeRef) != null) {
					String sitePermission = siteService.getMembersRole(siteService.getSite(productDataNodeRef).getShortName(), authorityName);
					if (sitePermission != null) {							
						if (PermissionService.CONSUMER.equals(permissionToSet) || sitePermission.contains(permissionToSet)) {
							permissionService.setPermission(nodeRef, authorityName, permissionToSet, true);
						} else if ("SiteCollaborator".equals(sitePermission) || "SiteManager".equals(sitePermission)) {
							permissionService.setPermission(nodeRef, authorityName, PermissionService.COORDINATOR, true);
						}
					}
				} else {
					permissionService.setPermission(nodeRef, authorityName, permissionToSet, true);
				}
			}

			//Set read permissions
			if (setOtherGroupPermissions) {
				if (!permissionService.getInheritParentPermissions(nodeRef)) {
					permissionService.setInheritParentPermissions(nodeRef, true);
				}
				for(AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
					if (!authorityPermissionGroup.contains(permission.getAuthority()) && !PermissionService.READ.equals(permission.getPermission())) {
						permissionService.setPermission(nodeRef, permission.getAuthority(), PermissionService.READ , true);
					}
				}
			} else {
				for(AccessPermission permission : permissionService.getAllSetPermissions(nodeRef)) {
					if (!authorityPermissionGroup.contains(permission.getAuthority())){
						permissionService.clearPermission(nodeRef, permission.getAuthority());
					}
				}
			}

			//Set inherit permission
			if (permissionService.getInheritParentPermissions(nodeRef)) {
				permissionService.setInheritParentPermissions(nodeRef, false);
			}
		}
	}
}
