package fr.becpg.repo.entity.remote.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ImportEntityJsonVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ImportEntityJsonVisitor {

	private static Log logger = LogFactory.getLog(ImportEntityJsonVisitor.class);

	Set<String> ignoredKeys = new HashSet<>();

	{
		ignoredKeys.add(RemoteEntityService.ATTR_TYPE);
		ignoredKeys.add(RemoteEntityService.ATTR_NAME);
		ignoredKeys.add(RemoteEntityService.ATTR_PATH);
		ignoredKeys.add(RemoteEntityService.ATTR_NODEREF);
		ignoredKeys.add(RemoteEntityService.ELEM_ATTRIBUTES);
		ignoredKeys.add(RemoteEntityService.ELEM_DATALISTS);
		ignoredKeys.add(RemoteEntityService.ATTR_SITE);
		ignoredKeys.add(RemoteEntityService.ATTR_PARENT_ID);
		ignoredKeys.add(RemoteEntityService.ELEM_CONTENT);
	}

	EntityDictionaryService entityDictionaryService;

	NamespaceService namespaceService;

	AssociationService associationService;

	NodeService nodeService;

	EntityListDAO entityListDAO;

	private RemoteParams remoteParams;

	private class ImportJsonContext {

		Map<NodeRef, NodeRef> cache = new HashMap<>();
		String entityPath = null;
		NodeRef entityNodeRef = null;
		boolean retry = true;
		boolean lastRetry = false;
	}

	/**
	 * <p>Constructor for ImportEntityJsonVisitor.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public ImportEntityJsonVisitor(EntityDictionaryService entityDictionaryService, NamespaceService namespaceService,
			AssociationService associationService, NodeService nodeService, EntityListDAO entityListDAO) {
		super();
		this.entityDictionaryService = entityDictionaryService;
		this.namespaceService = namespaceService;
		this.associationService = associationService;
		this.nodeService = nodeService;
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.io.IOException if any.
	 * @throws org.json.JSONException if any.
	 * @throws fr.becpg.common.BeCPGException if any.
	 */
	public NodeRef visit(NodeRef entityNodeRef, InputStream in) throws IOException, JSONException {

		JSONTokener tokener = new JSONTokener(new InputStreamReader(in));
		JSONObject root = new JSONObject(tokener);

		if (logger.isDebugEnabled()) {
			logger.debug("Visiting: " + root.toString(3));
		}
		if (root.has(RemoteEntityService.ELEM_ENTITY)) {

			JSONObject entity = root.getJSONObject(RemoteEntityService.ELEM_ENTITY);

			remoteParams = new RemoteParams(RemoteEntityFormat.json);

			if (entity.has(RemoteEntityService.ELEM_PARAMS)) {
				remoteParams.setJsonParams(entity.getJSONObject(RemoteEntityService.ELEM_PARAMS));
			}

			if (entityNodeRef != null) {
				entity.put(RemoteEntityService.ATTR_ID, entityNodeRef.getId());
			}

			ImportJsonContext context = new ImportJsonContext();
			NodeRef ret = null;
			int retryCount = 0;
			while (context.retry && (retryCount < 3)) {
				if (retryCount == 1) {
					logger.debug("Last retry");
					context.lastRetry = true;
				}
				context.retry = false;
				ret = visit(entity, false, null, context, true);
				retryCount++;
				logger.debug("Retrying count:"+retryCount);
			}

			return ret;
		}

		throw new BeCPGException("No entity found in JSON");

	}

	private NodeRef visit(JSONObject entity, boolean lookupOnly, QName assocName, ImportJsonContext context, boolean isRoot) throws JSONException {

		QName type = null;

		QName propName = ContentModel.PROP_NAME;

		boolean isInPath = false;

		if (entity.has(RemoteEntityService.ATTR_TYPE)) {
			type = createQName(entity.getString(RemoteEntityService.ATTR_TYPE));
			propName = RemoteHelper.getPropName(type, entityDictionaryService);
		}

		NodeRef parentNodeRef = null;

		if (entity.has(RemoteEntityService.ATTR_PARENT_ID)) {
			parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, entity.getString(RemoteEntityService.ATTR_PARENT_ID));
			if (context.cache.containsKey(parentNodeRef)) {
				parentNodeRef = context.cache.get(parentNodeRef);
			}
		}

		if (((parentNodeRef == null) || !nodeService.exists(parentNodeRef))) {
			if (entity.has(RemoteEntityService.ATTR_PATH)) {
				String path = entity.getString(RemoteEntityService.ATTR_PATH);
				parentNodeRef = findNodeByPath(path);
				if ( !isRoot && (context.entityPath != null) && path.startsWith(context.entityPath)) {
					isInPath = true;
				}
			

			} else {
				parentNodeRef = null;
			}
		}

		if (!isRoot && !isInPath && (context.entityNodeRef != null) && (parentNodeRef != null)) {
			int i = 0;

			NodeRef folder = parentNodeRef;

			while (folder != null && i < 4) {
				i++;

				if (context.entityNodeRef.equals(folder)) {
					isInPath = true;
					break;
				}

				folder = nodeService.getPrimaryParent(folder).getParentRef();
			}
			
			
		}

		Map<QName, Serializable> properties = jsonToProperties(entity, context);
		Map<QName, List<NodeRef>> associations = jsonToAssocs(entity, context);

		if (!properties.containsKey(propName) && entity.has(RemoteEntityService.ATTR_NAME)) {
			properties.put(propName, entity.getString(RemoteEntityService.ATTR_NAME));
		}

		NodeRef entityNodeRef = null;
		NodeRef jsonEntityNodeRef = null;
		if (entity.has(RemoteEntityService.ATTR_ID)) {
			jsonEntityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, entity.getString(RemoteEntityService.ATTR_ID));
			if (context.cache.containsKey(jsonEntityNodeRef)) {
				entityNodeRef = context.cache.get(jsonEntityNodeRef);
			} else {
				entityNodeRef = jsonEntityNodeRef;
			}

		}

		if ((entityNodeRef == null) || !nodeService.exists(entityNodeRef)) {

			entityNodeRef = findNode(type, parentNodeRef, properties, associations);
			context.cache.put(jsonEntityNodeRef, entityNodeRef);

		}

		if (lookupOnly) {

			if (entityNodeRef == null) {

				String errMsg = "Cannot find node";
				if (assocName != null) {
					errMsg += " for association " + assocName.toPrefixString(namespaceService);
				}

				if (!properties.isEmpty()) {
					errMsg += ", with properties " + properties.toString();
				}
				if (!associations.isEmpty()) {
					errMsg += ", with associations " + associations.toString();
				}

				if (parentNodeRef != null) {
					errMsg += ", in path " + parentNodeRef;
				}
				
				if (isInPath && !context.lastRetry) {
					// will be create later retry
					context.retry = true;

					logger.info("Mark retring for :" + errMsg);

				} else {

					//TODO create is own assoc exception for catching then
					throw new BeCPGException(errMsg);
				}
			}

			return entityNodeRef;
		}

		if (entity.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			associations.putAll(jsonToAssocs(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES), context));
			properties.putAll(jsonToProperties(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES), context));
		}

		if (entityNodeRef == null) {

			String name = (String) properties.get(propName);

			if ((name == null) || name.trim().isEmpty()) {
				name = RemoteEntityService.EMPTY_NAME_PREFIX + UUID.randomUUID().toString();
			}

			if (parentNodeRef == null) {
				parentNodeRef = findNodeByPath(null);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Node not found creating: " + name + " in " + parentNodeRef);

			}

			entityNodeRef = nodeService
					.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), type, properties)
					.getChildRef();

			if (jsonEntityNodeRef != null) {
				context.cache.put(jsonEntityNodeRef, entityNodeRef);
			}

		} else {

			for (Entry<QName, Serializable> prop : properties.entrySet()) {
				if (prop.getValue() == null) {
					nodeService.removeProperty(entityNodeRef, prop.getKey());
				} else {
					nodeService.setProperty(entityNodeRef, prop.getKey(), prop.getValue());
				}
			}
		}

		if (isRoot) {
			context.entityNodeRef = entityNodeRef;
			context.entityPath = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
			
		}

		for (Entry<QName, List<NodeRef>> assocEntry : associations.entrySet()) {
			associationService.update(entityNodeRef, assocEntry.getKey(), assocEntry.getValue());
		}

		if (entity.has(RemoteEntityService.ELEM_DATALISTS)) {
			visitDataLists(entityNodeRef, entity.getJSONObject(RemoteEntityService.ELEM_DATALISTS), context);
		}
		
		if (entity.has(RemoteEntityService.ELEM_CONTENT)) {
			visitContent(entityNodeRef, entity.getJSONObject(RemoteEntityService.ELEM_CONTENT), context);
			
		}

		return entityNodeRef;
	}

	private void visitContent(NodeRef entityNodeRef, JSONObject jsonObject, ImportJsonContext context) {
//	TODO	String mimetype = mimetypeService.guessMimetype(fileName);
//		ContentWriter writer = contentService.getWriter(entityNodeRef, ContentModel.PROP_CONTENT, true);
//		writer.setMimetype(mimetype);
//		try (OutputStream out = writer.getContentOutputStream()) {
//			xmlEntityVisitor.visitData(in, out);
//		} catch (IOException | ParserConfigurationException | SAXException e) {
//			throw new BeCPGException("Cannot create or update entity :" + entityNodeRef + " at format " + format, e);
//		}
		
	}

	private NodeRef findNode(QName type, NodeRef parentNodeRef, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> associations) {
		if (properties.isEmpty() && associations.isEmpty()) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Try to find node of type: " + type + " in " + parentNodeRef);
			if (!properties.isEmpty()) {
				logger.debug(" - properties : " + properties.toString());
			}
			if (!associations.isEmpty()) {
				logger.debug(" - assocs : " + associations.toString());
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
		if (type != null) {
			queryBuilder = queryBuilder.ofType(type);
		}

		if (parentNodeRef != null) {
			queryBuilder = queryBuilder.inParent(parentNodeRef);
		}

		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			if (entry.getValue() != null) {
				queryBuilder = queryBuilder.andPropEquals(entry.getKey(), entry.getValue().toString());
			}
		}

		if ((associations != null) && !associations.isEmpty()) {

			List<NodeRef> nodes = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).inDBIfPossible().list();

			for (Map.Entry<QName, List<NodeRef>> nestedEntry : associations.entrySet()) {

				for (Iterator<NodeRef> iterator = nodes.iterator(); iterator.hasNext();) {
					NodeRef nodeRef = iterator.next();
					if (nodeService.exists(nodeRef)) {

						List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, nestedEntry.getKey());

						boolean foundMatch = false;

						for (AssociationRef assocRef : assocRefs) {
							if (nestedEntry.getValue().contains(assocRef.getTargetRef())) {
								foundMatch = true;
							}
						}

						if (!foundMatch) {
							iterator.remove();
						}
					}
				}

			}

			return (nodes != null) && !nodes.isEmpty() ? nodes.get(0) : null;

		} else {
			return queryBuilder.inDBIfPossible().ftsLanguage().singleValue();
		}

	}

	@SuppressWarnings("unchecked")
	private void visitDataLists(NodeRef entityNodeRef, JSONObject datalists, ImportJsonContext context) throws JSONException {

		boolean replaceExisting = remoteParams.extractParams(RemoteParams.PARAM_REPLACE_EXISTING_LISTS, false);
		String dataListsToReplace = remoteParams.extractParams(RemoteParams.PARAM_DATALISTS_TO_REPLACE, "");

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}

		new LinkedList<>();

		Iterator<String> iterator = datalists.keys();

		while (iterator.hasNext()) {
			String key = iterator.next();
			QName dataListQName = createQName(key);

			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, dataListQName);
			if (listNodeRef == null) {
				logger.debug("Creating list: " + dataListQName + " in " + listContainerNodeRef);
				listNodeRef = entityListDAO.createList(listContainerNodeRef, dataListQName);
			}

			Set<NodeRef> listItemToKeep = new HashSet<>();

			JSONArray items = datalists.getJSONArray(key);

			for (int i = 0; i < items.length(); i++) {
				JSONObject listItem = items.getJSONObject(i);
				listItem.put(RemoteEntityService.ATTR_PARENT_ID, listNodeRef.getId());
				if (!listItem.has(RemoteEntityService.ATTR_TYPE)) {
					listItem.put(RemoteEntityService.ATTR_TYPE, dataListQName);
				}
				try {
					listItemToKeep.add(visit(listItem, false, null, context, false));
				} catch (BeCPGException e) {
					if (Boolean.TRUE.equals(remoteParams.extractParams(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, Boolean.TRUE))) {
						throw e;
					}

				}
			}

			if (replaceExisting || dataListsToReplace.contains(key)) {
				for (NodeRef tmp : entityListDAO.getListItems(listNodeRef, dataListQName)) {
					if (!listItemToKeep.contains(tmp)) {
						nodeService.addAspect(tmp, ContentModel.ASPECT_TEMPORARY, null);
						nodeService.deleteNode(tmp);
					}
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Map<QName, List<NodeRef>> jsonToAssocs(JSONObject entity, ImportJsonContext context) throws JSONException {
		Map<QName, List<NodeRef>> assocs = new HashMap<>();

		Iterator<String> iterator = entity.keys();

		while (iterator.hasNext()) {
			String key = iterator.next();
			String propName = key;

			if (!ignoredKeys.contains(propName)) {
				QName propQName = createQName(propName);

				AssociationDefinition ad = entityDictionaryService.getAssociation(propQName);
				if (ad != null) {
					List<NodeRef> tmp = new ArrayList<>();
					if (entity.get(key) != null) {

						if (entity.get(key) instanceof JSONArray) {

							JSONArray values = entity.getJSONArray(key);
							for (int i = 0; i < values.length(); i++) {

								JSONObject assocEntity = values.getJSONObject(i);

								appendAssoc(tmp, assocEntity, ad.getTargetClass().getName(), propQName, ad.isChild(), context);

							}

						} else {

							JSONObject assocEntity = entity.getJSONObject(key);

							appendAssoc(tmp, assocEntity, ad.getTargetClass().getName(), propQName, ad.isChild(), context);
						}

					}

					if (ad.isTargetMandatory() && tmp.isEmpty()) {
						throw new BeCPGException("Mandatory association not found");
					}

					assocs.put(propQName, tmp);

				}
			}
		}

		return assocs;
	}

	private void appendAssoc(List<NodeRef> nodes, JSONObject assocEntity, QName type, QName propQName, boolean isChild, ImportJsonContext context)
			throws JSONException {
		if (!assocEntity.has(RemoteEntityService.ATTR_TYPE)) {
			assocEntity.put(RemoteEntityService.ATTR_TYPE, type);
		}

		try {
			NodeRef tmp = visit(assocEntity, !isChild, propQName, context, false);
			if(tmp != null){
				nodes.add(tmp);
			}
		} catch (BeCPGException e) {
			if (Boolean.TRUE.equals(remoteParams.extractParams(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, Boolean.TRUE))) {
				throw e;
			}

		}

	}

	@SuppressWarnings("unchecked")
	private Map<QName, Serializable> jsonToProperties(JSONObject entity, ImportJsonContext context) throws JSONException {
		Map<QName, Serializable> nodeProps = new HashMap<>();

		Iterator<String> iterator = entity.keys();

		while (iterator.hasNext()) {

			String key = iterator.next();
			String propName = key;
			boolean isMlText = false;
			if (key.contains("_")) {
				propName = key.split("_")[0];
				isMlText = true;
			}

			if (!ignoredKeys.contains(propName)) {

				QName propQName = createQName(propName);

				PropertyDefinition pd = entityDictionaryService.getProperty(propQName);
				if (pd != null) {

					if (DataTypeDefinition.MLTEXT.equals(pd.getDataType().getName())) {

						String value = null;
						MLText mlValue = null;
						if (nodeProps.get(propQName) != null) {

							if (nodeProps.get(propQName) instanceof String) {
								value = (String) nodeProps.get(propQName);
							} else if (nodeProps.get(propQName) instanceof MLText) {
								mlValue = (MLText) nodeProps.get(propQName);
							}

						}

						if (key.contains("_")) {
							Locale locale = MLTextHelper.parseLocale(key.split("_")[1]);
							if (MLTextHelper.isSupportedLocale(locale)) {
								if (mlValue == null) {
									mlValue = new MLText();
								}

								if (entity.getString(key) != null) {
									mlValue.addValue(locale, entity.getString(key));
								} else {
									mlValue.removeValue(locale);
								}

								if (value != null) {
									mlValue.addValue(MLTextHelper.getNearestLocale(Locale.getDefault()), value);
								}
								nodeProps.put(propQName, mlValue);
							}

						} else {
							value = entity.getString(key);

							if (value.startsWith("{") && value.endsWith("}")) {

								MLText mlText = new MLText();

								String content = value.replace("{", "").replace("}", "");
								String[] contents = content.split(",");

								for (String cont : contents) {
									if (cont.contains(":")) {
										String locale = cont.split(":")[0];
										String actualValue = cont.split(":")[1].replace("\"", "");
										mlText.addValue(MLTextHelper.parseLocale(locale), actualValue);
									}
								}

								nodeProps.put(propQName, mlText);
							} else {
								nodeProps.put(propQName, value);
							}
						}

					} else if (!isMlText) {
						Serializable value = null;
						if ((entity.get(key) != null) && !JSONObject.NULL.equals(entity.get(key))) {
							if (pd.isMultiValued() && (entity.getJSONArray(key) != null)) {

								value = new ArrayList<Serializable>();
								JSONArray values = entity.getJSONArray(key);
								for (int i = 0; i < values.length(); i++) {

									Serializable val;

									if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) {
										val = visit(values.getJSONObject(i), true, propQName, context, false);
									} else {
										if (RemoteHelper.isJSONValue(propQName)) {
											val = values.getJSONObject(i).toString();
										} else {
											val = (Serializable) values.get(i);
										}
									}
									((List<Serializable>) value).add(val);
								}

							} else {
								if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) {
									value = visit(entity.getJSONObject(key), true, propQName, context, false);
								} else {
									if (RemoteHelper.isJSONValue(propQName)) {
										value = entity.getJSONObject(key).toString();
									} else {
										value = (Serializable) entity.get(key);
									}

								}

							}

							nodeProps.put(propQName, value);
						} else {
							nodeProps.put(propQName, null);
						}

					}

				}

			}

		}

		return nodeProps;
	}

	private NodeRef findNodeByPath(String parentPath) {
		NodeRef ret = null;

		NodeRef rootNode = nodeService.getRootNode(RepoConsts.SPACES_STORE);
		if ((parentPath != null) && !parentPath.isEmpty()) {
			ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, parentPath);
		}

		if (ret == null) {
			ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(rootNode, RemoteEntityService.FULL_PATH_IMPORT_TO_DO);
		}

		return ret;

	}

	/**
	 * Helper to create a QName from either a fully qualified or short-name
	 * QName string
	 *
	 * @param qnameStr
	 *            Fully qualified or short-name QName string
	 * @return QName
	 */
	public QName createQName(String qnameStr) {
		try {
			if ((qnameStr != null) && qnameStr.contains("|")) {
				qnameStr = qnameStr.split("|")[0];
			}

			QName qname;
			if ((qnameStr != null) && (qnameStr.indexOf(QName.NAMESPACE_BEGIN) != -1)) {
				qname = QName.createQName(qnameStr);
			} else {
				qname = QName.createQName(qnameStr, namespaceService);
			}
			return qname;
		} catch (Exception ex) {
			String msg = ex.getMessage();
			if (msg == null) {
				msg = "";
			}
			throw new InvalidArgumentException(qnameStr + " isn't a valid QName. " + msg);
		}

	}
}
