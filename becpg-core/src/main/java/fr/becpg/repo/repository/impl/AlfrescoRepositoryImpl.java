package fr.becpg.repo.repository.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;

import fr.becpg.repo.repository.L2CacheSupport;
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
import fr.becpg.repo.repository.model.AspectAwareDataItem;

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

		if (entity.isTransient()) {
			return entity;
		}

		if (!L2CacheSupport.isCacheOnlyEnable()) {

			Map<QName, Serializable> properties = extractProperties(entity);

			if (entity.getNodeRef() == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Create instanceOf :" + entity.getClass().getName());
				}

				for (Iterator<Map.Entry<QName, Serializable>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<QName, Serializable> prop = iterator.next();
					if (prop.getValue() == null) {
						iterator.remove();
					}

				}

				String name = entity.getName();
				if (entity.getName() == null) {
					name = UUID.randomUUID().toString();
				}

				properties.put(ContentModel.PROP_NAME, name);

				NodeRef productNodeRef = nodeService.createNode(entity.getParentNodeRef(), ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), repositoryEntityDefReader.getType(entity.getClass()),
						properties).getChildRef();
				entity.setNodeRef(productNodeRef);

			} else {
				logger.debug("Update instanceOf :" + entity.getClass().getName());

				for (Iterator<Map.Entry<QName, Serializable>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<QName, Serializable> prop = iterator.next();
					if (prop.getValue() == null && !repositoryEntityDefReader.isEnforced(entity.getClass(), prop.getKey())) {
						iterator.remove();
						nodeService.removeProperty(entity.getNodeRef(), prop.getKey());
					}

				}

				nodeService.addProperties(entity.getNodeRef(), properties);
			}

			saveAssociations(entity);
			saveDataLists(entity);
			saveDataListViews(entity);
			saveAspects(entity);

		} else {
			if (entity.getNodeRef() == null) {
				entity.setNodeRef(L2CacheSupport.generateNodeRef());
			}

			if (logger.isInfoEnabled()) {
				logger.info("Save entity " + entity.getName() + " only on memory");
			}
		}

		if (L2CacheSupport.isThreadCacheEnable()) {
			L2CacheSupport.getCurrentThreadCache().put(entity.getNodeRef(), entity);
		}

		return entity;
	}

	// For now only add aspect
	private void saveAspects(T entity) {
		if (entity instanceof AspectAwareDataItem) {
			if (((AspectAwareDataItem) entity).getAspects() != null) {
				for (QName aspect : ((AspectAwareDataItem) entity).getAspects()) {
					if (!nodeService.hasAspect(entity.getNodeRef(), aspect)) {
						nodeService.addAspect(entity.getNodeRef(), aspect, new HashMap<QName, Serializable>());
					}
				}
			}
		}
	}

	private Map<QName, Serializable> extractProperties(T entity) {

		Map<QName, Serializable> properties = repositoryEntityDefReader.getProperties(entity);

		for (Map.Entry<QName, T> prop : repositoryEntityDefReader.getEntityProperties(entity).entrySet()) {
			if (prop.getValue() == null || !prop.getValue().isTransient()) {
				properties.put(prop.getKey(), getOrCreateNodeRef(prop, entity));
			}
		}

		if (entity.getExtraProperties() != null) {
			properties.putAll(entity.getExtraProperties());
		}

		return properties;

	}

	private void saveAssociations(T entity) {

		// TODO manage child assocs

		for (Map.Entry<QName, T> association : repositoryEntityDefReader.getSingleEntityAssociations(entity).entrySet()) {
			if (association.getValue() == null || !association.getValue().isTransient()) {
				associationService.update(entity.getNodeRef(), association.getKey(), getOrCreateNodeRef(association, entity));
			}
		}
		for (Map.Entry<QName, NodeRef> association : repositoryEntityDefReader.getSingleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
		for (Map.Entry<QName, List<NodeRef>> association : repositoryEntityDefReader.getMultipleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
	}

	private NodeRef getOrCreateNodeRef(Map.Entry<QName, T> entry, T entity) {
		if (entry.getValue() != null) {
			if (entry.getValue().getNodeRef() == null) {
				if (entry.getValue().getParentNodeRef() == null) {
					entry.getValue().setParentNodeRef(entity.getParentNodeRef());
				}
				save(entry.getValue());
			}
			return entry.getValue().getNodeRef();
		}
		return null;
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

	@Override
	public NodeRef getOrCreateDataListContainer(T entity) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entity.getNodeRef());
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entity.getNodeRef());
		}
		return listContainerNodeRef;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType, List<? extends RepositoryEntity> dataList) {
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
					if (dataListItem != null && !dataListItem.isTransient()) {
						nodeService.addAspect(dataListItem.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
						nodeService.deleteNode(dataListItem.getNodeRef());
					}
				}

				((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes().clear();

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

		return findOne(id, L2CacheSupport.getCurrentThreadCache());

	}

	@SuppressWarnings({ "unchecked" })
	private T findOne(NodeRef id, Map<NodeRef, RepositoryEntity> caches) {

		if (caches.containsKey(id)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Internal cache HIT for key :" + id);
			}
			return (T) caches.get(id);
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

			caches.put(id, entity);

			Map<QName, Serializable> properties = nodeService.getProperties(id);

			BeanWrapper beanWrapper = new BeanWrapperImpl(entity);

			for (final PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {

				Method readMethod = pd.getReadMethod();

				if (readMethod != null) {
					if (readMethod.isAnnotationPresent(AlfProp.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadProperties(entity, pd, readMethod, properties, caches);
					} else if (readMethod.isAnnotationPresent(AlfSingleAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadAssoc(entity, pd, readMethod, caches, false, readMethod.getAnnotation(AlfSingleAssoc.class).isChildAssoc());
					} else if (readMethod.isAnnotationPresent(AlfMultiAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						loadAssoc(entity, pd, readMethod, caches, true, readMethod.getAnnotation(AlfMultiAssoc.class).isChildAssoc());
					} else if (readMethod.isAnnotationPresent(DataListView.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						QName datalistViewQname = repositoryEntityDefReader.readQName(readMethod);
						PropertyUtils.setProperty(entity,pd.getName(), loadDataListView(entity, datalistViewQname, readMethod.getReturnType(), caches));
					} else if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						QName datalistQname = repositoryEntityDefReader.readQName(readMethod);
		
						PropertyUtils.setProperty(entity,pd.getName(), createDataList(entity, pd, datalistQname, datalistQname, caches));
					}

				}

			}

			loadAspects(entity);

			return entity;

		} catch (Exception e) {
			logger.error("Cannot load entity", e);

			throw new UnsupportedOperationException(e);
		}
	}

	private void loadAspects(T entity) {
		if (entity instanceof AspectAwareDataItem) {
			((AspectAwareDataItem) entity).setAspects(nodeService.getAspects(entity.getNodeRef()));
		}

	}

	private <R> R loadDataListView(final T entity, QName datalistContainerQname, Class<R> returnType, Map<NodeRef, RepositoryEntity> caches) throws InstantiationException,
			IllegalAccessException, IntrospectionException, InvocationTargetException, NoSuchMethodException {

		R ret = returnType.newInstance();

		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(ret);
		for (final PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();

			if (readMethod != null) {
				if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {

					final QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

					PropertyUtils.setProperty(ret ,pd.getName(), createDataList(entity, pd, datalistContainerQname, datalistQname, caches));
				}
			}
		}

		return ret;
	}

	private List<T> createDataList(final T entity, final PropertyDescriptor pd, final QName datalistContainerQname, final QName datalistQname,
			final Map<NodeRef, RepositoryEntity> caches) {
		if (logger.isTraceEnabled()) {
			logger.debug("read dataList : " + pd.getName());
		}

		LazyLoadingDataList<T> dataList = new LazyLoadingDataList<T>();
		dataList.setDataProvider(new LazyLoadingDataList.DataProvider<T>() {
			public List<T> getData() {
				return loadDataList(entity.getNodeRef(), datalistContainerQname, datalistQname, caches);
			}

			@Override
			public String getFieldName() {
				return pd.getName();
			}

		});
		return dataList;
	}

	private void loadAssoc(T entity, PropertyDescriptor pd, Method readMethod, Map<NodeRef, RepositoryEntity> caches, boolean multiple, boolean isChildAssoc)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (multiple) {
			if (logger.isTraceEnabled()) {
				logger.trace("read multi assoc : " + pd.getName());
			}
			List<NodeRef> assocRefs = null;
			if (!isChildAssoc) {
				assocRefs = associationService.getTargetAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			} else {
				assocRefs = associationService.getChildAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			}

			if (assocRefs != null && readMethod.getAnnotation(AlfMultiAssoc.class).isEntity()) {
				List<RepositoryEntity> entities = new LinkedList<>();
				for (NodeRef nodeRef : assocRefs) {
					entities.add(findOne(nodeRef, caches));
				}
				PropertyUtils.setProperty(entity,pd.getName(), entities);
			} else {
				PropertyUtils.setProperty(entity,pd.getName(), assocRefs);
			}
		} else {

			if (logger.isTraceEnabled()) {
				logger.trace("read single assoc : " + pd.getName());
			}

			NodeRef assocRef = null;
			if (!isChildAssoc) {
				assocRef = associationService.getTargetAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			} else {
				assocRef = associationService.getChildAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			}

			if (assocRef != null && (pd.getPropertyType().isAnnotationPresent(AlfType.class) || readMethod.getAnnotation(AlfSingleAssoc.class).isEntity())) {
				PropertyUtils.setProperty(entity,pd.getName(), findOne(assocRef, caches));
			} else {
				PropertyUtils.setProperty(entity,pd.getName(), assocRef);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadProperties(T entity, PropertyDescriptor pd, Method readMethod, Map<QName, Serializable> properties, Map<NodeRef, RepositoryEntity> caches)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (logger.isTraceEnabled()) {
			logger.trace("read property : " + pd.getName());
		}
		QName qname = repositoryEntityDefReader.readQName(readMethod);
		Object prop = properties.get(qname);

		if (prop != null && Enum.class.isAssignableFrom(pd.getPropertyType())) {
			if (((String) prop).isEmpty()) {
				PropertyUtils.setProperty(entity,pd.getName(), null);
			} else {
				PropertyUtils.setProperty(entity,pd.getName(), Enum.valueOf((Class<Enum>) pd.getPropertyType(), (String) prop));
			}
		} else if (prop != null && pd.getPropertyType().isAnnotationPresent(AlfType.class)) {
			PropertyUtils.setProperty(entity,pd.getName(), findOne((NodeRef) prop, caches));
		} else if (readMethod.isAnnotationPresent(AlfMlText.class)) {
			PropertyUtils.setProperty(entity,pd.getName(), mlNodeService.getProperty(entity.getNodeRef(), qname));
		} else {
			PropertyUtils.setProperty(entity,pd.getName(), prop);
		}

	}

	@Override
	public List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname) {
		return loadDataList(entityNodeRef, datalistContainerQname, datalistQname, L2CacheSupport.getCurrentThreadCache());
	}

	private List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname, Map<NodeRef, RepositoryEntity> caches) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistContainerQname);

			if (dataListNodeRef != null) {
				LinkedList<T> dataList = new LinkedList<T>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(dataListNodeRef, datalistQname);
				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					T item = findOne(listItemNodeRef, caches);
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
