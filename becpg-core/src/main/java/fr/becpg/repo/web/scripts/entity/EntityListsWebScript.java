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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.io.Serializable;
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
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.repo.report.jscript.ReportAssociationDecorator;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.SecurityService;

/**
 * The Class ProductListsWebScript.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class EntityListsWebScript extends AbstractWebScript {

	private static final String RESULT_CONTAINER = "container";

	private static final String RESULT_DATALISTS = "datalists";

	private static final String RESULT_ENTITY = "entity";

	private static final String RESULT_LIST_TYPES = "listTypes";

	private static final String RESULT_ACL_TYPE = "aclType";

	private static final String RESULT_ACL_TYPE_NODE = "aclTypeNode";

	private static final String RESULT_REPORTS = "reports";
	private static final String KEY_NAME_NAME = "name";

	private static final String KEY_NAME_TITLE = "title";

	private static final String KEY_NAME_DESCRIPTION = "description";

	private static final String KEY_NAME_ENTITY_NAME = "entityName";

	private static final String KEY_NAME_NODE_REF = "nodeRef";

	private static final String KEY_NAME_ITEM_TYPE = "itemType";

	private static final String KEY_NAME_STATE = "state";

	private static final String KEY_NAME_PARENT_NODE_REF = "parentNodeRef";

	private static final String KEY_NAME_USER_ACCESS = "userAccess";

	private static final String KEY_NAME_PERMISSIONS = "permissions";

	private static final String KEY_NAME_EDIT = "edit";

	private static final String KEY_NAME_DELETE = "delete";

	private static final String KEY_NAME_EDIT_CHILDREN = "editChildren";

	private static final String KEY_NAME_CREATE = "create";

	private static final String KEY_NAME_USER_SECURITY_ROLES = "userSecurityRoles";

	private static final String KEY_NAME_CHANGE_STATE = "changeState";

	private static final String KEY_NAME_ASPECTS = "aspects";

	private static final String KEY_NAME_TYPE = "type";

	private static final String KEY_NAME_PATH = "path";

	private static final String KEY_NAME_IS_DEFAULT_VARIANT = "isDefaultVariant";

	private static final String KEY_NAME_COLOR = "color";

	private static final String KEY_NAME_VARIANT = "variants";

	private static final String KEY_NAME_IS_MODEL_VARIANT = "isModelVariant";

	private static final String KEY_NAME_VARIANT_PARENT = "variantParent";

	private static final String KEY_NAME_COMPARE_WITH_ENTITIES = "compareWithEntities";

	private static final String PARAM_STORE_TYPE = "store_type";

	private static final String PARAM_STORE_ID = "store_id";

	private static final String PARAM_ACL_MODE = "aclMode";

	private static final String PARAM_ID = "id";

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

	private LockService lockService;

	private ReportAssociationDecorator reportAssociationDecorator;

	private BeCPGLicenseManager becpgLicenseManager;

	/**
	 * <p>Setter for the field <code>becpgLicenseManager</code>.</p>
	 *
	 * @param becpgLicenseManager a {@link fr.becpg.repo.license.BeCPGLicenseManager} object
	 */
	public void setBecpgLicenseManager(BeCPGLicenseManager becpgLicenseManager) {
		this.becpgLicenseManager = becpgLicenseManager;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>entityTplService</code>.</p>
	 *
	 * @param entityTplService a {@link fr.becpg.repo.entity.EntityTplService} object.
	 */
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>transactionService</code>.</p>
	 *
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object.
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>reportAssociationDecorator</code>.</p>
	 *
	 * @param reportAssociationDecorator a {@link fr.becpg.repo.report.jscript.ReportAssociationDecorator} object.
	 */
	public void setReportAssociationDecorator(ReportAssociationDecorator reportAssociationDecorator) {
		this.reportAssociationDecorator = reportAssociationDecorator;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	private Serializable defaultValue(Serializable val, Serializable def) {
		if (val != null) {
			return val;
		} else {
			return def;
		}
	}

	private JSONArray makeListTypes(Iterable<ClassDefinition> classDefinitions) throws JSONException {
		JSONArray listTypes = new JSONArray();
		for (ClassDefinition classDefinition : classDefinitions) {
			JSONObject obj = new JSONObject();

			QName name = classDefinition.getName();
			if (name != null) {
				obj.put(KEY_NAME_NAME, name.toPrefixString(namespaceService));
			}

			obj.put(KEY_NAME_TITLE, defaultValue(classDefinition.getTitle(dictionaryService), ""));
			obj.put(KEY_NAME_DESCRIPTION, defaultValue(classDefinition.getDescription(dictionaryService), ""));

			listTypes.put(obj);
		}
		JSONObject last = new JSONObject();
		last.put(KEY_NAME_NAME, "CustomView");
		last.put(KEY_NAME_TITLE, I18NUtil.getMessage("entity-datalist-customview-title"));
		last.put(KEY_NAME_DESCRIPTION, I18NUtil.getMessage("entity-datalist-customview-description"));
		listTypes.put(last);
		return listTypes;
	}

	private JSONArray makeDatalists(Iterable<NodeRef> lists, NodeRef entity, boolean hasWritePermission, Map<NodeRef, Boolean> accessMap)
			throws JSONException {
		boolean entityIsLocked = lockService.isLocked(entity);

		JSONArray datalist = new JSONArray();
		for (NodeRef list : lists) {
			JSONObject object = new JSONObject();

			object.put(KEY_NAME_ENTITY_NAME, nodeService.getProperty(entity, ContentModel.PROP_NAME));

			Serializable name = nodeService.getProperty(list, ContentModel.PROP_NAME);
			object.put(KEY_NAME_NAME, name);

			object.put(KEY_NAME_TITLE, defaultValue(nodeService.getProperty(list, ContentModel.PROP_TITLE), name));

			object.put(KEY_NAME_DESCRIPTION, defaultValue(nodeService.getProperty(list, ContentModel.PROP_DESCRIPTION), ""));

			object.put(KEY_NAME_NODE_REF, list.toString());

			object.put(KEY_NAME_ITEM_TYPE, defaultValue(nodeService.getProperty(list, DataListModel.PROP_DATALISTITEMTYPE), ""));

			object.put(KEY_NAME_STATE, defaultValue(nodeService.getProperty(list, BeCPGModel.PROP_ENTITYLIST_STATE), "ToValidate"));

			JSONObject permissions = new JSONObject();
			permissions.put(KEY_NAME_EDIT, hasWritePermission && !entityIsLocked);
			permissions.put(KEY_NAME_DELETE, hasWritePermission && !entityIsLocked);

			String dataListQName = (String) nodeService.getProperty(list, DataListModel.PROP_DATALISTITEMTYPE);
			permissions.put(KEY_NAME_EDIT_CHILDREN, !entityIsLocked
					&& (securityService.computeAccessMode(entity, nodeService.getType(entity), dataListQName) == SecurityService.WRITE_ACCESS));

			Boolean accessMapListNodeRef = accessMap.get(list);
			if (accessMapListNodeRef == null) {
				accessMapListNodeRef = false;
			}
			permissions.put(KEY_NAME_CHANGE_STATE, (accessMapListNodeRef) && !entityIsLocked);
			object.put(KEY_NAME_PERMISSIONS, permissions);

			datalist.put(object);
		}
		return datalist;
	}

	private JSONObject makeEntity(NodeRef entity, String path) throws JSONException, IllegalStateException {
		JSONObject result = new JSONObject();

		result.put(KEY_NAME_NODE_REF, entity.toString());

		ChildAssociationRef primaryParent = nodeService.getPrimaryParent(entity);
		if (primaryParent == null) {
			throw new IllegalStateException("Entity is root node");
		}
		result.put(KEY_NAME_PARENT_NODE_REF, primaryParent.getParentRef());

		result.put(KEY_NAME_NAME, nodeService.getProperty(entity, ContentModel.PROP_NAME));

		JSONObject userAccess = new JSONObject();
		boolean entityIsLocked = lockService.isLocked(entity);
		boolean isArchived = nodeService.hasAspect(entity, BeCPGModel.ASPECT_ARCHIVED_ENTITY);
		boolean hasWriteLicense = becpgLicenseManager.hasWriteLicense();
		userAccess.put(KEY_NAME_CREATE, (permissionService.hasPermission(entity, "CreateChildren") == AccessStatus.ALLOWED) && hasWriteLicense
				&& !entityIsLocked && !isArchived);
		userAccess.put(KEY_NAME_EDIT,
				(permissionService.hasPermission(entity, "Write") == AccessStatus.ALLOWED) && hasWriteLicense && !entityIsLocked && !isArchived);
		userAccess.put(KEY_NAME_DELETE,
				(permissionService.hasPermission(entity, "Delete") == AccessStatus.ALLOWED) && hasWriteLicense && !entityIsLocked && !isArchived);
		result.put(KEY_NAME_USER_ACCESS, userAccess);

		JSONArray userSecurityRoles = new JSONArray();
		for (String securityRole : securityService.getUserSecurityRoles()) {
			userSecurityRoles.put(securityRole);
		}
		result.put(KEY_NAME_USER_SECURITY_ROLES, userSecurityRoles);

		JSONArray aspects = new JSONArray();
		for (QName aspect : nodeService.getAspects(entity)) {
			aspects.put(aspect.toPrefixString(namespaceService));
		}
		result.put(KEY_NAME_ASPECTS, aspects);

		result.put(KEY_NAME_TYPE, nodeService.getType(entity).toPrefixString(namespaceService));

		result.put(KEY_NAME_PATH, path);

		Set<NodeRef> nodeRefs = new HashSet<>();
		nodeRefs.add(entity);
		List<ChildAssociationRef> variantsAssociations = new ArrayList<>();
		NodeRef entityTplNodeRef = associationService.getTargetAssoc(entity, BeCPGModel.ASSOC_ENTITY_TPL_REF);
		if (entityTplNodeRef != null) {
			nodeRefs.add(entityTplNodeRef);
		}
		for (NodeRef nodeRef : nodeRefs) {
			if ((permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED)
					&& nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_VARIANT)) {
				for (ChildAssociationRef association : nodeService.getChildAssocs(nodeRef)) {
					if (association.getTypeQName().isMatch(BeCPGModel.ASSOC_VARIANTS)) {
						variantsAssociations.add(association);
					}
				}
			}
		}

		if (!variantsAssociations.isEmpty()) {
			JSONArray variants = new JSONArray();
			for (ChildAssociationRef association : variantsAssociations) {
				NodeRef variant = association.getChildRef();
				JSONObject obj = new JSONObject();
				obj.put(KEY_NAME_NODE_REF, variant.toString());
				obj.put(KEY_NAME_NAME, nodeService.getProperty(variant, ContentModel.PROP_NAME));
				Serializable isDefaultVariant = nodeService.getProperty(variant, BeCPGModel.PROP_IS_DEFAULT_VARIANT);
				obj.put(KEY_NAME_IS_DEFAULT_VARIANT, defaultValue(isDefaultVariant, false));
				obj.put(KEY_NAME_COLOR, defaultValue(nodeService.getProperty(variant, BeCPGModel.PROP_COLOR), ""));
				NodeRef variantParent = nodeService.getPrimaryParent(variant).getParentRef();
				obj.put(KEY_NAME_IS_MODEL_VARIANT, nodeService.hasAspect(variantParent, BeCPGModel.ASPECT_ENTITY_TPL));
				obj.put(KEY_NAME_VARIANT_PARENT, variantParent.toString());
				variants.put(obj);
			}
			result.put(KEY_NAME_VARIANT, variants);
		}

		List<AssociationRef> compareAssociations = new ArrayList<>();
		if (nodeService.hasAspect(entity, BeCPGModel.ASPECT_COMPARE_WITH)) {
			compareAssociations = nodeService.getTargetAssocs(entity, BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES);
		}
		if (!compareAssociations.isEmpty()) {
			JSONArray compareWithEntities = new JSONArray();
			for (AssociationRef association : compareAssociations) {
				NodeRef compareWithEntity = association.getTargetRef();
				JSONObject obj = new JSONObject();
				obj.put(KEY_NAME_NODE_REF, compareWithEntity.toString());
				obj.put(KEY_NAME_NAME, nodeService.getProperty(compareWithEntity, ContentModel.PROP_NAME));
				compareWithEntities.put(obj);
			}
			result.put(KEY_NAME_COMPARE_WITH_ENTITIES, compareWithEntities);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Suggest values according to query
	 * <code>
	 * url : /becpg/entitylists/node/{store_type}/{store_id}/{id}.
	 * </code>
	 */
	@Override
	public final void execute(WebScriptRequest req, WebScriptResponse res) {
		JSONObject result = new JSONObject();
		try {
			// Always return in browser local
			I18NUtil.setContentLocale(null);

			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			String aclMode = req.getParameter(PARAM_ACL_MODE);

			List<NodeRef> listsNodeRef = new ArrayList<>();
			final NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
			NodeRef listContainerNodeRef = null;
			QName nodeType = nodeService.getType(nodeRef);
			boolean hasWritePermission = false;// admin
			Map<NodeRef, Boolean> accessRights = new HashMap<>(); // can
			// delete
			// entity
			// lists
			boolean skipFilter = false;

			// We get datalist for a given aclGroup
			if ((aclMode != null) && SecurityModel.TYPE_ACL_GROUP.equals(nodeType)) {
				logger.debug("We want to get datalist for current ACL entity");
				String aclType = (String) nodeService.getProperty(nodeRef, SecurityModel.PROP_ACL_GROUP_NODE_TYPE);
				QName aclTypeQname = QName.createQName(aclType, namespaceService);

				NodeRef typeNodeRef = BeCPGQueryBuilder.createQuery().ofType(aclTypeQname).singleValue();

				result.put(RESULT_ACL_TYPE, aclType);
				if (typeNodeRef != null) {
					result.put(RESULT_ACL_TYPE_NODE, typeNodeRef.toString());
				} else {
					result.put(RESULT_ACL_TYPE_NODE, "null");
				}

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

				result.put(RESULT_LIST_TYPES, makeListTypes(classDefinitions));

				hasWritePermission = true;
				skipFilter = true;
			}
			// We get datalist for entity
			else if (dictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITY_V2)) {

				NodeRef entityTplNodeRef;
				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)) {
					entityTplNodeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);
				} else {
					entityTplNodeRef = entityTplService.getEntityTpl(nodeType);
				}

				// #1763 Do not work on permissions changed or when node is

				if (entityTplNodeRef != null) {

					final NodeRef templateNodeRef = entityTplNodeRef;
					// Redmine #59 : copy missing datalists as admin, otherwise,
					// if
					// a datalist is added in product template, users cannot see
					// datalists of valid products
					StopWatch watch = null;
					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}
					boolean mlAware = MLPropertyInterceptor.setMLAware(true);

					try {

						AuthenticationUtil.runAs(() -> {
							RetryingTransactionCallback<Object> actionCallback = () -> {
								policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ACTIVITY_LIST);

								entityListDAO.copyDataLists(templateNodeRef, nodeRef, false);

								return null;
							};
							return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
						}, AuthenticationUtil.getAdminUserName());

					} finally {
						MLPropertyInterceptor.setMLAware(mlAware);
					}

					if (logger.isDebugEnabled() && (watch != null)) {
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

				boolean isExternalUser = AuthorityHelper.isCurrentUserExternal();

				Iterator<NodeRef> it = listsNodeRef.iterator();
				while (it.hasNext()) {
					NodeRef temp = it.next();
					if (permissionService.hasPermission(temp, PermissionService.READ) == AccessStatus.ALLOWED) {
						String dataListType = (String) nodeService.getProperty(temp, DataListModel.PROP_DATALISTITEMTYPE);
						int accessMode = securityService.computeAccessMode(nodeRef, nodeType, dataListType);

						if (SecurityService.NONE_ACCESS != accessMode) {
							String dataListName = (String) nodeService.getProperty(temp, ContentModel.PROP_NAME);
							accessMode = Math.min(accessMode, securityService.computeAccessMode(nodeRef, nodeType, dataListName));
						}

						if (SecurityService.NONE_ACCESS == accessMode) {
							if (logger.isTraceEnabled()) {
								logger.trace("Don't display dataList:" + dataListType);
							}
							it.remove();
						} else {
							accessRights.put(temp, (!isExternalUser && (SecurityService.WRITE_ACCESS == accessMode)
									&& (permissionService.hasPermission(temp, PermissionService.WRITE) == AccessStatus.ALLOWED)));
						}
					} else {
						it.remove();
					}
				}
			}

			Path path = nodeService.getPath(nodeRef);

			String stringPath = path.toPrefixString(namespaceService);
			String displayPath = path.toDisplayPath(nodeService, permissionService);

			String retPath = SiteHelper.extractDisplayPath(stringPath, displayPath);

			if (nodeService.hasAspect(nodeRef, ReportModel.ASPECT_REPORT_ENTITY)) {
				result.put(RESULT_REPORTS, reportAssociationDecorator.decorate(ReportModel.ASSOC_REPORTS, nodeRef,
						associationService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORTS)));
			}

			result.put(RESULT_ENTITY, makeEntity(nodeRef, retPath));
			result.put(RESULT_CONTAINER, listContainerNodeRef != null ? listContainerNodeRef.toString() : null);

			// hasWritePermission as it used to appear in the model
			final boolean effectiveHasWritePermission = hasWritePermission
					|| authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser());

			JSONObject permissions = new JSONObject();
			permissions.put(KEY_NAME_CREATE, effectiveHasWritePermission && !lockService.isLocked(nodeRef));
			result.put(KEY_NAME_PERMISSIONS, permissions);
			result.put(RESULT_DATALISTS, makeDatalists(listsNodeRef, nodeRef, effectiveHasWritePermission, accessRights));

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			result.write(res.getWriter());
		} catch (JSONException | IOException | IllegalStateException e) {
			logger.error(e, e);
			throw new WebScriptException("Failed to build JSON file", e);
		}
	}

}
