package fr.becpg.repo.repository.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
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
import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

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

@Service
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

		if (logger.isDebugEnabled()) {
			logger.debug("save instanceOf :" + entity.getClass().getName());
		}

		if (entity.getNodeRef() == null) {

			String name = entity.getName();
			if (entity.getName() == null) {
				name = UUID.randomUUID().toString();
			}

			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, name);

			NodeRef productNodeRef = nodeService.createNode(entity.getParentNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), repositoryEntityDefReader.getType(entity.getClass()), properties)
					.getChildRef();
			entity.setNodeRef(productNodeRef);

		}

		saveProperties(entity);
		saveAssociations(entity);
		saveDataLists(entity);

		return entity;
	}

	private void saveProperties(T entity) {

		Map<QName, Serializable> properties = repositoryEntityDefReader.getProperties(entity);

		nodeService.addProperties(entity.getNodeRef(), properties);

	}

	private void saveAssociations(T entity) {

		for (Map.Entry<QName, NodeRef> association : repositoryEntityDefReader.getSingleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
		for (Map.Entry<QName, List<NodeRef>> association : repositoryEntityDefReader.getMultipleAssociations(entity).entrySet()) {
			associationService.update(entity.getNodeRef(), association.getKey(), association.getValue());
		}
	}

	private void saveDataLists(T entity) {
		// Container
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entity.getNodeRef());
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entity.getNodeRef());
		}

		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
		if (datalists != null) {
			for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
				saveDataList(listContainerNodeRef, dataListEntry.getKey(), dataListEntry.getValue());
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void saveDataList(NodeRef listContainerNodeRef, QName dataListType, List<? extends RepositoryEntity> dataList) {
		if (dataList != null && listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListType);

			if (dataListNodeRef == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Create dataList of type : " + dataListType);
				}

				dataListNodeRef = entityListDAO.createList(listContainerNodeRef, dataListType);
			}

			if (dataList instanceof LazyLoadingDataList && ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).isLoaded()) {

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T findOne(NodeRef id) {

		if (id == null) {
			throw new IllegalArgumentException("NodeRef cannot be null ");
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
						if (logger.isTraceEnabled()) {
							logger.debug("read property : " + pd.getName());
						}
						QName qname = repositoryEntityDefReader.readQName(readMethod);
						Object prop = properties.get(qname);
						if (prop != null && Enum.class.isAssignableFrom(pd.getPropertyType())) {
							PropertyUtils.setProperty(entity, pd.getName(), Enum.valueOf((Class<Enum>) pd.getPropertyType(), (String) prop));
						} else if (readMethod.isAnnotationPresent(AlfMlText.class)) {
							PropertyUtils.setProperty(entity, pd.getName(), mlNodeService.getProperty(id, qname));
						} else {
							PropertyUtils.setProperty(entity, pd.getName(), prop);
						}
					}

					if (readMethod.isAnnotationPresent(AlfSingleAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						if (logger.isTraceEnabled()) {
							logger.debug("read single assoc : " + pd.getName());
						}
						PropertyUtils.setProperty(entity, pd.getName(), associationService.getTargetAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod)));
					}

					if (readMethod.isAnnotationPresent(AlfMultiAssoc.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						if (logger.isTraceEnabled()) {
							logger.debug("read multi assoc : " + pd.getName());
						}
						PropertyUtils.setProperty(entity, pd.getName(), associationService.getTargetAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod)));
					}

					if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(DataList.class)) {
						if (logger.isTraceEnabled()) {
							logger.debug("read dataList : " + pd.getName());
						}
						final QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

						LazyLoadingDataList<T> dataList = new LazyLoadingDataList<T>();
						dataList.setDataProvider(new LazyLoadingDataList.DataProvider<T>() {
							public List<T> getData() {
								return loadDataList(entity.getNodeRef(), datalistQname);
							}

							@Override
							public String getFieldName() {
								return pd.getName();
							}

						});

						PropertyUtils.setProperty(entity, pd.getName(), dataList);
					}

				}

			}

			return entity;

		} catch (Exception e) {
			logger.error("Cannot load entity", e);

			throw new UnsupportedOperationException(e);
		}

	}

	@Override
	public List<T> loadDataList(NodeRef entityNodeRef, QName datalistQname) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistQname);

			if (dataListNodeRef != null) {
				LinkedList<T> dataList = new LinkedList<T>();
				List<NodeRef> listItemNodeRefs = entityListDAO.getListItems(dataListNodeRef, datalistQname);
				for (NodeRef listItemNodeRef : listItemNodeRefs) {
					dataList.add(findOne(listItemNodeRef));
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

}
