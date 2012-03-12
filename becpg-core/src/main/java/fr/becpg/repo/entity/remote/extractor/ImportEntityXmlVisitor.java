package fr.becpg.repo.entity.remote.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class ImportEntityXmlVisitor {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private BeCPGSearchService beCPGSearchService;
	
	private EntityProviderCallBack entityProviderCallBack; 
	

	public void setEntityProviderCallBack(EntityProviderCallBack entityProviderCallBack) {
		this.entityProviderCallBack = entityProviderCallBack;
	}

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

			EntityXmlHandler handler = new EntityXmlHandler(entityNodeRef);
			saxParser.parse(in, handler);

			return handler.getCurNodeRef();
		} finally {
			IOUtils.closeQuietly(in);
		}

	}
	

	public Map<String, byte[]> visitData( InputStream in) throws IOException, SAXException, ParserConfigurationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			EntityDataXmlHandler handler = new EntityDataXmlHandler();
			saxParser.parse(in, handler);

			return handler.getDatas();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	private class EntityDataXmlHandler extends DefaultHandler {

		Map<String,byte[]> datas = new HashMap<String, byte[]>();

		StringBuffer currValue = new StringBuffer();
		
		String name;

		public Map<String, byte[]> getDatas() {
			return datas;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			currValue = new StringBuffer();
			name = attributes.getValue("name");
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currValue.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equals("becpg:image")){
				datas.put(name, Base64.decodeBase64(currValue.toString()));
			}
		}
		
	}
	


	private class EntityXmlHandler extends DefaultHandler {

		NodeRef entityNodeRef = null;

		Stack<NodeRef> curNodeRef = new Stack<NodeRef>();

		StringBuffer currValue = new StringBuffer();

		Stack<String> typeStack = new Stack<String>();

		Stack<QName> currAssoc = new Stack<QName>();

		Stack<String> currAssocType = new Stack<String>();

		QName currProp = null;

		QName nodeType = null;

		public EntityXmlHandler(NodeRef entityNodeRef) {
			this.entityNodeRef = entityNodeRef;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			String type = attributes.getValue("type");
			typeStack.push(type);

			if (type != null && type.equals("node")) {
				String path = attributes.getValue("path");
				String name = attributes.getValue("name");
				String nodeRef = attributes.getValue("nodeRef");

				if (entityNodeRef != null && curNodeRef.isEmpty()) {
					logger.debug("We force node update by providing nodeRef");
					nodeRef = entityNodeRef.toString();
				}
				String code = attributes.getValue("code");

				nodeType = QName.createQName(qName, namespaceService);

				NodeRef node = null;
				if (currAssocType.isEmpty() || !currAssocType.peek().equals("childAssoc")) {
					node = findNode(nodeRef, code, name, nodeType);
				}
				// Entity node
				if (curNodeRef.isEmpty()) {

					if (node == null) {
						logger.debug("Add entity main node");
						node = createNode(path, nodeType, name);
					} else {
						logger.debug("Update entity main node");
					}
					curNodeRef.push(node);
				} else {
					if (!currAssoc.isEmpty() && !currAssocType.isEmpty()) {
						if (currAssocType.peek().equals("childAssoc")) {

							curNodeRef.push(createAssocNode(curNodeRef.peek(), nodeType, currAssoc.peek(), name));
						} else {
							if(node == null && entityProviderCallBack!=null){
								logger.debug("Node not found calling provider");
								try {
								 node = entityProviderCallBack.provideNode(new NodeRef(nodeRef));
								} catch (BeCPGException e) {
									throw new SAXException("Cannot call entityProviderCallBack ",e);
								}
							}
							 if (node != null) {
								logger.debug("Node found creating assoc: " + currAssoc.peek());
								nodeService.createAssociation(curNodeRef.peek(), node, currAssoc.peek());
								curNodeRef.push(node);
							 } else {
								throw new SAXException("Cannot add node to assoc, node not found : " + name);
							 }
						}

					} else {
						logger.error("Not in current assoc node");
					}
				}
			} else if (type != null && (type.equals("assoc") || type.equals("childAssoc"))) {
				currAssoc.push(QName.createQName(qName, namespaceService));
				currAssocType.push(type);
				removeAllExistingAssoc(curNodeRef.peek(), currAssoc.peek(), type);
			} else if (type != null && type.length() > 0) {
				currProp = QName.createQName(qName, namespaceService);
			}

			currValue = new StringBuffer();

		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currValue.append(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			String type = typeStack.pop();
			if (type != null && type.equals("node")) {
				entityNodeRef = curNodeRef.pop();
			} else if (type != null && (type.equals("assoc") || type.equals("childAssoc"))) {
				currAssoc.pop();
				currAssocType.pop();
			} else if (type != null && type.length() > 0) {
				//logger.debug("Set property : " + currProp + " value " + currValue);
				if (currValue.length() > 0) {
					nodeService.setProperty(curNodeRef.peek(), currProp, currValue.toString());
				} else {
					nodeService.setProperty(curNodeRef.peek(), currProp, null);
				}
			}
		}

		public NodeRef getCurNodeRef() {
			return entityNodeRef;
		}

	};

	private void removeAllExistingAssoc(NodeRef nodeRef, QName assocName, String type) {
		if (type.equals("childAssoc")) {
			for (ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
				if (assoc.getQName().equals(assocName)) {
					nodeService.deleteNode(assoc.getChildRef());
				}
			}
		} else {
			for (AssociationRef assoc : nodeService.getTargetAssocs(nodeRef, assocName)) {
				nodeService.removeAssociation(nodeRef, assoc.getTargetRef(), assoc.getTypeQName());
			}
		}
	}

	private NodeRef createNode(String parentPath, QName type, String name) throws SAXException {
		NodeRef parentNodeRef = findNodeByPath(parentPath);
	
		if (parentNodeRef != null) {
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, name);

			return nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, type, properties).getChildRef();
		}
		throw new SAXException("Path doesn't exist on repository :"+parentPath);
	}

	private NodeRef createAssocNode(NodeRef parentNodeRef, QName type, QName assocName, String name) {
		logger.debug("Creating child assoc: " + assocName + " add type :" + type);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, name);
		return nodeService.createNode(parentNodeRef, assocName,
				assocName, type, properties).getChildRef();
	}

	private NodeRef findNodeByPath(String parentPath) {
		String runnedQuery = "+PATH:\"" + parentPath + "\"";
		List<NodeRef> ret = beCPGSearchService.luceneSearch(runnedQuery, 1);
		if (ret.size() > 0) {
			logger.debug("Found node for query :" + runnedQuery);
		} else {
		
			 ret = beCPGSearchService.luceneSearch(RepoConsts.PATH_QUERY_IMPORT_TO_DO, 1);
		}
		
		return ret.get(0);
		
	}

	private NodeRef findNode(String nodeRef, String code, String name, QName type) {
		if (nodeRef != null && nodeService.exists(new NodeRef(nodeRef))) {
			return new NodeRef(nodeRef);
		}

		String runnedQuery = "";
		
		

		if (type != null) {
			runnedQuery += " +TYPE:\"" + type.toString() + "\" ";
		}

		

		if (code != null && code.length() > 0) {
			runnedQuery += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_CODE, code, null);
			List<NodeRef> ret = beCPGSearchService.luceneSearch(runnedQuery, 1);
			if (ret.size() > 0) {
				logger.debug("Found node for query :" + runnedQuery);
				return ret.get(0);
			}
		}
		
		

		if (name != null && name.length() > 0) {
			runnedQuery += LuceneHelper.getCondEqualValue(ContentModel.PROP_NAME, name, code != null && code.length() > 0 ? LuceneHelper.Operator.OR : null);
			List<NodeRef> ret = beCPGSearchService.luceneSearch(runnedQuery, 1);
			if (ret.size() > 0) {
				logger.debug("Found node for query :" + runnedQuery);
				return ret.get(0);
			}
		}
		
		

		return null;
	}

}
