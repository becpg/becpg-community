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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.extractor.RemoteJSONContext.JsonVisitNodeType;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>
 * JsonSchemaEntityVisitor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonSchemaEntityVisitor extends JsonEntityVisitor {

	private static final Log logger = LogFactory.getLog(JsonSchemaEntityVisitor.class);

	private static final String TYPE_STRING = "string";
	private static final String TYPE_OBJECT = "object";
	private static final String TYPE_ARRAY = "array";
	private static final String TYPE_NUMBER = "number";
	private static final String TYPE_BOOLEAN = "boolean";

	private static final String PROP_TITLE = "title";
	private static final String PROP_TYPE = "type";
	private static final String PROP_DESCRIPTION = "description";
	private static final String PROP_PROPERTIES = "properties";
	private static final String PROP_ITEMS = "items";

	SysAdminParams sysAdminParams;

	public JsonSchemaEntityVisitor(SysAdminParams sysAdminParams, NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			EntityDictionaryService entityDictionaryService, ContentService contentService, SiteService siteService,
			AttributeExtractorService attributeExtractor, VersionService versionService, LockService lockService) {
		super(mlNodeService, nodeService, namespaceService, entityDictionaryService, contentService, siteService, attributeExtractor, versionService,
				lockService);
		this.sysAdminParams = sysAdminParams;
	}
	

	/** {@inheritDoc} */
	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {

		JSONObject root = new JSONObject();
		root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
		root.put("$id", sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort()
					+ "/alfresco/service/becpg/remote/entity?nodeRef=" + entityNodeRef + "&format=json_schema");
		root.put(PROP_TYPE, TYPE_OBJECT);
		root.put(PROP_TITLE, "Entity");
		root.put(PROP_DESCRIPTION, "Entity schema object");

		QName nodeType = nodeService.getType(entityNodeRef).getPrefixedQName(namespaceService);
		ClassDefinition classDefinition = entityDictionaryService.getClass(nodeType);

		JSONObject entity = addProperty(root, RemoteEntityService.ELEM_ENTITY, TYPE_OBJECT, classDefinition.getTitle(entityDictionaryService),
				classDefinition.getDescription(entityDictionaryService));

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {

			RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);

			visitNode(entityNodeRef, entity, JsonVisitNodeType.ENTITY, context);
			visitLists(entityNodeRef, entity, context);

			//TODO Manage catalog
			entity.put("required", new JSONArray());
			root.write(out);
		}

	}

	@Override
	protected void visitNode(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName, RemoteJSONContext context)
			throws JSONException {
		cacheList.add(nodeRef);
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		if (JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.CONTENT.equals(type) || JsonVisitNodeType.ASSOC.equals(type)
				|| (JsonVisitNodeType.CHILD_ASSOC.equals(type) && !ContentModel.TYPE_FOLDER.equals(nodeType))) {

			if (nodeService.getPrimaryParent(nodeRef) != null) {

				addProperty(entity, RemoteEntityService.ATTR_PATH, TYPE_STRING, "Path of the entity", null);

				if (!JsonVisitNodeType.ASSOC.equals(type)) {

					JSONObject site = new JSONObject();

					addProperty(site, RemoteEntityService.ATTR_ID, TYPE_STRING, "Site id", null);
					addProperty(site, RemoteEntityService.ATTR_NAME, TYPE_STRING, "Site name", null);
					addProperty(entity, RemoteEntityService.ATTR_SITE, TYPE_OBJECT, "Entity site information", null, site);

					addProperty(entity, RemoteEntityService.ATTR_PARENT_ID, TYPE_STRING, "Parent entity id", null);

				}
			}
		}

		addProperty(entity, RemoteEntityService.ATTR_TYPE, TYPE_STRING, "Prefixed qname type of the entity", null);
		addProperty(entity, RemoteEntityService.ATTR_NAME, TYPE_STRING, "Name of the entity", null);

		Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

		if (!JsonVisitNodeType.CHILD_ASSOC.equals(type)) {

			addProperty(entity, entityDictionaryService.toPrefixString(BeCPGModel.PROP_CODE), TYPE_STRING, "Code of the entity", null);
			addProperty(entity, entityDictionaryService.toPrefixString(BeCPGModel.PROP_ERP_CODE), TYPE_STRING, "ERP code of the entity", null);
			addProperty(entity, entityDictionaryService.toPrefixString(BeCPGModel.PROP_VERSION_LABEL), TYPE_STRING, "Version of the entity", null);
			addProperty(entity, RemoteEntityService.ATTR_ID, TYPE_STRING, "Id of the entity", null);

		}

		JSONObject attributes = addProperty(entity, RemoteEntityService.ELEM_ATTRIBUTES, TYPE_OBJECT, "Entity attributes", null);

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

		if (isAll()) {
			addProperty(entity, "metadata", TYPE_STRING, "Metadata", null);
		}

		if (JsonVisitNodeType.CONTENT.equals(type) || (ContentModel.TYPE_CONTENT.equals(nodeType)
				&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CONTENT, Boolean.FALSE)))) {
			visitContent(nodeRef, entity);
		}

	}

	@Override
	protected void visitLists(NodeRef nodeRef, JSONObject entity, RemoteJSONContext context) throws JSONException {

		NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);

		JSONObject entityLists = addProperty(entity, RemoteEntityService.ELEM_DATALISTS, TYPE_OBJECT, "Datalists of the entity", null);

		if (listContainerNodeRef != null) {
			List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(listContainerNodeRef);
			for (ChildAssociationRef assocRef : assocRefs) {

				NodeRef listNodeRef = assocRef.getChildRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if ((dataListType != null) && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
					String dataListName = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);
					if (!(dataListName).startsWith(RepoConsts.WUSED_PREFIX) && params.shouldExtractList(dataListName)) {
						if ((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
								|| entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM))) {

							List<ChildAssociationRef> listItemRefs = nodeService.getChildAssocs(listNodeRef);
							ClassDefinition classDefinition = entityDictionaryService.getClass(dataListTypeQName);

							if ((listItemRefs != null) && !listItemRefs.isEmpty()) {
								JSONObject list = addProperty(entityLists, dataListType, TYPE_ARRAY,
										classDefinition.getTitle(entityDictionaryService), classDefinition.getDescription(entityDictionaryService));

								for (ChildAssociationRef listItemRef : listItemRefs) {

									NodeRef listItem = listItemRef.getChildRef();
									JSONObject jsonAssocNode = new JSONObject();
									jsonAssocNode.put(PROP_TYPE, TYPE_OBJECT);

									list.put(PROP_ITEMS, jsonAssocNode);

									visitNode(listItem, jsonAssocNode, JsonVisitNodeType.DATALIST, context);

									break;

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

	@Override
	protected void visitContent(NodeRef nodeRef, JSONObject entity) throws JSONException {
		addProperty(entity, RemoteEntityService.ELEM_CONTENT, TYPE_STRING, "Base64 Content", null);
	}

	@Override
	protected void visitAssocs(NodeRef nodeRef, JSONObject entity, QName assocName, RemoteJSONContext context) throws JSONException {

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

					JSONObject jsonAssocs = new JSONObject();

					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						addProperty(entity, entityDictionaryService.toPrefixString(nodeType), TYPE_ARRAY, assocDef.getTitle(entityDictionaryService),
								assocDef.getDescription(entityDictionaryService), jsonAssocs);

					}

					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {

							NodeRef childRef = assocRef.getChildRef();
							JSONObject jsonAssocNode = new JSONObject();
							if (assocDef.isTargetMany()) {
								jsonAssocs.put(PROP_ITEMS, jsonAssocNode);
							} else {
								addProperty(entity, entityDictionaryService.toPrefixString(nodeType), TYPE_OBJECT,
										assocDef.getTitle(entityDictionaryService), assocDef.getDescription(entityDictionaryService), jsonAssocNode);
							}

							visitNode(childRef, jsonAssocNode, JsonVisitNodeType.CHILD_ASSOC, context);
							break;
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

					JSONObject jsonAssocs = new JSONObject();

					List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						addProperty(entity, entityDictionaryService.toPrefixString(nodeType), TYPE_ARRAY, assocDef.getTitle(entityDictionaryService),
								assocDef.getDescription(entityDictionaryService), jsonAssocs);
					}
					for (AssociationRef assocRef : assocRefs) {
						NodeRef childRef = assocRef.getTargetRef();

						JSONObject jsonAssocNode = new JSONObject();
						if (assocDef.isTargetMany()) {
							jsonAssocs.put(PROP_ITEMS, jsonAssocNode);
						} else {
							addProperty(entity, entityDictionaryService.toPrefixString(nodeType), TYPE_OBJECT,
									assocDef.getTitle(entityDictionaryService), assocDef.getDescription(entityDictionaryService), jsonAssocNode);
						}

						visitNode(childRef, jsonAssocNode, JsonVisitNodeType.ASSOC, nodeType, context);

					}
				}

			}

		} else {
			logger.warn("No typeDef found for :" + nodeRef);
		}

	}

	@Override
	protected void visitProps(NodeRef nodeRef, JSONObject entity, QName assocName, Map<QName, Serializable> props, RemoteJSONContext context)
			throws JSONException {

		if (props != null) {
			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				QName propQName = entry.getKey();
				QName propName = entry.getKey().getPrefixedQName(namespaceService);
				if (!propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& (!propQName.getNamespaceURI().equals(ReportModel.REPORT_URI) || matchProp(assocName, propName, true))
						&& !propQName.equals(ContentModel.PROP_CONTENT) && params.shouldExtractField(propQName)) {
					PropertyDefinition propertyDefinition = entityDictionaryService.getProperty(entry.getKey());
					if (propertyDefinition != null) {
						// Assoc properties filter
						if (!matchProp(assocName, propName, false)) {
							continue;
						}
						if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
								&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)) {
							visitMltextAttributes(entityDictionaryService.toPrefixString(propName), entity, propertyDefinition);
						} else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())
								&& !propertyDefinition.getConstraints().isEmpty()
								&& Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, Boolean.TRUE))) {
							for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
								if (constraint.getConstraint() instanceof DynListConstraint) {
									visitMltextAttributes(entityDictionaryService.toPrefixString(propName), entity, propertyDefinition);
									break;
								}
							}
						}
						visitPropValue(propName, entity, entry.getValue(), context, propertyDefinition);
					} else {
						logger.debug("Properties not in dictionnary: " + entry.getKey());
					}
				}
			}
		}

	}

	private void visitMltextAttributes(String propType, JSONObject entity, PropertyDefinition propertyDefinition) throws JSONException {
		for (Locale locale : MLTextHelper.getSupportedLocales()) {
			String code = MLTextHelper.localeKey(locale);
			if ((code != null) && !code.isBlank()) {
				addProperty(entity, propType + "_" + code, TYPE_STRING, propertyDefinition.getTitle(entityDictionaryService),
						propertyDefinition.getDescription(entityDictionaryService));
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void visitPropValue(QName propType, JSONObject entity, Serializable value, RemoteJSONContext context,
			PropertyDefinition propertyDefinition) throws JSONException {
		if (propertyDefinition.isMultiValued() && value!=null) {
			JSONObject arrayDef = addProperty(entity, entityDictionaryService.toPrefixString(propType), TYPE_ARRAY,
					propertyDefinition.getTitle(entityDictionaryService), propertyDefinition.getDescription(entityDictionaryService));
			for (Serializable subEl : (List<Serializable>) value) {
				JSONObject node = new JSONObject();

				if (subEl instanceof NodeRef) {

					visitNode((NodeRef) subEl, node, JsonVisitNodeType.ASSOC, context);
				} else {
					if (subEl != null) {
						if (RemoteHelper.isJSONValue(propType)) {
							node.put(PROP_TYPE, TYPE_OBJECT);
						} else if ((JsonHelper.formatValue(subEl) != null) && !JsonHelper.formatValue(subEl).toString().isEmpty()) {
							node.put(PROP_TYPE, TYPE_STRING);
						}
					}
				}

				arrayDef.put(PROP_ITEMS, node);
				break;
			}
		} else if (value instanceof NodeRef) {
			JSONObject node = addProperty(entity, entityDictionaryService.toPrefixString(propType), TYPE_OBJECT,
					propertyDefinition.getTitle(entityDictionaryService), propertyDefinition.getDescription(entityDictionaryService));
			visitNode((NodeRef) value, node, JsonVisitNodeType.ASSOC, context);
		} else {
			if (RemoteHelper.isJSONValue(propType)) {
				addProperty(entity, entityDictionaryService.toPrefixString(propType), TYPE_OBJECT,
						propertyDefinition.getTitle(entityDictionaryService), propertyDefinition.getDescription(entityDictionaryService));
			} else {
				addProperty(entity, entityDictionaryService.toPrefixString(propType), getType(propertyDefinition.getDataType()),
						propertyDefinition.getTitle(entityDictionaryService), propertyDefinition.getDescription(entityDictionaryService));
			}
		}
	}

	private String getType(DataTypeDefinition dataType) {
		switch (dataType.getName().toPrefixString(namespaceService)) {
		case "d:float":
		case "d:double":
		case "d:long":
		case "d:int":
			return TYPE_NUMBER;
		case "d:date":
		case "d:datetime":
		case "d:text":
		case "d:mltext":
			return TYPE_STRING;
		case "d:boolean":
			return TYPE_BOOLEAN;
		default:
			return TYPE_OBJECT;
		}

	}

	private JSONObject addProperty(JSONObject entity, String attr, String type, String title, String description, JSONObject object) {
		JSONObject properties = new JSONObject();
		if (entity.has(PROP_PROPERTIES)) {
			properties = entity.getJSONObject(PROP_PROPERTIES);
		} else {
			entity.put(PROP_PROPERTIES, properties);
		}
		object.put(PROP_TYPE, type);
		object.put(PROP_TITLE, title);
		if (description != null) {
			object.put(PROP_DESCRIPTION, description);
		}
		properties.put(attr, object);
		return object;
	}

	private JSONObject addProperty(JSONObject entity, String attr, String type, String title, String description) {
		return addProperty(entity, attr, type, title, description, new JSONObject());
	}

}
