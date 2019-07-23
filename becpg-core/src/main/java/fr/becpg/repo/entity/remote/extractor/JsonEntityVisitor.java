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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.entity.remote.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;

/**
 *
 * @author matthieu
 *
 */
public class JsonEntityVisitor extends AbstractEntityVisitor {

	public JsonEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			DictionaryService dictionaryService, ContentService contentService, SiteService siteService) {
		super(mlNodeService, nodeService, namespaceService, dictionaryService, contentService, siteService);
	}

	private static final Log logger = LogFactory.getLog(JsonEntityVisitor.class);

	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {

		JSONObject entity = new JSONObject();

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			visitNode(entityNodeRef, entity, true, true, false);
			visitLists(entityNodeRef, entity);
	
			entity.write(out);
		}

	}

	@Override
	public void visit(List<NodeRef> entities, OutputStream result) throws JSONException, IOException {

		JSONObject ret = new JSONObject();
		JSONArray jsonEntities = new JSONArray();
		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {

			for (NodeRef nodeRef : entities) {
				JSONObject entity = new JSONObject();

				if ((this.filteredProperties != null) && !this.filteredProperties.isEmpty()) {
					entityList = true;
					visitNode(nodeRef, entity, true, true, false);
				} else {
					visitNode(nodeRef, entity, false, false, false);
				}

				jsonEntities.put(entity);
			}

			ret.put("entities", jsonEntities);
			ret.write(out);
		}

	}

	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {
		JSONObject data = new JSONObject();

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			// Visit node
			visitNode(entityNodeRef, data, false, false, true);
			data.write(out);
		}

	}

	private void visitNode(NodeRef nodeRef, JSONObject entity, boolean assocs, boolean props, boolean content) throws JSONException {
		cacheList.add(nodeRef);

		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);
		String name = (String) nodeService.getProperty(nodeRef, RemoteHelper.getPropName(nodeType, dictionaryService));

		Path path = null;

		if (nodeService.getPrimaryParent(nodeRef) != null) {
			NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			if (parentRef != null) {
				path = nodeService.getPath(parentRef);
				entity.put(RemoteEntityService.ATTR_PATH, path.toPrefixString(namespaceService));

			}
		} else {
			logger.warn("Node : " + nodeRef + " has no primary parent");
		}

		entity.put(RemoteEntityService.ATTR_TYPE, nodeType.toPrefixString(namespaceService));

		if (name != null) {
			entity.put(RemoteEntityService.ATTR_NAME, name);
		}

		entity.put(RemoteEntityService.ATTR_NODEREF, nodeRef);

		// Assoc first
		if (assocs) {
			visitAssocs(nodeRef, entity);
		}

		if (path != null) {
			visitSite(nodeRef, entity, path);
		}

		if (props) {
			visitProps(nodeRef, entity);
		}

		

		if (content) {
			visitContent(nodeRef, entity);
		}

	}

	private void visitLists(NodeRef nodeRef, JSONObject entity) throws JSONException {

		NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);

		if (listContainerNodeRef != null) {
			List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(listContainerNodeRef);
			for (ChildAssociationRef assocRef : assocRefs) {

				NodeRef listNodeRef = assocRef.getChildRef();
				String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);

				if ((dataListType != null) && !dataListType.isEmpty()) {

					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);

					if ((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
							|| dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)
							|| ((String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME)).startsWith(RepoConsts.WUSED_PREFIX))) {

						if ((filteredLists == null) || filteredLists.isEmpty()
								|| filteredLists.contains(nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME))) {

							JSONArray list = new JSONArray();
							entity.put(dataListTypeQName.toPrefixString(), list);

							List<ChildAssociationRef> listItemRefs = nodeService.getChildAssocs(listNodeRef);

							for (ChildAssociationRef listItemRef : listItemRefs) {

								NodeRef listItem = listItemRef.getChildRef();
								JSONObject jsonAssocNode = new JSONObject();
								list.put(jsonAssocNode);

								visitNode(listItem, jsonAssocNode, true, true, false);

							}

						}

					} else {
						logger.warn("Existing " + dataListTypeQName + " list doesn't inheritate from 'bcpg:entityListItem'.");
					}
				}

			}
		}

	}

	private void visitContent(NodeRef nodeRef, JSONObject entity) throws JSONException {

		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (contentReader != null) {

			try (InputStream in = contentReader.getContentInputStream(); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = in.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
				entity.put(ContentModel.PROP_CONTENT.toPrefixString(namespaceService), buffer.toByteArray());

			} catch (ContentIOException | IOException e) {
				throw new JSONException("Cannot serialyze data");
			}
		}

	}

	private void visitAssocs(NodeRef nodeRef, JSONObject entity) throws JSONException {

		TypeDefinition typeDef = dictionaryService.getType(nodeService.getType(nodeRef));
		if (typeDef != null) {

			Map<QName, AssociationDefinition> assocs = new HashMap<>(typeDef.getAssociations());
			for (QName aspect : nodeService.getAspects(nodeRef)) {
				if (dictionaryService.getAspect(aspect) != null) {
					assocs.putAll(dictionaryService.getAspect(aspect).getAssociations());
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
					nodeType.getPrefixString().split(":");
					// fields & child assocs filter
					if (((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(nodeType))) {
						continue;
					}

					JSONArray jsonAssocs = new JSONArray();

					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						entity.put(nodeType.toPrefixString(), jsonAssocs);
					}

					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {

							NodeRef childRef = assocRef.getChildRef();
							JSONObject jsonAssocNode = new JSONObject();
							if (assocDef.isTargetMany()) {
								jsonAssocs.put(jsonAssocNode);
							} else {
								entity.put(nodeType.toPrefixString(), jsonAssocNode);
							}

							visitNode(childRef, jsonAssocNode, light ? false : true, light ? false : true, false);

						}
					}
				}

			}

			for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
				AssociationDefinition assocDef = entry.getValue();

				if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER) && !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL)
						&& !assocDef.isChild()) {
					QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
					// fields & assocs filter
					if ((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(nodeType)) {
						continue;
					}

					JSONArray jsonAssocs = new JSONArray();

					List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						entity.put(nodeType.toPrefixString(), jsonAssocs);
					}
					for (AssociationRef assocRef : assocRefs) {
						NodeRef childRef = assocRef.getTargetRef();

						JSONObject jsonAssocNode = new JSONObject();
						if (assocDef.isTargetMany()) {
							jsonAssocs.put(jsonAssocNode);
						} else {
							entity.put(nodeType.toPrefixString(), jsonAssocNode);
						}

						// extract assoc properties
						if (filteredAssocProperties.containsKey(nodeType)) {
							cachedAssocRef = Collections.singletonMap(childRef, filteredAssocProperties.get((nodeType)));
							visitNode(childRef, jsonAssocNode, shouldDumpAll(childRef), true, false);

						} else {
							visitNode(childRef, jsonAssocNode, shouldDumpAll(childRef), shouldDumpAll(childRef), false);
						}
						cachedAssocRef = null;
					}
				}

			}

		} else {
			logger.warn("No typeDef found for :" + nodeRef);
		}

	}

	private void visitProps(NodeRef nodeRef, JSONObject entity) throws JSONException {

		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
		if (props != null) {
			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				QName propQName = entry.getKey();
				if ((entry.getValue() != null) && !propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(ReportModel.REPORT_URI) && !propQName.equals(ContentModel.PROP_CONTENT)) {
					PropertyDefinition propertyDefinition = dictionaryService.getProperty(entry.getKey());
					if (propertyDefinition != null) {
						QName propName = entry.getKey().getPrefixedQName(namespaceService);
						propName.getPrefixString().split(":");
						// filter props
						if ((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(propName)
								&& (extractLevel == 1)) {
							continue;
						}
						// Assoc properties filter
						if ((cachedAssocRef != null) && (cachedAssocRef.get(nodeRef) != null) && cachedAssocRef.containsKey(nodeRef)
								&& !cachedAssocRef.get(nodeRef).contains(propName)) {
							continue;
						}

						Map<NodeRef, List<QName>> tmpCachedAssocRef = cachedAssocRef;

						MLText mlValues = null;
						if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
								&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)) {
							mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDefinition.getName());
							visitMltextAttributes(propName.toPrefixString(), entity, mlValues);
						} else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())) {
							if (!propertyDefinition.getConstraints().isEmpty()) {
								for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
									if (constraint.getConstraint() instanceof DynListConstraint) {
										mlValues = ((DynListConstraint) constraint.getConstraint()).getMLAwareAllowedValues().get(entry.getValue());
										visitMltextAttributes(propName.toPrefixString(), entity, mlValues);
										break;
									}
								}
							}
						}
						cachedAssocRef = null;
						visitPropValue(propName.toPrefixString(), entity, entry.getValue());
						cachedAssocRef = tmpCachedAssocRef;

					} else {
						logger.debug("Properties not in dictionnary: " + entry.getKey());
					}

				}

			}
		}

	}

	private void visitMltextAttributes(String propType, JSONObject entity, MLText mlValues) throws JSONException {
		if (mlValues != null) {
			for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
				String code = MLTextHelper.localeKey(mlEntry.getKey());
				if ((code != null) && !code.isEmpty() && (mlEntry.getValue() != null)) {
					entity.put(propType + "_" + code, mlEntry.getValue());

				}
			}
		}
	}

	private void visitSite(NodeRef nodeRef, JSONObject entity, Path path) throws JSONException {

		String siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));

		if (siteId != null) {
			entity.put("siteId", siteId);

			SiteInfo site = siteService.getSite(siteId);

			if (site != null) {
				entity.put("siteName", site.getTitle());

			}
		}

	}

	@SuppressWarnings("unchecked")
	private void visitPropValue(String propType, JSONObject entity, Serializable value) throws JSONException {
		if (value instanceof List) {
			JSONArray tmp = new JSONArray();
			entity.put(propType, tmp);
			for (Serializable subEl : (List<Serializable>) value) {
				tmp.put(JsonHelper.formatValue(subEl));

			}
		} else if (value instanceof NodeRef) {
			JSONObject node = new JSONObject();
			entity.put(propType, node);

			visitNode((NodeRef) value, node, shouldDumpAll((NodeRef) value), shouldDumpAll((NodeRef) value), false);
		} else {
			if (value != null && JsonHelper.formatValue(value)!=null && !JsonHelper.formatValue(value).toString().isEmpty()) {
				entity.put(propType, JsonHelper.formatValue(value));
			}
		}
	}
}
