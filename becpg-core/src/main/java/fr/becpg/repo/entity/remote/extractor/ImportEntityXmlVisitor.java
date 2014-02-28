/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author matthieu
 * 
 */
public class ImportEntityXmlVisitor {

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private EntityProviderCallBack entityProviderCallBack;
	
	public void setEntityProviderCallBack(EntityProviderCallBack entityProviderCallBack) {
		this.entityProviderCallBack = entityProviderCallBack;
	}

	private static Log logger = LogFactory.getLog(ImportEntityXmlVisitor.class);

	public ImportEntityXmlVisitor(NodeService nodeService, NamespaceService namespaceService) {
		super();
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
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

	public Map<String, byte[]> visitData(InputStream in) throws IOException, SAXException, ParserConfigurationException {
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

		private Map<String, byte[]> datas = new HashMap<String, byte[]>();

		private StringBuffer currValue = new StringBuffer();

		private String name;

		public Map<String, byte[]> getDatas() {
			return datas;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			currValue = new StringBuffer();
			name = attributes.getValue(RemoteEntityService.ATTR_NAME);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currValue.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals(BeCPGModel.BECPG_PREFIX + ":" +RemoteEntityService.ELEM_IMAGE)) {
				datas.put(name, Base64.decodeBase64(currValue.toString()));
			}
		}

	}

	private class EntityXmlHandler extends DefaultHandler {

		private NodeRef entityNodeRef = null;

		private Stack<NodeRef> curNodeRef = new Stack<NodeRef>();

		private StringBuffer currValue = new StringBuffer();

		private Stack<String> typeStack = new Stack<String>();

		private Stack<QName> currAssoc = new Stack<QName>();

		private Stack<String> currAssocType = new Stack<String>();

		private boolean isNodeRefAssoc = false;

		private QName currProp = null;

		private QName nodeType = null;

		public EntityXmlHandler(NodeRef entityNodeRef) {
			this.entityNodeRef = entityNodeRef;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			String type = attributes.getValue(RemoteEntityService.ATTR_TYPE);
			typeStack.push(type);

			if (type != null && type.equals(RemoteEntityService.NODE_TYPE)) {
				String path = attributes.getValue(RemoteEntityService.ATTR_PATH);
				String name = attributes.getValue(RemoteEntityService.ATTR_NAME);
				String nodeRef = attributes.getValue(RemoteEntityService.ATTR_NODEREF);

				if (entityNodeRef != null && curNodeRef.isEmpty()) {
					logger.debug("We force node update by providing nodeRef");
					nodeRef = entityNodeRef.toString();
				}
				String code = attributes.getValue(RemoteEntityService.ATTR_CODE);

				nodeType = QName.createQName(qName, namespaceService);

				NodeRef node = null;
				if (currAssocType.isEmpty() || !currAssocType.peek().equals(RemoteEntityService.CHILD_ASSOC_TYPE) || isNodeRefAssoc) {
					node = findNode(nodeRef, code, name, path, nodeType, currProp);
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

					if (!currAssocType.isEmpty() && currAssocType.peek().equals(RemoteEntityService.CHILD_ASSOC_TYPE) && !isNodeRefAssoc) {
						curNodeRef.push(createAssocNode(curNodeRef.peek(), nodeType, currAssoc.peek(), name));
					} else {

						if (node == null && entityProviderCallBack != null) {
							logger.debug("Node not found calling provider");
							try {
								node = entityProviderCallBack.provideNode(new NodeRef(nodeRef));
							} catch (BeCPGException e) {
								throw new SAXException("Cannot call entityProviderCallBack for nodeRef: "+nodeRef, e);
							} finally {
								if(node == null){
									logger.error("Cannot add node to assoc, node not found : " + nodeRef);
								}
							}
						}
						if (node == null) {
							throw new SAXException("Cannot add node to assoc, node not found : " + name);
						}

						if (!currAssoc.isEmpty() && !isNodeRefAssoc) {
							logger.debug("Node found creating assoc: " + currAssoc.peek());
							nodeService.createAssociation(curNodeRef.peek(), node, currAssoc.peek());
							curNodeRef.push(node);
						}

						if (isNodeRefAssoc) {
							// Case d:nodeRef
							
							if (logger.isDebugEnabled()) {
								logger.debug("Set property to : " + currProp.toPrefixString(namespaceService) + " value " + node + " for type " + type);
							}
							
							nodeService.setProperty(curNodeRef.peek(), currProp, node);
							curNodeRef.push(node);
						}
					}
				}
				
			} else if (type != null && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE))) {
				currAssoc.push(QName.createQName(qName, namespaceService));
				currAssocType.push(type);
				removeAllExistingAssoc(curNodeRef.peek(), currAssoc.peek(), type);
			} else if (type != null && type.length() > 0) {
				if (type.equals(RemoteEntityService.NODEREF_TYPE)) {
					isNodeRefAssoc = true;
				}
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
			if (type != null && type.equals(RemoteEntityService.NODE_TYPE)) {
				entityNodeRef = curNodeRef.pop();
			} else if (type != null && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE))) {
				currAssoc.pop();
				currAssocType.pop();
			} else if (type != null && type.length() > 0 && !type.equals(RemoteEntityService.NODEREF_TYPE)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Set property : " + currProp.toPrefixString() + " value " + currValue + " for type " + type);
				}
				if (currValue.length() > 0) {
					nodeService.setProperty(curNodeRef.peek(), currProp, currValue.toString());
				} else {
					nodeService.setProperty(curNodeRef.peek(), currProp, null);
				}
			} else {
				isNodeRefAssoc = false;
			}
		}

		public NodeRef getCurNodeRef() {
			return entityNodeRef;
		}

	};

	private void removeAllExistingAssoc(NodeRef nodeRef, QName assocName, String type) {
		if (type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {
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

			NodeRef ret = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);

			if (ret == null) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, name);
				logger.debug("Creating missing node :" + name + " at path :" + parentPath);
				ret = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, type, properties).getChildRef();
			} else {
				if (!nodeService.getType(ret).equals(type)) {
					logger.error("Found node with same name: " + name + " but incorrect type :" + nodeService.getType(ret) + "/" + type + " under: " + parentPath);
					return null;
				}
			}

			return ret;
		}
		throw new SAXException("Path doesn't exist on repository :" + parentPath);
	}

	private NodeRef createAssocNode(NodeRef parentNodeRef, QName type, QName assocName, String name) {
		logger.debug("Creating child assoc: " + assocName + " add type :" + type);
		NodeRef ret = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);
		if(ret == null){
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, name);
			return nodeService.createNode(parentNodeRef, assocName, assocName, type, properties).getChildRef();
		} else {
			return ret;
		}
	}

	private NodeRef findNodeByPath(String parentPath) {
		
		NodeRef rootNode =  nodeService.getRootNode(RepoConsts.SPACES_STORE);
		
		NodeRef ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode,parentPath);  
		
		if (ret ==null) {
			ret = BeCPGQueryBuilder.createQuery().selectNodeByPath( rootNode ,"/app:company_home"+RepoConsts.FULL_PATH_IMPORT_TO_DO);
		}

		return ret;

	}

	/*
	 * We search for : 
	 * 1° - TYPE/PATH/CODE/SAME NAME 
	 * 2° - TYPE/PATH/SAME NAME 
	 * 3° - TYPE/SAME NAME
	 */
	private NodeRef findNode(String nodeRef, String code, String name, String path, QName type, QName currProp) {
		if (nodeRef != null && nodeService.exists(new NodeRef(nodeRef))) {
			return new NodeRef(nodeRef);
		}
		
		BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery();
		

		if (type != null) {
			beCPGQueryBuilder.ofType(type);
		}

		if (path != null) {
			beCPGQueryBuilder.inPath(path);
		}

		if (code != null && code.length() > 0) {
			beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_CODE, code);
		} else if (name != null && name.length() > 0) {
			beCPGQueryBuilder.andPropEquals(RemoteHelper.getPropName(type), name);
		}
		
		beCPGQueryBuilder.maxResults(RepoConsts.MAX_RESULTS_256);

		List<NodeRef> ret = beCPGQueryBuilder.inDB().list();
		if (!ret.isEmpty()) {
			for (NodeRef node : ret) {
				if (nodeService.exists(node) && name.equals(nodeService.getProperty(node, RemoteHelper.getPropName(type)))) {
					logger.debug("Found node for query :" + beCPGQueryBuilder.toString());
					return node;
				}
			}
		}

		if (code != null) {
			logger.debug("Retrying findNode without code for previous query : " + beCPGQueryBuilder.toString());
			return findNode(nodeRef, null, name, path, type, currProp);
		}

		if (path != null) {
			logger.debug("Retrying findNode without path for previous query : " + beCPGQueryBuilder.toString());
			return findNode(nodeRef, code, name, null, type, currProp);
		}

		return null;
	}

}
