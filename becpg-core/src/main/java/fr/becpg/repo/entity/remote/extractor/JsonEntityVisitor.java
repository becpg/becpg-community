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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.rule.RuleModel;
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
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
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
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;

/**
 * <p>
 * JsonEntityVisitor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonEntityVisitor extends AbstractEntityVisitor {

	private AttributeExtractorService attributeExtractor;
	private VersionService versionService;
	private LockService lockService;
	private AssociationService associationService;
	private EntityListDAO entityListDAO;

	public JsonEntityVisitor(RemoteServiceRegisty remoteServiceRegisty) {
		super(remoteServiceRegisty);

		this.attributeExtractor = remoteServiceRegisty.attributeExtractor();
		this.versionService = remoteServiceRegisty.versionService();
		this.lockService = remoteServiceRegisty.lockService();
		this.associationService = remoteServiceRegisty.associationService();
		this.entityListDAO = remoteServiceRegisty.entityListDAO();
	}

	private static final Log logger = LogFactory.getLog(JsonEntityVisitor.class);

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
			JSONObject pagination = new JSONObject();
			
			pagination.put("hasMoreItems", pagingResult.hasMoreItems());
			pagination.put("count", pagingResult.getTotalResultCount().getFirst());
			
			JSONArray jsonEntities = new JSONArray();

			for (NodeRef nodeRef : pagingResult.getPage()) {
				JSONObject root = new JSONObject();
				JSONObject entity = new JSONObject();
				root.put(RemoteEntityService.ELEM_ENTITY, entity);
				RemoteJSONContext context = new RemoteJSONContext(nodeRef);

				visitNode(nodeRef, entity, JsonVisitNodeType.ENTITY_LIST, context);
				jsonEntities.put(root);
			}

			ret.put("entities", jsonEntities);
			ret.put("pagination", pagination);

			ret.write(out);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {
		JSONObject data = new JSONObject();

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);

			// Visit node
			visitNode(entityNodeRef, data, JsonVisitNodeType.CONTENT, context);
			data.write(out);
		}

	}

	protected void visitNode(NodeRef entityNodeRef, JSONObject entity, JsonVisitNodeType type, RemoteJSONContext context) throws JSONException {
		try {
			visitNode(entityNodeRef, entity, type, null, context);
		} catch (RemoteException e) {
			logger.warn("Error while visiting nodeRef " + entityNodeRef + ": " + e.getMessage());
		}
	}

	protected void visitNode(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName, RemoteJSONContext context)
			throws JSONException, RemoteException {
		cacheList.add(nodeRef);
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		if (JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.CONTENT.equals(type) || JsonVisitNodeType.ASSOC.equals(type)
				|| (JsonVisitNodeType.CHILD_ASSOC.equals(type) && !ContentModel.TYPE_FOLDER.equals(nodeType))) {

			if (nodeService.getPrimaryParent(nodeRef) != null) {
				NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				if (parentRef != null) {
					Path parentPath = nodeService.getPath(parentRef);
					String path = parentPath.toPrefixString(namespaceService);

					entity.put(RemoteEntityService.ATTR_PATH, path.replace(context.getEntityPath(nodeService, namespaceService), "~"));
					if (!JsonVisitNodeType.ASSOC.equals(type)) {
						visitSite(entity, parentPath);
						entity.put(RemoteEntityService.ATTR_PARENT_ID, parentRef.getId());
					}
				}
			} else {
				logger.warn("Node : " + nodeRef + " has no primary parent");
			}
		}

		entity.put(RemoteEntityService.ATTR_TYPE, entityDictionaryService.toPrefixString(nodeType));

		QName propName = RemoteHelper.getPropName(nodeType, entityDictionaryService);
		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

		visitPropValue(propName, entity, properties.get(propName), context);

		if (!JsonVisitNodeType.CHILD_ASSOC.equals(type)) {

			if ((properties.get(BeCPGModel.PROP_CODE) != null)
					&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CODE, Boolean.TRUE))) {
				visitPropValue(BeCPGModel.PROP_CODE, entity, properties.get(BeCPGModel.PROP_CODE), context);
			}
			// erpCode
			if ((properties.get(BeCPGModel.PROP_ERP_CODE) != null)
					&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_ERP_CODE, Boolean.TRUE))) {
				visitPropValue(BeCPGModel.PROP_ERP_CODE, entity, properties.get(BeCPGModel.PROP_ERP_CODE), context);
			}

			if (properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL) != null
					&& !((String) properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL)).isBlank()) {
				entity.put(RemoteEntityService.ATTR_VERSION, properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL));
			} else if (properties.get(BeCPGModel.PROP_VERSION_LABEL) != null && !((String) properties.get(BeCPGModel.PROP_VERSION_LABEL)).isBlank()) {
				entity.put(RemoteEntityService.ATTR_VERSION, properties.get(BeCPGModel.PROP_VERSION_LABEL));
			} else if (properties.get(ContentModel.PROP_VERSION_LABEL) != null
					&& !((String) properties.get(ContentModel.PROP_VERSION_LABEL)).isBlank()) {
				entity.put(RemoteEntityService.ATTR_VERSION, properties.get(ContentModel.PROP_VERSION_LABEL));
			}

			if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_IS_INITIAL_VERSION, Boolean.FALSE)) && lockService.isLocked(nodeRef)) {

				String lockInfo = lockService.getAdditionalInfo(nodeRef);

				try {
					JSONObject jsonInfo = new JSONObject(lockInfo);

					if (jsonInfo.has("lockType") && jsonInfo.get("lockType").equals("versioning")) {
						String currentVersion = (String) entity.get(RemoteEntityService.ATTR_VERSION);

						if (currentVersion != null) {
							Collection<Version> nodeRefVersions = versionService.getVersionHistory(nodeRef).getAllVersions();
							Optional<Double> previousVersion = nodeRefVersions.stream().map(Version::getVersionLabel)
									.filter(label -> !label.equals(currentVersion)).map(Double::parseDouble)
									.max(Comparator.comparing(Double::valueOf));
							previousVersion.ifPresent(version -> entity.put(RemoteEntityService.ATTR_VERSION, version.toString()));
						}
					}
				} catch (JSONException e) {
					logger.info("lock additional information cannot be parsed");
				}
			}

			if ((nodeRef != null) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))) {

				String nodePath = nodeService.getPath(nodeRef).toPrefixString(namespaceService);

				if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_UPDATE_ENTITY_NODEREFS, Boolean.FALSE))
						&& nodePath.contains(context.getEntityPath(nodeService, namespaceService))) {

					NodeRef currentNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeRef.getId());

					NodeRef newNode = null;

					if (context.getCache().containsKey(currentNode)) {
						newNode = context.getCache().get(currentNode);
					} else {
						newNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
						context.getCache().put(currentNode, newNode);
					}

					entity.put(RemoteEntityService.ATTR_ID, newNode.getId());
				} else {

					if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_REPLACE_HISTORY_NODEREFS, Boolean.FALSE))
							&& nodeService.getPath(nodeRef).toPrefixString(namespaceService).contains(RepoConsts.ENTITIES_HISTORY_XPATH)) {
						NodeRef parentNode = nodeService.getPrimaryParent(nodeRef).getParentRef();

						String parentName = (String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME);

						NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);

						entity.put(RemoteEntityService.ATTR_ID, originalNode.getId());
					} else {
						entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
					}
				}
			}
		} else {
			if ((nodeRef != null) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))
					&& !ContentModel.TYPE_FOLDER.equals(nodeType)) {
				entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
			}
		}

		JSONObject attributes = new JSONObject();
		if (JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.DATALIST.equals(type)
				|| ((JsonVisitNodeType.ENTITY_LIST.equals(type) || JsonVisitNodeType.CONTENT.equals(type)) && (params.getFilteredProperties() != null)
						&& !params.getFilteredProperties().isEmpty())
				|| ((nodeType != null) && params.getFilteredAssocProperties().containsKey(nodeType))
				|| ((assocName != null) && params.getFilteredAssocProperties().containsKey(assocName))
				|| JsonVisitNodeType.CHILD_ASSOC.equals(type)) {

			// Assoc first
			visitAssocs(nodeRef, attributes, assocName, context);
			visitProps(nodeRef, attributes, assocName, properties, context);

		}

		if (attributes.length() > 0) {
			entity.put(RemoteEntityService.ELEM_ATTRIBUTES, attributes);
		}

		if (isAll() && (attributeExtractor != null)) {
			entity.put("metadata", attributeExtractor.extractMetadata(nodeType, nodeRef));
		}

		if (JsonVisitNodeType.CONTENT.equals(type) || (ContentModel.TYPE_CONTENT.equals(nodeType)
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CONTENT, Boolean.FALSE)))) {
			visitContent(nodeRef, entity);
		}

	}

	protected void visitLists(NodeRef nodeRef, JSONObject entity, RemoteJSONContext context) throws JSONException {

		NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);

		JSONObject entityLists = new JSONObject();
		entity.put(RemoteEntityService.ELEM_DATALISTS, entityLists);

		if (listContainerNodeRef != null) {
			List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(listContainerNodeRef);

			for (ChildAssociationRef assocRef : assocRefs) {

				NodeRef listNodeRef = assocRef.getChildRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if ((dataListType != null) && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
					String dataListName = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);
					if (!(dataListName).startsWith(RepoConsts.WUSED_PREFIX) && !dataListName.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)) {
						if ((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
								|| entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM))) {
							
							Map<QName, List<NodeRef>> listItemsByType = entityListDAO.getListItemsByType(listNodeRef);
							
							for (Entry<QName, List<NodeRef>> entry : listItemsByType.entrySet()) {
								QName listItemType = entry.getKey();
								List<NodeRef> listItems = entry.getValue();
								String listName = dataListName;
								String listType = dataListType;
								boolean shouldExtract = true;
								
								if (!listItemType.equals(dataListTypeQName)) {
									shouldExtract = Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NESTED_DATALIST_TYPE, Boolean.TRUE));
									listName = dataListName + "@" + listItemType.toPrefixString(namespaceService);
									listType = dataListType + "@" + listItemType.toPrefixString(namespaceService);
								}
								
								shouldExtract = shouldExtract && params.shouldExtractList(listName);
								
								if (shouldExtract && (listItems != null) && !listItems.isEmpty()) {
									
									JSONArray list = new JSONArray();

									if (!entityLists.has(listType)) {
										entityLists.put(listType, list);
									} else {
										entityLists.put(listType + "|" + dataListName, list);
									}
									for (NodeRef listItem : listItems) {
										JSONObject jsonAssocNode = new JSONObject();
										list.put(jsonAssocNode);
										
										visitNode(listItem, jsonAssocNode, JsonVisitNodeType.DATALIST, context);
									}
								}
							}
							
						} else {
							logger.warn(
									"Existing " + dataListName + " (" + dataListTypeQName + ") list doesn't inheritate from 'bcpg:entityListItem'.");
						}
					}
				}

			}
		}

	}

	protected void visitContent(NodeRef nodeRef, JSONObject entity) throws JSONException {

		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (contentReader != null) {

			StringBuilder buffer = new StringBuilder();
			try (Reader reader = new InputStreamReader(new Base64InputStream(contentReader.getContentInputStream(), true, -1, null))) {

				char[] buf = new char[4096];
				int n;
				while ((n = reader.read(buf)) >= 0) {

					buffer.append(buf, 0, n);
				}

				entity.put(RemoteEntityService.ELEM_CONTENT, buffer.toString());

			} catch (ContentIOException | IOException e) {
				throw new JSONException("Cannot serialyze data");
			}

		}

	}

	protected void visitAssocs(NodeRef nodeRef, JSONObject entity, QName assocName, RemoteJSONContext context) throws JSONException, RemoteException {

		TypeDefinition typeDef = entityDictionaryService.getType(nodeService.getType(nodeRef));
		if (typeDef != null) {

			Map<QName, AssociationDefinition> assocs = new HashMap<>(typeDef.getAssociations());
			for (QName aspect : nodeService.getAspects(nodeRef)) {
				if (entityDictionaryService.getAspect(aspect) != null) {
					assocs.putAll(entityDictionaryService.getAspect(aspect).getAssociations());
				} else {
					logger.warn("No definition for :" + aspect);
				}
			}

			// First childs
			for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
				AssociationDefinition assocDef = entry.getValue();

				if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER)
						&& !assocDef.getName().equals(BeCPGModel.ASSOC_ENTITYLISTS) && assocDef.isChild()) {
					QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
					// fields & child assocs filter
					if (((params.getFilteredProperties() != null) && !params.getFilteredProperties().isEmpty()
							&& !params.getFilteredProperties().contains(nodeType))) {
						continue;
					}

					JSONArray jsonAssocs = new JSONArray();

					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocs);
					}

					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {

							NodeRef childRef = assocRef.getChildRef();
							JSONObject jsonAssocNode = new JSONObject();
							if (assocDef.isTargetMany()) {
								jsonAssocs.put(jsonAssocNode);
							} else {
								entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocNode);
							}

							visitNode(childRef, jsonAssocNode, JsonVisitNodeType.CHILD_ASSOC, context);

						}
					}
				}

			}

			for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
				AssociationDefinition assocDef = entry.getValue();

				if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER) && !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL)
						&& !assocDef.isChild() && params.shouldExtractField(assocDef.getName())

				) {

					QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);

					if (!matchProp(assocName, nodeType, false)) {
						continue;
					}

					JSONArray jsonAssocs = new JSONArray();

					List<NodeRef> assocRefs = associationService.getTargetAssocs(nodeRef, assocDef.getName());
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocs);
					}
					for (NodeRef childRef : assocRefs) {
						JSONObject jsonAssocNode = new JSONObject();
						if (assocDef.isTargetMany()) {
							jsonAssocs.put(jsonAssocNode);
						} else {
							entity.put(entityDictionaryService.toPrefixString(nodeType), jsonAssocNode);
						}

						visitNode(childRef, jsonAssocNode, JsonVisitNodeType.ASSOC, nodeType, context);

					}
				}

			}

		} else {
			logger.warn("No typeDef found for :" + nodeRef);
		}

	}

	protected boolean matchProp(QName assocName, QName propName, boolean checkFilter) {

		if (assocName == null) {
			if (params.getFilteredProperties() != null && !params.getFilteredProperties().isEmpty()) {
				return params.getFilteredProperties().contains(propName);
			} else {
				return !checkFilter;
			}

		} else {
			if ((params.getFilteredAssocProperties() != null) && !params.getFilteredAssocProperties().isEmpty()) {
				return params.getFilteredAssocProperties().containsKey(assocName)
						&& params.getFilteredAssocProperties().get(assocName).contains(propName);
			} else {
				return !checkFilter;
			}
		}

	}

	protected void visitProps(NodeRef nodeRef, JSONObject entity, QName assocName, Map<QName, Serializable> props, RemoteJSONContext context)
			throws JSONException, RemoteException {

		if (props != null) {
			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				QName propQName = entry.getKey();
				QName propName = entry.getKey().getPrefixedQName(namespaceService);
				if ((entry.getValue() != null) && !propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& (!propQName.getNamespaceURI().equals(ReportModel.REPORT_URI) || matchProp(assocName, propName, true)
								|| Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_REPORT_PROPS, Boolean.FALSE)))
						&& !propQName.equals(ContentModel.PROP_CONTENT) && params.shouldExtractField(propQName)) {
					PropertyDefinition propertyDefinition = entityDictionaryService.getProperty(entry.getKey());
					if (propertyDefinition != null) {

						// Assoc properties filter
						if (!matchProp(assocName, propName, false)) {
							continue;
						}

						MLText mlValues = null;
						if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
								&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)
								&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT, Boolean.TRUE))) {
							mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDefinition.getName());
							visitMltextAttributes(entityDictionaryService.toPrefixString(propName), entity, mlValues);
						} else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())
								&& !propertyDefinition.getConstraints().isEmpty() && !propertyDefinition.isMultiValued()
								&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, Boolean.TRUE))) {
							for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
								if (constraint.getConstraint() instanceof DynListConstraint) {
									mlValues = ((DynListConstraint) constraint.getConstraint()).getMLDisplayLabel((String) entry.getValue());
									visitMltextAttributes(entityDictionaryService.toPrefixString(propName), entity, mlValues);
									break;
								}
							}
						}

						visitPropValue(propName, entity, entry.getValue(), context);

					} else {
						logger.debug("Properties not in dictionary: " + entry.getKey());
					}

				}

			}
		}

	}

	private void visitMltextAttributes(String propType, JSONObject entity, MLText mlValues) throws JSONException {
		if (mlValues != null) {
			for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
				if (MLTextHelper.isSupportedLocale(mlEntry.getKey())) {
					String code = MLTextHelper.localeKey(mlEntry.getKey());
					if ((code != null) && !code.isBlank() && (mlEntry.getValue() != null)) {
						entity.put(propType + "_" + code, mlEntry.getValue());
					}
				}
			}
		}
	}

	private void visitSite(JSONObject entity, Path path) throws JSONException {

		String siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));

		if (siteId != null) {
			JSONObject sitej = new JSONObject();

			sitej.put(RemoteEntityService.ATTR_ID, siteId);

			SiteInfo site = siteService.getSite(siteId);

			if (site != null) {
				sitej.put(RemoteEntityService.ATTR_NAME, site.getTitle());

			}

			entity.put(RemoteEntityService.ATTR_SITE, sitej);
		}

	}

	@SuppressWarnings("unchecked")
	private void visitPropValue(QName propType, JSONObject entity, Serializable value, RemoteJSONContext context)
			throws JSONException, RemoteException {
		if (value instanceof List) {
			JSONArray tmp = new JSONArray();
			entity.put(entityDictionaryService.toPrefixString(propType), tmp);
			for (Serializable subEl : (List<Serializable>) value) {
				if (subEl instanceof NodeRef) {

					if (nodeService.exists((NodeRef) subEl)) {
						JSONObject node = new JSONObject();
						tmp.put(node);
						visitNode((NodeRef) subEl, node, JsonVisitNodeType.ASSOC, context);
					} else {
						throw new RemoteException("node does not exist: " + subEl + ", for prop: " + propType);
					}

				} else {
					if (subEl != null) {
						if (RemoteHelper.isJSONValue(propType)) {
							tmp.put(new JSONObject((String) subEl));
						} else if ((JsonHelper.formatValue(subEl) != null) && !JsonHelper.formatValue(subEl).toString().isEmpty()) {
							tmp.put(JsonHelper.formatValue(subEl));
						}
					}
				}

			}
		} else if (value instanceof NodeRef) {
			if (nodeService.exists((NodeRef) value)) {
				JSONObject node = new JSONObject();
				entity.put(entityDictionaryService.toPrefixString(propType), node);
				visitNode((NodeRef) value, node, JsonVisitNodeType.ASSOC, context);
			} else {
				throw new IllegalStateException("node does not exist: " + value + ", for prop: " + propType);
			}
		} else {

			if (value != null) {
				if (RemoteHelper.isJSONValue(propType)) {
					entity.put(entityDictionaryService.toPrefixString(propType), new JSONObject((String) value));
				} else if ((JsonHelper.formatValue(value) != null) && !JsonHelper.formatValue(value).toString().isEmpty()) {
					entity.put(entityDictionaryService.toPrefixString(propType), JsonHelper.formatValue(value));
				}
			}
		}
	}
}
