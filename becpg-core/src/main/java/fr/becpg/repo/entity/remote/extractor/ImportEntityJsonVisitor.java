package fr.becpg.repo.entity.remote.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 *
 * @author matthieu
 *
 */
public class ImportEntityJsonVisitor {

	private static Log logger = LogFactory.getLog(ImportEntityJsonVisitor.class);

	Set<String> ignoredKeys = new HashSet<>();

	{
		ignoredKeys.add(RemoteEntityService.ATTR_TYPE);
		ignoredKeys.add(RemoteEntityService.ATTR_PATH);
		ignoredKeys.add(RemoteEntityService.ATTR_NODEREF);
		ignoredKeys.add(RemoteEntityService.ELEM_ATTRIBUTES);
		ignoredKeys.add(RemoteEntityService.ELEM_DATALISTS);
		ignoredKeys.add(RemoteEntityService.ATTR_SITE);
		ignoredKeys.add(RemoteEntityService.ATTR_PARENT_ID);
	}

	EntityDictionaryService entityDictionaryService;

	NamespaceService namespaceService;

	AssociationService associationService;

	NodeService nodeService;

	EntityListDAO entityListDAO;

	public ImportEntityJsonVisitor(EntityDictionaryService entityDictionaryService, NamespaceService namespaceService,
			AssociationService associationService, NodeService nodeService, EntityListDAO entityListDAO) {
		super();
		this.entityDictionaryService = entityDictionaryService;
		this.namespaceService = namespaceService;
		this.associationService = associationService;
		this.nodeService = nodeService;
		this.entityListDAO = entityListDAO;
	}

	public NodeRef visit(NodeRef entityNodeRef, InputStream in) throws IOException, JSONException, BeCPGException {

		try (Reader reader = new InputStreamReader(in)) {

			JSONTokener tokener = new JSONTokener(reader);
			JSONObject entity = new JSONObject(tokener);
			if (entityNodeRef != null) {
				entity.put(RemoteEntityService.ATTR_ID, entityNodeRef.getId());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Visiting: " + entity.toString(3));
			}
			if (entity.has(RemoteEntityService.ELEM_ENTITY)) {
				return visit(entity.getJSONObject(RemoteEntityService.ELEM_ENTITY), false);
			}
			return null;

		}

	}

	private NodeRef visit(JSONObject entity, boolean lookupOnly) throws JSONException {

		QName type = null;
		String path = null;

		QName propName = ContentModel.PROP_NAME;

		if (entity.has(RemoteEntityService.ATTR_TYPE)) {
			type = createQName(entity.getString(RemoteEntityService.ATTR_TYPE));
			propName = RemoteHelper.getPropName(type, entityDictionaryService);
		}

		if (entity.has(RemoteEntityService.ATTR_PATH)) {
			path = entity.getString(RemoteEntityService.ATTR_PATH);
		}

		NodeRef parentNodeRef = null;

		if (entity.has(RemoteEntityService.ATTR_PARENT_ID)) {
			parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, entity.getString(RemoteEntityService.ATTR_PARENT_ID));
		}

		Map<QName, Serializable> properties = jsonToProperties(entity);
		Map<QName, List<NodeRef>> associations = jsonToAssocs(entity);

		NodeRef entityNodeRef = null;

		if (entity.has(RemoteEntityService.ATTR_ID)) {
			entityNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, entity.getString(RemoteEntityService.ATTR_ID));
		}

		if ((entityNodeRef == null) || !nodeService.exists(entityNodeRef)) {

			entityNodeRef = findNode(type, parentNodeRef, path, properties, associations);
		}

		if (lookupOnly) {
			return entityNodeRef;
		}

		if (entity.has(RemoteEntityService.ELEM_ATTRIBUTES)) {
			properties.putAll(jsonToProperties(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES)));
			associations.putAll(jsonToAssocs(entity.getJSONObject(RemoteEntityService.ELEM_ATTRIBUTES)));
		}

		if (entityNodeRef == null) {

			if ((parentNodeRef == null) || !nodeService.exists(parentNodeRef)) {
				parentNodeRef = findNodeByPath(path);

			}

			String name = (String) properties.get(propName);

			if ((name == null) || name.trim().isEmpty()) {
				name = RemoteEntityService.EMPTY_NAME_PREFIX + UUID.randomUUID().toString();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Node not found creating: " + name + " in " + parentNodeRef);

			}

			entityNodeRef = nodeService
					.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), type, properties)
					.getChildRef();

		} else {
			for (Entry<QName, Serializable> prop : properties.entrySet()) {
				if (prop.getValue() == null) {
					nodeService.removeProperty(entityNodeRef, prop.getKey());
				} else {
					nodeService.setProperty(entityNodeRef, prop.getKey(), prop.getValue());
				}
			}
		}

		for (Entry<QName, List<NodeRef>> assocEntry : associations.entrySet()) {
			associationService.update(entityNodeRef, assocEntry.getKey(), assocEntry.getValue());
		}

		if (entity.has(RemoteEntityService.ELEM_DATALISTS)) {
			visitDataLists(entityNodeRef, entity.getJSONObject(RemoteEntityService.ELEM_DATALISTS));
		}

		return entityNodeRef;
	}

	private NodeRef findNode(QName type, NodeRef parentNodeRef, String path, Map<QName, Serializable> properties,
			Map<QName, List<NodeRef>> associations) {
		if (logger.isDebugEnabled()) {
			logger.debug("Try to find node of type: " + type + " in " + path);
			logger.debug(" - properties : " + properties.toString());
			logger.debug(" - assocs : " + associations.toString());
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();
		if (type != null) {
			queryBuilder = queryBuilder.ofType(type);
		}
		if (path != null) {
			queryBuilder = queryBuilder.inPath(path);
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

			List<NodeRef> nodes = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).inDBIfPossible().ftsLanguage().list();

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
	private void visitDataLists(NodeRef entityNodeRef, JSONObject datalists) throws JSONException {
		Iterator<String> iterator = datalists.keys();

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entityNodeRef);
		}

		while (iterator.hasNext()) {
			String key = iterator.next();
			QName dataListQName = createQName(key);

			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, dataListQName);
			if (listNodeRef == null) {
				logger.debug("Creating list: " + dataListQName + " in " + listContainerNodeRef);
				listNodeRef = entityListDAO.createList(listContainerNodeRef, dataListQName);
			}

			JSONArray items = datalists.getJSONArray(key);
			for (int i = 0; i < items.length(); i++) {
				JSONObject listItem = items.getJSONObject(i);
				listItem.put(RemoteEntityService.ATTR_PARENT_ID, listNodeRef.getId());
				if (!listItem.has(RemoteEntityService.ATTR_TYPE)) {
					listItem.put(RemoteEntityService.ATTR_TYPE, dataListQName);
				}

				visit(listItem, false);
			}

		}

	}

	@SuppressWarnings("unchecked")
	private Map<QName, List<NodeRef>> jsonToAssocs(JSONObject entity) throws JSONException {
		Map<QName, List<NodeRef>> assocs = new HashMap<>();

		Iterator<String> iterator = entity.keys();

		while (iterator.hasNext()) {
			String key = iterator.next();
			String propName = key;

			if (!ignoredKeys.contains(propName)) {
				QName propQName = createQName(propName);

				AssociationDefinition ad = entityDictionaryService.getAssociation(propQName);
				if (ad != null) {

					if (entity.get(key) != null) {

						List<NodeRef> tmp = new ArrayList<>();

						if (entity.get(key) instanceof JSONArray) {

							JSONArray values = entity.getJSONArray(key);
							for (int i = 0; i < values.length(); i++) {

								JSONObject assocEntity = values.getJSONObject(i);
								if (!assocEntity.has(RemoteEntityService.ATTR_TYPE)) {
									assocEntity.put(RemoteEntityService.ATTR_TYPE, ad.getTargetClass().getName());
								}

								NodeRef ret = visit(assocEntity, true);
								if (ret != null) {
									tmp.add(ret);
								}

							}

						} else {

							JSONObject assocEntity = entity.getJSONObject(key);
							if (!assocEntity.has(RemoteEntityService.ATTR_TYPE)) {
								assocEntity.put(RemoteEntityService.ATTR_TYPE, ad.getTargetClass().getName());
							}

							NodeRef ret = visit(assocEntity, true);
							if (ret != null) {
								tmp.add(ret);
							}
						}

						assocs.put(propQName, tmp);
					}

				}
			}
		}

		return assocs;
	}

	@SuppressWarnings("unchecked")
	private Map<QName, Serializable> jsonToProperties(JSONObject entity) throws JSONException {
		Map<QName, Serializable> nodeProps = new HashMap<>();

		Iterator<String> iterator = entity.keys();

		while (iterator.hasNext()) {

			String key = iterator.next();
			String propName = key;
			if (key.contains("_")) {
				propName = key.split("_")[0];
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
							nodeProps.put(propQName, value);
						}

					} else {
						Serializable value = null;
						if (entity.get(key) != null) {
							if (pd.isMultiValued() && (entity.getJSONArray(key) != null)) {

								value = new ArrayList<Serializable>();
								JSONArray values = entity.getJSONArray(key);
								for (int i = 0; i < values.length(); i++) {

									Serializable val;

									if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) {
										val = visit(values.getJSONObject(i), true);
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
									value = visit(entity.getJSONObject(key), true);
								} else {
									if (RemoteHelper.isJSONValue(propQName)) {
										value = entity.getJSONObject(key).toString();
									} else {
										value = (Serializable) entity.get(key);
									}

								}

							}

							nodeProps.put(propQName, value);
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
			QName qname;
			if (qnameStr.indexOf(QName.NAMESPACE_BEGIN) != -1) {
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
