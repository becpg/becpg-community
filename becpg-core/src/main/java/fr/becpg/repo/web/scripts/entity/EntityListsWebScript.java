/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.SecurityService;

/**
 * The Class ProductListsWebScript.
 *
 * @author querephi
 */
public class EntityListsWebScript extends DeclarativeWebScript {

	private static final String PARAM_STORE_TYPE = "store_type";

	private static final String PARAM_STORE_ID = "store_id";

	private static final String PARAM_ACL_MODE = "aclMode";

	private static final String PARAM_ID = "id";

	private static final String MODEL_KEY_NAME_ENTITY = "entity";

	private static final String MODEL_KEY_NAME_CONTAINER = "container";
	
	private static final String MODEL_KEY_USER_SECURITY_ROLES = "userSecurityRoles";

	private static final String MODEL_KEY_NAME_LISTS = "lists";

	private static final String MODEL_HAS_WRITE_PERMISSION = "hasWritePermission";

	private static final String MODEL_HAS_CHANGE_STATE_PERMISSION = "hasChangeStatePermission";

	private static final String MODEL_KEY_ACL_TYPE = "aclType";
	
	private static final String MODEL_KEY_ACL_TYPE_NODE = "aclTypeNode";

	private static final String MODEL_PROP_KEY_LIST_TYPES = "listTypes";

	private static final String MODEL_KEY_NAME_ENTITY_PATH = "entityPath";

	private static final String MODEL_ACCESS_MAP = "accessMap";

	private static final Log logger = LogFactory.getLog(EntityListsWebScript.class);

	private NodeService nodeService;

	private SecurityService securityService;

	private EntityListDAO entityListDAO;

	private EntityTplService entityTplService;

	private NamespaceService namespaceService;

	private TransactionService transactionService;

	private DictionaryService dictionaryService;

	private AuthorityService authorityService;

	private PermissionService permissionService;

	private AssociationService associationService;

	private BehaviourFilter policyBehaviourFilter;

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * Suggest values according to query
	 *
	 * url : /becpg/entitylists/node/{store_type}/{store_id}/{id}.
	 *
	 * @param req
	 *            the req
	 * @param status
	 *            the status
	 * @param cache
	 *            the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String aclMode = req.getParameter(PARAM_ACL_MODE);

		logger.debug("entityListsWebScript executeImpl()");

		List<NodeRef> listsNodeRef = new ArrayList<>();
		final NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		NodeRef listContainerNodeRef = null;
		QName nodeType = nodeService.getType(nodeRef);
		boolean hasChangeStatePermission = false;
		boolean hasWritePermission = false;// admin
		Map<NodeRef, Boolean> accessRights = new HashMap<>(); // can
		// delete
		// entity
		// lists
		boolean skipFilter = false;

		// Date lastModified = null;

		Map<String, Object> model = new HashMap<>();

		// We get datalist for a given aclGroup
		if ((aclMode != null) && SecurityModel.TYPE_ACL_GROUP.equals(nodeType)) {
			logger.debug("We want to get datalist for current ACL entity");
			String aclType = (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_ACL_GROUP_NODE_TYPE);
			QName aclTypeQname = QName.createQName(aclType, namespaceService);
			
			
			NodeRef typeNodeRef = BeCPGQueryBuilder.createQuery().ofType(aclTypeQname).singleValue();
			
			if(typeNodeRef!=null) {
				model.put(MODEL_KEY_ACL_TYPE_NODE, typeNodeRef.toString());
			}
			model.put(MODEL_KEY_ACL_TYPE, aclType);
			
				
			NodeRef templateNodeRef = entityTplService.getEntityTpl(aclTypeQname);
			if (templateNodeRef != null) {
				listContainerNodeRef = entityListDAO.getListContainer(templateNodeRef);
				if (listContainerNodeRef == null) {
					listContainerNodeRef = entityListDAO.createListContainer(templateNodeRef);
				}
			} else {
				logger.error("Cannot get templateNodeRef for type : " + aclType);
			}
			skipFilter = true;
		}
		// We get datalist for entityTpl
		else if ((nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS) && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL))
				|| BeCPGModel.TYPE_SYSTEM_ENTITY.equals(nodeType)) {

			listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
			if (listContainerNodeRef == null) {
				listContainerNodeRef = entityListDAO.createListContainer(nodeRef);
			}

			// Add types that can be added
			Set<ClassDefinition> classDefinitions = new HashSet<>();
			Collection<QName> entityListTypes = dictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true);

			for (QName entityListType : entityListTypes) {
				if (!BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(entityListType)) {
					classDefinitions.add(dictionaryService.getClass(entityListType));
				}
			}

			model.put(MODEL_PROP_KEY_LIST_TYPES, classDefinitions);

			hasWritePermission = true;
			skipFilter = true;
		}
		// We get datalist for entity
		else {

			NodeRef entityTplNodeRef;
			if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)) {
				entityTplNodeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
			} else {
				entityTplNodeRef = entityTplService.getEntityTpl(nodeType);
			}

			// #1763 Do not work on permissions changed or when node is locked
			//
			// Date propModified = (Date) nodeService.getProperty(nodeRef,
			// ContentModel.PROP_MODIFIED);
			// lastModified = entityTplNodeRef != null ? (Date)
			// nodeService.getProperty(entityTplNodeRef,
			// ContentModel.PROP_MODIFIED) : null;
			// if (lastModified == null || (propModified != null &&
			// lastModified.getTime() < propModified.getTime())) {
			// lastModified = propModified;
			// }
			//
			// if (lastModified != null &&
			// BrowserCacheHelper.shouldReturnNotModified(req, lastModified)) {
			// status.setCode(HttpServletResponse.SC_NOT_MODIFIED);
			// status.setRedirect(true);
			//
			// if (logger.isDebugEnabled()) {
			// logger.debug("Send Not_MODIFIED status");
			// }
			// return model;
			// }

			if (entityTplNodeRef != null) {

				final NodeRef templateNodeRef = entityTplNodeRef;
				// Redmine #59 : copy missing datalists as admin, otherwise, if
				// a datalist is added in product template, users cannot see
				// datalists of valid products
				StopWatch watch = null;
				if (logger.isDebugEnabled()) {
					watch = new StopWatch();
					watch.start();
				}
				boolean mlAware = MLPropertyInterceptor.isMLAware();

				try {
					MLPropertyInterceptor.setMLAware(true);
					AuthenticationUtil.runAs(() -> {
						RetryingTransactionCallback<Object> actionCallback = () -> {
							policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
							entityListDAO.copyDataLists(templateNodeRef, nodeRef, false);

							return null;
						};
						return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
					}, AuthenticationUtil.getAdminUserName());

				} finally {
					MLPropertyInterceptor.setMLAware(mlAware);
				}

				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.debug("copyDataLists executed in  " + watch.getTotalTimeSeconds() + " seconds - templateNodeRef " + templateNodeRef);
				}

			}

			listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
		}

		if (listContainerNodeRef != null) {

			listsNodeRef = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);
		}

		// filter list with perms
		if (!skipFilter) {

			boolean isExternalUser = isCurrentUserExternal();

			Iterator<NodeRef> it = listsNodeRef.iterator();
			while (it.hasNext()) {
				NodeRef temp = it.next();
				String dataListType = (String) nodeService.getProperty(temp, DataListModel.PROP_DATALISTITEMTYPE);
				int access_mode = securityService.computeAccessMode(nodeType, dataListType);
				
				if(SecurityService.NONE_ACCESS != access_mode) {
					String dataListName = (String) nodeService.getProperty(temp, ContentModel.PROP_NAME);
					access_mode = securityService.computeAccessMode(nodeType, dataListName);
				}
				

				if (SecurityService.NONE_ACCESS == access_mode) {
					if (logger.isTraceEnabled()) {
						logger.trace("Don't display dataList:" + dataListType);
					}
					it.remove();
				} else if (!isExternalUser && (SecurityService.WRITE_ACCESS == access_mode)
						&& (permissionService.hasPermission(temp, PermissionService.WRITE) == AccessStatus.ALLOWED)) {
					accessRights.put(temp, true);
				} else {
					accessRights.put(temp, false);
				}
			}
		}

		// if (lastModified == null) {
		// lastModified = new Date();
		// }
		//
		// cache.setIsPublic(false);
		// cache.setMustRevalidate(true);
		// cache.setNeverCache(false);
		// cache.setMaxAge(0L);
		// cache.setLastModified(lastModified);

		Path path = nodeService.getPath(nodeRef);

		String stringPath = path.toPrefixString(namespaceService);
		String displayPath = path.toDisplayPath(nodeService, permissionService);

		String retPath = SiteHelper.extractDisplayPath(stringPath, displayPath);

		model.put(MODEL_KEY_NAME_ENTITY_PATH, retPath);
		model.put(MODEL_KEY_NAME_ENTITY, nodeRef);
		model.put(MODEL_KEY_NAME_CONTAINER, listContainerNodeRef);
		model.put(MODEL_KEY_USER_SECURITY_ROLES, securityService.getUserSecurityRoles());
		
		model.put(MODEL_HAS_WRITE_PERMISSION,
				hasWritePermission || authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser()));
		model.put(MODEL_HAS_CHANGE_STATE_PERMISSION, hasChangeStatePermission);
		model.put(MODEL_KEY_NAME_LISTS, listsNodeRef);
		model.put(MODEL_ACCESS_MAP, accessRights);

		return model;
	}

	private boolean isCurrentUserExternal() {
		for (String currAuth : authorityService.getAuthorities()) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}

}
