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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;

/**
 *
 * @author matthieu
 *
 */
public class XmlEntityVisitor extends AbstractEntityVisitor {

	private final AssociationService associationService;

	public XmlEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			EntityDictionaryService entityDictionaryService, ContentService contentService, SiteService siteService,
			 AssociationService associationService) {
		super(mlNodeService, nodeService, namespaceService, entityDictionaryService, contentService, siteService);
		this.associationService = associationService;
	}

	private static final Log logger = LogFactory.getLog(XmlEntityVisitor.class);

	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws XMLStreamException {

		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node
		visitNode(entityNodeRef, xmlw, true, true, false);
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	@Override
	public void visit(List<NodeRef> entities, OutputStream result) throws XMLStreamException {
		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node
		xmlw.writeStartElement(BeCPGModel.BECPG_PREFIX, RemoteEntityService.ELEM_ENTITIES, BeCPGModel.BECPG_URI);

		for (NodeRef nodeRef : entities) {
			if ((this.filteredProperties != null) && !this.filteredProperties.isEmpty()) {
				entityList = true;
				visitNode(nodeRef, xmlw, true, true, false);
			} else {
				visitNode(nodeRef, xmlw, false, false, false);
			}
		}

		xmlw.writeEndElement();
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws XMLStreamException {

		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node
		visitNode(entityNodeRef, xmlw, false, false, true);
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	private void visitNode(NodeRef nodeRef, XMLStreamWriter xmlw, boolean assocs, boolean props, boolean content) throws XMLStreamException {
		cacheList.add(nodeRef);

		extractLevel++;

		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);
		String prefix = nodeType.getPrefixString().split(":")[0];
		String name = (String) nodeService.getProperty(nodeRef, RemoteHelper.getPropName(nodeType, entityDictionaryService));
		// lists filter
		if (DataListModel.TYPE_DATALIST.equals(nodeType) && (filteredLists != null) && !filteredLists.isEmpty() && !filteredLists.contains(name)) {
			extractLevel--;
			return;
		}
		xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());
		boolean isCharact = false;

		if (light && entityDictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

			QName pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(nodeType);

			if (pivotAssoc != null) {
				NodeRef part = associationService.getTargetAssoc(nodeRef, pivotAssoc);
				if ((part != null)) {
					isCharact = true;
					writeStdAttributes(xmlw, part, nodeType, name, isCharact);
				}

			}

		}

		if (!isCharact) {
			writeStdAttributes(xmlw, nodeRef, nodeType, name, false);
		}

		// Assoc first
		if (assocs) {
			visitAssocs(nodeRef, xmlw);
		}

		if (props) {
			visitProps(nodeRef, xmlw);
		}

		if (content) {
			visitContent(nodeRef, xmlw);
		}

		xmlw.writeEndElement();
		extractLevel--;
	}

	private void writeStdAttributes(XMLStreamWriter xmlw, NodeRef nodeRef, QName nodeType, String name, boolean isCharact) throws XMLStreamException {
		Path path = null;

		if (nodeService.getPrimaryParent(nodeRef) != null) {
			NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			if (parentRef != null) {
				path = nodeService.getPath(parentRef);
				xmlw.writeAttribute(isCharact ? RemoteEntityService.CHARACT_ATTR_PATH : RemoteEntityService.ATTR_PATH,
						path.toPrefixString(namespaceService));
			}
		} else {
			logger.warn("Node : " + nodeRef + " has no primary parent");
		}

		xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.NODE_TYPE);

		if (name != null) {
			xmlw.writeAttribute(isCharact ? RemoteEntityService.CHARACT_ATTR_NAME : RemoteEntityService.ATTR_NAME, name);
		}
		xmlw.writeAttribute(isCharact ? RemoteEntityService.CHARACT_ATTR_NODEREF : RemoteEntityService.ATTR_NODEREF, nodeRef.toString());

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)) {
			if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE) != null) {
				xmlw.writeAttribute(isCharact ? RemoteEntityService.CHARACT_ATTR_CODE : RemoteEntityService.ATTR_CODE,
						(String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
			} else {
				logger.warn("Node : " + nodeRef + " has null becpg code");
			}

		}
		// erpCode
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ERP_CODE)) {
			if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_ERP_CODE) != null) {
				xmlw.writeAttribute(isCharact ? RemoteEntityService.CHARACT_ATTR_ERP_CODE : RemoteEntityService.ATTR_ERP_CODE,
						(String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_ERP_CODE));
			}
		}

		if ((path != null) && !isCharact) {
			visitSite(nodeRef, xmlw, path);
		}

	}

	private void visitContent(NodeRef nodeRef, XMLStreamWriter xmlw) throws XMLStreamException {

		xmlw.writeStartElement(BeCPGModel.BECPG_PREFIX, RemoteEntityService.ELEM_DATA, BeCPGModel.BECPG_URI);

		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (contentReader != null) {
			try (InputStream in = contentReader.getContentInputStream();
					Reader reader = new InputStreamReader(new Base64InputStream(in, true, -1, null))) {

				char[] buf = new char[4096];
				int n;
				while ((n = reader.read(buf)) >= 0) {
					xmlw.writeCharacters(buf, 0, n);
				}

			} catch (ContentIOException | IOException e) {
				throw new XMLStreamException("Cannot serialyze data");
			}
		}

		xmlw.writeEndElement();
	}

	private void visitAssocs(NodeRef nodeRef, XMLStreamWriter xmlw) throws XMLStreamException {

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
						&& assocDef.isChild()) {
					QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
					String prefix = nodeType.getPrefixString().split(":")[0];
					// fields & child assocs filter
					if (((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(nodeType)
							&& (extractLevel == 1) && !nodeType.equals(BeCPGModel.ASSOC_ENTITYLISTS))
							|| (nodeType.equals(BeCPGModel.ASSOC_ENTITYLISTS) && entityList && (extractLevel == 1))) {

						continue;
					}

					xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());
					xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.CHILD_ASSOC_TYPE);
					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {
							NodeRef childRef = assocRef.getChildRef();
							visitNode(childRef, xmlw, light ? false : true, light ? false : true, false);
						}
					}

					xmlw.writeEndElement();
				}

			}

			for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
				AssociationDefinition assocDef = entry.getValue();

				if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER) && !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL)
						&& !assocDef.isChild()) {
					QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
					String prefix = nodeType.getPrefixString().split(":")[0];
					// fields & assocs filter
					if ((filteredProperties != null) && !filteredProperties.isEmpty() && !filteredProperties.contains(nodeType)
							&& (extractLevel == 1)) {
						continue;
					}

					xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());

					xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.ASSOC_TYPE);
					List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
					for (AssociationRef assocRef : assocRefs) {
						NodeRef childRef = assocRef.getTargetRef();
						// extract assoc properties
						if (filteredAssocProperties.containsKey(nodeType)) {
							cachedAssocRef = Collections.singletonMap(childRef, filteredAssocProperties.get((nodeType)));
							visitNode(childRef, xmlw, shouldDumpAll(childRef), true, false);

						} else {
							visitNode(childRef, xmlw, shouldDumpAll(childRef), shouldDumpAll(childRef), false);
						}
						cachedAssocRef = null;
					}
					xmlw.writeEndElement();
				}

			}

		} else {
			logger.warn("No typeDef found for :" + nodeRef);
		}

	}

	private void visitProps(NodeRef nodeRef, XMLStreamWriter xmlw) throws XMLStreamException {

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
						String prefix = propName.getPrefixString().split(":")[0];
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

						xmlw.writeStartElement(prefix, propName.getLocalName(), propName.getNamespaceURI());
						xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE,
								propertyDefinition.getDataType().getName().toPrefixString(namespaceService));

						MLText mlValues = null;
						if (DataTypeDefinition.MLTEXT.equals(propertyDefinition.getDataType().getName())
								&& (mlNodeService.getProperty(nodeRef, propertyDefinition.getName()) instanceof MLText)) {
							mlValues = (MLText) mlNodeService.getProperty(nodeRef, propertyDefinition.getName());
							visitMltextAttributes(xmlw, mlValues);
						} else if (DataTypeDefinition.TEXT.equals(propertyDefinition.getDataType().getName())) {
							if (!propertyDefinition.getConstraints().isEmpty()) {
								for (ConstraintDefinition constraint : propertyDefinition.getConstraints()) {
									if (constraint.getConstraint() instanceof DynListConstraint) {
										mlValues = ((DynListConstraint) constraint.getConstraint()).getMLAwareAllowedValues().get(entry.getValue());
										visitMltextAttributes(xmlw, mlValues);
										break;
									}
								}
							}
						}
						cachedAssocRef = null;
						visitPropValue(entry.getValue(), xmlw);
						cachedAssocRef = tmpCachedAssocRef;
						xmlw.writeEndElement();

					} else {
						logger.debug("Properties not in dictionnary: " + entry.getKey());
					}

				}

			}
		}

	}

	private void visitMltextAttributes(XMLStreamWriter xmlw, MLText mlValues) throws XMLStreamException {
		if (mlValues != null) {
			for (Map.Entry<Locale, String> mlEntry : mlValues.entrySet()) {
				String code = MLTextHelper.localeKey(mlEntry.getKey());
				if ((code != null) && !code.isEmpty()) {
					xmlw.writeAttribute(code.replaceAll(":", "_"), writeCDATA(mlEntry.getValue()));
				}
			}
		}
	}

	private void visitSite(NodeRef nodeRef, XMLStreamWriter xmlw, Path path) throws XMLStreamException {

		String siteId = SiteHelper.extractSiteId(path.toPrefixString(namespaceService));

		if (siteId != null) {
			xmlw.writeStartElement("metadata", "siteId", path.toPrefixString(namespaceService));
			xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, "d:text");
			xmlw.writeCData(siteId);
			xmlw.writeEndElement();

			SiteInfo site = siteService.getSite(siteId);

			xmlw.writeStartElement("metadata", "siteName", path.toPrefixString(namespaceService));
			xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, "d:text");
			if (site != null) {
				xmlw.writeCData(site.getTitle());
			}
			xmlw.writeEndElement();
		}

	}

	@SuppressWarnings("unchecked")
	private void visitPropValue(Serializable value, XMLStreamWriter xmlw) throws XMLStreamException {
		if (value instanceof List) {
			xmlw.writeStartElement(RemoteEntityService.ELEM_LIST);
			for (Serializable subEl : (List<Serializable>) value) {
				xmlw.writeStartElement(RemoteEntityService.ELEM_LIST_VALUE);
				visitPropValue(subEl, xmlw);
				xmlw.writeEndElement();
			}
			xmlw.writeEndElement();
		} else if (value instanceof NodeRef) {
			visitNode((NodeRef) value, xmlw, shouldDumpAll((NodeRef) value), shouldDumpAll((NodeRef) value), false);
		} else if (value instanceof Date) {
			xmlw.writeCharacters(ISO8601DateFormat.format((Date) value));
		} else {
			if (value != null) {
				xmlw.writeCData(value.toString());
			}
		}
	}

}
