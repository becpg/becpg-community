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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.EntityProviderCallBack;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * ImportEntityXmlVisitor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ImportEntityXmlVisitor {

	private final Pattern nodeRefPattern = Pattern.compile("(workspace://SpacesStore/[a-z0-9A-Z\\-]*)");

	private EntityProviderCallBack entityProviderCallBack;

	private Map<NodeRef, NodeRef> cache = new HashMap<>();

	private final EntityDictionaryService entityDictionaryService;

	private final AssociationService associationService;
	
	private final NodeService nodeService;
	
	private final NamespaceService namespaceService;
	
	private final PersonService personService;
	
	private final AuthorityService authorityService;


	private boolean forceCreateInPath;

	/**
	 * <p>
	 * Setter for the field <code>entityProviderCallBack</code>.
	 * </p>
	 *
	 * @param entityProviderCallBack
	 *            a {@link fr.becpg.repo.entity.remote.EntityProviderCallBack}
	 *            object.
	 */
	public void setEntityProviderCallBack(EntityProviderCallBack entityProviderCallBack) {
		this.entityProviderCallBack = entityProviderCallBack;
	}

	/**
	 * <p>
	 * Setter for the field <code>cache</code>.
	 * </p>
	 *
	 * @param cache
	 *            a {@link java.util.Map} object.
	 */
	public void setCache(Map<NodeRef, NodeRef> cache) {
		this.cache = cache;
	}

	private static final Log logger = LogFactory.getLog(ImportEntityXmlVisitor.class);

	/**
	 * <p>
	 * Constructor for ImportEntityXmlVisitor.
	 * </p>
	 *
	 * @param remoteServiceRegisty a {@link fr.becpg.repo.entity.remote.RemoteServiceRegisty} object
	 */
	public ImportEntityXmlVisitor(RemoteServiceRegisty remoteServiceRegisty) {
		super();
		this.nodeService = remoteServiceRegisty.nodeService();
		this.entityDictionaryService = remoteServiceRegisty.entityDictionaryService();
		this.associationService = remoteServiceRegisty.associationService();
		this.namespaceService = remoteServiceRegisty.namespaceService();
		this.personService = remoteServiceRegisty.personService();
		this.authorityService = remoteServiceRegisty.authorityService();
	}

	/**
	 * <p>
	 * visit.
	 * </p>
	 *
	 * @param entityNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param in
	 *            a {@link java.io.InputStream} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.io.IOException
	 *             if any.
	 * @throws org.xml.sax.SAXException
	 *             if any.
	 * @throws javax.xml.parsers.ParserConfigurationException
	 *             if any.
	 */
	public NodeRef visit(NodeRef entityNodeRef, NodeRef destNodeRef, InputStream in) throws IOException, SAXException, ParserConfigurationException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		SAXParser saxParser = factory.newSAXParser();

		EntityXmlHandler handler = new EntityXmlHandler(entityNodeRef, destNodeRef);
		saxParser.parse(in, handler);
		handler.handlePropertiesQueue();
		handler.removeExistingAssociations();

		return handler.getCurNodeRef();

	}

	/**
	 * <p>
	 * visitData.
	 * </p>
	 *
	 * @param in
	 *            a {@link java.io.InputStream} object.
	 * @param out
	 *            a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException
	 *             if any.
	 * @throws org.xml.sax.SAXException
	 *             if any.
	 * @throws javax.xml.parsers.ParserConfigurationException
	 *             if any.
	 */
	public void visitData(InputStream in, OutputStream out) throws IOException, SAXException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		SAXParser saxParser = factory.newSAXParser();

		EntityDataXmlHandler handler = new EntityDataXmlHandler(out);
		saxParser.parse(in, handler);

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

		private final Stack<NodeRef> curNodeRef = new Stack<>();

		private StringBuffer currValue = new StringBuffer();

		private Map<Locale, String> mltextAttributes = new HashMap<>();

		private final Stack<String> typeStack = new Stack<>();

		private final Stack<QName> currAssoc = new Stack<>();

		private final Stack<String> currAssocType = new Stack<>();

		private ArrayList<Serializable> multipleValues = null;

		private Map<NodeRef, Map<QName, String>> propertiesQueue = new HashMap<>();

		private List<ChildAssociationRef> toRemoveChildAssocsQueue = new ArrayList<>();

		private QName currProp = null;


		public EntityXmlHandler(NodeRef entityNodeRef, NodeRef destNodeRef) {
			this.entityNodeRef = entityNodeRef;
			this.destNodeRef = destNodeRef;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			if (logger.isTraceEnabled()) {
				logger.trace("Enter : " + qName);
			}

			if (!qName.startsWith("metadata:")) {
				
				if(attributes.getValue(RemoteEntityService.ATTR_CREATE_IN_PATH)!=null) {
					forceCreateInPath = Boolean.TRUE.toString().equals(attributes.getValue(RemoteEntityService.ATTR_CREATE_IN_PATH));
				}

				String type = attributes.getValue(RemoteEntityService.ATTR_TYPE);
				typeStack.push(type);

				if ((type != null) && type.equals(RemoteEntityService.NODE_TYPE)) {
					String path = attributes.getValue(RemoteEntityService.ATTR_PATH);
					String name = PropertiesHelper.cleanName(attributes.getValue(RemoteEntityService.ATTR_NAME));
					String nodeRef = attributes.getValue(RemoteEntityService.ATTR_NODEREF);
					String code = attributes.getValue(RemoteEntityService.ATTR_CODE);
					String erpCode = attributes.getValue(RemoteEntityService.ATTR_ERP_CODE);

					if (nodeRef == null) {
						nodeRef = "bcpg://tmpStore/" + UUID.randomUUID().toString();
					}

					if ((name == null) || name.trim().isEmpty()) {
						name = RemoteEntityService.EMPTY_NAME_PREFIX + UUID.randomUUID().toString();
					}

					if ((entityNodeRef != null) && curNodeRef.isEmpty()) {
						logger.debug("We force node update by providing nodeRef");
						nodeRef = entityNodeRef.toString();
					}

					QName nodeType = parseQName(qName);

					if (nodeType == null) {
						nodeType = ContentModel.TYPE_CONTENT;
					}

					NodeRef node = null;
					if (currAssocType.isEmpty() || !currAssocType.peek().equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {
						node = findNode(nodeRef, code, erpCode, name, curNodeRef.isEmpty() ? this.destNodeRef : null, path, nodeType, currProp,
								cache);
					}
					// Entity node
					if (curNodeRef.isEmpty()) {

						if (node == null) {
							logger.debug("Add entity main node");
							if (this.destNodeRef != null) {
								node = createNode(this.destNodeRef, nodeType, name, erpCode);
							} else {
								node = createNode(path, nodeType, name, erpCode);
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

								NodeRef childNode = createChildAssocNode(curNodeRef.peek(), nodeType, currAssoc.peek(), name, new NodeRef(nodeRef),
										attributes);
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
										node = entityProviderCallBack.provideNode(new NodeRef(nodeRef), cache);
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
									logger.debug("Creating new node from xml :"+ nodeType);
									node = createNode(path, nodeType, name, erpCode);
									cache.put(new NodeRef(nodeRef), node);
								}
							}

							if (node == null || !nodeService.exists(node)) {
								throw new SAXException("Cannot add node to assoc, node not found : " + name);
							}

							if (!currAssoc.isEmpty() && (currAssoc.peek() != null)) {
								if (currAssocType.peek().equals(RemoteEntityService.NODEREF_TYPE)
										|| currAssocType.peek().equals(RemoteEntityService.CATEGORY_TYPE)) {
									if (multipleValues != null) {
										if (logger.isDebugEnabled()) {
											logger.debug("Add multiple value for "
													+ currAssoc.peek().toPrefixString(namespaceService) + " value " + node);
										}
										multipleValues.add(node);
									} else {
										if (logger.isDebugEnabled()) {
											logger.debug("Set property to : " + currAssoc.peek().toPrefixString(namespaceService)
													+ " value " + node + " for type " + type);
										}
										nodeService.setProperty(curNodeRef.peek(), currAssoc.peek(), node);
									}
								} else {
									if(logger.isDebugEnabled()) {
										logger.debug("Create association : " + currAssoc.peek().toPrefixString(namespaceService)
												+ " value " + node + " for type " + nodeService.getType(node).toPrefixString(namespaceService));
									}
									try {
										nodeService.createAssociation(curNodeRef.peek(), node, currAssoc.peek());
									} catch (AssociationExistsException e) {
										// association already exists
									  logger.debug("Association Already exist", e);
									}

								}

							}

							curNodeRef.push(node);

						}
					}

				} else if ((type != null) && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)
						|| type.equals(RemoteEntityService.NODEREF_TYPE) || type.equals(RemoteEntityService.CATEGORY_TYPE))) {
					currAssoc.push(parseQName(qName));
					currAssocType.push(type);
					if (logger.isTraceEnabled()) {
						logger.trace("Push assoc type:" + type);
					}

					if (!type.equals(RemoteEntityService.NODEREF_TYPE) && !type.equals(RemoteEntityService.CATEGORY_TYPE)) {
						queueExistingAssociations(curNodeRef.peek(), currAssoc.peek(), type);
					}
				} else if ((type != null) && (type.length() > 0)) {

					if (RemoteEntityService.MLTEXT_TYPE.equals(type)) {
						Locale locale;
						String strLocale = "";
						for (int i = 0; i < attributes.getLength(); i++) {
							strLocale = attributes.getQName(i);
							locale = MLTextHelper.parseLocale(strLocale);
							if (!attributes.getValue(i).equals(RemoteEntityService.MLTEXT_TYPE) && (attributes.getQName(i) != null)
									&& MLTextHelper.isSupportedLocale(locale)) { 
								mltextAttributes.put(locale, StringEscapeUtils.unescapeHtml4(readCDATA(attributes.getValue(i))));
							}
						}
					}

					currProp = parseQName(qName);

				} else if (RemoteEntityService.ELEM_LIST.equals(qName)) {
					logger.trace("init multipleValues");
					multipleValues = new ArrayList<>();
				}

				currValue = new StringBuffer();
			} else {
				if (qName.equals("metadata:forceCreateInPath")) {
					forceCreateInPath = true;
				}

			}
		}

		private QName parseQName(String qName) {
			try {
				return QName.createQName(qName, namespaceService);
			} catch (NamespaceException e) {
				logger.warn("Wrong qname " + qName + " ignoring");
			}
			return null;
		}

		private void retrieveNodeContent(NodeRef origNodeRef, NodeRef destNodeRef) throws BeCPGException {
			if ((entityProviderCallBack != null)
					&& !entityDictionaryService.isSubClass(nodeService.getType(destNodeRef), BeCPGModel.TYPE_ENTITY_V2)
					&& !entityDictionaryService.isSubClass(nodeService.getType(destNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
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
					if ((currAssocType.isEmpty() || (!currAssocType.peek().equals(RemoteEntityService.NODEREF_TYPE)
							&& !currAssocType.peek().equals(RemoteEntityService.CATEGORY_TYPE))) && (currValue.length() > 0)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Add " + currValue.toString() + " to multipleValues ");
						}
						multipleValues.add(currValue.toString());
					}
				} else if ((type != null) && (type.equals(RemoteEntityService.ASSOC_TYPE) || type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)
						|| type.equals(RemoteEntityService.NODEREF_TYPE) || type.equals(RemoteEntityService.CATEGORY_TYPE))) {

					if (multipleValues != null) {
						nodeService.setProperty(curNodeRef.peek(), currAssoc.peek(), multipleValues);
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
								nodeService.setProperty(curNodeRef.peek(), currProp,
										PropertiesHelper.cleanName((String) properties.get(currProp)));
							} else {
								nodeService.setProperty(curNodeRef.peek(), currProp, properties.get(currProp));
							}
						} else {
							if (multipleValues != null) {
								nodeService.setProperty(curNodeRef.peek(), currProp, multipleValues);
								multipleValues = null;
							} else {
								if (currValue.length() > 0) {
									Matcher nodeRefMatcher = nodeRefPattern.matcher(currValue.toString());

									if (nodeRefMatcher.find() && !ContentModel.PROP_NAME.equals(currProp)
											&& !BeCPGModel.PROP_ACTIVITYLIST_DATA.equals(currProp)) {
										queueProperties(curNodeRef.peek(), currProp, currValue.toString());
									} else {
										if (ContentModel.PROP_NAME.equals(currProp)) {
											if (nodeService.getChildByName(
													nodeService.getPrimaryParent(curNodeRef.peek()).getParentRef(),
													ContentModel.ASSOC_CONTAINS, PropertiesHelper.cleanName(currValue.toString())) == null) {

												nodeService.setProperty(curNodeRef.peek(), currProp,
														PropertiesHelper.cleanName(currValue.toString()));
											}
										} else {
											if (RemoteEntityService.MLTEXT_TYPE.equals(type)) {
												MLText mltext = new MLText();
												mltext.addValue(MLTextHelper.getNearestLocale(Locale.getDefault()), currValue.toString());
												mltextAttributes.forEach(mltext::addValue);
												nodeService.setProperty(curNodeRef.peek(), currProp, mltext);
												mltextAttributes.clear();
											} else {
												nodeService.setProperty(curNodeRef.peek(), currProp, currValue.toString());
											}

										}
									}
								} else {
									nodeService.setProperty(curNodeRef.peek(), currProp, null);
								}
							}
						}
					} else if (multipleValues != null) {
						multipleValues = null;
					}
				}
			}
		}

		protected String readCDATA(String attribute) {
			return attribute != null
					? attribute.replace("&amp;", "&").replace("&quot;", "\"").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">")
					: "";
		}

		private void queueProperties(NodeRef nodeRef, QName propQname, String value) {
			logger.debug("Queue propertie: " + propQname + " " + value);

			Map<QName, String> props = propertiesQueue.get(nodeRef);
			if (props == null) {
				props = new HashMap<>();
			}

			props.put(propQname, value);
			propertiesQueue.put(nodeRef, props);
		}

		public NodeRef getCurNodeRef() {
			return entityNodeRef;
		}

		public void handlePropertiesQueue() throws SAXException {

			for (Map.Entry<NodeRef, Map<QName, String>> entry : propertiesQueue.entrySet()) {
				Map<QName, String> props = entry.getValue();
				for (Map.Entry<QName, String> value : props.entrySet()) {
					Matcher nodeRefMatcher = nodeRefPattern.matcher(value.getValue());
					StringBuffer sb = new StringBuffer();
					while (nodeRefMatcher.find()) {
						String replacement = nodeRefMatcher.group(1);
						NodeRef origNodeRef = new NodeRef(replacement);

						if (!nodeService.exists(origNodeRef)) {
							NodeRef replacementNode = null;

							if (cache.containsKey(origNodeRef)) {
								logger.debug("found replacement nodeRef for" + value.getValue());
								replacementNode = cache.get(origNodeRef);

							} else if (entityProviderCallBack != null) {
								logger.debug("Node not found calling provider");
								try {
									replacementNode = entityProviderCallBack.provideNode(origNodeRef, cache);
									cache.put(origNodeRef, replacementNode);
								} catch (BeCPGException e) {
									throw new SAXException("Cannot call entityProviderCallBack for nodeRef: " + origNodeRef, e);
								}
							}

							if (replacementNode == null || !nodeService.exists(replacementNode)) {
								logger.error("Cannot find replacement node for : " + value.getValue());

							} else {
								replacement = replacementNode.toString();
							}

						}

						nodeRefMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

					}
					nodeRefMatcher.appendTail(sb);
					if(nodeService.exists(entry.getKey())) {
						nodeService.setProperty(entry.getKey(), value.getKey(), sb.toString());
					}
					
				}
			}
		}

		private void queueExistingAssociations(NodeRef nodeRef, QName assocName, String type) {
			logger.debug("Queue existing assocs : " + nodeRef + " " + assocName);

			if (assocName != null) {
				if (type.equals(RemoteEntityService.CHILD_ASSOC_TYPE)) {
					for (ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
						if (assoc.getQName().equals(assocName)) {
							toRemoveChildAssocsQueue.add(assoc);
						}
					}
				} else {
					for (AssociationRef assoc : nodeService.getTargetAssocs(nodeRef, assocName)) {
						nodeService.removeAssociation(nodeRef, assoc.getTargetRef(), assoc.getTypeQName());
					}
				}
			}
		}

		public void removeExistingAssociations() {

			for (ChildAssociationRef assoc : toRemoveChildAssocsQueue) {
				if (nodeService.exists(assoc.getChildRef())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Delete childAssoc :" + assoc.toString());
					}

					nodeService.deleteNode(assoc.getChildRef());
				}
			}

		}

		private boolean shouldIgnoreProperty(QName currProp) {
			if ((currProp == null) || ContentModel.PROP_VERSION_LABEL.equals(currProp) || ContentModel.PROP_VERSION_TYPE.equals(currProp)
					|| ContentModel.PROP_AUTO_VERSION.equals(currProp) || ContentModel.PROP_AUTO_VERSION_PROPS.equals(currProp)
					|| ContentModel.PROP_MODIFIED.equals(currProp) || ContentModel.PROP_MODIFIER.equals(currProp)
					|| ContentModel.PROP_CREATED.equals(currProp) || ContentModel.PROP_CREATOR.equals(currProp)) {
				return true;
			}
			return false;
		}

		private NodeRef createNode(String parentPath, QName type, String name, String erpCode) throws SAXException {
			NodeRef parentNodeRef = findNodeByPath(parentPath);

			if (parentNodeRef != null) {
				return createNode(parentNodeRef, type, name, erpCode);
			}
			throw new SAXException("Path doesn't exist on repository :" + parentPath);
		}

		private NodeRef createNode(NodeRef parentNodeRef, QName type, String name, String erpCode) {
			NodeRef ret = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);

			if (ret == null) {
				if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type) || ContentModel.TYPE_PERSON.equals(type)) {
					throw new IllegalStateException("Cannot create  "+name+ ", creating group is not yet implemented");
				} else {
				
				
					Map<QName, Serializable> props = new HashMap<>();
					props.put(ContentModel.PROP_NAME, name);
					if ((erpCode != null) && !erpCode.isEmpty()) {
						props.put(BeCPGModel.PROP_ERP_CODE, erpCode);
					}
	
					QName assocName = ContentModel.ASSOC_CONTAINS;
					logger.debug("Creating missing node :" + name + " at path :" + parentNodeRef + ", type = " + type + ", assocName = " + assocName);
	
					if (ContentModel.TYPE_CATEGORY.equals(type)) {
						// fixes tag creation
						assocName = ContentModel.ASSOC_SUBCATEGORIES;
					}
	
					ret = nodeService
							.createNode(parentNodeRef, assocName,
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), type, props)
							.getChildRef();
				}

			} else {
				if (!nodeService.getType(ret).equals(type)) {
					logger.error("Found node with same name: " + name + " but incorrect type :" + nodeService.getType(ret) + "/"
							+ type + " under: " + parentNodeRef);
					return null;
				}
			}

			return ret;

		}

		private NodeRef createChildAssocNode(NodeRef parentNodeRef, QName type, QName assocName, String name, NodeRef existingNodeRef,
				Attributes attributes) {

			if (cache.containsKey(existingNodeRef)) {
				logger.debug("Cache contains :" + cache.get(existingNodeRef) + " of nodeRef " + existingNodeRef + " " + name);

				existingNodeRef = cache.get(existingNodeRef);
			}

			// Translate
			if ((existingNodeRef == null) || !nodeService.exists(existingNodeRef)
					|| type.equals(nodeService.getType(existingNodeRef))) {

				NodeRef ret = nodeService.getChildByName(parentNodeRef, assocName, name);

				// rendre générique ?
				if (ForumModel.ASSOC_DISCUSSION.equals(assocName)) {
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNodeRef);
					for (ChildAssociationRef childAssoc : childAssocs) {
						if (childAssoc.getTypeQName().equals(assocName)) {
							ret = childAssoc.getChildRef();
							logger.debug("Found discussion assoc");
						}
					}
				}

				logger.debug("Getting assoc of type " + assocName + " on " + parentNodeRef + ", name= " + name + " -> " + ret);
				if (ret == null) {

					if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						QName pivot = entityDictionaryService.getDefaultPivotAssoc(type);
						if (pivot != null) {

							if ((attributes.getValue(RemoteEntityService.CHARACT_ATTR_NODEREF) != null)
									|| (attributes.getValue(RemoteEntityService.CHARACT_ATTR_CODE) != null)
									|| (attributes.getValue(RemoteEntityService.CHARACT_ATTR_ERP_CODE) != null)
									|| (attributes.getValue(RemoteEntityService.CHARACT_ATTR_NAME) != null)) {

								NodeRef pivotAssocNodeRef = findNode(attributes.getValue(RemoteEntityService.CHARACT_ATTR_NODEREF),
										attributes.getValue(RemoteEntityService.CHARACT_ATTR_CODE),
										attributes.getValue(RemoteEntityService.CHARACT_ATTR_ERP_CODE),
										PropertiesHelper.cleanName(attributes.getValue(RemoteEntityService.CHARACT_ATTR_NAME)), null,
										attributes.getValue(RemoteEntityService.CHARACT_ATTR_PATH), BeCPGModel.TYPE_ENTITYLIST_ITEM, pivot, cache);

								if (pivotAssocNodeRef != null) {

									logger.debug("findByPivotAssoc " + pivot + " " + type);
									ret = findByPivotAssoc(parentNodeRef, pivotAssocNodeRef, pivot, type);
								}
							}

						}

					}
					if (ret == null) {
						logger.debug("Creating child assoc: " + assocName + " add type :" + type + " name :" + name);
						Map<QName, Serializable> properties = new HashMap<>();
						properties.put(ContentModel.PROP_NAME, name);
						return nodeService
								.createNode(parentNodeRef, assocName,
										QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), type, properties)
								.getChildRef();
					} else {
						return ret;
					}
				} else {

					logger.debug("Find child assoc: " + assocName + " add type :" + type + " with same name :" + name);

					if (toRemoveChildAssocsQueue.remove(nodeService.getPrimaryParent(ret)) && logger.isDebugEnabled()) {
						logger.debug("Removing from childQueue :" + nodeService.getPrimaryParent(ret).toString());
					}
					return ret;
				}
			} else {
				if (!nodeService.getPrimaryParent(existingNodeRef).getParentRef().equals(parentNodeRef)) {
					nodeService.moveNode(existingNodeRef, parentNodeRef, assocName,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)));
				}

				logger.debug("Using existing child assoc: " + assocName + " add type :" + type + " name :" + name);

				if (toRemoveChildAssocsQueue.remove(nodeService.getPrimaryParent(existingNodeRef)) && logger.isDebugEnabled()) {
					logger.debug("Removing from childQueue :" + nodeService.getPrimaryParent(existingNodeRef).toString());
				}
				return existingNodeRef;
			}

		}

		private NodeRef findByPivotAssoc(NodeRef parentNodeRef, NodeRef pivotAssocNodeRef, QName pivotAssoc, QName type) {

			for (NodeRef listItemNodeRef : associationService.getChildAssocs(parentNodeRef, ContentModel.ASSOC_CONTAINS)) {
				NodeRef part = associationService.getTargetAssoc(listItemNodeRef, pivotAssoc);
				if ((part != null) && part.equals(pivotAssocNodeRef)) {
					return listItemNodeRef;
				}
			}

			return null;
		}

		private NodeRef findNodeByPath(String parentPath) {
			NodeRef ret = null;

			NodeRef rootNode = nodeService.getRootNode(RepoConsts.SPACES_STORE);
			if ((parentPath != null) && !parentPath.isEmpty()) {
				ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, parentPath);
			}

			if (ret == null) {
				if (forceCreateInPath && parentPath!=null) {
					String path = parentPath.substring(0, parentPath.lastIndexOf("/"));
					String name = parentPath.substring(parentPath.lastIndexOf("/")+1, parentPath.length());
				
					NodeRef parentNodeRef = findNodeByPath(path);

					ret = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, PropertiesHelper.cleanFolderName(name));
					if (ret == null) {
						
						Map<QName, Serializable> properties = new HashMap<>();
						properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanFolderName(name));
						
						
						 ret =  nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
										QName.resolveToQName(namespaceService, name.replaceAll("_x0020_", " ")), ContentModel.TYPE_FOLDER, properties)
								.getChildRef();
						if(logger.isDebugEnabled()) {
							logger.debug("Creating path " + parentPath + "  "+ nodeService.getPath(ret).toPrefixString(namespaceService) + " "+ PropertiesHelper.cleanFolderName(name));
						}
					}
					return ret;

				}

				ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, RemoteEntityService.FULL_PATH_IMPORT_TO_DO);
			}

			return ret;

		}

		/*
		 * We search for : 1° - TYPE/PATH/CODE/SAME NAME 2° - TYPE/PATH/SAME
		 * NAME 3° - TYPE/SAME NAME
		 */
		private NodeRef findNode(String nodeRef, String code, String erpCode, String name, NodeRef parentNodeRef, String path, QName type,
				QName currProp, Map<NodeRef, NodeRef> cache) {
			if ((nodeRef != null) && nodeService.exists(new NodeRef(nodeRef))) {
				return new NodeRef(nodeRef);
			}

			if ((nodeRef != null) && (cache != null) && cache.containsKey(new NodeRef(nodeRef))) {
				return cache.get(new NodeRef(nodeRef));
			}

			if (ContentModel.TYPE_PERSON.equals(type)) {
				logger.debug("try to get person : " + name);
				return personService.getPersonOrNull(name);
			}

			if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)) {
				logger.debug("try to get authority : " + name);
				return authorityService.getAuthorityNodeRef(name);
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

			if ((code != null) && (!code.isEmpty())) {
				beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_CODE, code);
			} else if ((erpCode != null) && (!erpCode.isEmpty())) {
				beCPGQueryBuilder.andPropEquals(BeCPGModel.PROP_ERP_CODE, erpCode);
			} else if ((name != null) && (!name.isEmpty()) && !name.startsWith(RemoteEntityService.EMPTY_NAME_PREFIX)) {
				beCPGQueryBuilder.andPropEquals(RemoteHelper.getPropName(type, entityDictionaryService), name);
			}

			if (inBD) {
				beCPGQueryBuilder.inDB();
			}

			beCPGQueryBuilder.maxResults(RepoConsts.MAX_RESULTS_256);

			List<NodeRef> ret = beCPGQueryBuilder.list();
			if (!ret.isEmpty()) {
				for (NodeRef node : ret) {
					if (nodeService.exists(node) && ((name == null) || name.startsWith(RemoteEntityService.EMPTY_NAME_PREFIX)
							|| name.equals(PropertiesHelper.cleanName((String) nodeService.getProperty(node,
									RemoteHelper.getPropName(type, entityDictionaryService)))))) {
						logger.debug("Found node for query :" + beCPGQueryBuilder.toString());
						return node;
					}
				}
			}

			if (((code != null) && (!code.isEmpty())) && (erpCode != null) && (!erpCode.isEmpty())) {
				logger.debug("Retrying findNode without code for previous query : " + beCPGQueryBuilder.toString());
				return findNode(nodeRef, null, erpCode, name, parentNodeRef, path, type, currProp, null);
			} else if ((erpCode != null) && !erpCode.isEmpty() && (name != null) && (!name.isEmpty())
					&& !name.startsWith(RemoteEntityService.EMPTY_NAME_PREFIX)) {
				logger.debug("Retrying findNode without erpCode for previous query : " + beCPGQueryBuilder.toString());
				return findNode(nodeRef, null, null, name, parentNodeRef, path, type, currProp, null);
			} else if (!forceCreateInPath && (path != null) && (name != null) && (!name.isEmpty())
					&& !name.startsWith(RemoteEntityService.EMPTY_NAME_PREFIX)) {
				logger.debug("Retrying findNode without path for previous query : " + beCPGQueryBuilder.toString());
				return findNode(nodeRef, code, erpCode, name, parentNodeRef, null, type, currProp, null);
			}

			logger.debug("No existing node found for " + beCPGQueryBuilder.toString());
			return null;
		}

	}
}
