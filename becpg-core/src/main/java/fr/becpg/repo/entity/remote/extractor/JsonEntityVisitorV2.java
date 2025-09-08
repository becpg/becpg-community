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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

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
 * JsonEntityVisitor class using Jackson Streaming API.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class JsonEntityVisitorV2 extends AbstractEntityVisitor {

    private AttributeExtractorService attributeExtractor;
    private VersionService versionService;
    private LockService lockService;
    private AssociationService associationService;
    private EntityListDAO entityListDAO;
    private static final Log logger = LogFactory.getLog(JsonEntityVisitorV2.class);
    private final JsonFactory jsonFactory;

    /**
     * <p>Constructor for JsonEntityVisitor.</p>
     *
     * @param remoteServiceRegisty a {@link fr.becpg.repo.entity.remote.RemoteServiceRegisty} object
     */
    public JsonEntityVisitorV2(RemoteServiceRegisty remoteServiceRegisty) {
        super(remoteServiceRegisty);
        this.attributeExtractor = remoteServiceRegisty.attributeExtractor();
        this.versionService = remoteServiceRegisty.versionService();
        this.lockService = remoteServiceRegisty.lockService();
        this.associationService = remoteServiceRegisty.associationService();
        this.entityListDAO = remoteServiceRegisty.entityListDAO();
        this.jsonFactory = new JsonFactory();
    }

    @Override
    public void visit(NodeRef entityNodeRef, OutputStream result) throws IOException {
        try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8);
             JsonGenerator generator = jsonFactory.createGenerator(out)) {
            RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);
            generator.writeStartObject();
            generator.writeFieldName(RemoteEntityService.ELEM_ENTITY);
            generator.writeStartObject();
            visitNode(entityNodeRef, generator, JsonVisitNodeType.ENTITY, null, context);
            visitLists(entityNodeRef, generator, context);
            generator.writeEndObject();
            generator.writeEndObject();
        }
    }

    @Override
    public void visit(PagingResults<NodeRef> pagingResult, OutputStream result) throws IOException {
        try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8);
             JsonGenerator generator = jsonFactory.createGenerator(out)) {
            generator.writeStartObject();
            generator.writeFieldName("pagination");
            generator.writeStartObject();
            generator.writeBooleanField("hasMoreItems", pagingResult.hasMoreItems());
            generator.writeNumberField("count", pagingResult.getTotalResultCount().getFirst());
            generator.writeEndObject();

            generator.writeFieldName("entities");
            generator.writeStartArray();
            for (NodeRef nodeRef : pagingResult.getPage()) {
                generator.writeStartObject();
                generator.writeFieldName(RemoteEntityService.ELEM_ENTITY);
                generator.writeStartObject();
                RemoteJSONContext context = new RemoteJSONContext(nodeRef);
                visitNode(nodeRef, generator, JsonVisitNodeType.ENTITY_LIST, null, context);
                generator.writeEndObject();
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.writeEndObject();
        }
    }

    @Override
    public void visitData(NodeRef entityNodeRef, OutputStream result) throws IOException {
        try (OutputStreamWriter out = new OutputStreamWriter(result, StandardCharsets.UTF_8);
             JsonGenerator generator = jsonFactory.createGenerator(out)) {
            RemoteJSONContext context = new RemoteJSONContext(entityNodeRef);
            generator.writeStartObject();
            visitNode(entityNodeRef, generator, JsonVisitNodeType.CONTENT, null, context);
            generator.writeEndObject();
        }
    }

    protected void visitNode(NodeRef nodeRef, JsonGenerator generator, JsonVisitNodeType type, QName assocName, RemoteJSONContext context) throws IOException {
        try {
            cacheList.add(nodeRef);
            QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

            if (JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.CONTENT.equals(type) || JsonVisitNodeType.ASSOC.equals(type)
                    || (JsonVisitNodeType.CHILD_ASSOC.equals(type) && !ContentModel.TYPE_FOLDER.equals(nodeType))) {
                NodeRef parentRef = getPrimaryParentRef(nodeRef);
                if (parentRef != null) {
                    Path parentPath = nodeService.getPath(parentRef);
                    String path = parentPath.toPrefixString(namespaceService);
                    generator.writeStringField(RemoteEntityService.ATTR_PATH, path.replace(context.getEntityPath(nodeService, namespaceService), "~"));
                    if (!JsonVisitNodeType.ASSOC.equals(type)) {
                        visitSite(generator, parentPath);
                        generator.writeStringField(RemoteEntityService.ATTR_PARENT_ID, parentRef.getId());
                    }
                } else {
                    logger.warn("Node : " + nodeRef + " has no primary parent");
                }
            }

            generator.writeStringField(RemoteEntityService.ATTR_TYPE, entityDictionaryService.toPrefixString(nodeType));

            QName propName = RemoteHelper.getPropName(nodeType, entityDictionaryService);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            visitPropValue(propName, generator, properties.get(propName), context);

            if (!JsonVisitNodeType.CHILD_ASSOC.equals(type)) {
                if ((properties.get(BeCPGModel.PROP_CODE) != null)
                        && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CODE, Boolean.TRUE))) {
                    visitPropValue(BeCPGModel.PROP_CODE, generator, properties.get(BeCPGModel.PROP_CODE), context);
                }
                if ((properties.get(BeCPGModel.PROP_ERP_CODE) != null)
                        && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_ERP_CODE, Boolean.TRUE))) {
                    visitPropValue(BeCPGModel.PROP_ERP_CODE, generator, properties.get(BeCPGModel.PROP_ERP_CODE), context);
                }

                if (properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL) != null
                        && !((String) properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL)).isBlank()) {
                    generator.writeStringField(RemoteEntityService.ATTR_VERSION, (String) properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL));
                } else if (properties.get(BeCPGModel.PROP_VERSION_LABEL) != null && !((String) properties.get(BeCPGModel.PROP_VERSION_LABEL)).isBlank()) {
                    generator.writeStringField(RemoteEntityService.ATTR_VERSION, (String) properties.get(BeCPGModel.PROP_VERSION_LABEL));
                } else if (properties.get(ContentModel.PROP_VERSION_LABEL) != null
                        && !((String) properties.get(ContentModel.PROP_VERSION_LABEL)).isBlank()) {
                    generator.writeStringField(RemoteEntityService.ATTR_VERSION, (String) properties.get(ContentModel.PROP_VERSION_LABEL));
                }

                if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_IS_INITIAL_VERSION, Boolean.FALSE)) && lockService.isLocked(nodeRef)) {
                    String lockInfo = lockService.getAdditionalInfo(nodeRef);
                    try (JsonParser parser = jsonFactory.createParser(lockInfo)) {
                        while (parser.nextToken() != null) {
                            if (parser.currentName() != null && parser.currentName().equals("lockType") && parser.nextTextValue().equals("versioning")) {
                                String currentVersion = (String) properties.get(BeCPGModel.PROP_VERSION_LABEL);
                                if (currentVersion != null) {
                                    Collection<Version> nodeRefVersions = versionService.getVersionHistory(nodeRef).getAllVersions();
                                    Optional<Double> previousVersion = nodeRefVersions.stream()
                                            .map(Version::getVersionLabel)
                                            .filter(label -> !label.equals(currentVersion))
                                            .map(Double::parseDouble)
                                            .max(Comparator.comparing(Double::valueOf));
                                    previousVersion.ifPresent(version -> {
                                        try {
                                            generator.writeStringField(RemoteEntityService.ATTR_VERSION, version.toString());
                                        } catch (IOException e) {
                                            logger.info("Cannot write version field", e);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                if ((nodeRef != null) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))) {
                    String nodePath = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
                    if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_UPDATE_ENTITY_NODEREFS, Boolean.FALSE))
                            && nodePath.contains(context.getEntityPath(nodeService, namespaceService))) {
                        NodeRef currentNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeRef.getId());
                        NodeRef newNode = context.getCache().computeIfAbsent(currentNode, k -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()));
                        generator.writeStringField(RemoteEntityService.ATTR_ID, newNode.getId());
                    } else if (Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_REPLACE_HISTORY_NODEREFS, Boolean.FALSE))
                            && nodePath.contains(RepoConsts.ENTITIES_HISTORY_XPATH)) {
                        NodeRef parentNode = getPrimaryParentRef(nodeRef);
                        String parentName = (String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME);
                        NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);
                        generator.writeStringField(RemoteEntityService.ATTR_ID, originalNode.getId());
                    } else {
                        generator.writeStringField(RemoteEntityService.ATTR_ID, nodeRef.getId());
                    }
                }
            } else {
                if ((nodeRef != null) && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_NODEREF, Boolean.TRUE))
                        && !ContentModel.TYPE_FOLDER.equals(nodeType)) {
                    generator.writeStringField(RemoteEntityService.ATTR_ID, nodeRef.getId());
                }
            }

            if (JsonVisitNodeType.ENTITY.equals(type) || JsonVisitNodeType.DATALIST.equals(type)
                    || ((JsonVisitNodeType.ENTITY_LIST.equals(type) || JsonVisitNodeType.CONTENT.equals(type)) && (params.getFilteredProperties() != null)
                    && !params.getFilteredProperties().isEmpty())
                    || ((nodeType != null) && params.getFilteredAssocProperties().containsKey(nodeType))
                    || ((assocName != null) && params.getFilteredAssocProperties().containsKey(assocName))
                    || JsonVisitNodeType.CHILD_ASSOC.equals(type)) {
                generator.writeFieldName(RemoteEntityService.ELEM_ATTRIBUTES);
                generator.writeStartObject();
                visitAssocs(nodeRef, generator, assocName, context);
                visitProps(nodeRef, generator, assocName, properties, context);
                generator.writeEndObject();
            }

            if (isAll() && (attributeExtractor != null)) {
                generator.writeFieldName("metadata");
                String metadata = attributeExtractor.extractMetadata(nodeType, nodeRef);
                try (JsonParser parser = jsonFactory.createParser(metadata)) {
                    generator.copyCurrentStructure(parser);
                }
            }

            if (JsonVisitNodeType.CONTENT.equals(type) || (ContentModel.TYPE_CONTENT.equals(nodeType)
                    && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_CONTENT, Boolean.FALSE)))) {
                visitContent(nodeRef, generator);
            }
        } catch (RemoteException e) {
            logger.warn("Error while visiting nodeRef " + nodeRef + ": " + e.getMessage());
        }
    }

    protected void visitLists(NodeRef nodeRef, JsonGenerator generator, RemoteJSONContext context) throws IOException {
        NodeRef listContainerNodeRef = nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
        generator.writeFieldName(RemoteEntityService.ELEM_DATALISTS);
        generator.writeStartObject();
        
        Set<String> usedListTypes = new HashSet<>();

        if (listContainerNodeRef != null) {
            List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(listContainerNodeRef);
            for (ChildAssociationRef assocRef : assocRefs) {
                NodeRef listNodeRef = assocRef.getChildRef();
                String dataListType = (String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
                if ((dataListType != null) && !dataListType.isEmpty()) {
                    QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
                    String dataListName = (String) nodeService.getProperty(listNodeRef, ContentModel.PROP_NAME);
                    if (!(dataListName).startsWith(RepoConsts.WUSED_PREFIX) && !dataListName.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)) {
                        if (BeCPGModel.TYPE_ENTITYLIST_ITEM.equals(dataListTypeQName)
                                || entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
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
                                    String fieldName = usedListTypes.contains(listType) ? listType + "|" + dataListName : listType;
                                    generator.writeFieldName(fieldName);
                                    generator.writeStartArray();
                                    for (NodeRef listItem : listItems) {
                                        generator.writeStartObject();
                                        visitNode(listItem, generator, JsonVisitNodeType.DATALIST, null, context);
                                        generator.writeEndObject();
                                    }
                                    generator.writeEndArray();
                                    usedListTypes.add(listType);
                                }
                            }
                        } else {
                            logger.warn("Existing " + dataListName + " (" + dataListTypeQName + ") list doesn't inheritate from 'bcpg:entityListItem'.");
                        }
                    }
                }
            }
        }
        generator.writeEndObject();
    }

    protected void visitContent(NodeRef nodeRef, JsonGenerator generator) throws IOException {
        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (contentReader != null) {
            StringBuilder buffer = new StringBuilder();
            try (Reader reader = new InputStreamReader(new Base64InputStream(contentReader.getContentInputStream(), true, -1, null))) {
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) >= 0) {
                    buffer.append(buf, 0, n);
                }
                generator.writeStringField(RemoteEntityService.ELEM_CONTENT, buffer.toString());
            } catch (ContentIOException | IOException e) {
                throw new IOException("Cannot serialize data", e);
            }
        }
    }

    protected void visitAssocs(NodeRef nodeRef, JsonGenerator generator, QName assocName, RemoteJSONContext context) throws IOException {
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

            for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
                AssociationDefinition assocDef = entry.getValue();
                if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
                        && !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
                        && !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER)
                        && !assocDef.getName().equals(BeCPGModel.ASSOC_ENTITYLISTS) && assocDef.isChild()) {
                    QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
                    if (((params.getFilteredProperties() != null) && !params.getFilteredProperties().isEmpty()
                            && !params.getFilteredProperties().contains(nodeType))) {
                        continue;
                    }
                    List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
                    if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
                        generator.writeFieldName(entityDictionaryService.toPrefixString(nodeType));
                        generator.writeStartArray();
                    }
                    for (ChildAssociationRef assocRef : assocRefs) {
                        if (assocRef.getTypeQName().equals(assocDef.getName())) {
                            NodeRef childRef = assocRef.getChildRef();
                            if (assocDef.isTargetMany()) {
                                generator.writeStartObject();
                                visitNode(childRef, generator, JsonVisitNodeType.CHILD_ASSOC, null, context);
                                generator.writeEndObject();
                            } else {
                                generator.writeFieldName(entityDictionaryService.toPrefixString(nodeType));
                                generator.writeStartObject();
                                visitNode(childRef, generator, JsonVisitNodeType.CHILD_ASSOC, null, context);
                                generator.writeEndObject();
                            }
                        }
                    }
                    if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
                        generator.writeEndArray();
                    }
                }
            }

            for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
                AssociationDefinition assocDef = entry.getValue();
                if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
                        && !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
                        && !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER) && !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL)
                        && !assocDef.isChild() && params.shouldExtractField(assocDef.getName())) {
                    QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
                    if (!matchProp(assocName, nodeType, false)) {
                        continue;
                    }
                    List<NodeRef> assocRefs = associationService.getTargetAssocs(nodeRef, assocDef.getName());
                    if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
                        generator.writeFieldName(entityDictionaryService.toPrefixString(nodeType));
                        generator.writeStartArray();
                    }
                    for (NodeRef childRef : assocRefs) {
                        if (assocDef.isTargetMany()) {
                            generator.writeStartObject();
                            visitNode(childRef, generator, JsonVisitNodeType.ASSOC, nodeType, context);
                            generator.writeEndObject();
                        } else {
                            generator.writeFieldName(entityDictionaryService.toPrefixString(nodeType));
                            generator.writeStartObject();
                            visitNode(childRef, generator, JsonVisitNodeType.ASSOC, nodeType, context);
                            generator.writeEndObject();
                        }
                    }
                    if (assocDef.isTargetMany() && !assocRefs.isEmpty()) {
                        generator.writeEndArray();
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

    protected void visitProps(NodeRef nodeRef, JsonGenerator generator, QName assocName, Map<QName, Serializable> props, RemoteJSONContext context)
            throws IOException, RemoteException {
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
                        if (!matchProp(assocName, propName, false)) {
                            continue;
                        }
                        MLText mlValues = null;
                        if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
                                && (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)
                                && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT, Boolean.TRUE))) {
                            mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDefinition.getName());
                            visitMltextAttributes(entityDictionaryService.toPrefixString(propName), generator, mlValues);
                        } else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())
                                && !propertyDefinition.getConstraints().isEmpty() && !propertyDefinition.isMultiValued()
                                && Boolean.TRUE.equals(params.extractParams(RemoteParams.PARAM_APPEND_MLTEXT_CONSTRAINT, Boolean.TRUE))) {
                            for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
                                if (constraint.getConstraint() instanceof DynListConstraint) {
                                    mlValues = ((DynListConstraint) constraint.getConstraint()).getMLDisplayLabel((String) entry.getValue());
                                    visitMltextAttributes(entityDictionaryService.toPrefixString(propName), generator, mlValues);
                                    break;
                                }
                            }
                        }
                        visitPropValue(propName, generator, entry.getValue(), context);
                    } else {
                        logger.debug("Properties not in dictionary: " + entry.getKey());
                    }
                }
            }
        }
    }

    private void visitMltextAttributes(String propType, JsonGenerator generator, MLText mlValues) throws IOException {
        if (mlValues != null) {
            for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
                if (MLTextHelper.isSupportedLocale(mlEntry.getKey())) {
                    String code = MLTextHelper.localeKey(mlEntry.getKey());
                    if ((code != null) && !code.isBlank() && (mlEntry.getValue() != null)) {
                        generator.writeStringField(propType + "_" + code, mlEntry.getValue());
                    }
                }
            }
        }
    }

    private void visitSite(JsonGenerator generator, Path path) throws IOException {
        String siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));
        if (siteId != null) {
            generator.writeFieldName(RemoteEntityService.ATTR_SITE);
            generator.writeStartObject();
            generator.writeStringField(RemoteEntityService.ATTR_ID, siteId);
            SiteInfo site = siteService.getSite(siteId);
            if (site != null) {
                generator.writeStringField(RemoteEntityService.ATTR_NAME, site.getTitle());
            }
            generator.writeEndObject();
        }
    }

    @SuppressWarnings("unchecked")
    private void visitPropValue(QName propType, JsonGenerator generator, Serializable value, RemoteJSONContext context)
            throws IOException, RemoteException {
        if (value instanceof List) {
            generator.writeFieldName(entityDictionaryService.toPrefixString(propType));
            generator.writeStartArray();
            for (Serializable subEl : (List<Serializable>) value) {
                if (subEl instanceof NodeRef) {
                    if (nodeService.exists((NodeRef) subEl)) {
                        generator.writeStartObject();
                        visitNode((NodeRef) subEl, generator, JsonVisitNodeType.ASSOC, null, context);
                        generator.writeEndObject();
                    } else {
                        throw new RemoteException("node does not exist: " + subEl + ", for prop: " + propType);
                    }
                } else {
                    if (subEl != null) {
                        if (RemoteHelper.isJSONValue(propType)) {
                            try (JsonParser parser = jsonFactory.createParser((String) subEl)) {
                                generator.copyCurrentStructure(parser);
                            }
                        } else if (JsonHelper.formatValue(subEl) != null && !JsonHelper.formatValue(subEl).toString().isEmpty()) {
                            Object formattedValue = JsonHelper.formatValue(subEl);
                            if (formattedValue instanceof Number) {
                                generator.writeNumberField(null, ((Number) formattedValue).doubleValue());
                            } else if (formattedValue instanceof Boolean) {
                                generator.writeBooleanField(null, (Boolean) formattedValue);
                            } else {
                                generator.writeStringField(null, formattedValue.toString());
                            }
                        }
                    }
                }
            }
            generator.writeEndArray();
        } else if (value instanceof NodeRef) {
            if (nodeService.exists((NodeRef) value)) {
                generator.writeFieldName(entityDictionaryService.toPrefixString(propType));
                generator.writeStartObject();
                visitNode((NodeRef) value, generator, JsonVisitNodeType.ASSOC, null, context);
                generator.writeEndObject();
            } else {
                throw new IllegalStateException("node does not exist: " + value + ", for prop: " + propType);
            }
        } else {
            if (value != null) {
                if (RemoteHelper.isJSONValue(propType)) {
                    generator.writeFieldName(entityDictionaryService.toPrefixString(propType));
                    try (JsonParser parser = jsonFactory.createParser((String) value)) {
                        generator.copyCurrentStructure(parser);
                    }
                } else if (JsonHelper.formatValue(value) != null && !JsonHelper.formatValue(value).toString().isEmpty()) {
                    Object formattedValue = JsonHelper.formatValue(value);
                    generator.writeFieldName(entityDictionaryService.toPrefixString(propType));
                    if (formattedValue instanceof Number) {
                        generator.writeNumberField(null, ((Number) formattedValue).doubleValue());
                    } else if (formattedValue instanceof Boolean) {
                        generator.writeBooleanField(null, (Boolean) formattedValue);
                    } else {
                        generator.writeStringField(null, formattedValue.toString());
                    }
                }
            }
        }
    }
}