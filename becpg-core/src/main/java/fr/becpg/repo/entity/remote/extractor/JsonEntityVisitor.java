/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
package fr.becpg.repo.entity.remote.extractor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.entity.remote.extractor.RemoteJSONContext.JsonVisitNodeType;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.json.JsonHelper;

/**
 * <p>
 * JsonEntityVisitor class.
 * </p>
 * <p>
 * Node metadata is read through the internal <code>nodeService</code>, the bean the rest of beCPG
 * reads with, and not through the public <code>NodeService</code>, whose security interceptor
 * re-evaluates the caller's permissions on every single call. The visitor makes about a dozen such
 * calls per node and a listing serializes hundreds of nodes, so that check alone accounted for a
 * factor six between an administrator and a supplier account on
 * <code>becpg/remote/entity/list</code>. What a caller is allowed to see is settled upstream, by
 * the search that produced the page.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonEntityVisitor extends AbstractEntityVisitor {

	private static final Log logger = LogFactory.getLog(JsonEntityVisitor.class);

	private static final String PAGINATION_HAS_MORE_ITEMS = "hasMoreItems";
	private static final String PAGINATION_COUNT = "count";
	private static final String PAGINATION_TOTAL_ITEMS = "totalItems";
	private static final String KEY_ENTITIES = "entities";
	private static final String KEY_PAGINATION = "pagination";
	private static final String KEY_METADATA = "metadata";

	private static final String PATH_SEPARATOR_REPLACEMENT = "~";
	private static final String DATALIST_TYPE_SEPARATOR = "@";
	private static final String DATALIST_NAME_SEPARATOR = "|";
	private static final String QNAME_PREFIX_SEPARATOR = ":";
	private static final String ACCESS_DENIED_VALUE = "#AccessDenied";
	private static final String ERR_CANNOT_SERIALIZE = "Cannot serialize data";

	private final AttributeExtractorService attributeExtractor;
	private final VersionService versionService;
	private final LockService lockService;
	private final AssociationService associationService;
	private final EntityListDAO entityListDAO;
	private final PermissionService permissionService;

	/** Association definitions assembled during this request, keyed by node type and aspects. */
	private final Map<String, Map<QName, AssociationDefinition>> assocDefsByShape = new HashMap<>();

	/** Read access of the nodes checked during this request, one evaluation per node. */
	private final Map<NodeRef, Boolean> readAccessByNode = new HashMap<>();

	private Boolean requiresAssocs = null;

	/**
	 * <p>Constructor for JsonEntityVisitor.</p>
	 *
	 * @param remoteServiceRegisty a {@link fr.becpg.repo.entity.remote.RemoteServiceRegisty} object
	 */
	public JsonEntityVisitor(RemoteServiceRegisty remoteServiceRegisty) {
		super(remoteServiceRegisty);

		this.attributeExtractor = remoteServiceRegisty.attributeExtractor();
		this.versionService = remoteServiceRegisty.versionService();
		this.lockService = remoteServiceRegisty.lockService();
		this.associationService = remoteServiceRegisty.associationService();
		this.entityListDAO = remoteServiceRegisty.entityListDAO();
		this.permissionService = remoteServiceRegisty.permissionService();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {
		JSONObject root = new JSONObject();
		JSONObject entity = new JSONObject();
		root.put(RemoteEntityService.ELEM_ENTITY, entity);

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);
			visitNode(entityNodeRef, entity, JsonVisitNodeType.ENTITY, context);
			visitLists(entityNodeRef, entity, context);
			root.write(out);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void visit(PagingResults<NodeRef> pagingResult, OutputStream result) throws JSONException, IOException {
		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			JSONObject ret = new JSONObject();
			ret.put(KEY_ENTITIES, buildEntitiesArray(pagingResult));
			ret.put(KEY_PAGINATION, buildPaginationObject(pagingResult));
			ret.write(out);
		}
	}

	private JSONObject buildPaginationObject(PagingResults<NodeRef> pagingResult) throws JSONException {
		JSONObject pagination = new JSONObject();
		pagination.put(PAGINATION_HAS_MORE_ITEMS, pagingResult.hasMoreItems());
		pagination.put(PAGINATION_COUNT, pagingResult.getPage().size());
		pagination.put(PAGINATION_TOTAL_ITEMS, pagingResult.getTotalResultCount().getFirst());
		return pagination;
	}

	private JSONArray buildEntitiesArray(PagingResults<NodeRef> pagingResult) throws JSONException {
		JSONArray jsonEntities = new JSONArray();
		for (NodeRef nodeRef : pagingResult.getPage()) {
			JSONObject root = new JSONObject();
			JSONObject entity = new JSONObject();
			root.put(RemoteEntityService.ELEM_ENTITY, entity);
			RemoteJSONContext context = new RemoteJSONContext(nodeRef);
			visitNode(nodeRef, entity, JsonVisitNodeType.ENTITY_LIST, context);
			jsonEntities.put(root);
		}
		return jsonEntities;
	}

	/** {@inheritDoc} */
	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {
		JSONObject data = new JSONObject();
		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);
			visitNode(entityNodeRef, data, JsonVisitNodeType.CONTENT, context);
			data.write(out);
		}
	}

	/**
	 * <p>visitNode.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @param type a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext.JsonVisitNodeType} object
	 * @param context a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext} object
	 * @throws org.json.JSONException if any.
	 */
	protected void visitNode(NodeRef entityNodeRef, JSONObject entity, JsonVisitNodeType type, RemoteJSONContext context) throws JSONException {
		try {
			visitNode(entityNodeRef, entity, type, null, context);
		} catch (RemoteException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Error while visiting nodeRef " + entityNodeRef + ": " + e.getMessage());
			}
		}
	}

	/**
	 * <p>visitNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @param type a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext.JsonVisitNodeType} object
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object
	 * @param context a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext} object
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	protected void visitNode(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (tryVisitPreStoredJson(nodeRef, entity, type, assocName, context)) {
			return;
		}

		if (tryVisitCachedNode(nodeRef, entity)) {
			return;
		}

		try {
			cacheList.add(nodeRef);
			QName nodeType = unsecuredNodeService.getType(nodeRef).getPrefixedQName(namespaceService);

			processParentInformation(nodeRef, entity, type, nodeType, context);

			if (JsonVisitNodeType.ENTITY_LIST.equals(type) && params.isAppendParent()) {
				visitListingRowParent(nodeRef, entity);
			}
			entity.put(RemoteEntityService.ATTR_TYPE, entityDictionaryService.toPrefixString(nodeType));

			Map<QName, Serializable> properties = unsecuredNodeService.getProperties(nodeRef);
			processPrimaryProperty(nodeRef, entity, nodeType, properties, context);
			processEntityMetadataAndVersion(nodeRef, entity, type, nodeType, properties, context);
			processAttributes(nodeRef, entity, type, assocName, nodeType, properties, context);
			processMetadataAndContent(nodeRef, entity, type, nodeType);
		} finally {
			cacheList.remove(nodeRef);
		}
	}

	private boolean tryVisitPreStoredJson(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName, RemoteJSONContext context) throws JSONException {
		if (!unsecuredNodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)
				|| !BeCPGModel.EntityFormat.JSON.toString().equals(String.valueOf(unsecuredNodeService.getProperty(nodeRef, BeCPGModel.PROP_ENTITY_FORMAT)))) {
			return false;
		}

		ContentReader reader = contentService.getReader(nodeRef, BeCPGModel.PROP_ENTITY_DATA);
		if (reader != null) {
			String jsonString = reader.getContentString();
			if (jsonString != null && !jsonString.isEmpty()) {
				try {
					JSONObject root = new JSONObject(jsonString);
					if (root.has(RemoteEntityService.ELEM_ENTITY)) {
						JSONObject archivedEntity = root.getJSONObject(RemoteEntityService.ELEM_ENTITY);
						QName nodeType = unsecuredNodeService.getType(nodeRef).getPrefixedQName(namespaceService);

						if (archivedEntity.has(RemoteEntityService.ATTR_TYPE)) {
							entity.put(RemoteEntityService.ATTR_TYPE, archivedEntity.get(RemoteEntityService.ATTR_TYPE));
						}

						if (nodeRef != null && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))) {
							entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
						}

						QName primaryPropName = RemoteHelper.getPropName(nodeType, entityDictionaryService);
						String primaryPropKey = entityDictionaryService.toPrefixString(primaryPropName);
						if (archivedEntity.has(primaryPropKey)) {
							entity.put(primaryPropKey, archivedEntity.get(primaryPropKey));
						}

						String codeKey = BeCPGModel.PROP_CODE.toPrefixString(namespaceService);
						if (archivedEntity.has(codeKey) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CODE, Boolean.TRUE))) {
							entity.put(codeKey, archivedEntity.get(codeKey));
						}

						String erpCodeKey = BeCPGModel.PROP_ERP_CODE.toPrefixString(namespaceService);
						if (archivedEntity.has(erpCodeKey) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_ERP_CODE, Boolean.TRUE))) {
							entity.put(erpCodeKey, archivedEntity.get(erpCodeKey));
						}

						if (!JsonVisitNodeType.CHILD_ASSOC.equals(type) && archivedEntity.has(RemoteEntityService.ATTR_VERSION)) {
							entity.put(RemoteEntityService.ATTR_VERSION, archivedEntity.get(RemoteEntityService.ATTR_VERSION));
						}

						if (shouldProcessParent(type, nodeType)) {
							if (archivedEntity.has(RemoteEntityService.ATTR_PATH)) {
								entity.put(RemoteEntityService.ATTR_PATH, archivedEntity.get(RemoteEntityService.ATTR_PATH));
							}
							if (archivedEntity.has(RemoteEntityService.ATTR_PARENT_ID)) {
								entity.put(RemoteEntityService.ATTR_PARENT_ID, archivedEntity.get(RemoteEntityService.ATTR_PARENT_ID));
							}
							if (archivedEntity.has(RemoteEntityService.ATTR_SITE)) {
								entity.put(RemoteEntityService.ATTR_SITE, archivedEntity.get(RemoteEntityService.ATTR_SITE));
							}
						}

						if (isAll() && archivedEntity.has(KEY_METADATA)) {
							entity.put(KEY_METADATA, archivedEntity.get(KEY_METADATA));
						}

						if ((JsonVisitNodeType.CONTENT.equals(type) || (ContentModel.TYPE_CONTENT.equals(nodeType)
								&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CONTENT, Boolean.FALSE))))
								&& archivedEntity.has(RemoteEntityService.ELEM_CONTENT)) {
							entity.put(RemoteEntityService.ELEM_CONTENT, archivedEntity.get(RemoteEntityService.ELEM_CONTENT));
						}

						if (shouldExtractAttributes(type, assocName, nodeType) && archivedEntity.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
							JSONObject archivedAttributes = archivedEntity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES);
							JSONObject filteredAttributes = filterArchivedAttributes(archivedAttributes, assocName);
							entity.put(RemoteEntityService.ELEM_ATTRIBUTES, filteredAttributes);
						}

						cacheList.add(nodeRef);
						return true;
					}
				} catch (JSONException e) {
					logger.error("Error parsing pre-stored JSON for node: " + nodeRef, e);
				}
			}
		}
		return false;
	}

	private boolean tryVisitCachedNode(NodeRef nodeRef, JSONObject entity) throws JSONException {
		if (!cacheList.contains(nodeRef)) {
			return false;
		}
		if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))) {
			entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
		}
		QName nodeType = unsecuredNodeService.getType(nodeRef).getPrefixedQName(namespaceService);
		entity.put(RemoteEntityService.ATTR_TYPE, entityDictionaryService.toPrefixString(nodeType));
		return true;
	}

	private void processParentInformation(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName nodeType, RemoteJSONContext context) {
		if (!shouldProcessParent(type, nodeType)) {
			return;
		}

		NodeRef parentRef = getPrimaryParentRefQuietly(nodeRef);
		if (parentRef != null) {
			populateParentDetails(parentRef, entity, type, context);
		} else if (logger.isWarnEnabled()) {
			logger.warn("Node : " + nodeRef + " has no primary parent");
		}
	}

	private boolean shouldProcessParent(JsonVisitNodeType type, QName nodeType) {
		return JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.CONTENT.equals(type) || JsonVisitNodeType.ASSOC.equals(type)
				|| (JsonVisitNodeType.CHILD_ASSOC.equals(type) && !ContentModel.TYPE_FOLDER.equals(nodeType));
	}

	private NodeRef getPrimaryParentRefQuietly(NodeRef nodeRef) {
		try {
			return getPrimaryParentRef(nodeRef);
		} catch (RemoteException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to resolve primary parent for node " + nodeRef + ": " + e.getMessage());
			}
			return null;
		}
	}

	private void populateParentDetails(NodeRef parentRef, JSONObject entity, JsonVisitNodeType type, RemoteJSONContext context) {
		try {
			Path parentPath = unsecuredNodeService.getPath(parentRef);
			String path = parentPath.toPrefixString(namespaceService);

			entity.put(RemoteEntityService.ATTR_PATH, path.replace(context.getEntityPath(unsecuredNodeService, namespaceService), PATH_SEPARATOR_REPLACEMENT));
			if (!JsonVisitNodeType.ASSOC.equals(type)) {
				visitSite(entity, parentPath);
				entity.put(RemoteEntityService.ATTR_PARENT_ID, parentRef.getId());
			}
		} catch (RuntimeException e) {
			logParentPathFailure(parentRef, e);
		}
	}

	/**
	 * A parent whose path the caller may not read is the normal case, not an incident.
	 * <p>
	 * An entity is routinely visible to a user who cannot read the folder it sits in — that is
	 * exactly what a supplier account looks like on the portal — so
	 * {@link org.alfresco.repo.security.permissions.AccessDeniedException} here says "this parent
	 * is out of scope", the same thing {@link #createTargetAssociationNode} already treats as
	 * DEBUG. Logging it at WARN produced one line per row on every listing call and buried the
	 * failures that do deserve attention. Anything else is still a real surprise: it keeps WARN.
	 */
	private void logParentPathFailure(NodeRef parentRef, RuntimeException e) {
		if (e instanceof AccessDeniedException) {
			if (logger.isDebugEnabled()) {
				logger.debug("Parent node " + parentRef + " is not readable by the current user, path omitted: "
						+ e.getMessage());
			}
		} else if (logger.isWarnEnabled()) {
			logger.warn("Failed to resolve path for parent node " + parentRef + ": " + e.getMessage());
		}
	}

	private void processPrimaryProperty(NodeRef nodeRef, JSONObject entity, QName nodeType, Map<QName, Serializable> properties, RemoteJSONContext context)
			throws JSONException, RemoteException {
		QName propName = RemoteHelper.getPropName(nodeType, entityDictionaryService);
		PropertyDefinition namePropertyDefinition = entityDictionaryService.getProperty(propName);

		if (namePropertyDefinition != null && DataTypeDefinition.MLTEXT.equals(namePropertyDefinition.getDataType().getName())
				&& (mlNodeService.getProperty(nodeRef, namePropertyDefinition.getName()) instanceof MLText mlValues)
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT, Boolean.TRUE))) {
			visitMltextAttributes(entityDictionaryService.toPrefixString(propName), entity, mlValues);
		}
		visitPropValue(propName, entity, properties.get(propName), context);
	}

	private void processEntityMetadataAndVersion(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName nodeType, Map<QName, Serializable> properties, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (JsonVisitNodeType.CHILD_ASSOC.equals(type)) {
			if (nodeRef != null && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))
					&& !ContentModel.TYPE_FOLDER.equals(nodeType)) {
				entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
			}
			return;
		}

		extractStandardCodeProperties(entity, properties, context);
		extractVersionLabel(entity, properties);
		extractLockedPreviousVersion(nodeRef, entity);
		extractNodeId(nodeRef, entity, context);
	}

	private void extractStandardCodeProperties(JSONObject entity, Map<QName, Serializable> properties, RemoteJSONContext context) throws JSONException, RemoteException {
		if (properties.get(BeCPGModel.PROP_CODE) != null
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CODE, Boolean.TRUE))) {
			visitPropValue(BeCPGModel.PROP_CODE, entity, properties.get(BeCPGModel.PROP_CODE), context);
		}
		if (properties.get(BeCPGModel.PROP_ERP_CODE) != null
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_ERP_CODE, Boolean.TRUE))) {
			visitPropValue(BeCPGModel.PROP_ERP_CODE, entity, properties.get(BeCPGModel.PROP_ERP_CODE), context);
		}
	}

	private void extractVersionLabel(JSONObject entity, Map<QName, Serializable> properties) throws JSONException {
		if (properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL) instanceof String manualVersion && !manualVersion.isBlank()) {
			entity.put(RemoteEntityService.ATTR_VERSION, manualVersion);
		} else if (properties.get(BeCPGModel.PROP_VERSION_LABEL) instanceof String versionLabel && !versionLabel.isBlank()) {
			entity.put(RemoteEntityService.ATTR_VERSION, versionLabel);
		} else if (properties.get(ContentModel.PROP_VERSION_LABEL) instanceof String contentVersion && !contentVersion.isBlank()) {
			entity.put(RemoteEntityService.ATTR_VERSION, contentVersion);
		}
	}

	private void extractLockedPreviousVersion(NodeRef nodeRef, JSONObject entity) {
		if (!Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_IS_INITIAL_VERSION, Boolean.FALSE)) || !lockService.isLocked(nodeRef)) {
			return;
		}

		String lockInfo = lockService.getAdditionalInfo(nodeRef);
		if (lockInfo == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Node " + nodeRef + " is locked and has no additional informations");
			}
			return;
		}

		try {
			JSONObject jsonInfo = new JSONObject(lockInfo);
			if (jsonInfo.has(EntityVersionService.LOCK_TYPE_PARAM) && EntityVersionService.LOCK_TYPE_VERSIONING.equals(jsonInfo.get(EntityVersionService.LOCK_TYPE_PARAM))) {
				String currentVersion = (String) entity.opt(RemoteEntityService.ATTR_VERSION);
				if (currentVersion != null) {
					findPreviousVersionLabel(nodeRef, currentVersion)
							.ifPresent(version -> entity.put(RemoteEntityService.ATTR_VERSION, version));
				}
			}
		} catch (JSONException e) {
			if (logger.isInfoEnabled()) {
				logger.info("lock additional information cannot be parsed");
			}
		}
	}

	private Optional<String> findPreviousVersionLabel(NodeRef nodeRef, String currentVersion) {
		VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
		if (versionHistory == null) {
			return Optional.empty();
		}
		Collection<Version> nodeRefVersions = versionHistory.getAllVersions();
		return nodeRefVersions.stream().map(Version::getVersionLabel)
				.filter(label -> !label.equals(currentVersion))
				.map(this::parseDoubleQuietly)
				.flatMap(Optional::stream)
				.max(Comparator.naturalOrder())
				.map(Object::toString);
	}

	private Optional<Double> parseDoubleQuietly(String value) {
		try {
			return Optional.of(Double.parseDouble(value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	private void extractNodeId(NodeRef nodeRef, JSONObject entity, RemoteJSONContext context) throws JSONException {
		if (nodeRef == null || !Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))) {
			return;
		}
		entity.put(RemoteEntityService.ATTR_ID, resolveExportedNodeId(nodeRef, context));
	}

	/**
	 * The id under which a node is exported: its own, unless the caller asked for the nodeRefs of
	 * the entity to be renumbered, or for a node of the history space to be reported under the id
	 * it had before being versioned.
	 * <p>
	 * Both options are off by default, and both are the only reason to know where the node sits.
	 * The path is therefore resolved only when one of them is on — reading it for every node of
	 * every row was one repository read per row spent on an answer nobody asked for.
	 *
	 * @param nodeRef the node being exported
	 * @param context the context of the current visit
	 * @return the id to write, never null
	 */
	private String resolveExportedNodeId(NodeRef nodeRef, RemoteJSONContext context) {
		boolean updateEntityNodeRefs = Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_UPDATE_ENTITY_NODEREFS, Boolean.FALSE));
		boolean replaceHistoryNodeRefs = Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_REPLACE_HISTORY_NODEREFS, Boolean.FALSE));

		if (!updateEntityNodeRefs && !replaceHistoryNodeRefs) {
			return nodeRef.getId();
		}

		String nodePath = resolveNodePathQuietly(nodeRef);
		if (nodePath == null) {
			return nodeRef.getId();
		}

		if (updateEntityNodeRefs && nodePath.contains(context.getEntityPath(unsecuredNodeService, namespaceService))) {
			NodeRef currentNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeRef.getId());
			return context.getCache()
					.computeIfAbsent(currentNode, k -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate())).getId();
		}

		if (replaceHistoryNodeRefs && nodePath.contains(RepoConsts.ENTITIES_HISTORY_XPATH)) {
			return resolveHistoryOriginalNodeId(nodeRef);
		}

		return nodeRef.getId();
	}

	/**
	 * The id a node of the history space had before being versioned, which its primary parent
	 * carries as its name.
	 *
	 * @param nodeRef the history node
	 * @return the original id, or the node's own id when the parent cannot be read
	 */
	private String resolveHistoryOriginalNodeId(NodeRef nodeRef) {
		NodeRef parentNode = getPrimaryParentRefQuietly(nodeRef);
		if (parentNode != null && unsecuredNodeService.getProperty(parentNode, ContentModel.PROP_NAME) instanceof String parentName) {
			return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName).getId();
		}
		return nodeRef.getId();
	}

	private String resolveNodePathQuietly(NodeRef nodeRef) {
		try {
			return unsecuredNodeService.getPath(nodeRef).toPrefixString(namespaceService);
		} catch (RuntimeException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to resolve path for node " + nodeRef + ": " + e.getMessage());
			}
			return null;
		}
	}

	private boolean requiresAssociations() {
		if (requiresAssocs == null) {
			requiresAssocs = params == null
					|| params.requiresAssociations(qname -> entityDictionaryService.getAssociation(qname) != null);
		}
		return requiresAssocs.booleanValue();
	}

	private void processAttributes(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName, QName nodeType, Map<QName, Serializable> properties, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (!shouldExtractAttributes(type, assocName, nodeType)) {
			return;
		}

		JSONObject attributes = new JSONObject();
		if (requiresAssociations()) {
			visitAssocs(nodeRef, attributes, assocName, context);
		}
		visitProps(nodeRef, attributes, assocName, properties, context);

		if (attributes.length() > 0) {
			entity.put(RemoteEntityService.ELEM_ATTRIBUTES, attributes);
		}
	}

	private boolean shouldExtractAttributes(JsonVisitNodeType type, QName assocName, QName nodeType) {
		return JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.DATALIST.equals(type)
				|| ((JsonVisitNodeType.ENTITY_LIST.equals(type) || JsonVisitNodeType.CONTENT.equals(type))
						&& params.getFilteredProperties() != null && !params.getFilteredProperties().isEmpty())
				|| (nodeType != null && params.getFilteredAssocProperties().containsKey(nodeType))
				|| (assocName != null && params.getFilteredAssocProperties().containsKey(assocName))
				|| JsonVisitNodeType.CHILD_ASSOC.equals(type);
	}

	private void processMetadataAndContent(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName nodeType) throws JSONException {
		if (isAll() && attributeExtractor != null) {
			entity.put(KEY_METADATA, attributeExtractor.extractMetadata(nodeType, nodeRef));
		}

		if (JsonVisitNodeType.CONTENT.equals(type) || (ContentModel.TYPE_CONTENT.equals(nodeType)
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CONTENT, Boolean.FALSE)))) {
			visitContent(nodeRef, entity);
		}
	}

	/**
	 * Appends the parentage of a listing row (<code>ENTITY_LIST</code>), so that a consumer of
	 * <code>becpg/remote/entity/list</code> can attribute a row to its container without an extra
	 * query. Emitted only when <code>appendParent=true</code> is requested, see
	 * {@link fr.becpg.repo.entity.remote.RemoteParams#PARAM_APPEND_PARENT} : by default nothing is
	 * written and the listing output is left untouched.
	 * <p>
	 * Three attributes are added:
	 * <ul>
	 * <li><code>parent</code> : nodeRef id of the primary parent (for a datalist item, the datalist
	 * itself),</li>
	 * <li><code>path</code> : prefixed path of that primary parent, absolute (a listing row is its
	 * own context entity, so there is no entity path to make it relative to),</li>
	 * <li><code>entityId</code> : nodeRef id of the entity owning the datalist, only for a
	 * <code>bcpg:entityListItem</code> — e.g. the <code>pjt:project</code> of a
	 * <code>pjt:taskList</code> row.</li>
	 * </ul>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	private void visitListingRowParent(NodeRef nodeRef, JSONObject entity) throws JSONException, RemoteException {
		NodeRef parentRef = getPrimaryParentRef(nodeRef);
		if (parentRef == null) {
			logger.warn("Node : " + nodeRef + " has no primary parent");
			return;
		}

		entity.put(RemoteEntityService.ATTR_PARENT_ID, parentRef.getId());

		try {
			entity.put(RemoteEntityService.ATTR_PATH, unsecuredNodeService.getPath(parentRef).toPrefixString(namespaceService));
		} catch (RuntimeException e) {
			logParentPathFailure(parentRef, e);
		}

		if ((entityListDAO != null)
				&& entityDictionaryService.isSubClass(unsecuredNodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
			if (entityNodeRef != null) {
				entity.put(RemoteEntityService.ATTR_ENTITY_ID, entityNodeRef.getId());
			}
		}
	}

	/**
	 * <p>visitLists.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @param context a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext} object
	 * @throws org.json.JSONException if any.
	 */
	protected void visitLists(NodeRef nodeRef, JSONObject entity, RemoteJSONContext context) throws JSONException {
		if (unsecuredNodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_FORMAT)
				&& BeCPGModel.EntityFormat.JSON.toString().equals(String.valueOf(unsecuredNodeService.getProperty(nodeRef, BeCPGModel.PROP_ENTITY_FORMAT)))) {
			visitArchivedLists(nodeRef, entity);
			return;
		}

		NodeRef listContainerNodeRef = unsecuredNodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);

		JSONObject entityLists = new JSONObject();
		entity.put(RemoteEntityService.ELEM_DATALISTS, entityLists);

		if (listContainerNodeRef != null) {
			visitDataListContainer(nodeRef, listContainerNodeRef, entityLists, context);
		}
	}

	private void visitDataListContainer(NodeRef entityNodeRef, NodeRef listContainerNodeRef, JSONObject entityLists, RemoteJSONContext context) {
		List<ChildAssociationRef> assocRefs = unsecuredNodeService.getChildAssocs(listContainerNodeRef);
		for (ChildAssociationRef assocRef : assocRefs) {
			processDataListNode(entityNodeRef, assocRef.getChildRef(), entityLists, context);
		}
	}

	private void processDataListNode(NodeRef entityNodeRef, NodeRef listNodeRef, JSONObject entityLists, RemoteJSONContext context) {
		// A list the caller may not read is the list being out of scope, not an export failure.
		// `bcpg:reqCtrlList` is the everyday case: it is internal, a supplier never has rights on it,
		// and the entity is returned without it exactly as intended. The ACL that says so is set by
		// SecurityFormulationHandler on the list node itself, one per list, and its items inherit it
		// — so one question here answers for the whole list.
		if (!canRead(listNodeRef)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Datalist " + listNodeRef + " of node " + entityNodeRef
						+ " is not readable by the current user, returning entity without it");
			}
			return;
		}

		String dataListType = (String) unsecuredNodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
		if (dataListType == null || dataListType.isEmpty()) {
			return;
		}

		QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
		String dataListName = (String) unsecuredNodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);
		if (dataListName == null || dataListName.startsWith(RepoConsts.WUSED_PREFIX) || dataListName.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)) {
			return;
		}

		if (BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName) || entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
			Map<QName, List<NodeRef>> listItemsByType = entityListDAO.getListItemsByType(listNodeRef);
			for (Map.Entry<QName, List<NodeRef>> entry : listItemsByType.entrySet()) {
				processDataListItemsByType(entityNodeRef, entry.getKey(), entry.getValue(), dataListType, dataListTypeQName, dataListName, entityLists, context);
			}
		} else if (logger.isWarnEnabled()) {
			logger.warn("Existing " + dataListName + " (" + dataListTypeQName + ") list doesn't inheritate from 'bcpg:entityListItem'.");
		}
	}

	private void processDataListItemsByType(NodeRef entityNodeRef, QName listItemType, List<NodeRef> listItems, String dataListType, QName dataListTypeQName, String dataListName, JSONObject entityLists, RemoteJSONContext context) {
		String listName = dataListName;
		String listType = dataListType;
		boolean shouldExtract = true;

		if (!listItemType.equals(dataListTypeQName)) {
			shouldExtract = Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NESTED_DATALIST_TYPE, Boolean.TRUE));
			String typePrefix = listItemType.toPrefixString(namespaceService);
			listName = dataListName + DATALIST_TYPE_SEPARATOR + typePrefix;
			listType = dataListType + DATALIST_TYPE_SEPARATOR + typePrefix;
		}

		shouldExtract = shouldExtract && params.shouldExtractList(listName);

		if (shouldExtract && listItems != null && !listItems.isEmpty()) {
			extractSingleDataList(entityNodeRef, listItems, listName, listType, dataListName, entityLists, context);
		}
	}

	private void extractSingleDataList(NodeRef entityNodeRef, List<NodeRef> listItems, String listName, String listType, String dataListName, JSONObject entityLists, RemoteJSONContext context) {
		try {
			JSONArray list = new JSONArray();
			for (NodeRef listItem : listItems) {
				JSONObject jsonAssocNode = new JSONObject();
				visitNode(listItem, jsonAssocNode, JsonVisitNodeType.DATALIST, context);
				list.put(jsonAssocNode);
			}
			String listKey = entityLists.has(listType) ? listType + DATALIST_NAME_SEPARATOR + dataListName : listType;
			entityLists.put(listKey, list);
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Skipping datalist '" + listName + "' (" + listType + ") for node " + entityNodeRef
						+ " — export failed, returning entity without it: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * <p>visitContent.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @throws org.json.JSONException if any.
	 */
	protected void visitContent(NodeRef nodeRef, JSONObject entity) throws JSONException {
		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (contentReader == null) {
			return;
		}

		StringBuilder buffer = new StringBuilder();
		try (Reader reader = new InputStreamReader(new Base64InputStream(contentReader.getContentInputStream(), true, -1, null), StandardCharsets.UTF_8)) {
			char[] buf = new char[4096];
			int n;
			while ((n = reader.read(buf)) >= 0) {
				buffer.append(buf, 0, n);
			}
			entity.put(RemoteEntityService.ELEM_CONTENT, buffer.toString());
		} catch (ContentIOException | IOException e) {
			throw new JSONException(ERR_CANNOT_SERIALIZE);
		}
	}

	/**
	 * <p>visitAssocs.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object
	 * @param context a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext} object
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	protected void visitAssocs(NodeRef nodeRef, JSONObject entity, QName assocName, RemoteJSONContext context) throws JSONException, RemoteException {
		TypeDefinition typeDef = entityDictionaryService.getType(unsecuredNodeService.getType(nodeRef));
		if (typeDef == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("No typeDef found for :" + nodeRef);
			}
			return;
		}

		Map<QName, AssociationDefinition> assocs = collectAssociations(typeDef, unsecuredNodeService.getAspects(nodeRef));
		visitChildAssociations(nodeRef, entity, assocs, context);
		visitTargetAssociations(nodeRef, entity, assocName, assocs, context);
	}

	/**
	 * The associations a node can carry: those of its type plus those of each of its
	 * aspects. The result depends on that shape only, so it is assembled once per
	 * distinct combination and shared unmodifiable between the rows of a request.
	 *
	 * @param typeDef the type definition of the node being visited
	 * @param aspects its aspects
	 * @return the association definitions, never null
	 */
	private Map<QName, AssociationDefinition> collectAssociations(TypeDefinition typeDef, Set<QName> aspects) {
		String shape = buildAssociationShapeKey(typeDef.getName(), aspects);

		Map<QName, AssociationDefinition> cached = assocDefsByShape.get(shape);
		if (cached != null) {
			return cached;
		}

		Map<QName, AssociationDefinition> assocs = new HashMap<>(typeDef.getAssociations());
		for (QName aspect : aspects) {
			if (entityDictionaryService.getAspect(aspect) != null) {
				assocs.putAll(entityDictionaryService.getAspect(aspect).getAssociations());
			} else if (logger.isWarnEnabled()) {
				logger.warn("No definition for :" + aspect);
			}
		}

		Map<QName, AssociationDefinition> shared = Collections.unmodifiableMap(assocs);
		assocDefsByShape.put(shape, shared);
		return shared;
	}

	/**
	 * Identity of an association shape. Aspects are sorted so that two nodes
	 * carrying the same set in a different order share one entry.
	 *
	 * @param type the node type
	 * @param aspects its aspects
	 * @return a stable key
	 */
	private static String buildAssociationShapeKey(QName type, Set<QName> aspects) {
		StringBuilder key = new StringBuilder(type.toString());
		List<String> sorted = new ArrayList<>(aspects.size());
		for (QName aspect : aspects) {
			sorted.add(aspect.toString());
		}
		Collections.sort(sorted);
		for (String aspect : sorted) {
			key.append('|').append(aspect);
		}
		return key.toString();
	}

	private void visitChildAssociations(NodeRef nodeRef, JSONObject entity, Map<QName, AssociationDefinition> assocs, RemoteJSONContext context) throws JSONException, RemoteException {
		List<ChildAssociationRef> assocRefs = unsecuredNodeService.getChildAssocs(nodeRef);
		for (AssociationDefinition assocDef : assocs.values()) {
			if (isChildAssociationToExport(assocDef)) {
				processChildAssociationEntry(nodeRef, entity, assocDef, assocRefs, context);
			}
		}
	}

	private boolean isChildAssociationToExport(AssociationDefinition assocDef) {
		QName name = assocDef.getName();
		return !NamespaceService.RENDITION_MODEL_1_0_URI.equals(name.getNamespaceURI())
				&& !NamespaceService.SYSTEM_MODEL_1_0_URI.equals(name.getNamespaceURI())
				&& !ContentModel.ASSOC_ORIGINAL.equals(name)
				&& !RuleModel.ASSOC_RULE_FOLDER.equals(name)
				&& !BeCPGModel.ASSOC_ENTITYLISTS.equals(name)
				&& assocDef.isChild();
	}

	private void processChildAssociationEntry(NodeRef nodeRef, JSONObject entity, AssociationDefinition assocDef, List<ChildAssociationRef> assocRefs, RemoteJSONContext context) throws JSONException, RemoteException {
		QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
		if (params.getFilteredProperties() != null && !params.getFilteredProperties().isEmpty()
				&& !params.getFilteredProperties().contains(nodeType)) {
			return;
		}

		JSONArray jsonAssocs = new JSONArray();
		if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
			entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocs);
		}

		for (ChildAssociationRef assocRef : assocRefs) {
			if (assocRef.getTypeQName().equals(assocDef.getName())) {
				JSONObject jsonAssocNode = new JSONObject();
				if (assocDef.isTargetMany()) {
					jsonAssocs.put(jsonAssocNode);
				} else {
					entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocNode);
				}
				visitNode(assocRef.getChildRef(), jsonAssocNode, JsonVisitNodeType.CHILD_ASSOC, context);
			}
		}
	}

	private void visitTargetAssociations(NodeRef nodeRef, JSONObject entity, QName assocName, Map<QName, AssociationDefinition> assocs, RemoteJSONContext context) throws JSONException, RemoteException {
		for (AssociationDefinition assocDef : assocs.values()) {
			if (isTargetAssociationToExport(assocDef)) {
				processTargetAssociationEntry(nodeRef, entity, assocName, assocDef, context);
			}
		}
	}

	private boolean isTargetAssociationToExport(AssociationDefinition assocDef) {
		QName name = assocDef.getName();
		return !NamespaceService.RENDITION_MODEL_1_0_URI.equals(name.getNamespaceURI())
				&& !NamespaceService.SYSTEM_MODEL_1_0_URI.equals(name.getNamespaceURI())
				&& !RuleModel.ASSOC_RULE_FOLDER.equals(name)
				&& !ContentModel.ASSOC_ORIGINAL.equals(name)
				&& !assocDef.isChild()
				&& params.shouldExtractField(name);
	}

	private void processTargetAssociationEntry(NodeRef nodeRef, JSONObject entity, QName assocName, AssociationDefinition assocDef, RemoteJSONContext context) throws JSONException, RemoteException {
		if ((params != null) && params.canSkipProperty(assocName, assocDef.getName())) {
			return;
		}

		QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
		if (!matchProp(assocName, nodeType, false)) {
			return;
		}

		JSONArray jsonAssocs = new JSONArray();
		List<NodeRef> assocRefs = associationService.getTargetAssocs(nodeRef, assocDef.getName());
		if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
			entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocs);
		}

		for (NodeRef childRef : assocRefs) {
			JSONObject jsonAssocNode = createTargetAssociationNode(nodeRef, childRef, assocDef, nodeType, context);
			if (assocDef.isTargetMany()) {
				jsonAssocs.put(jsonAssocNode);
			} else {
				entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocNode);
			}
		}
	}

	/**
	 * Serializes an association target, or reports it as <code>#AccessDenied</code> when the caller
	 * may not read it.
	 * <p>
	 * A target is reached by traversal, so nothing has filtered it: it is the one place where this
	 * visitor asks the question itself, rather than letting a permission chain ask it again on
	 * every metadata read.
	 *
	 * @param nodeRef the node holding the association
	 * @param childRef the target
	 * @param assocDef the association definition
	 * @param nodeType the prefixed association name
	 * @param context the context of the current visit
	 * @return the serialized target, never null
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	private JSONObject createTargetAssociationNode(NodeRef nodeRef, NodeRef childRef, AssociationDefinition assocDef, QName nodeType, RemoteJSONContext context) throws JSONException, RemoteException {
		JSONObject jsonAssocNode = new JSONObject();

		if (!canRead(childRef)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Association target " + childRef + " of " + nodeRef + " (assoc " + assocDef.getName()
						+ ") not accessible for current user, marking as #AccessDenied");
			}
			jsonAssocNode.put(RemoteEntityService.ATTR_ID, childRef.getId());
			jsonAssocNode.put(entityDictionaryService.toPrefixString(ContentModel.PROP_NAME), ACCESS_DENIED_VALUE);
			return jsonAssocNode;
		}

		visitNode(childRef, jsonAssocNode, JsonVisitNodeType.ASSOC, nodeType, context);
		return jsonAssocNode;
	}

	/**
	 * Whether the caller may read a node, answered once per node and per request.
	 * <p>
	 * This is the whole of the access control this visitor performs, and it is asked in the two
	 * places nothing else filters: an association target, and a datalist of the entity. Everywhere
	 * else the metadata is read without a check, because the page came from a search that had
	 * already granted it.
	 * <p>
	 * The same node comes back over and over — <code>bcpg:entityTpl</code> is one node for every
	 * product of a listing — and each visit would otherwise be one permission evaluation. The cache
	 * is what keeps this check from becoming the cost that reading through the public
	 * <code>NodeService</code> was.
	 *
	 * @param nodeRef the node to test
	 * @return true when the node is readable
	 */
	private boolean canRead(NodeRef nodeRef) {
		return readAccessByNode.computeIfAbsent(nodeRef,
				ref -> AccessStatus.ALLOWED.equals(permissionService.hasPermission(ref, PermissionService.READ)));
	}

	/**
	 * <p>matchProp.</p>
	 *
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object
	 * @param propName a {@link org.alfresco.service.namespace.QName} object
	 * @param checkFilter a boolean
	 * @return a boolean
	 */
	protected boolean matchProp(QName assocName, QName propName, boolean checkFilter) {
		if (assocName != null && params.getFilteredAssocProperties() != null && !params.getFilteredAssocProperties().isEmpty()
				&& params.getFilteredAssocProperties().containsKey(assocName)) {
			return params.getFilteredAssocProperties().get(assocName).contains(propName);
		}
		if (params.getFilteredProperties() != null && !params.getFilteredProperties().isEmpty()) {
			return params.getFilteredProperties().contains(propName);
		}
		return !checkFilter;
	}

	/**
	 * <p>visitProps.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link org.json.JSONObject} object
	 * @param assocName a {@link org.alfresco.service.namespace.QName} object
	 * @param props a {@link java.util.Map} object
	 * @param context a {@link fr.becpg.repo.entity.remote.extractor.RemoteJSONContext} object
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	protected void visitProps(NodeRef nodeRef, JSONObject entity, QName assocName, Map<QName, Serializable> props, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (props == null) {
			return;
		}

		for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
			processSingleProperty(nodeRef, entity, assocName, entry.getKey(), entry.getValue(), context);
		}
	}

	private void processSingleProperty(NodeRef nodeRef, JSONObject entity, QName assocName, QName propQName, Serializable propValue, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (propValue == null) {
			return;
		}

		if ((params != null) && params.canSkipProperty(assocName, propQName)) {
			return;
		}

		QName propName = propQName.getPrefixedQName(namespaceService);
		if (!shouldExportProperty(propQName, propName, assocName)) {
			return;
		}

		PropertyDefinition propertyDefinition = entityDictionaryService.getProperty(propQName);
		if (propertyDefinition == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Properties not in dictionary: " + propQName);
			}
			return;
		}

		if (!matchProp(assocName, propName, false)) {
			return;
		}

		processPropertyMlText(nodeRef, entity, propName, propValue, propertyDefinition);
		visitPropValue(propName, entity, propValue, context);
	}

	private boolean shouldExportProperty(QName propQName, QName propName, QName assocName) {
		String nsUri = propQName.getNamespaceURI();
		if (NamespaceService.SYSTEM_MODEL_1_0_URI.equals(nsUri) || NamespaceService.RENDITION_MODEL_1_0_URI.equals(nsUri)) {
			return false;
		}
		if (ContentModel.PROP_CONTENT.equals(propQName) || !params.shouldExtractField(propQName)) {
			return false;
		}
		if (ReportModel.REPORT_URI.equals(nsUri)) {
			return matchProp(assocName, propName, true)
					|| Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_REPORT_PROPS, Boolean.FALSE));
		}
		return true;
	}

	private void processPropertyMlText(NodeRef nodeRef, JSONObject entity, QName propName, Serializable propValue, PropertyDefinition propertyDefinition) throws JSONException {
		QName dataTypeName = propertyDefinition.getDataType().getName();
		String prefixPropName = entityDictionaryService.toPrefixString(propName);

		if (DataTypeDefinition.MLTEXT.equals(dataTypeName)
				&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText mlValues)
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT, Boolean.TRUE))) {
			visitMltextAttributes(prefixPropName, entity, mlValues);
		} else if (DataTypeDefinition.TEXT.equals(dataTypeName)
				&& !propertyDefinition.getConstraints().isEmpty() && !propertyDefinition.isMultiValued()
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, Boolean.TRUE))) {
			extractDynListConstraintMlText(entity, prefixPropName, (String) propValue, propertyDefinition);
		}
	}

	private void extractDynListConstraintMlText(JSONObject entity, String prefixPropName, String propValue, PropertyDefinition propertyDefinition) throws JSONException {
		for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
			if (constraint.getConstraint() instanceof DynListConstraint dynListConstraint) {
				MLText mlValues = dynListConstraint.getMLDisplayLabel(propValue);
				visitMltextAttributes(prefixPropName, entity, mlValues);
				break;
			}
		}
	}

	private void visitArchivedLists(NodeRef nodeRef, JSONObject entity) throws JSONException {
		ContentReader reader = contentService.getReader(nodeRef, BeCPGModel.PROP_ENTITY_DATA);
		if (reader == null) {
			return;
		}
		String jsonString = reader.getContentString();
		if (jsonString == null || jsonString.isEmpty()) {
			return;
		}
		try {
			JSONObject root = new JSONObject(jsonString);
			if (root.has(RemoteEntityService.ELEM_ENTITY)) {
				JSONObject archivedEntity = root.getJSONObject(RemoteEntityService.ELEM_ENTITY);
				if (archivedEntity.has(RemoteEntityService.ELEM_DATALISTS)) {
					JSONObject archivedLists = archivedEntity.getJSONObject(RemoteEntityService.ELEM_DATALISTS);
					JSONObject filteredLists = new JSONObject();
					Iterator<String> keys = archivedLists.keys();
					while (keys.hasNext()) {
						String key = keys.next();
						String listName = extractArchivedListName(key);
						if (params.shouldExtractList(listName)) {
							Object listVal = archivedLists.get(key);
							QName listTypeQName = getQNameQuietly(key);
							Object filteredListVal = filterSubJsonValue(listVal, listTypeQName);
							if (filteredListVal != null) {
								filteredLists.put(key, filteredListVal);
							}
						}
					}
					if (filteredLists.length() > 0) {
						entity.put(RemoteEntityService.ELEM_DATALISTS, filteredLists);
					}
				}
			}
		} catch (JSONException e) {
			logger.error("Error parsing pre-stored JSON datalists for node: " + nodeRef, e);
		}
	}

	private JSONObject filterArchivedAttributes(JSONObject attributes, QName assocName) throws JSONException {
		JSONObject filtered = new JSONObject();
		Iterator<String> keys = attributes.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (KEY_METADATA.equals(key)) {
				if (isAll()) {
					filtered.put(key, attributes.get(key));
				}
				continue;
			}
			if (isArchivedAttributeMatching(key, assocName)) {
				Object value = attributes.get(key);
				QName childAssocName = getQNameQuietly(key);
				Object filteredValue = filterSubJsonValue(value, childAssocName != null ? childAssocName : assocName);
				if (filteredValue != null) {
					filtered.put(key, filteredValue);
				}
			}
		}
		return filtered;
	}

	private Object filterSubJsonValue(Object value, QName assocName) throws JSONException {
		if (value instanceof JSONObject jsonObj) {
			return filterSubJsonObject(jsonObj, assocName);
		} else if (value instanceof JSONArray jsonArr) {
			JSONArray filteredArr = new JSONArray();
			for (int i = 0; i < jsonArr.length(); i++) {
				Object item = jsonArr.get(i);
				Object filteredItem = filterSubJsonValue(item, assocName);
				if (filteredItem != null) {
					filteredArr.put(filteredItem);
				}
			}
			return filteredArr;
		}
		return value;
	}

	private JSONObject filterSubJsonObject(JSONObject jsonObj, QName assocName) throws JSONException {
		JSONObject filteredObj = new JSONObject();
		Iterator<String> keys = jsonObj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object val = jsonObj.get(key);

			if (KEY_METADATA.equals(key)) {
				if (isAll()) {
					filteredObj.put(key, val);
				}
				continue;
			}

			if (RemoteEntityService.ATTR_ID.equals(key) || RemoteEntityService.ATTR_TYPE.equals(key)
					|| RemoteEntityService.ATTR_VERSION.equals(key) || RemoteEntityService.ATTR_PATH.equals(key)) {
				filteredObj.put(key, val);
				continue;
			}

			if (RemoteEntityService.ELEM_ATTRIBUTES.equals(key) && val instanceof JSONObject attrsObj) {
				JSONObject filteredAttrs = filterArchivedAttributes(attrsObj, assocName);
				filteredObj.put(key, filteredAttrs);
				continue;
			}

			if (isArchivedAttributeMatching(key, assocName)) {
				QName childAssoc = getQNameQuietly(key);
				Object filteredVal = filterSubJsonValue(val, childAssoc != null ? childAssoc : assocName);
				if (filteredVal != null) {
					filteredObj.put(key, filteredVal);
				}
			}
		}
		return filteredObj;
	}

	private QName getQNameQuietly(String key) {
		try {
			String baseKey = key;
			if (key.contains("_") && key.lastIndexOf('_') > key.indexOf(':')) {
				baseKey = key.substring(0, key.lastIndexOf('_'));
			}
			return QName.createQName(baseKey, namespaceService);
		} catch (NamespaceException e) {
			return null;
		}
	}

	private boolean isArchivedAttributeMatching(String key, QName assocName) {
		QName qname = getQNameQuietly(key);
		if (qname == null) {
			return false;
		}

		if (key.contains("_") && key.lastIndexOf('_') > key.indexOf(':')) {
			try {
				if (entityDictionaryService.getProperty(qname) != null || isQNameInFilteredParams(qname, assocName)) {
					if (!Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT, Boolean.TRUE))) {
						return false;
					}
				}
			} catch (NamespaceException e) {
				// Ignore
			}
		}

		if (!params.shouldExtractField(qname)) {
			return false;
		}

		return matchProp(assocName, qname, false);
	}

	private boolean isQNameInFilteredParams(QName qname, QName assocName) {
		if (assocName == null) {
			if (params.getFilteredProperties() != null && params.getFilteredProperties().contains(qname)) {
				return true;
			}
			return params.getFilteredAssocProperties() != null && params.getFilteredAssocProperties().containsKey(qname);
		} else {
			return params.getFilteredAssocProperties() != null && params.getFilteredAssocProperties().containsKey(assocName)
					&& params.getFilteredAssocProperties().get(assocName).contains(qname);
		}
	}

	private String extractArchivedListName(String key) {
		String[] parts = key.split("\\" + DATALIST_NAME_SEPARATOR);
		if (parts.length > 1) {
			return parts[1];
		}
		String[] qnameParts = key.split(QNAME_PREFIX_SEPARATOR);
		if (qnameParts.length > 1) {
			return qnameParts[1];
		}
		return key;
	}

	private void visitMltextAttributes(String propType, JSONObject entity, MLText mlValues) throws JSONException {
		if (mlValues == null) {
			return;
		}

		for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
			if (MLTextHelper.isSupportedLocale(mlEntry.getKey())) {
				String code = MLTextHelper.localeKey(mlEntry.getKey());
				if (code != null && !code.isBlank() && mlEntry.getValue() != null) {
					entity.put(propType + "_" + code, mlEntry.getValue());
				}
			}
		}
	}

	private void visitSite(JSONObject entity, Path path) throws JSONException {
		String siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));
		if (siteId == null) {
			return;
		}

		JSONObject sitej = new JSONObject();
		sitej.put(RemoteEntityService.ATTR_ID, siteId);

		SiteInfo site = siteService.getSite(siteId);
		if (site != null) {
			sitej.put(RemoteEntityService.ATTR_NAME, site.getTitle());
		}

		entity.put(RemoteEntityService.ATTR_SITE, sitej);
	}

	@SuppressWarnings("unchecked")
	private void visitPropValue(QName propType, JSONObject entity, Serializable value, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (value instanceof List<?> listValue) {
			visitPropValueList(propType, entity, (List<Serializable>) listValue, context);
		} else if (value instanceof NodeRef nodeRefValue) {
			visitPropValueNodeRef(propType, entity, nodeRefValue, context);
		} else if (value != null) {
			visitPropValueScalar(propType, entity, value);
		}
	}

	private void visitPropValueList(QName propType, JSONObject entity, List<Serializable> listValue, RemoteJSONContext context)
			throws JSONException, RemoteException {
		JSONArray tmp = new JSONArray();
		entity.put(entityDictionaryService.toPrefixString(propType), tmp);
		for (Serializable subEl : listValue) {
			if (subEl instanceof NodeRef subNodeRef) {
				visitPropValueListNodeRef(propType, tmp, subNodeRef, context);
			} else if (subEl != null) {
				visitPropValueListScalar(propType, tmp, subEl);
			}
		}
	}

	private void visitPropValueListNodeRef(QName propType, JSONArray tmpArray, NodeRef nodeRef, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (unsecuredNodeService.exists(nodeRef)) {
			JSONObject node = new JSONObject();
			tmpArray.put(node);
			visitNode(nodeRef, node, JsonVisitNodeType.ASSOC, context);
		} else {
			throw new RemoteException("node does not exist: " + nodeRef + ", for prop: " + propType);
		}
	}

	private void visitPropValueListScalar(QName propType, JSONArray tmpArray, Serializable value) throws JSONException {
		if (RemoteHelper.isJSONValue(propType)) {
			tmpArray.put(new JSONObject((String) value));
		} else {
			Object formatted = JsonHelper.formatValue(value);
			if (formatted != null && !formatted.toString().isEmpty()) {
				tmpArray.put(formatted);
			}
		}
	}

	private void visitPropValueNodeRef(QName propType, JSONObject entity, NodeRef nodeRef, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (unsecuredNodeService.exists(nodeRef)) {
			JSONObject node = new JSONObject();
			entity.put(entityDictionaryService.toPrefixString(propType), node);
			visitNode(nodeRef, node, JsonVisitNodeType.ASSOC, context);
		} else {
			throw new RemoteException("node does not exist: " + nodeRef + ", for prop: " + propType);
		}
	}

	private void visitPropValueScalar(QName propType, JSONObject entity, Serializable value) throws JSONException {
		if (RemoteHelper.isJSONValue(propType)) {
			entity.put(entityDictionaryService.toPrefixString(propType), new JSONObject((String) value));
		} else {
			Object formatted = JsonHelper.formatValue(value);
			if (formatted != null && !formatted.toString().isEmpty()) {
				entity.put(entityDictionaryService.toPrefixString(propType), formatted);
			}
		}
	}
}
