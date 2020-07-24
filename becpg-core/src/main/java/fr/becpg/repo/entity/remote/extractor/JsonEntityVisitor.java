/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
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
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;

/**
 * <p>JsonEntityVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonEntityVisitor extends AbstractEntityVisitor {

	/**
	 * <p>Constructor for JsonEntityVisitor.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public JsonEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			EntityDictionaryService entityDictionaryService, ContentService contentService, SiteService siteService) {
		super(mlNodeService, nodeService, namespaceService, entityDictionaryService, contentService, siteService);
	}
	
	public JsonEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			EntityDictionaryService entityDictionaryService, ContentService contentService, SiteService siteService) {
		this(mlNodeService, nodeService, namespaceService, entityDictionaryService, contentService, siteService, null);
	}

	private enum JsonVisitNodeType {
		ENTITY, ENTITY_LIST, CONTENT, ASSOC, DATALIST
	}
	
	
	private static final Log logger = LogFactory.getLog(JsonEntityVisitor.class);
	
	public void setAll(boolean value) {
		allFields = value;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {

		JSONObject root = new JSONObject();
		JSONObject entity  = new JSONObject();
		root.put(RemoteEntityService.ELEM_ENTITY, entity);
		
		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			visitNode(entityNodeRef, entity, JsonVisitNodeType.ENTITY);
			visitLists(entityNodeRef, entity);
			root.write(out);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void visit(List<NodeRef> entities, OutputStream result) throws JSONException, IOException {

		JSONObject ret = new JSONObject();
		JSONArray jsonEntities = new JSONArray();
		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {

			for (NodeRef nodeRef : entities) {
				JSONObject root = new JSONObject();
				JSONObject entity  = new JSONObject();
				root.put(RemoteEntityService.ELEM_ENTITY, entity);
				visitNode(nodeRef, entity, JsonVisitNodeType.ENTITY_LIST);
				jsonEntities.put(root);
			}

			ret.put("entities", jsonEntities);
			ret.write(out);
		}

	}
	
	

	/** {@inheritDoc} */
	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws JSONException, IOException {
		JSONObject data = new JSONObject();

		try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
			// Visit node
			visitNode(entityNodeRef, data, JsonVisitNodeType.CONTENT);
			data.write(out);
		}

	}
	

	private void visitNode(NodeRef entityNodeRef, JSONObject entity, JsonVisitNodeType type) throws JSONException {
		visitNode(entityNodeRef, entity,type, null); 
	}

	private void visitNode(NodeRef nodeRef, JSONObject entity, JsonVisitNodeType type, QName assocName) throws JSONException {
		cacheList.add(nodeRef);
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		Path path = null;
		
		if( JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.CONTENT.equals(type) || JsonVisitNodeType.ASSOC.equals(type)) {
			
			if (nodeService.getPrimaryParent(nodeRef) != null) {
				NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				if (parentRef != null) {
					path = nodeService.getPath(parentRef);
					entity.put(RemoteEntityService.ATTR_PATH, path.toPrefixString(namespaceService));
					if(! JsonVisitNodeType.ASSOC.equals(type)) {
						visitSite(nodeRef, entity, path);
						entity.put(RemoteEntityService.ATTR_PARENT_ID, parentRef.getId());
					}
				}
			} else {
				logger.warn("Node : " + nodeRef + " has no primary parent");
			}
		}

		entity.put(RemoteEntityService.ATTR_TYPE, nodeType.toPrefixString(namespaceService));

		QName propName = RemoteHelper.getPropName(nodeType, entityDictionaryService);
		visitPropValue(propName, entity, nodeService.getProperty(nodeRef, propName));
		
		
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)) {
			if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE) != null) {
				visitPropValue(BeCPGModel.PROP_CODE, entity, nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
			}

		}
		// erpCode
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ERP_CODE)) {
			if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_ERP_CODE) != null) {
				visitPropValue(BeCPGModel.PROP_ERP_CODE, entity, nodeService.getProperty(nodeRef, BeCPGModel.PROP_ERP_CODE));
			}
		}

		
		if(nodeRef!=null) {
		
			entity.put(RemoteEntityService.ATTR_ID, nodeRef.getId());
		}
		
	
			JSONObject attributes  = new JSONObject();
			if(JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.DATALIST.equals(type) 
					|| ((JsonVisitNodeType.ENTITY_LIST.equals(type) || JsonVisitNodeType.CONTENT.equals(type)) && filteredProperties!=null && !filteredProperties.isEmpty())
					|| (nodeType !=null && filteredAssocProperties.containsKey(nodeType))) {
				// Assoc first
				visitAssocs(nodeRef, attributes);	
				visitProps(nodeRef, attributes, assocName);
				
			}
			
			if(attributes.length()>0) {
				entity.put(RemoteEntityService.ELEM_ATTRIBUTES, attributes);
			}
		
		if (allFields && attributeExtractor != null) {
			entity.put("metadata", attributeExtractor.extractMetadata(nodeType, nodeRef));
		}
		
		

		if (JsonVisitNodeType.CONTENT.equals(type) ) {
			visitContent(nodeRef, entity);
		}

	}

	private void visitLists(NodeRef nodeRef, JSONObject entity) throws JSONException {

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

					if (((BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
							|| entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM))
							&& !((String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME)).startsWith(RepoConsts.WUSED_PREFIX))) {

						if ((filteredLists == null) || filteredLists.isEmpty()
								|| filteredLists.contains(nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME))) {


							List<ChildAssociationRef> listItemRefs = nodeService.getChildAssocs(listNodeRef);
							
							if(listItemRefs!=null && !listItemRefs.isEmpty()) {
								JSONArray list = new JSONArray();
								entityLists.put(dataListTypeQName.toPrefixString(namespaceService), list);
	
								for (ChildAssociationRef listItemRef : listItemRefs) {
	
									NodeRef listItem = listItemRef.getChildRef();
									JSONObject jsonAssocNode = new JSONObject();
									list.put(jsonAssocNode);
	
									visitNode(listItem, jsonAssocNode,JsonVisitNodeType.DATALIST);
	
								}
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
					if (((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(nodeType))) {
						continue;
					}

					JSONArray jsonAssocs = new JSONArray();

					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
						entity.put(nodeType.toPrefixString(namespaceService), jsonAssocs);
					}

					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {

							NodeRef childRef = assocRef.getChildRef();
							JSONObject jsonAssocNode = new JSONObject();
							if (assocDef.isTargetMany()) {
								jsonAssocs.put(jsonAssocNode);
							} else {
								entity.put(nodeType.toPrefixString(namespaceService), jsonAssocNode);
							}

							visitNode(childRef, jsonAssocNode, JsonVisitNodeType.ASSOC);

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
						entity.put(nodeType.toPrefixString(namespaceService), jsonAssocs);
					}
					for (AssociationRef assocRef : assocRefs) {
						NodeRef childRef = assocRef.getTargetRef();

						JSONObject jsonAssocNode = new JSONObject();
						if (assocDef.isTargetMany()) {
							jsonAssocs.put(jsonAssocNode);
						} else {
							entity.put(nodeType.toPrefixString(namespaceService), jsonAssocNode);
						}

						visitNode(childRef, jsonAssocNode,JsonVisitNodeType.ASSOC, nodeType);

					}
				}

			}

		} else {
			logger.warn("No typeDef found for :" + nodeRef);
		}

	}

	private void visitProps(NodeRef nodeRef, JSONObject entity, QName assocName) throws JSONException {

		
		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
		if (props != null) {
			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				QName propQName = entry.getKey();
				if ((entry.getValue() != null) && !propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(ReportModel.REPORT_URI) && !propQName.equals(ContentModel.PROP_CONTENT)) {
					PropertyDefinition propertyDefinition = entityDictionaryService.getProperty(entry.getKey());
					if (propertyDefinition != null) {
						QName propName = entry.getKey().getPrefixedQName(namespaceService);

						// Assoc properties filter
						if (assocName ==null &&  (filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(propName)) {
							continue;
						} else if(assocName !=null &&  (filteredAssocProperties != null) && !filteredAssocProperties.isEmpty() && (!filteredAssocProperties.containsKey(assocName)
								|| !filteredAssocProperties.get(assocName).contains(propName))) {
							continue;
						}
						
					

						MLText mlValues = null;
						if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
								&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)) {
							mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDefinition.getName());
							visitMltextAttributes(propName.toPrefixString(namespaceService), entity, mlValues);
						} else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())) {
							if (!propertyDefinition.getConstraints().isEmpty()) {
								for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
									if (constraint.getConstraint() instanceof DynListConstraint) {
										mlValues = ((DynListConstraint) constraint.getConstraint()).getMLAwareAllowedValues().get(entry.getValue());
										visitMltextAttributes(propName.toPrefixString(namespaceService), entity, mlValues);
										break;
									}
								}
							}
						}
						visitPropValue(propName, entity, entry.getValue());

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
	private void visitPropValue(QName propType, JSONObject entity, Serializable value) throws JSONException {
		if (value instanceof List) {
			JSONArray tmp = new JSONArray();
			entity.put(propType.toPrefixString(namespaceService), tmp);
			for (Serializable subEl : (List<Serializable>) value) {
				tmp.put(JsonHelper.formatValue(subEl));

			}
		} else if (value instanceof NodeRef) {
			JSONObject node = new JSONObject();
			entity.put(propType.toPrefixString(namespaceService), node);
			visitNode((NodeRef) value, node, JsonVisitNodeType.ASSOC);
		} else {
			
			if(value!=null) {
			    if(RemoteHelper.isJSONValue(propType)) {
				   entity.put(propType.toPrefixString(namespaceService),new JSONObject((String)value));
			    } else if (JsonHelper.formatValue(value)!=null && !JsonHelper.formatValue(value).toString().isEmpty()) {
					entity.put(propType.toPrefixString(namespaceService), JsonHelper.formatValue(value));
				}
			}
		}
	}
}
