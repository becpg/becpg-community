package fr.becpg.repo.repository.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;

import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListView;

@Repository
public class AlfrescoRepositoryImpl<T extends RepositoryEntity> implements AlfrescoRepository<T> {

	private NodeService nodeService;

	private NodeService mlNodeService;

	private static Log logger = LogFactory.getLog(AlfrescoRepositoryImpl.class);

	private RepositoryEntityDefReader<T> repositoryEntityDefReader;

	private EntityListDAO entityListDAO;

	private AssociationService associationService;

	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<T> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	private Map<QName, Class<? extends RepositoryEntity>> domainMapping = new HashMap<QName, Class<? extends RepositoryEntity>>();

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@SuppressWarnings("unchecked")
	public void init() throws ClassNotFoundException {
		logger.debug("Scanning classpath for AlfType annotation");
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(AlfType.class));

		for (BeanDefinition bd : scanner.findCandidateComponents("fr.becpg.*")) {
			registerEntity((Class<? extends RepositoryEntity>) Class.forName(bd.getBeanClassName()));
		}

	}

	private void registerEntity(Class<? extends RepositoryEntity> clazz) {
		logger.debug("Register entity : " + clazz.getName());
		domainMapping.put(repositoryEntityDefReader.getType(clazz), clazz);
	}

	@Override
	public T create(NodeRef parentNodeRef, T entity) {
		entity.setParentNodeRef(parentNodeRef);
		return save(entity);
	}

	@Override
	public T save(T entity) {

		if (entity.getNodeRef() == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Create instanceOf :" + entity.getClass().getName());
			}
			Map<QName, Serializable> properties = extractProperties(entity);

			String name = entity.getName();
			if (entity.getName() == null) {
				name = UUID.randomUUID().toString();
			}

			properties.put(ContentModel.PROP_NAME, name);

			NodeRef productNodeRef = nodeService.createNode(entity.getParentNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), repositoryEntityDefReader.getType(entity.getClass()), properties)
					.getChildRef();
			entity.setNodeRef(productNodeRef);

		} else {
			logger.debug("Update instanceOf :" + entity.getClass().getName());

			nodeService.addProperties(entity.getNodeRef(), extractProperties(entity));
		}

		saveAssociations(entity);
		saveDataLists(entity);
		saveDataListViews(entity);

		return entity;
	}

	private Map<QName, Serializable> extractProperties(T entity) {

		Map<QName, Serializable> properties = repositoryEntityDefReader.getProperties(entity);

		for (Map.Entry<QName, T> prop : repositoryEntityDefReader.getEntityProperties(entity).entrySet()) {
			NodeRef assocNodeRef = null;
			if (prop.getValue() != null) {
				if (prop.getValue().getNodeRef() == null && prop.getValue().getParentNodeRef() == null) {
					prop.getValue().setParentNodeRef(entity.getParentNodeRef());
				}
				save(prop.getValue());
				assocNodeRef = prop.getValue().getNodeRef();
			}

			properties.put(prop.getKey(), assocNodeRef);
		}

		return properties;

	}

	private void saveAssociations(T entity) {

		for (Map.Entry<QName, T> association : repositoryEntityDefReader.getSingleEntityAssociations(entity).entrySet()) {
			NodeRef assocNodeRef = null;
			if (association.getValue() != null) {
				if (association.getValue().getNodeRef() == null && association.getValue().getParentNodeRef() == null) {
					association.getValue().setParentNodeRef(entity.getParentNodeRef());
				}
				save(association.getValue());
				assocNodeRef = association.getValue().getNodeRef();
			}
			associationService.update(entity.getNodeRef(), association.getKey(), assocNodeRef);
		}
		for (Map.Entry<QName, NodeRef> association : repositoryEntityDefReader.getSingleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
		for (Map.Entry<QName, List<NodeRef>> association : repositoryEntityDefReader.getMultipleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
	}

	private void saveDataLists(T entity) {

		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
		if (datalists != null && !datalists.isEmpty()) {
			// Container
			NodeRef listContainerNodeRef = getOrCreateDataListContainer(entity);

			for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
				saveDataList(listContainerNodeRef, dataListEntry.getKey(), dataListEntry.getKey(), dataListEntry.getValue());
			}
		}

	}

	private void saveDataListViews(T entity) {
		Map<QName, ?> datalistViews = repositoryEntityDefReader.getDataListViews(entity);
		for (Map.Entry<QName, ?> dataListViewEntry : datalistViews.entrySet()) {

			Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
			if (datalists != null && !datalists.isEmpty()) {
				NodeRef listContainerNodeRef = getOrCreateDataListContainer(entity);

				for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
					saveDataList(listContainerNodeRef, dataListViewEntry.getKey(), dataListEntry.getKey(), dataListEntry.getValue());
				}

			}

		}

	}

	private NodeRef getOrCreateDataListContainer(T entity) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entity.getNodeRef());
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entity.getNodeRef());
		}
		return listContainerNodeRef;
	}

	@SuppressWarnings("unchecked")
	private void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType, List<? extends RepositoryEntity> dataList) {
		if (dataList != null && listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListContainerType);

			boolean isLazyList = dataList instanceof LazyLoadingDataList;

			if (dataListNodeRef == null && (!isLazyList || ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).isLoaded())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Create dataList of type : " + dataListContainerType);
				}
				if (logger.isInfoEnabled() && dataList.isEmpty()) {
					logger.info("Creating empty datalist :" + dataListContainerType);
				}

				dataListNodeRef = entityListDAO.createList(listContainerNodeRef, dataListContainerType);
			}

			if (isLazyList && ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).isLoaded()) {

				for (RepositoryEntity dataListItem : dataList) {
					dataListItem.setParentNodeRef(dataListNodeRef);
					save((T) dataListItem);
				}

				if (logger.isDebugEnabled()) {
					if (!((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes().isEmpty()) {
						logger.debug("Nodes to delete :" + ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes().size());
					}
				}

				for (RepositoryEntity dataListItem : ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes()) {
					nodeService.deleteNode(dataListItem.getNodeRef());
				}

			} else {

				// Case we create entity
				for (RepositoryEntity dataListItem : dataList) {

					dataListItem.setParentNodeRef(dataListNodeRef);
					save((T) dataListItem);

					if (logger.isDebugEnabled()) {
						logger.debug("Save dataList item: " + dataListItem.toString());
					}
				}
			}

		}

	}

	@Override
	public Iterable<T> save(Iterable<? extends T> entities) {
		List<T> ret = new ArrayList<T>();

		for (T entity : entities) {
			ret.add(save(entity));
		}
		return ret;
	}

	@Override
	public T findOne(NodeRef id) {
		if (id == null) {
			throw new IllegalArgumentException("NodeRef cannot be null ");
		}

		return findOne(id, new HashMap<NodeRef, T>());

	}

	@SuppressWarnings({ "unchecked" })
	private T findOne(NodeRef id, Map<NodeRef, T> caches) {

		if (caches.containsKey(id)) {
			return caches.get(id);
		}

		QName type = nodeService.getType(id);

		Class<T> entityClass = (Class<T>) domainMapping.get(type);
		if (entityClass == null) {
			throw new IllegalArgumentException("Type is not registered : " + type);
		}

		try {

			final T entity = entityClass.newInstance();

			if (logger.isDebugEnabled()) {
				logger.debug("findOne instanceOf :" + entity.getClass().getName());
			}

			entity.setNodeRef(id);

			Map<QName, Serializable> properties = nodeService.getProperties(id);

			for (final PropertyDescriptor pd : Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();

				if (readMethod != null) {
					if (readMethod.isAnnotationPresent(AlfProp.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadProperties(entity, pd, readMethod, properties, caches);
					}

					else if (readMethod.isAnnotationPresent(AlfSingleAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadAssoc(entity, pd, readMethod, caches, false);
					}

					else if (readMethod.isAnnotationPresent(AlfMultiAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadAssoc(entity, pd, readMethod, caches, true);
					} else if (readMethod.isAnnotationPresent(DataListView.class) && readMethod.isAnnotationPresent(AlfQname.class)) {

						QName datalistViewQname = repositoryEntityDefReader.readQName(readMethod);

						PropertyUtils.setProperty(entity, pd.getName(), loadDataListView(entity, datalistViewQname, readMethod.getReturnType()));

					} else if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {

						QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

						PropertyUtils.setProperty(entity, pd.getName(), createDataList(entity, pd, datalistQname, datalistQname));

					}

				}

			}

			caches.put(id, entity);

			return entity;

		} catch (Exception e) {
			logger.error("Cannot load entity", e);

			throw new UnsupportedOperationException(e);
		}
	}

	private <R> R loadDataListView(final T entity, QName datalistContainerQname, Class<R> returnType) throws InstantiationException, IllegalAccessException,
			IntrospectionException, InvocationTargetException, NoSuchMethodException {

		R ret = returnType.newInstance();

		for (final PropertyDescriptor pd : Introspector.getBeanInfo(returnType).getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();

			if (readMethod != null) {
				if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {

					final QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

					PropertyUtils.setProperty(ret, pd.getName(), createDataList(entity, pd, datalistContainerQname, datalistQname));
				}
			}
		}

		return ret;
	}

	private List<T> createDataList(final T entity, final PropertyDescriptor pd, final QName datalistContainerQname, final QName datalistQname) {
		if (logger.isTraceEnabled()) {
			logger.debug("read dataList : " + pd.getName());
		}

		LazyLoadingDataList<T> dataList = new LazyLoadingDataList<T>();
		dataList.setDataProvider(new LazyLoadingDataList.DataProvider<T>() {
			public List<T> getData() {
				return loadDataList(entity.getNodeRef(), datalistContainerQname, datalistQname);
			}

			@Override
			public String getFieldName() {
				return pd.getName();
			}

		});
		return dataList;
	}

	private void loadAssoc(T entity, PropertyDescriptor pd, Method readMethod, Map<NodeRef, T> caches, boolean multiple) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (multiple) {
			if (logger.isTraceEnabled()) {
				logger.trace("read multi assoc : " + pd.getName());
			}
			PropertyUtils.setProperty(entity, pd.getName(), associationService.getTargetAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod)));
		} else {

			if (logger.isTraceEnabled()) {
				logger.trace("read single assoc : " + pd.getName());
			}

			NodeRef assocRef = associationService.getTargetAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));

			if (assocRef != null && pd.getPropertyType().isAnnotationPresent(AlfType.class)) {
				if (caches.containsKey(assocRef)) {
					PropertyUtils.setProperty(entity, pd.getName(), caches.get(assocRef));
				} else {
					PropertyUtils.setProperty(entity, pd.getName(), findOne(assocRef));
				}
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), assocRef);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadProperties(T entity, PropertyDescriptor pd, Method readMethod, Map<QName, Serializable> properties, Map<NodeRef, T> caches) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		if (logger.isTraceEnabled()) {
			logger.trace("read property : " + pd.getName());
		}
		QName qname = repositoryEntityDefReader.readQName(readMethod);
		Object prop = properties.get(qname);
		if (prop != null && Enum.class.isAssignableFrom(pd.getPropertyType())) {
			if (((String) prop).isEmpty()) {
				PropertyUtils.setProperty(entity, pd.getName(), null);
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), Enum.valueOf((Class<Enum>) pd.getPropertyType(), (String) prop));
			}
		} else if (prop != null && pd.getPropertyType().isAnnotationPresent(AlfType.class)) {
			if (caches.containsKey(prop)) {
				PropertyUtils.setProperty(entity, pd.getName(), caches.get(prop));
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), findOne((NodeRef) prop));
			}
		} else if (readMethod.isAnnotationPresent(AlfMlText.class)) {
			PropertyUtils.setProperty(entity, pd.getName(), mlNodeService.getProperty(entity.getNodeRef(), qname));
		} else {
			PropertyUtils.setProperty(entity, pd.getName(), prop);
		}

	}

	@Override
	public List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistContainerQname);

			if (dataListNodeRef != null) {
				LinkedList<T> dataList = new LinkedList<T>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(dataListNodeRef, datalistQname);
				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					T item = findOne(listItemNodeRef);
					if (logger.isDebugEnabled()) {
						logger.debug("Load item :" + item.toString());
					}

					dataList.add(item);
				}
				return dataList;

			}
		}

		return new LinkedList<T>();
	}

	@Override
	public boolean exists(NodeRef id) {
		return nodeService.exists(id);
	}

	@Override
	public Iterable<T> findAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long count() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(NodeRef id) {
		nodeService.deleteNode(id);
	}

	@Override
	public void delete(T entity) {
		nodeService.deleteNode(entity.getNodeRef());

	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		for (T entity : entities) {
			delete(entity);
		}
	}

	@Override
	public void deleteAll() {
		for (T entity : findAll()) {
			delete(entity);
		}
	}

	@Override
	public boolean hasDataList(NodeRef entityNodeRef, QName datalistContainerQname) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistContainerQname);

			if (dataListNodeRef != null) {
				return true;
			}
		}
		return false;
	}

}
