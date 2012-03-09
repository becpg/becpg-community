package fr.becpg.repo.entity.remote.extractor;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;

/**
 * 
 * @author matthieu
 * 
 */
public class XmlEntityVisitor {

	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;

	private static Log logger = LogFactory.getLog(XmlEntityVisitor.class);

	public XmlEntityVisitor(NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService) {
		super();
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.dictionaryService = dictionaryService;
	}

	public void visit(NodeRef entityNodeRef, OutputStream result) throws XMLStreamException {

		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();

		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node
		visitNode(entityNodeRef, xmlw, true, true);
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}
	
	

	public void visit(List<NodeRef> entities, OutputStream result) throws XMLStreamException {
		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();

		// Create an XML stream writer
		XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

		if (logger.isDebugEnabled()) {
			logger.debug("Indent xml formater ON");
			xmlw = new IndentingXMLStreamWriter(xmlw);
		}

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node
		xmlw.writeStartElement("becpg:entities");
		
		for (Iterator<NodeRef> iterator = entities.iterator(); iterator.hasNext();) {
			NodeRef nodeRef = (NodeRef) iterator.next();
			visitNode(nodeRef, xmlw, false, false);
		}
		
		xmlw.writeEndElement();
		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();
		
	}
	

	public void visit(NodeRef entityNodeRef, Map<String, byte[]> images, OutputStream result) throws XMLStreamException {
		// Create an output factory
				XMLOutputFactory xmlof = XMLOutputFactory.newInstance();

				// Create an XML stream writer
				XMLStreamWriter xmlw = xmlof.createXMLStreamWriter(result);

				if (logger.isDebugEnabled()) {
					logger.debug("Indent xml formater ON");
					xmlw = new IndentingXMLStreamWriter(xmlw);
				}

				// Write XML prologue
				xmlw.writeStartDocument();
				// Visit node
				xmlw.writeStartElement("becpg:data");
				
				NodeRef parentRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
				
				if(nodeService.getType(parentRef).equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
					 parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
				}
				
				Path path = nodeService.getPath(parentRef);

				xmlw.writeAttribute("path", path.toPrefixString(namespaceService));
				xmlw.writeAttribute("type", "node");

				String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

				xmlw.writeAttribute("name", name);
				xmlw.writeAttribute("nodeRef", entityNodeRef.toString());

				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_CODE)) {
					xmlw.writeAttribute("code", (String) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE));
				}
				
				for (Entry<String, byte[]> image : images.entrySet() ) {
					xmlw.writeStartElement("becpg:image");
					xmlw.writeAttribute("name", image.getKey());
					xmlw.writeCData(Base64.encodeBase64String(image.getValue()));
					xmlw.writeEndElement();
				}
				
				
				xmlw.writeEndElement();
				// Write document end. This closes all open structures
				xmlw.writeEndDocument();
				// Close the writer to flush the output
				xmlw.close();
		
	}

	
	

	private void visitNode(NodeRef nodeRef, XMLStreamWriter xmlw, boolean assocs, boolean props) throws XMLStreamException {

		QName nodeType = nodeService.getType(nodeRef);

		xmlw.writeStartElement(nodeType.toPrefixString(namespaceService));

		NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
		
		if(nodeService.getType(parentRef).equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
			 parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
		}
		
		Path path = nodeService.getPath(parentRef);

		xmlw.writeAttribute("path", path.toPrefixString(namespaceService));
		xmlw.writeAttribute("type", "node");

		String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

		xmlw.writeAttribute("name", name);
		xmlw.writeAttribute("nodeRef", nodeRef.toString());

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_CODE)) {
			xmlw.writeAttribute("code", (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE));
		}

		if (props) {
			visitProps(nodeRef, xmlw);
		}
		if (assocs) {
			visitAssocs(nodeRef, xmlw);

		}

		xmlw.writeEndElement();

	}

	private void visitAssocs(NodeRef nodeRef, XMLStreamWriter xmlw) throws XMLStreamException {

		Map<QName, AssociationDefinition> assocs = new HashMap<QName, AssociationDefinition>(dictionaryService.getType(nodeService.getType(nodeRef)).getAssociations());
		for (QName aspect : nodeService.getAspects(nodeRef)) {
			assocs.putAll(dictionaryService.getAspect(aspect).getAssociations());
		}

		for (Map.Entry<QName, AssociationDefinition> entry : assocs.entrySet()) {
			AssociationDefinition assocDef = entry.getValue();

			if(!assocDef.getName().getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
				&& !assocDef.getName().getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
				&& !assocDef.getName().equals(ContentModel.ASSOC_ORIGINAL)){
				xmlw.writeStartElement(assocDef.getName().toPrefixString(namespaceService));
			
			
				if(assocDef.isChild()){
					xmlw.writeAttribute("type","childAssoc" );
					List<ChildAssociationRef> assocRefs = nodeService.getChildAssocs(nodeRef);
					for (ChildAssociationRef assocRef : assocRefs) {
						if (assocRef.getTypeQName().equals(assocDef.getName())) {
							NodeRef childRef = assocRef.getChildRef();
							visitNode(childRef, xmlw, true, true);
						}
					}
				} else {
					xmlw.writeAttribute("type","assoc" );
					List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
					for (AssociationRef assocRef : assocRefs) {
							NodeRef childRef = assocRef.getTargetRef();
							visitNode(childRef, xmlw, false, false);
					}
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
				if (entry.getValue() != null 
						&& !propQName.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !propQName.getNamespaceURI().equals(ReportModel.REPORT_URI)
						&& !propQName.equals(ContentModel.PROP_CONTENT)) {
					logger.debug("Extract prop : "+entry.getKey());
					PropertyDefinition propertyDefinition = dictionaryService.getProperty(entry.getKey());
					if(propertyDefinition!=null){
						xmlw.writeStartElement(entry.getKey().toPrefixString(namespaceService));
						xmlw.writeAttribute("type",propertyDefinition.getDataType().getName().toPrefixString(namespaceService) );
						visitPropValue(entry.getValue(), xmlw);
						xmlw.writeEndElement();
					} else {
						logger.warn("Properties not in dictionnary: "+entry.getKey());
					}
					
				}

			}
		}

	}

	private void visitPropValue(Serializable value, XMLStreamWriter xmlw) throws XMLStreamException {
		if (value instanceof NodeRef) {
			visitNode((NodeRef) value, xmlw, false, false);
		} if( value instanceof Date) {
			xmlw.writeCharacters(ISO8601DateFormat.format((Date)value));
		} else {
			xmlw.writeCharacters(value.toString());
		}
	}


}
