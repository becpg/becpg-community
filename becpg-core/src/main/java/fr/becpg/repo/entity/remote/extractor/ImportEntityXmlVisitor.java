/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 *
 * @author matthieu
 *
 */
public class ImportEntityXmlVisitor {

	private static final String FULL_PATH_IMPORT_TO_DO = "/app:company_home/cm:Exchange/cm:Import/cm:ImportToDo";

	private final Pattern nodeRefPattern = Pattern.compile("(workspace://SpacesStore/[a-z0-9A-Z\\-]*)");

	private EntityProviderCallBack entityProviderCallBack;

	private final EntityDictionaryService entityDictionaryService;

	private final ServiceRegistry serviceRegistry;

	public void setEntityProviderCallBack(EntityProviderCallBack entityProviderCallBack) {
		this.entityProviderCallBack = entityProviderCallBack;
	}

	private static final Log logger = LogFactory.getLog(ImportEntityXmlVisitor.class);

	public ImportEntityXmlVisitor(ServiceRegistry serviceRegistry, EntityDictionaryService entityDictionaryService) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.entityDictionaryService = entityDictionaryService;
	}

	public NodeRef visit(NodeRef entityNodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties, InputStream in)
			throws IOException, SAXException, ParserConfigurationException {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			EntityXmlHandler handler = new EntityXmlHandler(entityNodeRef, destNodeRef, properties);
			saxParser.parse(in, handler);
			handler.handlePropertiesQueue();
			handler.removeExistingAssociations();

			return handler.getCurNodeRef();
		} finally {
			IOUtils.closeQuietly(in);
		}

	}

	public void visitData(InputStream in, OutputStream out) throws IOException, SAXException, ParserConfigurationException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			EntityDataXmlHandler handler = new EntityDataXmlHandler(out);
			saxParser.parse(in, handler);

		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private class EntityDataXmlHandler extends DefaultHandler {

		private final Writer writer;

		boolean isData = false;

		public EntityDataXmlHandler(OutputStream out) {
			writer = new OutputStreamWriter(new Base64OutputStream(out, false));
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			isData = false;
			if (qName.equals("bcpg:" + RemoteEntityService.ELEM_DATA)) {
				isData = true;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (isData) {
				try {
					writer.write(ch, start, length);
				} catch (IOException e) {
					throw new SAXException("Cannot write outputStream");
				}
			}

		}

		@Override
		public void endDocument() throws SAXException {
			try {
				writer.flush();
			} catch (IOException e) {
				throw new SAXException("Error flushing character output", e);
			}
		}

	}

	private class EntityXmlHandler extends DefaultHandler {

		private NodeRef entityNodeRef = null;

		private NodeRef destNodeRef = null;

		private Map<QName, Serializable> properties = null;

		private final Map<NodeRef, NodeRef> cache = new HashMap<>();

		private final Stack<NodeRef> curNodeRef = new Stack<>();

		private StringBuffer currValue = new StringBuffer();

		private final Stack<String> typeStack = new Stack<>();

		private final Stack<QName> currAssoc = new Stack<>();

		private final Stack<String> currAssocType = new Stack<>();

		private ArrayList<Serializable> multipleValues = null;

		private Map<NodeRef, Map<QName, String>> propertiesQueue = new HashMap<>();

		private List<ChildAssociationRef> toRemoveChildAssocsQueue = new ArrayList<>();

		private QName currProp = null;

		private QName nodeType = null;

		public EntityXmlHandler(NodeRef entityNodeRef, NodeRef destNodeRef, Map<QName, Serializable> properties) {
			this.entityNodeRef = entityNodeRef;
			this.destNodeRef = destNodeRef;
			this.properties = properties;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			if (logger.isTraceEnabled()) {
				logger.trace("Enter : " + qName);
			}

			if (!qName.startsWith("metadata:")) {

				String type = attributes.getValue(RemoteEntityService.ATTR_TYPE);
				typeStack.push(type);

				if ((type != null) && type.equals(RemoteEntityService.NODE_TYPE)) {
					String path = attributes.getValue(RemoteEntityService.ATTR_PATH);
					String name = attributes.getValue(RemoteEntityService.ATTR_NAME);
					String nodeRef = attributes.getValue(RemoteEntityService.ATTR_NODEREF);
					String code = attributes.getValue(RemoteEntityService.ATTR_CODE);

					if ((entityNodeRef != null) && curNodeRef.isEmpty()) {
						logger.debug("We force node update by providing nodeRef");
						nodeRef = entityNodeRef.toString();
					}

					nodeType = parseQName(qName);

					NodeRef node = null;
					if (currAssocType.isEmpty() || !currAssocType.peek().equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {
						node = findNode(nodeRef, code, name, curNodeRef.isEmpty() ? this.destNodeRef : null, path, nodeType, currProp, cache);
					}
					// Entity node
					if (curNodeRef.isEmpty()) {

						if (node == null) {
							logger.debug("Add entity main node");
							if (this.destNodeRef != null) {
								node = createNode(this.destNodeRef, nodeType, name);
							} else {
								node = createNode(path, nodeType, name);
							}
							try {
								retrieveNodeContent(new NodeRef(nodeRef), node);
							} catch (BeCPGException e) {
								throw new SAXException("Cannot retrieve node content: " + nodeRef, e);
							}
							cache.put(new NodeRef(nodeRef), node);

						} else {
							logger.debug("Update entity main node");
						}
						curNodeRef.push(node);
					} else {

						if (!currAssocType.isEmpty() && currAssocType.peek().equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {

							if (currAssoc.peek() != null) {

								NodeRef childNode = createChildAssocNode(curNodeRef.peek(), nodeType, currAssoc.peek(), name, new NodeRef(nodeRef));
								curNodeRef.push(childNode);
								try {
									retrieveNodeContent(new NodeRef(nodeRef), childNode);
								} catch (BeCPGException e) {
									throw new SAXException("Cannot retrieve node content: " + nodeRef, e);
								}
								cache.put(new NodeRef(nodeRef), childNode);
							}
						} else {

							if (node == null) {
								if (entityProviderCallBack != null) {
									logger.debug("Node not found calling provider");
									try {
										node = entityProviderCallBack.provideNode(new NodeRef(nodeRef));
										cache.put(new NodeRef(nodeRef), node);
									} catch (BeCPGException e) {
										throw new SAXException("Cannot call entityProviderCallBack for nodeRef: " + nodeRef, e);
									} finally {
										if (node == null) {
											logger.error("Cannot add node to assoc, node not found : " + nodeRef);
										}
									}
								} else {
									// Case full xml
									logger.debug("Creating new node from xml");
									node = createNode(path, nodeType, name);
									cache.put(new NodeRef(nodeRef), node);
								}
							}

							if (node == null) {
								throw new SAXException("Cannot add node to assoc, node not found : " + name);
							}

							if (!currAssoc.isEmpty() && (currAssoc.peek() != null)) {

								if (currAssocType.peek().equals(RemoteEntityService.NODEREF_TYPE)) {
									// Case d:nodeRef
									if (logger.isDebugEnabled()) {
										logger.debug("Set property to : " + currAssoc.peek().toPrefixString(serviceRegistry.getNamespaceService())
												+ " value " + node + " for type " + type);
									}

									if (multipleValues != null) {
										if (logger.isDebugEnabled()) {
											logger.debug("Multiple value for d:nodeRef");
										}
										multipleValues.add(node);
									} else {
										serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currAssoc.peek(), node);
									}
								} else {

									logger.debug("Node found creating assoc: " + currAssoc.peek() + " for node " + curNodeRef.peek());

									serviceRegistry.getNodeService().createAssociation(curNodeRef.peek(), node, currAssoc.peek());

								}

							}

							curNodeRef.push(node);

						}
					}

				} else if ((type != null) && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)
						|| type.equals(RemoteEntityService.NODEREF_TYPE))) {
					currAssoc.push(parseQName(qName));
					currAssocType.push(type);
					if (!type.equals(RemoteEntityService.NODEREF_TYPE)) {
						queueExistingAssociations(curNodeRef.peek(), currAssoc.peek(), type);
					}
				} else if ((type != null) && (type.length() > 0)) {
					currProp = parseQName(qName);
				} else if (RemoteEntityService.ELEM_LIST.equals(qName)) {
					logger.trace("init multipleValues");
					multipleValues = new ArrayList<>();
				}

				currValue = new StringBuffer();
			}
		}

		private QName parseQName(String qName) {
			try {
				return QName.createQName(qName, serviceRegistry.getNamespaceService());
			} catch (NamespaceException e) {
				logger.warn("Wrong qname " + qName + " ignoring");
			}
			return null;
		}

		private void retrieveNodeContent(NodeRef origNodeRef, NodeRef destNodeRef) throws BeCPGException {
			if ((entityProviderCallBack != null)
					&& !entityDictionaryService.isSubClass(serviceRegistry.getNodeService().getType(destNodeRef), BeCPGModel.TYPE_ENTITY_V2)
					&& !entityDictionaryService.isSubClass(serviceRegistry.getNodeService().getType(destNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				entityProviderCallBack.provideContent(origNodeRef, destNodeRef);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currValue.append(ch, start, length);
		}

		// <pjt:projectLegends type="d:noderef">
		// <values>
		// <value>
		// <pjt:taskLegend
		// path="/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:TaskLegends"
		// type="node" name="Lancement produit"
		// nodeRef="workspace://SpacesStore/2d0ecc02-a290-443a-bce8-b4501c517682"
		// />
		// </value>
		// <value>
		// <pjt:taskLegend
		// path="/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:TaskLegends"
		// type="node" name="Marketing"
		// nodeRef="workspace://SpacesStore/2544b6e8-7b57-4b96-bddf-ad1ecc571a19"
		// />
		// </value>
		// </values>
		// </pjt:projectLegends>

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			if (logger.isTraceEnabled()) {
				logger.trace("End : " + qName);
			}

			if (!qName.startsWith("metadata:")) {

				String type = typeStack.pop();
				if ((type != null) && type.equals(RemoteEntityService.NODE_TYPE)) {
					logger.trace("Pop node");
					entityNodeRef = curNodeRef.pop();
				} else if (RemoteEntityService.ELEM_LIST_VALUE.equals(qName)) {
					if ((currAssocType.isEmpty() || !currAssocType.peek().equals(RemoteEntityService.NODEREF_TYPE)) && (currValue.length() > 0)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Add " + currValue.toString() + " to multipleValues ");
						}
						multipleValues.add(currValue.toString());
					}
				} else if ((type != null) && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)
						|| type.equals(RemoteEntityService.NODEREF_TYPE))) {

					if (multipleValues != null) {
						serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currAssoc.peek(), multipleValues);
						multipleValues = null;
					}

					currAssoc.pop();
					currAssocType.pop();

				} else if ((type != null) && (type.length() > 0)) {
					if (!shouldIgnoreProperty(currProp)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Set property : " + currProp.toPrefixString() + " value " + currValue + " for type " + type);
							logger.debug("Is multiple  : " + (multipleValues != null));
						}

						if ((curNodeRef.size() == 1) && (properties != null) && properties.containsKey(currProp)) {

							if (ContentModel.PROP_NAME.equals(currProp)) {
								serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp,
										PropertiesHelper.cleanName((String) properties.get(currProp)));
							} else {
								serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp, properties.get(currProp));
							}
						} else {
							if (multipleValues != null) {
								serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp, multipleValues);
								multipleValues = null;
							} else {
								if (currValue.length() > 0) {
									Matcher nodeRefMatcher = nodeRefPattern.matcher(currValue.toString());

									if (nodeRefMatcher.find()) {
										queueProperties(curNodeRef.peek(), currProp, currValue.toString());
									} else {
										if (ContentModel.PROP_NAME.equals(currProp)) {
											serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp,
													PropertiesHelper.cleanName(currValue.toString()));
										} else {
											serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp, currValue.toString());
										}
									}
								} else {
									serviceRegistry.getNodeService().setProperty(curNodeRef.peek(), currProp, null);
								}
							}
						}
					} else if (multipleValues != null) {
						multipleValues = null;
					}
				}
			}
		}

		private void queueProperties(NodeRef nodeRef, QName propQname, String value) {
			logger.debug("Queue propertie: " + propQname + " " + value);

			Map<QName, String> properties = propertiesQueue.get(nodeRef);
			if (properties == null) {
				properties = new HashMap<>();
			}

			properties.put(propQname, value);
			propertiesQueue.put(nodeRef, properties);
		}

		public NodeRef getCurNodeRef() {
			return entityNodeRef;
		}

		public void handlePropertiesQueue() throws SAXException {

			for (Map.Entry<NodeRef, Map<QName, String>> entry : propertiesQueue.entrySet()) {
				Map<QName, String> properties = entry.getValue();
				for (Map.Entry<QName, String> value : properties.entrySet()) {
					Matcher nodeRefMatcher = nodeRefPattern.matcher(value.getValue());
					StringBuffer sb = new StringBuffer();
					while (nodeRefMatcher.find()) {
						String replacement = nodeRefMatcher.group(1);
						NodeRef origNodeRef = new NodeRef(replacement);

						if (!serviceRegistry.getNodeService().exists(origNodeRef)) {
							NodeRef replacementNode = null;

							if (cache.containsKey(origNodeRef)) {
								logger.debug("found replacement nodeRef for" + value.getValue());
								replacementNode = cache.get(origNodeRef);

							} else if (entityProviderCallBack != null) {
								logger.debug("Node not found calling provider");
								try {
									replacementNode = entityProviderCallBack.provideNode(origNodeRef);
									cache.put(origNodeRef, replacementNode);
								} catch (BeCPGException e) {
									throw new SAXException("Cannot call entityProviderCallBack for nodeRef: " + origNodeRef, e);
								}
							}

							if (replacementNode == null) {
								logger.error("Cannot find replacement node for : " + value.getValue());

							} else {
								replacement = replacementNode.toString();
							}

						}

						nodeRefMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

					}
					nodeRefMatcher.appendTail(sb);
					serviceRegistry.getNodeService().setProperty(entry.getKey(), value.getKey(), sb.toString());
				}
			}
		}

		private void queueExistingAssociations(NodeRef nodeRef, QName assocName, String type) {
			logger.debug("Queue existing assocs : " + nodeRef + " " + assocName);

			if (assocName != null) {
				if (type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {
					for (ChildAssociationRef assoc : serviceRegistry.getNodeService().getChildAssocs(nodeRef)) {
						if (assoc.getQName().equals(assocName)) {
							toRemoveChildAssocsQueue.add(assoc);
						}
					}
				} else {
					for (AssociationRef assoc : serviceRegistry.getNodeService().getTargetAssocs(nodeRef, assocName)) {
						serviceRegistry.getNodeService().removeAssociation(nodeRef, assoc.getTargetRef(), assoc.getTypeQName());
					}
				}
			}
		}

		public void removeExistingAssociations() {

			for (ChildAssociationRef assoc : toRemoveChildAssocsQueue) {

				if(logger.isDebugEnabled()){
					logger.debug("Delete childAssoc :" + assoc.toString());
				}
				
				serviceRegistry.getNodeService().deleteNode(assoc.getChildRef());
			}

		}

		private boolean shouldIgnoreProperty(QName currProp) {
			if ((currProp == null) || ContentModel.PROP_VERSION_LABEL.equals(currProp) || ContentModel.PROP_VERSION_TYPE.equals(currProp)
					|| ContentModel.PROP_AUTO_VERSION.equals(currProp) || ContentModel.PROP_AUTO_VERSION_PROPS.equals(currProp)
					|| ContentModel.PROP_MODIFIED.equals(currProp) || ContentModel.PROP_MODIFIER.equals(currProp)
					|| ContentModel.PROP_CREATED.equals(currProp) || ContentModel.PROP_CREATOR.equals(currProp)
					|| ContentModel.PROP_NAME.equals(currProp)) {
				return true;
			}
			return false;
		}

		private NodeRef createNode(String parentPath, QName type, String name) throws SAXException {
			NodeRef parentNodeRef = findNodeByPath(parentPath);

			if (parentNodeRef != null) {
				return createNode(parentNodeRef, type, name);
			}
			throw new SAXException("Path doesn't exist on repository :" + parentPath);
		}

		private NodeRef createNode(NodeRef parentNodeRef, QName type, String name) {
			NodeRef ret = serviceRegistry.getNodeService().getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);

			if (ret == null) {
				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanName(name));
				logger.debug("Creating missing node :" + name + " at path :" + parentNodeRef);
				ret = serviceRegistry.getNodeService().createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(PropertiesHelper.cleanName(name))), type,
						properties).getChildRef();
			} else {
				if (!serviceRegistry.getNodeService().getType(ret).equals(type)) {
					logger.error("Found node with same name: " + name + " but incorrect type :" + serviceRegistry.getNodeService().getType(ret) + "/"
							+ type + " under: " + parentNodeRef);
					return null;
				}
			}

			return ret;

		}

		private NodeRef createChildAssocNode(NodeRef parentNodeRef, QName type, QName assocName, String name, NodeRef existingNodeRef) {
			

			if (cache.containsKey(existingNodeRef)) {
				logger.debug("Cache contains :" + cache.get(existingNodeRef) + " of nodeRef " + existingNodeRef + " " + name);

				existingNodeRef = cache.get(existingNodeRef);
			}

			// Translate
			if ((existingNodeRef == null) || !serviceRegistry.getNodeService().exists(existingNodeRef) 
					|| type.equals(serviceRegistry.getNodeService().getType(existingNodeRef))) {
				NodeRef ret = serviceRegistry.getNodeService().getChildByName(parentNodeRef, assocName, name);
				if (ret == null) {
					
					logger.debug("Creating child assoc: " + assocName + " add type :" + type + " name :" + name);
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanName(name));
					return serviceRegistry.getNodeService().createNode(parentNodeRef, assocName,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(PropertiesHelper.cleanName(name))),
							type, properties).getChildRef();
				} else {
					
					logger.debug("Find child assoc: " + assocName + " add type :" + type + " with same name :" + name);

					if(toRemoveChildAssocsQueue.remove(serviceRegistry.getNodeService().getPrimaryParent(ret)) && logger.isDebugEnabled()){
						logger.debug("Removing from childQueue :" + serviceRegistry.getNodeService().getPrimaryParent(ret).toString());
					}
					return ret;
				}
			} else {
				if (!serviceRegistry.getNodeService().getPrimaryParent(existingNodeRef).getParentRef().equals(parentNodeRef)) {
					serviceRegistry.getNodeService().moveNode(existingNodeRef, parentNodeRef, assocName,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(PropertiesHelper.cleanName(name))));
				} 
				
				logger.debug("Using existing child assoc: " + assocName + " add type :" + type + " name :" + name);
				

				if(toRemoveChildAssocsQueue.remove(serviceRegistry.getNodeService().getPrimaryParent(existingNodeRef)) && logger.isDebugEnabled()){
					logger.debug("Removing from childQueue :" + serviceRegistry.getNodeService().getPrimaryParent(existingNodeRef).toString());					
				}
				return existingNodeRef;
			}

		}

		private NodeRef findNodeByPath(String parentPath) {

			NodeRef rootNode = serviceRegistry.getNodeService().getRootNode(RepoConsts.SPACES_STORE);

			NodeRef ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, parentPath);

			if (ret == null) {
				ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, FULL_PATH_IMPORT_TO_DO);
			}

			return ret;

		}

		/*
		 * We search for : 1° - TYPE/PATH/CODE/SAME NAME 2° - TYPE/PATH/SAME
		 * NAME 3° - TYPE/SAME NAME
		 */
		private NodeRef findNode(String nodeRef, String code, String name, NodeRef parentNodeRef, String path, QName type, QName currProp,
				Map<NodeRef, NodeRef> cache) {
			if ((nodeRef != null) && serviceRegistry.getNodeService().exists(new NodeRef(nodeRef))) {
				return new NodeRef(nodeRef);
			}

			if ((cache != null) && cache.containsKey(new NodeRef(nodeRef))) {
				return cache.get(new NodeRef(nodeRef));
			}

			if (ContentModel.TYPE_PERSON.equals(type)) {
				logger.debug("try to get person : " + name);
				return serviceRegistry.getPersonService().getPerson(name);
			}

			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				logger.debug("try to get authority : " + name);
				return serviceRegistry.getAuthorityService().getAuthorityNodeRef(name);
			}

			BeCPGQueryBuilder beCPGQueryBuilder = BeCPGQueryBuilder.createQuery();

			boolean inBD = true;

			if (type != null) {
				beCPGQueryBuilder.ofType(type);
			}

			if (parentNodeRef != null) {
				beCPGQueryBuilder.parent(parentNodeRef);
			}

			if (path != null) {
				beCPGQueryBuilder.inPath(path);
				inBD = false;
			}

			if ((code != null) && (code.length() > 0)) {
				beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_CODE, code);
			} else if ((name != null) && (name.length() > 0)) {
				beCPGQueryBuilder.andPropEquals(RemoteHelper.getPropName(type, entityDictionaryService), cleanName(name));
			}

			if (inBD) {
				beCPGQueryBuilder.inDB();
			}

			beCPGQueryBuilder.maxResults(RepoConsts.MAX_RESULTS_256);

			List<NodeRef> ret = beCPGQueryBuilder.list();
			if (!ret.isEmpty()) {
				for (NodeRef node : ret) {
					if (serviceRegistry.getNodeService().exists(node) && name
							.equals(serviceRegistry.getNodeService().getProperty(node, RemoteHelper.getPropName(type, entityDictionaryService)))) {
						logger.debug("Found node for query :" + beCPGQueryBuilder.toString());
						return node;
					}
				}
			}

			if (code != null) {
				logger.debug("Retrying findNode without code for previous query : " + beCPGQueryBuilder.toString());
				return findNode(nodeRef, null, name, parentNodeRef, path, type, currProp, null);
			}

			if (path != null) {
				logger.debug("Retrying findNode without path for previous query : " + beCPGQueryBuilder.toString());
				return findNode(nodeRef, code, name, parentNodeRef, null, type, currProp, null);
			}

			logger.debug("No existing node found for " + beCPGQueryBuilder.toString());
			return null;
		}

		private String cleanName(String propValue) {
			return propValue.replaceAll("'", "");
		}
	}
}
