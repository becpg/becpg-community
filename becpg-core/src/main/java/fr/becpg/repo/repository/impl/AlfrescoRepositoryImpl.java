/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.repository.impl;

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
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Repository;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
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
import fr.becpg.repo.repository.model.DefaultListDataItem;

/**
 * <p>AlfrescoRepositoryImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Repository("alfrescoRepository")
public class AlfrescoRepositoryImpl<T extends RepositoryEntity> implements AlfrescoRepository<T> {

	@Autowired
	private NodeService nodeService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	private static final Log logger = LogFactory.getLog(AlfrescoRepositoryImpl.class);

	@Autowired
	private RepositoryEntityDefReader<T> repositoryEntityDefReader;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private AssociationService associationService;

	/** {@inheritDoc} */
	@Override
	public T create(NodeRef parentNodeRef, T entity) {
		entity.setParentNodeRef(parentNodeRef);
		return save(entity);
	}

	/** {@inheritDoc} */
	@Override
	public T save(T entity) {

		if (entity.isTransient()) {
			return entity;
		}

		if (!L2CacheSupport.isCacheOnlyEnable()) {

			if ((entity.getNodeRef() == null) || ((entity.getExtraProperties() != null) && (!entity.getExtraProperties().isEmpty()))
					|| (createCollisionSafeHashCode(entity) != entity.getDbHashCode())) {

				boolean shouldUpdate = true;

				if ((entity.getNodeRef() != null) && (entity.getExtraProperties() != null) && !entity.getExtraProperties().isEmpty()) {
					if (createCollisionSafeHashCode(entity) == entity.getDbHashCode()) {
						shouldUpdate = false;

						for (Map.Entry<QName, Serializable> extraProperty : entity.getExtraProperties().entrySet()) {
							Serializable prop = nodeService.getProperty(entity.getNodeRef(), extraProperty.getKey());

							if (!(((prop == null) && (extraProperty.getValue() == null))
									|| ((prop != null) && prop.equals(extraProperty.getValue())))) {
								shouldUpdate = true;
								if (logger.isDebugEnabled()) {
									logger.debug("Change detected in " + extraProperty.getKey() + " - actual: " + prop + " - new: "
											+ extraProperty.getValue());
								}
								break;
							}
						}
					}
				}

				if (shouldUpdate) {

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
						if ((name == null) || name.isEmpty()) {
							name = UUID.randomUUID().toString();
						}

						properties.put(ContentModel.PROP_NAME, name);

						NodeRef productNodeRef = nodeService.createNode(entity.getParentNodeRef(), ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
								repositoryEntityDefReader.getType(entity.getClass()), properties).getChildRef();
						entity.setNodeRef(productNodeRef);

					} else {

						if (logger.isDebugEnabled()) {
							logger.debug("Update instanceOf :" + entity.getClass().getName() + " " + entity.getName());
							logger.debug(" HashDiff :"
									+ BeCPGHashCodeBuilder.printDiff(entity, findOne(entity.getNodeRef(), new HashMap<NodeRef, RepositoryEntity>())));

						}

						Map<QName, Serializable> propToAdd = new HashMap<>();

						for (Map.Entry<QName, Serializable> prop : properties.entrySet()) {
							if (prop.getValue() == null) {
								nodeService.removeProperty(entity.getNodeRef(), prop.getKey());
							} else {
								propToAdd.put(prop.getKey(), prop.getValue());
							}

						}

						nodeService.addProperties(entity.getNodeRef(), propToAdd);

					}

					saveAssociations(entity);
					saveAspects(entity);

					entity.setDbHashCode(createCollisionSafeHashCode(entity));
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("Entity " + entity.getName() + " has no change  to save (same extra properties) ");
					}
				}

			} else {
				if (logger.isTraceEnabled()) {
					logger.trace("Entity " + entity.getName() + " has no change to save (same hashCode)");
				}
			}

			saveDataLists(entity);
			saveDataListViews(entity);

		} else {
			if (entity.getNodeRef() == null) {
				entity.setNodeRef(L2CacheSupport.generateNodeRef());
			}

			if (logger.isTraceEnabled()) {
				logger.trace("Save entity " + entity.getName() + " only on memory");
			}
		}

		if (L2CacheSupport.isThreadCacheEnable()) {
			L2CacheSupport.getCurrentThreadCache().put(entity.getNodeRef(), entity);
		}

		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDirty(T entity) {

		if ((entity.getNodeRef() == null) || ((entity.getExtraProperties() != null) && (!entity.getExtraProperties().isEmpty()))
				|| (createCollisionSafeHashCode(entity) != entity.getDbHashCode())) {

			boolean shouldUpdate = true;

			if ((entity.getNodeRef() != null) && (entity.getExtraProperties() != null) && !entity.getExtraProperties().isEmpty()) {
				if (createCollisionSafeHashCode(entity) == entity.getDbHashCode()) {
					shouldUpdate = false;

					for (Map.Entry<QName, Serializable> extraProperty : entity.getExtraProperties().entrySet()) {
						Serializable prop = nodeService.getProperty(entity.getNodeRef(), extraProperty.getKey());

						if (!(((prop == null) && (extraProperty.getValue() == null)) || ((prop != null) && prop.equals(extraProperty.getValue())))) {
							shouldUpdate = true;
							if (logger.isDebugEnabled()) {
								logger.debug("Change detected in " + extraProperty.getKey() + " - actual: " + prop + " - new: "
										+ extraProperty.getValue());
							}
							break;
						}
					}
				}
			}

			return shouldUpdate;
		}
		return false;
	}

	private void saveAspects(T entity) {
		if (entity instanceof AspectAwareDataItem) {
			if (((AspectAwareDataItem) entity).getAspects() != null) {
				for (QName aspect : ((AspectAwareDataItem) entity).getAspects()) {
					if (!nodeService.hasAspect(entity.getNodeRef(), aspect)) {
						nodeService.addAspect(entity.getNodeRef(), aspect, new HashMap<QName, Serializable>());
					}
				}
			}
			if (((AspectAwareDataItem) entity).getAspectsToRemove() != null) {
				for (QName aspect : ((AspectAwareDataItem) entity).getAspectsToRemove()) {
					if (nodeService.hasAspect(entity.getNodeRef(), aspect)) {
						nodeService.removeAspect(entity.getNodeRef(), aspect);
					}
				}
			}
		}
	}

	private Map<QName, Serializable> extractProperties(T entity) {

		Map<QName, Serializable> properties = repositoryEntityDefReader.getProperties(entity);

		for (Map.Entry<QName, T> prop : repositoryEntityDefReader.getEntityProperties(entity).entrySet()) {
			if ((prop.getValue() == null) || !prop.getValue().isTransient()) {
				properties.put(prop.getKey(), getOrCreateNodeRef(prop, entity));
			}
		}

		if (entity.getExtraProperties() != null) {
			properties.putAll(entity.getExtraProperties());
		}

		return properties;

	}

	private long createCollisionSafeHashCode(T entity) {

		return BeCPGHashCodeBuilder.reflectionHashCode(entity);

	}

	private void saveAssociations(T entity) {

		// TODO manage child assocs

		for (Map.Entry<QName, T> association : repositoryEntityDefReader.getSingleEntityAssociations(entity).entrySet()) {
			if ((association.getValue() == null) || !association.getValue().isTransient()) {
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
		if ((datalists != null) && !datalists.isEmpty()) {
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
			if ((datalists != null) && !datalists.isEmpty()) {
				NodeRef listContainerNodeRef = getOrCreateDataListContainer(entity);

				for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
					saveDataList(listContainerNodeRef, dataListViewEntry.getKey(), dataListEntry.getKey(), dataListEntry.getValue());
				}

			}

		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOrCreateDataListContainer(T entity) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entity.getNodeRef());
		if (listContainerNodeRef == null) {
			listContainerNodeRef = entityListDAO.createListContainer(entity.getNodeRef());
		}
		return listContainerNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType,
			List<? extends RepositoryEntity> dataList) {
		if ((dataList != null) && (listContainerNodeRef != null)) {

			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, dataListContainerType);

			boolean isLazyList = dataList instanceof LazyLoadingDataList;

			if ((dataListNodeRef == null) && !dataList.isEmpty()
					&& (!isLazyList || ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).isLoaded())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Create dataList of type : " + dataListContainerType);
				}

				dataListNodeRef = entityListDAO.createList(listContainerNodeRef, dataListContainerType);
			} else {

				if (logger.isTraceEnabled()) {
					logger.trace("Save dataList of type : " + dataListContainerType);
				}

			}

			if (dataListNodeRef != null) {

				if (isLazyList && ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).isLoaded()) {

					boolean deleteNodes = false;

					for (RepositoryEntity dataListItem : ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes()) {
						if ((dataListItem != null) && (dataListItem.getNodeRef() != null) && !dataListItem.isTransient()) {
							nodeService.addAspect(dataListItem.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
							nodeService.deleteNode(dataListItem.getNodeRef());
							deleteNodes = true;
						}
					}

					if (logger.isDebugEnabled() && deleteNodes) {
						logger.debug("Nodes to delete :" + ((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes().size()
								+ " in " + dataListContainerType);
					}

					((LazyLoadingDataList<? extends RepositoryEntity>) dataList).getDeletedNodes().clear();
				}
				for (RepositoryEntity dataListItem : dataList) {
					if ((dataListItem.getNodeRef() == null) || nodeService.exists(dataListItem.getNodeRef())) {
						dataListItem.setParentNodeRef(dataListNodeRef);

						if (logger.isTraceEnabled()) {
							logger.trace("Save dataList item: " + dataListItem.toString());
						}
						save((T) dataListItem);
					}
				}

			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Iterable<T> save(Iterable<? extends T> entities) {
		List<T> ret = new ArrayList<>();

		for (T entity : entities) {
			ret.add(save(entity));
		}
		return ret;
	}

	/** {@inheritDoc} */
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
			if (logger.isTraceEnabled()) {
				logger.trace("Internal cache HIT for key :" + id);
			}
			return (T) caches.get(id);
		}

		QName type = nodeService.getType(id);

		Class<T> entityClass = repositoryEntityDefReader.getEntityClass(type);
		if (entityClass == null) {
			if (entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				entityClass = (Class<T>) DefaultListDataItem.class;
			} else {
				throw new IllegalArgumentException("Type is not registered : " + type);
			}
		}

		try {

			final T entity = entityClass.getDeclaredConstructor().newInstance();

			if (logger.isTraceEnabled()) {
				logger.trace("findOne instanceOf :" + entity.getClass().getName());
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
						PropertyUtils.setProperty(entity, pd.getName(),
								loadDataListView(entity, datalistViewQname, readMethod.getReturnType(), caches));
					} else if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {
						QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

						PropertyUtils.setProperty(entity, pd.getName(), createDataList(entity, pd, datalistQname, null, caches));
					}
				}
			}

			loadAspects(entity);

			entity.setDbHashCode(createCollisionSafeHashCode(entity));

			return entity;

		} catch (Exception e) {
			if (e instanceof ConcurrencyFailureException) {
				throw (ConcurrencyFailureException) e;
			}
			logger.error("Cannot load entity: " + id, e);
			throw new UnsupportedOperationException(e);
		}
	}

	private void loadAspects(T entity) {
		if (entity instanceof AspectAwareDataItem) {
			((AspectAwareDataItem) entity).setAspects(new TreeSet<>(nodeService.getAspects(entity.getNodeRef())));
		}

	}

	private <R> R loadDataListView(final T entity, QName datalistContainerQname, Class<R> returnType, Map<NodeRef, RepositoryEntity> caches)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		R ret = returnType.getDeclaredConstructor().newInstance();

		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(ret);
		for (final PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();

			if (readMethod != null) {
				if (readMethod.isAnnotationPresent(DataList.class) && readMethod.isAnnotationPresent(AlfQname.class)) {

					final QName datalistQname = repositoryEntityDefReader.readQName(readMethod);

					PropertyUtils.setProperty(ret, pd.getName(), createDataList(entity, pd, datalistContainerQname, datalistQname, caches));
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

		LazyLoadingDataList<T> dataList = new LazyLoadingDataList<>();
		dataList.setDataProvider(new LazyLoadingDataList.DataProvider<T>() {
			@Override
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

	private void loadAssoc(T entity, PropertyDescriptor pd, Method readMethod, Map<NodeRef, RepositoryEntity> caches, boolean multiple,
			boolean isChildAssoc) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (multiple) {
			if (logger.isTraceEnabled()) {
				logger.trace("read multi assoc : " + pd.getName());
			}
			List<NodeRef> assocRefs;
			if (!isChildAssoc) {
				assocRefs = associationService.getTargetAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			} else {
				assocRefs = associationService.getChildAssocs(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			}

			if ((assocRefs != null) && readMethod.getAnnotation(AlfMultiAssoc.class).isEntity()) {
				List<RepositoryEntity> entities = new LinkedList<>();
				for (NodeRef nodeRef : assocRefs) {
					entities.add(findOne(nodeRef, caches));
				}
				PropertyUtils.setProperty(entity, pd.getName(), entities);
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), assocRefs);
			}
		} else {

			if (logger.isTraceEnabled()) {
				logger.trace("read single assoc : " + pd.getName());
			}

			NodeRef assocRef;
			if (!isChildAssoc) {
				assocRef = associationService.getTargetAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			} else {
				assocRef = associationService.getChildAssoc(entity.getNodeRef(), repositoryEntityDefReader.readQName(readMethod));
			}

			if ((assocRef != null)
					&& (pd.getPropertyType().isAnnotationPresent(AlfType.class) || readMethod.getAnnotation(AlfSingleAssoc.class).isEntity())) {
				PropertyUtils.setProperty(entity, pd.getName(), findOne(assocRef, caches));
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), assocRef);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadProperties(T entity, PropertyDescriptor pd, Method readMethod, Map<QName, Serializable> properties,
			Map<NodeRef, RepositoryEntity> caches) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (logger.isTraceEnabled()) {
			logger.trace("read property : " + pd.getName());
		}
		QName qname = repositoryEntityDefReader.readQName(readMethod);
		Object prop = properties.get(qname);

		if ((prop != null) && Enum.class.isAssignableFrom(pd.getPropertyType())) {
			if (((String) prop).isEmpty()) {
				PropertyUtils.setProperty(entity, pd.getName(), null);
			} else {
				PropertyUtils.setProperty(entity, pd.getName(), Enum.valueOf((Class<Enum>) pd.getPropertyType(), (String) prop));
			}
		} else if ((prop != null) && pd.getPropertyType().isAnnotationPresent(AlfType.class)) {
			PropertyUtils.setProperty(entity, pd.getName(), findOne((NodeRef) prop, caches));
		} else if (readMethod.isAnnotationPresent(AlfMlText.class)) {
			PropertyUtils.setProperty(entity, pd.getName(), mlNodeService.getProperty(entity.getNodeRef(), qname));
		} else {
			PropertyUtils.setProperty(entity, pd.getName(), prop);
		}

	}

	/** {@inheritDoc} */
	@Override
	public List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname) {
		return loadDataList(entityNodeRef, datalistContainerQname, datalistQname, L2CacheSupport.getCurrentThreadCache());
	}

	private List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname, Map<NodeRef, RepositoryEntity> caches) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistContainerQname);

			if (dataListNodeRef != null) {

				return entityListDAO.getListItems(dataListNodeRef, datalistQname).stream().map(el -> {
					T ret = findOne(el, caches);
					ret.setParentNodeRef(dataListNodeRef);
					return ret;
				}).collect(Collectors.toCollection(LinkedList::new));

			}
		}

		return new LinkedList<>();
	}

	/** {@inheritDoc} */
	@Override
	public boolean exists(NodeRef id) {
		return nodeService.exists(id);
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<T> findAll() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public long count() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void delete(NodeRef id) {
		nodeService.deleteNode(id);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(T entity) {
		nodeService.deleteNode(entity.getNodeRef());

	}

	/** {@inheritDoc} */
	@Override
	public void delete(Iterable<? extends T> entities) {
		for (T entity : entities) {
			delete(entity);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAll() {
		for (T entity : findAll()) {
			delete(entity);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasDataList(RepositoryEntity entity, QName datalistContainerQname) {

		if (entity.getNodeRef() != null) {
			return hasDataList(entity.getNodeRef(), datalistContainerQname);
		} else {
			Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
			if ((datalists != null) && !datalists.isEmpty()) {
				for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
					if (dataListEntry.getKey().equals(datalistContainerQname)) {
						return (dataListEntry.getValue() != null) && !dataListEntry.getValue().isEmpty();
					}
				}
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasDataList(NodeRef entityNodeRef, QName datalistContainerQname) {
		if (entityNodeRef != null) {
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

			if (listContainerNodeRef != null) {
				NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, datalistContainerQname);

				if (dataListNodeRef != null) {
					return true;
				}
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRegisteredType(QName type) {
		return repositoryEntityDefReader.getEntityClass(type) != null;
	}

        /** {@inheritDoc} */
        @Override
	public <R extends RepositoryEntity> List<R> getList(RepositoryEntity entity, Class<R> clazz) {
		QName qName = repositoryEntityDefReader.getType(clazz);
		return getList(entity, qName, qName);

	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public <R extends RepositoryEntity> List<R> getList(RepositoryEntity entity, QName datalistContainerQname, QName datalistQname) {

		if (datalistContainerQname.equals(datalistQname)) {

			Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(entity);
			if ((datalists != null) && !datalists.isEmpty()) {
				for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : datalists.entrySet()) {
					if (dataListEntry.getKey().equals(datalistQname)) {
						return (List<R>) dataListEntry.getValue();
					}
				}
			}
		}

		Map<QName, ?> datalistViews = repositoryEntityDefReader.getDataListViews((T) entity);
		for (Map.Entry<QName, ?> dataListViewEntry : datalistViews.entrySet()) {
			if (dataListViewEntry.getKey().equals(datalistContainerQname)) {
				Map<QName, List<? extends RepositoryEntity>> viewDatalists = repositoryEntityDefReader.getDataLists(dataListViewEntry.getValue());
				if ((viewDatalists != null) && !viewDatalists.isEmpty()) {
					for (Map.Entry<QName, List<? extends RepositoryEntity>> dataListEntry : viewDatalists.entrySet()) {
						if (dataListEntry.getKey().equals(datalistQname)) {
							return (List<R>) dataListEntry.getValue();
						}
					}
				}
			}
		}

		return new ArrayList<>();
	}
(??)
}
