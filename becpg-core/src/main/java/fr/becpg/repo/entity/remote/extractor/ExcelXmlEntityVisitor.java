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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
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
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;

/**
 * <p>ExcelXmlEntityVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ExcelXmlEntityVisitor extends AbstractEntityVisitor {

	/**
	 * <p>Constructor for ExcelXmlEntityVisitor.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public ExcelXmlEntityVisitor(RemoteServiceRegisty remoteServiceRegisty) {
		super(remoteServiceRegisty);
	}

	private static final Log logger = LogFactory.getLog(ExcelXmlEntityVisitor.class);

	/** {@inheritDoc} */
	@Override
	public void visit(NodeRef entityNodeRef, OutputStream result) throws XMLStreamException {

		XMLStreamWriter xmlw = createWriter(result);
		// Visit node
		visitNode(entityNodeRef, null, xmlw, true, true, false);
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	/** {@inheritDoc} */
	@Override
	public void visit(PagingResults<NodeRef> entities, OutputStream result) throws XMLStreamException {

		XMLStreamWriter xmlw = createWriter(result);

		xmlw.writeStartElement(RemoteEntityService.ELEM_ENTITIES);

		for (NodeRef nodeRef : entities.getPage()) {
			visitNode(nodeRef, null, xmlw, false, false, false);
		}

		xmlw.writeEndElement();
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	/** {@inheritDoc} */
	@Override
	public void visitData(NodeRef entityNodeRef, OutputStream result) throws XMLStreamException {

		XMLStreamWriter xmlw = createWriter(result);
		// Visit node
		visitNode(entityNodeRef, null, xmlw, false, false, true);
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	private XMLStreamWriter createWriter(OutputStream result) throws XMLStreamException {
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
		return xmlw;
	}

	private void visitNode(NodeRef nodeRef, String name, XMLStreamWriter xmlw, boolean assocs, boolean props, boolean content)
			throws XMLStreamException {

		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);
		xmlw.writeStartElement(name != null ? name : getXmlName(nodeType));

		NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

		Path path = nodeService.getPath(parentRef);

		xmlw.writeAttribute(RemoteEntityService.ATTR_PATH, path.toPrefixString(namespaceService));
		xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.NODE_TYPE);

		xmlw.writeAttribute(RemoteEntityService.ATTR_NAME,
				(String) nodeService.getProperty(nodeRef, RemoteHelper.getPropName(nodeType, entityDictionaryService)));
		xmlw.writeAttribute(RemoteEntityService.ATTR_NODEREF, nodeRef.toString());

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)) {
			xmlw.writeAttribute(RemoteEntityService.ATTR_CODE, (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
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

		xmlw.writeStartElement(RemoteEntityService.ELEM_DATA);

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

		Map<QName, AssociationDefinition> assocs = new HashMap<>(entityDictionaryService.getType(nodeService.getType(nodeRef)).getAssociations());
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			assocs.putAll(entityDictionaryService.getAspect(aspect).getAssociations());
		}
		// First childs
		for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
			AssociationDefinition assocDef = entry.getValue();

			if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
					&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
					&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && assocDef.isChild()) {
				QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
				xmlw.writeStartElement(getXmlName(nodeType));
				xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.CHILD_ASSOC_TYPE);
				List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
				for (ChildAssociationRef assocRef : assocRefs) {
					if (assocRef.getTypeQName().equals(assocDef.getName())) {
						NodeRef childRef = assocRef.getChildRef();
						visitNode(childRef, null, xmlw, true, true, false);
					}
				}
				xmlw.writeEndElement();
			}
		}

		for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
			AssociationDefinition assocDef = entry.getValue();

			if (!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
					&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
					&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL) && !assocDef.isChild()) {
				QName nodeType = assocDef.getName().getPrefixedQName(namespaceService);
				xmlw.writeStartElement(getXmlName(nodeType));

				xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE, RemoteEntityService.ASSOC_TYPE);
				List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
				for (AssociationRef assocRef : assocRefs) {
					NodeRef childRef = assocRef.getTargetRef();
					visitNode(childRef, getXmlName(assocDef.getTargetClass().getName()), xmlw, false, true, false);
				}
				xmlw.writeEndElement();
			}

		}

	}

	private String getXmlName(QName nodeType) {
		return nodeType.toPrefixString(namespaceService).replace(":", "_");
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
						xmlw.writeStartElement(getXmlName(propName));
						xmlw.writeAttribute(RemoteEntityService.ATTR_TYPE,
								propertyDefinition.getDataType().getName().toPrefixString(namespaceService));
						visitPropValue(entry.getValue(), xmlw);
						xmlw.writeEndElement();
					} else {
						logger.warn("Properties not in dictionary: " + entry.getKey());
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
		} else if (value instanceof NodeRef node) {
			visitNode(node, null, xmlw, false, false, false);
		} else if (value instanceof Date date) {
			xmlw.writeCharacters(ISO8601DateFormat.format(date));
		} else {
			xmlw.writeCData(value.toString());
		}
	}

}
