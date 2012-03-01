package fr.becpg.repo.entity.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class ImportEntityXmlVisitor {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private BeCPGSearchService beCPGSearchService;

	private static Log logger = LogFactory.getLog(ImportEntityXmlVisitor.class);

	
	public ImportEntityXmlVisitor(NodeService nodeService, NamespaceService namespaceService, BeCPGSearchService beCPGSearchService) {
		super();
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.beCPGSearchService = beCPGSearchService;
	}

	public NodeRef visit(NodeRef entityNodeRef, InputStream in) throws IOException, SAXException, ParserConfigurationException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(in, new EntityXmlHandler(entityNodeRef));
			//todo
			return entityNodeRef;
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	private class EntityXmlHandler extends DefaultHandler {

		//TODO
		NodeRef entityNodeRef =null;
		
		NodeRef curNodeRef = null;

		StringBuffer currValue = new StringBuffer();

		String type = "";

		QName currAssoc = null;

		QName currProp = null;

		QName nodeType = null;

		public EntityXmlHandler(NodeRef entityNodeRef) {
			this.entityNodeRef  = entityNodeRef;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			type = attributes.getValue("type");
			if (type == "node") {
				String path = attributes.getValue("path");
				String name = attributes.getValue("name");
				String nodeRef = attributes.getValue("nodeRef");
				String code = attributes.getValue("code");
				nodeType = QName.createQName(localName, namespaceService);

				NodeRef node = findNode(nodeRef, code, name, nodeType);
				// Entity node
				if (curNodeRef == null) {
					if (node == null) {
						node = createNode(path, name, nodeType);
					}
					curNodeRef = node;
				} else {
					if (currAssoc != null) {
						if (node != null) {
							nodeService.createAssociation(curNodeRef, node, currAssoc);
							curNodeRef = node;
						}
						logger.warn("Cannot add node to assoc, node not found : " + name);
					} else {
						logger.error("Not in current assoc node");
					}
				}
			} else if (type == "assoc") {

				currAssoc = QName.createQName(localName, namespaceService);
			} else {
				currProp = QName.createQName(localName, namespaceService);
			}

			currValue = new StringBuffer();

		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currValue.append(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (type == "node") {
				// Do nothing
			} else if (type == "assoc") {
				curNodeRef = nodeService.getPrimaryParent(curNodeRef).getParentRef();
			} else {
				nodeService.setProperty(curNodeRef, currProp, ImportHelper.loadPropertyValue(currValue, type));
			}

		}

	};

	private NodeRef createNode(String parentPath, String name, QName type) {
		NodeRef parentNodeRef = findNodeByPath(parentPath);
		if(parentNodeRef!=null){
			return nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, type).getChildRef();
		}
		return null;
	}

	private NodeRef findNodeByPath(String parentPath) {
		String runnedQuery = "+PATH:" + parentPath;
		List<NodeRef> ret = beCPGSearchService.luceneSearch(runnedQuery, 1);
		if (ret.size() > 0) {
			return ret.get(0);
		}

		return null;
	}

	private NodeRef findNode(String nodeRef, String code, String name, QName type) {
		if (nodeRef != null && nodeService.exists(new NodeRef(nodeRef))) {
			return new NodeRef(nodeRef);
		}

		String runnedQuery = "";

		if (code != null && code.length() > 0) {
			runnedQuery += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_CODE, code, LuceneHelper.Operator.AND);
		}

		if (name != null && name.length() > 0) {
			runnedQuery += LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, name, LuceneHelper.Operator.AND);
		}

		if (runnedQuery.length() > 0) {

			List<NodeRef> ret = beCPGSearchService.luceneSearch(runnedQuery, 1);
			if (ret.size() > 0) {
				return ret.get(0);
			}
		}

		return null;
	}

}
