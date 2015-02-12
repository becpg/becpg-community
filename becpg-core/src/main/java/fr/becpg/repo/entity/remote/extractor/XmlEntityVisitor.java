/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.remote.RemoteEntityService;

/**
 * 
 * @author matthieu
 * 
 */
public class XmlEntityVisitor {

	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
	private ContentService contentService;

	private boolean dumpAll = false;

	private Set<NodeRef> cacheList = new HashSet<NodeRef>();

	public void setDumpAll(boolean dumpAll) {
		this.dumpAll = dumpAll;
	}

	private static Log logger = LogFactory.getLog(XmlEntityVisitor.class);

	public XmlEntityVisitor(NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService,
			ContentService contentService) {
		super();
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.dictionaryService = dictionaryService;
		this.contentService = contentService;
	}

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

		for (Iterator<NodeRef> iterator = entities.iterator(); iterator.hasNext();) {
			NodeRef nodeRef = (NodeRef) iterator.next();
			visitNode(nodeRef, xmlw, false, false, false);
		}

		xmlw.writeEndElement();
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

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

		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);
		String prefix = nodeType.getPrefixString().split(":")[0];
		xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());

		if (nodeService.getPrimaryParent(nodeRef) != null) {
			NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
			if (parentRef != null) {
				Path path = nodeService.getPath(parentRef);

				xmlw.writeAttribute(RemoteEntityService.ATTR_PATH, path.toPrefixString(namespaceService));
			}
		} else {
			logger.warn("Node : "+nodeRef+ " has no primary parent");
		}
		
		
		xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.NODE_TYPE);

		String name = (String) nodeService.getProperty(nodeRef, RemoteHelper.getPropName(nodeType));

		xmlw.writeAttribute(RemoteEntityService.ATTR_NAME, name);
		xmlw.writeAttribute(RemoteEntityService.ATTR_NODEREF, nodeRef.toString());

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)) {
			if(nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE)!=null){
				xmlw.writeAttribute(RemoteEntityService.ATTR_CODE, (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
			} else {
				logger.warn("Node : "+nodeRef+ " has null becpg code");
			}
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

		Map<QName, AssociationDefinition> assocs = new HashMap<QName, AssociationDefinition>(dictionaryService.getType(nodeService.getType(nodeRef))
				.getAssociations());
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			if(dictionaryService.getAspect(aspect)!=null){
				assocs.putAll(dictionaryService.getAspect(aspect).getAssociations());
			} else {
				logger.warn("No definition for :"+aspect);
			}
		}
		// First childs
		for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
			AssociationDefinition assocDef = entry.getValue();

			if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
					&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
					&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) 
					&& !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER)
					&& assocDef.isChild()) {
				QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
				String prefix = nodeType.getPrefixString().split(":")[0];
				xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());
				xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.CHILD_ASSOC_TYPE);
				List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
				for (ChildAssociationRef assocRef : assocRefs) {
					if (assocRef.getTypeQName().equals(assocDef.getName())) {
						NodeRef childRef = assocRef.getChildRef();
						visitNode(childRef, xmlw, true, true, false);
					}
				}
				xmlw.writeEndElement();
			}

		}

		for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
			AssociationDefinition assocDef = entry.getValue();

			if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
					&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
					&& !assocDef.getName().equals(RuleModel.ASSOC_RULE_FOLDER)
					&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && !assocDef.isChild()) {
				QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
				String prefix = nodeType.getPrefixString().split(":")[0];
				xmlw.writeStartElement(prefix, nodeType.getLocalName(), nodeType.getNamespaceURI());

				xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.ASSOC_TYPE);
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
				for (AssociationRef assocRef : assocRefs) {
					NodeRef childRef = assocRef.getTargetRef();
					visitNode(childRef, xmlw, shouldDumpAll(childRef), shouldDumpAll(childRef), false);
				}
				xmlw.writeEndElement();
			}

		}

	}

	private void visitProps(NodeRef nodeRef, XMLStreamWriter xmlw) throws XMLStreamException {

		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
		if (props != null) {
			for (Map.Entry<QName, Serializable> entry : props.entrySet()) {
				QName propQName = entry.getKey();
				if (entry.getValue() != null && !propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(ReportModel.REPORT_URI) && !propQName.equals(ContentModel.PROP_CONTENT)) {
					PropertyDefinition propertyDefinition = dictionaryService.getProperty(entry.getKey());
					if (propertyDefinition != null) {
						QName propName = entry.getKey().getPrefixedQName(namespaceService);
						String prefix = propName.getPrefixString().split(":")[0];
						xmlw.writeStartElement(prefix, propName.getLocalName(), propName.getNamespaceURI());
						xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, propertyDefinition.getDataType().getName()
								.toPrefixString(namespaceService));
						visitPropValue(entry.getValue(), xmlw);
						xmlw.writeEndElement();
					} else {
						logger.warn("Properties not in dictionnary: " + entry.getKey());
					}

				}

			}
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
			// xmlw.writeCharacters(cleanInvalidXmlChars(value.toString(),""));
			xmlw.writeCData(value.toString());
		}
	}

	private boolean shouldDumpAll(NodeRef nodeRef) {
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		return dumpAll
				&& !cacheList.contains(nodeRef)
				&& !(ContentModel.TYPE_AUTHORITY.equals(nodeType) || ContentModel.TYPE_PERSON.equals(nodeType) || ContentModel.TYPE_AUTHORITY_CONTAINER
						.equals(nodeType));
	}

	//
	// /**
	// * From xml spec valid chars:<br>
	// * #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
	// [#x10000-#x10FFFF]<br>
	// * any Unicode character, excluding the surrogate blocks, FFFE, and
	// FFFF.<br>
	// * @param text The String to clean
	// * @param replacement The string to be substituted for each match
	// * @return The resulting String
	// */
	// private String cleanInvalidXmlChars(String text, String replacement) {
	// String re =
	// "[^\\x09\\x0A\\x0D\\x20-\\xD7FF\\xE000-\\xFFFD\\x10000-x10FFFF]";
	// return text.replaceAll(re, replacement);
	// }
	//

}
