package fr.becpg.repo.entity.remote;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;

@Service("remoteSchemaGenerator")
public class RemoteSchemaGenerator {

	private static Log logger = LogFactory.getLog(RemoteSchemaGenerator.class);

	@Autowired
	private DictionaryService dictionaryService;

	public void generateSchema(OutputStream out) throws XMLStreamException {

		// Create an output factory
		XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
		xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		// Create an XML stream writer
		XMLStreamWriter xmlw = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(out));

		// Write XML prologue
		xmlw.writeStartDocument();
		// Visit node

		createXSD(xmlw);

		// Write document end. This closes all open structures
		xmlw.writeEndDocument();
		// Close the writer to flush the output
		xmlw.close();

	}

	private void createXSD(XMLStreamWriter xmlw) throws XMLStreamException {

		xmlw.writeStartElement("xs", "schema", "http://www.w3.org/2001/XMLSchema");

		// xmlw.writeAttribute("targetNamespace", BeCPGModel.BECPG_URI);
		// xmlw.writeNamespace(BeCPGModel.BECPG_PREFIX, BeCPGModel.BECPG_URI);
		// xmlw.writeNamespace("sys",
		// "http://www.alfresco.org/model/system/1.0");
		// xmlw.writeNamespace("cm",
		// "http://www.alfresco.org/model/content/1.0");
		// xmlns:sys=""
		createBecpgBase(xmlw);

		Set<QName> cache = new HashSet<>();

		for (QName type : dictionaryService.getTypes(BeCPGModel.MODEL)) {
			createType(type, cache, xmlw);
		}

		for (QName type : dictionaryService.getTypes(ReportModel.MODEL)) {
			createType(type, cache, xmlw);
		}

		// }
		// <xsd:complexContent>
		// <xsd:extension base="type_de_base">
		// <!-- détail de l'extension -->
		// </xsd:restriction>
		// </xsd:complexContent>

		xmlw.writeEndElement();

	}

	private void createType(QName type, Set<QName> cache, XMLStreamWriter xmlw) throws XMLStreamException {

		if (!cache.contains(type)) {
			cache.add(type);

			if (dictionaryService.getClass(type) != null) {

				// if (BeCPGModel.BECPG_URI.equals(type.getNamespaceURI())) {
				QName parent = dictionaryService.getClass(type).getParentName();
				if (parent != null) {
					createType(parent, cache, xmlw);
				}

				logger.error("Create type :" + type.toPrefixString());
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexType");
				xmlw.writeAttribute("name", type.getPrefixString().replace(":", "_") + "_type");
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexContent");
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "extension");
				if (parent != null) {
					xmlw.writeAttribute("base", parent.toPrefixString().replace(":", "_") + "_type");
				} else {
					xmlw.writeAttribute("base", "xmlType");
				}

				boolean first = true;

				first = appendProperties(type, xmlw, first);
				first = appendAssocs(type, xmlw, first);

				first = appendAspects(type, xmlw, first);

				if (!first) {
					xmlw.writeEndElement();
				}
				xmlw.writeEndElement();
				xmlw.writeEndElement();
				xmlw.writeEndElement();

				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "element");
				xmlw.writeAttribute("name", type.getPrefixString().replace(":", "_"));
				xmlw.writeAttribute("type", type.getPrefixString().replace(":", "_") + "_type");
				xmlw.writeEndElement();

			}
			// }

		}

	}

	private boolean appendAspects(QName dataType, XMLStreamWriter xmlw, boolean first) throws XMLStreamException {
		for (AspectDefinition aspectDefinition : dictionaryService.getClass(dataType).getDefaultAspects(false)) {

			first = appendAssocs(null, aspectDefinition.getAssociations().keySet(), xmlw, first);
			first = appendProperties(null, aspectDefinition.getProperties().keySet(), xmlw, first);

		}

		return first;
	}

	private boolean appendProperties(QName dataType, Set<QName> keySet, XMLStreamWriter xmlw, boolean first) throws XMLStreamException {
		for (QName type : keySet) {

			PropertyDefinition propertyDefinition = dictionaryService.getProperty(type);
			if (dataType == null || propertyDefinition.getContainerClass().getName().equals(dataType)) {

				if (!type.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)
						&& !type.getNamespaceURI().equals(NamespaceService.RENDITION_MODEL_1_0_URI)
						&& !type.getNamespaceURI().equals(ReportModel.REPORT_URI) && !type.equals(ContentModel.PROP_CONTENT)) {

					if (first) {
						xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "choice");
						xmlw.writeAttribute("maxOccurs","unbounded");
						first = false;
					}

					xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "element");
					xmlw.writeAttribute("name", propertyDefinition.getName().getPrefixString().replace(":", "_"));
					if (propertyDefinition.isMandatoryEnforced()) {
						xmlw.writeAttribute("minOccurs", "1");
					}
					xmlw.writeAttribute("type", "xmlBase");
					// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
					// "complexType");
					// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
					// "complexContent");
					// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
					// "extension");
					// xmlw.writeAttribute("base", "xmlBase");
					// xmlw.writeEndElement();
					// xmlw.writeEndElement();
					// xmlw.writeEndElement();
					xmlw.writeEndElement();
				}
			}

		}

		return first;

	}

	private boolean appendAssocs(QName dataType, Set<QName> keySet, XMLStreamWriter xmlw, boolean first) throws XMLStreamException {
		for (QName assocQName : keySet) {
			AssociationDefinition assocDef = dictionaryService.getAssociation(assocQName);
			if ((dataType == null || dictionaryService.isSubClass(assocDef.getSourceClass().getName(), dataType))) {
				if (first) {
					xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "choice");
					xmlw.writeAttribute("maxOccurs","unbounded");
					first = false;
				}
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "element");
				xmlw.writeAttribute("name", assocDef.getName().getPrefixString().replace(":", "_"));
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexType");
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexContent");
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "extension");
				xmlw.writeAttribute("base", "xmlBase");

				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "sequence");
				xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "element");
				xmlw.writeAttribute("ref", assocDef.getTargetClass().getName().getPrefixString().replace(":", "_"));
				xmlw.writeEndElement();
				xmlw.writeEndElement();
				xmlw.writeEndElement();
				xmlw.writeEndElement();

				xmlw.writeEndElement();
				xmlw.writeEndElement();

			}
		}

		// for (QName assocQName : keySet) {
		// AssociationDefinition assocDef =
		// dictionaryService.getAssociation(assocQName);
		// if (dataType == null ||
		// dictionaryService.isSubClass(assocDef.getSourceClass().getName(),
		// dataType)) {
		// if(first){
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "sequence");
		// first = false ;
		// }
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "element");
		// xmlw.writeAttribute("name",
		// assocDef.getName().getPrefixString().replace(":", "_"));
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "complexType");
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "sequence");
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "extension");
		// xmlw.writeAttribute("base", "xmlBase");
		//
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "element");
		// xmlw.writeAttribute("ref",
		// assocDef.getTargetClass().getName().getPrefixString().replace(":",
		// "_"));
		// xmlw.writeEndElement();
		//
		// xmlw.writeEndElement();
		// xmlw.writeEndElement();
		// xmlw.writeEndElement();
		// xmlw.writeEndElement();
		//
		// }
		// }
		return first;
	}

	private boolean appendAssocs(QName dataType, XMLStreamWriter xmlw, boolean first) throws XMLStreamException {
		return appendAssocs(dataType, dictionaryService.getClass(dataType).getAssociations().keySet(), xmlw, first);

	}

	private boolean appendProperties(QName dataType, XMLStreamWriter xmlw, boolean first) throws XMLStreamException {
		return appendProperties(dataType, dictionaryService.getClass(dataType).getProperties().keySet(), xmlw, first);

	}

	private void createBecpgBase(XMLStreamWriter xmlw) throws XMLStreamException {
		xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexType");
		xmlw.writeAttribute("name", "xmlBase");
		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "complexContent");

		// xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema",
		// "extension");

		// xmlw.writeAttribute("base", "xs:string");

		addAttribute(RemoteEntityService.ATTR_TYPE, xmlw);

		// xmlw.writeEndElement();
		// xmlw.writeEndElement();
		xmlw.writeEndElement();

		xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexType");
		xmlw.writeAttribute("name", "xmlType");
		xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "complexContent");
		xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "extension");

		xmlw.writeAttribute("base", "xmlBase");

		addAttribute(RemoteEntityService.ATTR_PATH, xmlw);
		addAttribute(RemoteEntityService.ATTR_NAME, xmlw);
		addAttribute(RemoteEntityService.ATTR_NODEREF, xmlw);
		addAttribute(RemoteEntityService.ATTR_CODE, xmlw);

		xmlw.writeEndElement();
		xmlw.writeEndElement();
		xmlw.writeEndElement();
	}

	private void addAttribute(String name, XMLStreamWriter xmlw) throws XMLStreamException {
		xmlw.writeStartElement("http://www.w3.org/2001/XMLSchema", "attribute");
		xmlw.writeAttribute("type", "xs:string");
		xmlw.writeAttribute("name", name);
		xmlw.writeAttribute("use", "optional");
		xmlw.writeEndElement();
	}

}
