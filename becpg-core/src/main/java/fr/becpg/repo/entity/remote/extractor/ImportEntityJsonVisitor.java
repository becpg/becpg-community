package fr.becpg.repo.entity.remote.extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.entity.remote.extractor.RemoteJSONContext.JsonVisitNodeType;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
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

	private EntityDictionaryService entityDictionaryService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	private NodeService nodeService;

	private EntityListDAO entityListDAO;

	private ContentService contentService;

	private MimetypeService mimetypeService;

	private RemoteParams remoteParams;
	
	private PermissionService permissionService;

	/**
	 * <p>Constructor for ImportEntityJsonVisitor.</p>
	 * @param serviceRegistry
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public ImportEntityJsonVisitor(RemoteServiceRegisty remoteRegisty) {
		super();
		this.entityDictionaryService = remoteRegisty.entityDictionaryService();
		this.namespaceService = remoteRegisty.namespaceService();
		this.associationService = remoteRegisty.associationService();
		this.nodeService = remoteRegisty.nodeService();
		this.entityListDAO = remoteRegisty.entityListDAO();
		this.mimetypeService = remoteRegisty.mimetypeService();
		this.contentService = remoteRegisty.contentService();
		this.permissionService = remoteRegisty.permissionService();
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

			RemoteJSONContext context = new RemoteJSONContext();
			NodeRef ret = null;
			int retryCount = 0;
			while (context.isRetry() && (retryCount < 3)) {
				if (retryCount == 1) {
					logger.debug("Last retry");
					context.setLastRetry(true);
				}
				context.setRetry(false);
				ret = visit(entity, JsonVisitNodeType.ENTITY, null, context);
				retryCount++;
				logger.debug("Retrying count:" + retryCount);
			}

			return ret;
		}

		throw new BeCPGException("No entity found in JSON");

	}

	private NodeRef visit(JSONObject entity, JsonVisitNodeType jsonType, QName assocName, RemoteJSONContext context) throws JSONException {

		QName type = null;

		QName propName = ContentModel.PROP_NAME;
		if(assocName == null) {
			assocName = ContentModel.ASSOC_CONTAINS;
		}
		
		boolean isInPath = false;
		String path = null;

		if (entity.has(RemoteEntityService.ATTR_TYPE)) {
			type = createQName(entity.getString(RemoteEntityService.ATTR_TYPE));
			propName = RemoteHelper.getPropName(type, entityDictionaryService);
		}

		if (entity.has(RemoteEntityService.ATTR_PATH)) {
			path = entity.getString(RemoteEntityService.ATTR_PATH);
			if (path.startsWith("~")) {
				isInPath = true;
				if (context.getEntityNodeRef() != null) {
					path = path.replace("~", context.getEntityPath(nodeService, namespaceService));
				} else {
					logger.debug("Path not found for: "+path);
					path = null;
				}
			}

		}

		NodeRef parentNodeRef = null;

		if (JsonVisitNodeType.CHILD_ASSOC.equals(jsonType) && (context.getCurrentNodeRef() != null)) {
			parentNodeRef = context.getCurrentNodeRef();
			isInPath = true;
		} else if (entity.has(RemoteEntityService.ATTR_PARENT_ID)) {
			parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, entity.getString(RemoteEntityService.ATTR_PARENT_ID));
			if (context.getCache().containsKey(parentNodeRef)) {
				parentNodeRef = context.getCache().get(parentNodeRef);
			}
		}

		if (((parentNodeRef == null) || !nodeService.exists(parentNodeRef))) {
			if (path != null) {
				parentNodeRef = findNodeByPath(path);
			} else {
				parentNodeRef = null;
			}

		}

		if (JsonVisitNodeType.ASSOC.equals(jsonType) && !isInPath && (parentNodeRef != null) && (context.getEntityNodeRef() != null)) {
			int i = 0;

			NodeRef folder = parentNodeRef;

			while ((folder != null) && (i < 4)) {
				i++;

				if (context.getEntityNodeRef().equals(folder)) {
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
			if (context.getCache().containsKey(jsonEntityNodeRef)) {
				entityNodeRef = context.getCache().get(jsonEntityNodeRef);
			} else {
				entityNodeRef = jsonEntityNodeRef;
			}

		}

		if ((entityNodeRef == null) || !nodeService.exists(entityNodeRef)) {

			if (JsonVisitNodeType.CHILD_ASSOC.equals(jsonType)) {
				if (parentNodeRef != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("Try to find child: "+assocName+ " "+ (String) properties.get(propName));
					}
					entityNodeRef = nodeService.getChildByName(parentNodeRef, assocName, (String) properties.get(propName));
				}
			} else {
				entityNodeRef = findNode(type, parentNodeRef, properties, associations);
			}
			if (jsonEntityNodeRef != null) {
				context.getCache().put(jsonEntityNodeRef, entityNodeRef);
			}

		}

		if (JsonVisitNodeType.ASSOC.equals(jsonType)) {

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
				
				errMsg += ", isInPath " + isInPath+ ", lastRetry:"+context.isLastRetry();

				if (isInPath && !context.isLastRetry()) {
					// will be create later retry
					context.setRetry(true);
					logger.debug("Mark retring for :" + errMsg);
				} else {
				
					throw new BeCPGException(errMsg);
				}
			}

			return entityNodeRef;
		}
		
		if (JsonVisitNodeType.ENTITY.equals(jsonType) && entityNodeRef != null
				&& nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ARCHIVED_ENTITY)) {
			throw new BeCPGException("Cannot update entity since it is archived: " + entityNodeRef);
		}

		if (entity.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			properties.putAll(jsonToProperties(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES), context));
		}

		String name = null;
		
		if (properties.get(propName) instanceof String) {
			name = (String) properties.get(propName);
		} else if (properties.get(propName) instanceof MLText) {
			name = ((MLText) properties.get(propName)).getDefaultValue();
		}

		if ((name == null) || name.trim().isEmpty()) {
			name = RemoteEntityService.EMPTY_NAME_PREFIX + UUID.randomUUID().toString();
		}

		if (entityNodeRef == null) {

			if (parentNodeRef == null) {
				if (JsonVisitNodeType.CHILD_ASSOC.equals(jsonType)) {
					context.setRetry(true);
					if(logger.isDebugEnabled()) {
						logger.debug("Parent not found for child assoc retrying, with properties: " + properties.toString());
					}
					return null;
				}

				parentNodeRef = findNodeByPath(null);
			}

			if (logger.isDebugEnabled()) {
				logger.debug(" - Node not found creating: " + name + " in " + parentNodeRef);
			}
			
			if (permissionService.hasPermission(parentNodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED) {
				throw new IllegalAccessError("You have no rights to perform this operation");
			}

			entityNodeRef = nodeService
					.createNode(parentNodeRef, assocName,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), type, properties)
					.getChildRef();

			if (jsonEntityNodeRef != null) {
				context.getCache().put(jsonEntityNodeRef, entityNodeRef);
			}

		} else {
			
			if (permissionService.hasPermission(entityNodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED) {
				throw new IllegalAccessError("You have no rights to perform this operation");
			}

			for (Entry<QName, Serializable> prop : properties.entrySet()) {
				if (prop.getValue() == null) {
					nodeService.removeProperty(entityNodeRef, prop.getKey());
				} else {
					nodeService.setProperty(entityNodeRef, prop.getKey(), prop.getValue());
				}
			}
		}

		if (JsonVisitNodeType.ENTITY.equals(jsonType)) {
			context.setEntityNodeRef(entityNodeRef);
		} 
			
		NodeRef prevCurrentNodeRef = context.getCurrentNodeRef();
		try {
			context.setCurrentNodeRef(entityNodeRef);
			if (entity.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
				associations.putAll(jsonToAssocs(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES), context));
			}
	
			for (Entry<QName, List<NodeRef>> assocEntry : associations.entrySet()) {
				associationService.update(entityNodeRef, assocEntry.getKey(), assocEntry.getValue());
			}
	
			if (entity.has(RemoteEntityService.ELEM_DATALISTS)) {
				visitDataLists(entityNodeRef, entity.getJSONObject(RemoteEntityService.ELEM_DATALISTS), context);
			}
	
			if (entity.has(RemoteEntityService.ELEM_CONTENT)) {
				visitContent(entityNodeRef, name, entity.getString(RemoteEntityService.ELEM_CONTENT));
			}
			
		} finally {
			context.setCurrentNodeRef(prevCurrentNodeRef);
		}
		
		return entityNodeRef;
	}

	private void visitContent(NodeRef entityNodeRef, String fileName, String content) {
		String mimetype = mimetypeService.guessMimetype(fileName);
		ContentWriter writer = contentService.getWriter(entityNodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(mimetype);
		try (InputStream contentStream = new ByteArrayInputStream(content.getBytes());
				InputStream in = new Base64InputStream(contentStream, false, -1, null)) {
			writer.putContent(in);
		} catch (IOException e) {
			throw new BeCPGException("Cannot import JSON content");
		}

	}

	private NodeRef findNode(QName type, NodeRef parentNodeRef, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> associations) throws JSONException {
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
		
		boolean ignorePath = false;
		if(properties.containsKey(BeCPGModel.PROP_CODE)  || properties.containsKey(BeCPGModel.PROP_ERP_CODE)
			&& 	Boolean.TRUE.equals(remoteParams.extractParams(RemoteParams.PARAM_IGNORE_PATH_FOR_SEARCH, Boolean.FALSE))
				) {
			ignorePath = true;
		}
		

		if (parentNodeRef != null && !ignorePath) {
			queryBuilder = queryBuilder.inParent(parentNodeRef);
		}

		for (Entry<QName, Serializable> entry : properties.entrySet()) {
			if (entry.getValue() != null) {
				
				String stringValue = entry.getValue().toString();
				
				if (entry.getValue() instanceof MLText) {
					stringValue = ((MLText) entry.getValue()).getDefaultValue();
				}
				
				queryBuilder = queryBuilder.andPropEquals(entry.getKey(), stringValue);
			}
		}

		if ((associations != null) && !associations.isEmpty()) {

			List<NodeRef> nodes = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).excludeVersions().inDBIfPossible().list();

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
			return queryBuilder.inDBIfPossible().excludeVersions().ftsLanguage().singleValue();
		}

	}

	private void visitDataLists(NodeRef entityNodeRef, JSONObject datalists, RemoteJSONContext context) throws JSONException {

		boolean replaceExisting = remoteParams.extractParams(RemoteParams.PARAM_REPLACE_EXISTING_LISTS, false);
		String dataListsToReplace = remoteParams.extractParams(RemoteParams.PARAM_DATALISTS_TO_REPLACE, "");

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}

		Iterator<String> iterator = datalists.keys();

		while (iterator.hasNext()) {
			String key = iterator.next();
			String listName = key;
			String typeName = key;
			
			if (key.contains("@")) {
				listName = key.split("@")[0];
				typeName = key.split("@")[1];
			}
			
			QName dataListQName = createQName(listName);
			QName dataListTypeQName = createQName(typeName);
			
			String dataListName = getListName(listName);

			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, dataListName);
			if (listNodeRef == null) {
				logger.debug("Creating list: " + dataListQName + " in " + listContainerNodeRef);
				listNodeRef = entityListDAO.createList(listContainerNodeRef, dataListName, dataListQName);

				ClassDefinition classDef = entityDictionaryService.getClass(dataListQName);
				
				if (classDef != null) {
					MLText classTitleMLText = TranslateHelper.getTemplateTitleMLText(classDef.getName());
					MLText classDescritptionMLText = TranslateHelper.getTemplateDescriptionMLText(classDef.getName());
					
					nodeService.setProperty(listNodeRef, ContentModel.PROP_TITLE, classTitleMLText);
					nodeService.setProperty(listNodeRef, ContentModel.PROP_DESCRIPTION, classDescritptionMLText);
				}
			}

			Set<NodeRef> listItemToKeep = new HashSet<>();

			JSONArray items = datalists.getJSONArray(key);

			for (int i = 0; i < items.length(); i++) {
				JSONObject listItem = items.getJSONObject(i);
				listItem.put(RemoteEntityService.ATTR_PARENT_ID, listNodeRef.getId());
				if (!listItem.has(RemoteEntityService.ATTR_TYPE)) {
					listItem.put(RemoteEntityService.ATTR_TYPE, dataListTypeQName.toPrefixString(namespaceService));
				}
				try {
					listItemToKeep.add(visit(listItem, JsonVisitNodeType.DATALIST, null, context));
				} catch (BeCPGException e) {
					if (Boolean.TRUE.equals(remoteParams.extractParams(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, Boolean.TRUE))) {
						throw e;
					}

				}
			}

			if (replaceExisting || dataListsToReplace.contains(key)) {
				for (NodeRef tmp : entityListDAO.getListItems(listNodeRef, dataListTypeQName)) {
					if (!listItemToKeep.contains(tmp)) {
						nodeService.addAspect(tmp, ContentModel.ASPECT_TEMPORARY, null);
						nodeService.deleteNode(tmp);
					}
				}
			}
		}

	}
	
	private String getListName(String qnameStr) {
		if ((qnameStr != null) && qnameStr.contains("|")) {
			return qnameStr.split("\\|")[1];
		}
		
		QName qname;
		
		if ((qnameStr != null) && (qnameStr.indexOf(QName.NAMESPACE_BEGIN) != -1)) {
			qname = QName.createQName(qnameStr);
		} else {
			qname = QName.createQName(qnameStr, namespaceService);
		}
		return qname.getLocalName();
	}

	private Map<QName, List<NodeRef>> jsonToAssocs(JSONObject entity, RemoteJSONContext context) throws JSONException {
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

	private void appendAssoc(List<NodeRef> nodes, JSONObject assocEntity, QName type, QName propQName, boolean isChild, RemoteJSONContext context)
			throws JSONException {
		if (!assocEntity.has(RemoteEntityService.ATTR_TYPE)) {
			assocEntity.put(RemoteEntityService.ATTR_TYPE, type.toPrefixString(namespaceService));
		}

		try {
			NodeRef tmp = visit(assocEntity, isChild ? JsonVisitNodeType.CHILD_ASSOC : JsonVisitNodeType.ASSOC, propQName, context);
			if (tmp != null) {
				nodes.add(tmp);
			}
		} catch (BeCPGException e) {
			if (Boolean.TRUE.equals(remoteParams.extractParams(RemoteParams.PARAM_FAIL_ON_ASSOC_NOT_FOUND, Boolean.TRUE))) {
				throw e;
			}

		}

	}

	@SuppressWarnings("unchecked")
	private Map<QName, Serializable> jsonToProperties(JSONObject entity, RemoteJSONContext context) throws JSONException {
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
							String[] keyParts = key.split("_");
							String localKey = keyParts.length > 0 ? keyParts[1] : "";
							if (keyParts.length > 2) {
							    localKey += "_" + keyParts[2];
							}
							Locale locale = MLTextHelper.parseLocale(localKey);
							
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
							value = entity.get(key).toString();

							if (value.startsWith("{") && value.endsWith("}")) {

								MLText mlText = new MLText();

								String content = value.substring(1, value.length() - 1);
								
								String[] contents = content.split(",");

								for (String cont : contents) {
									if (cont.contains(":")) {
										String locale = cont.split(":")[0];
										
										Locale parseLocale = MLTextHelper.parseLocale(locale);
										
										if (MLTextHelper.isSupportedLocale(parseLocale)) {
											int index = cont.indexOf(":");
											String actualValue = cont.substring(index + 1);
											
											if (actualValue.length() > 1 && actualValue.startsWith("\"") && actualValue.endsWith("\"")) {
												actualValue = actualValue.substring(1, actualValue.length() - 1);
											}
											
											mlText.addValue(parseLocale, actualValue);
										}
									}
								}

								nodeProps.put(propQName, mlText);
							} else {
								if (mlValue != null) {
									mlValue.addValue(MLTextHelper.getNearestLocale(Locale.getDefault()), value);
									nodeProps.put(propQName, mlValue);
								} else {
									nodeProps.put(propQName, value);
								}
							}
						}

					} else if (!isMlText) {
						Serializable value = null;
						if ((entity.get(key) != null) && !JSONObject.NULL.equals(entity.get(key))) {
							if (pd.isMultiValued() && entity.get(key) instanceof JSONArray ) {

								value = new ArrayList<Serializable>();
								JSONArray values = entity.getJSONArray(key);
								for (int i = 0; i < values.length(); i++) {

									Serializable val;

									if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF) || pd.getDataType().getName().equals(DataTypeDefinition.CATEGORY) ) {
										val = visit(values.getJSONObject(i), JsonVisitNodeType.ASSOC, propQName, context);
									} else {
										if (RemoteHelper.isJSONValue(propQName) || values.get(i) instanceof JSONObject) {
											val = values.getJSONObject(i).toString();
										} else {
											val = (Serializable) values.get(i);
										}
									}
									((List<Serializable>) value).add(val);
								}

							} else {
								if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF) || pd.getDataType().getName().equals(DataTypeDefinition.CATEGORY) ) {
									value = visit(entity.getJSONObject(key), JsonVisitNodeType.ASSOC, propQName, context);
								} else {
									if (RemoteHelper.isJSONValue(propQName) || entity.get(key) instanceof JSONObject) {
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
				qnameStr = qnameStr.split("\\|")[0];
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
