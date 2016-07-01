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
package fr.becpg.repo.repository.impl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.DataListView;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;
import fr.becpg.repo.repository.model.BaseObject;

@Repository("repositoryEntityDefReader")
public class RepositoryEntityDefReaderImpl<T> implements RepositoryEntityDefReader<T> , ApplicationListener<ContextRefreshedEvent> {

	private static final Log logger = LogFactory.getLog(RepositoryEntityDefReaderImpl.class);

	@Autowired
	private NamespaceService namespaceService;

	
	private final Map<QName, Class<T>> domainMapping = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	public void onApplicationEvent(ContextRefreshedEvent refreshEvent) {
		domainMapping.clear();
		logger.debug("Scanning classpath for AlfType annotation ");
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(AlfType.class));

		for (BeanDefinition bd : scanner.findCandidateComponents("fr.becpg.*")) {
			try {
				registerEntity((Class<T>) Class.forName(bd.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				logger.error(e, e);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void registerEntity(Class<T> clazz) {
		logger.debug("Register entity : " + clazz.getName());
		domainMapping.put(getType((Class<? extends RepositoryEntity>) clazz), clazz);
	}
	

	@Override
	public Map<QName, T> getEntityProperties(T entity) {
		return readValueMap(entity, AlfProp.class, RepositoryEntity.class);
	}

	@Override
	public Map<QName, Serializable> getProperties(T entity) {
		return readValueMap(entity, AlfProp.class, Serializable.class);
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations(T entity) {
		return readValueMap(entity, AlfSingleAssoc.class, NodeRef.class);
	}

	@Override
	public Map<QName, T> getSingleEntityAssociations(T entity) {
		return readValueMap(entity, AlfSingleAssoc.class, RepositoryEntity.class);
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations(T entity) {
		return readValueMap(entity, AlfMultiAssoc.class, List.class);
	}

	@Override
	public <R> Map<QName, List<? extends RepositoryEntity>> getDataLists(R entity) {
		return readValueMap(entity, DataList.class, List.class);

	}

	@Override
	public Map<QName, T> getDataListViews(T entity) {
		return readValueMap(entity, DataListView.class, BaseObject.class);
	}

	@Override
	public Map<QName, Serializable> getIdentifierAttributes(T entity) {
		return readValueMap(entity, DataListIdentifierAttr.class, Serializable.class);
	}


	@Override
	public QName getType(Class<? extends RepositoryEntity> clazz) {
		if (clazz.getAnnotation(AlfQname.class) != null) {

			String qName = clazz.getAnnotation(AlfQname.class).qname();
			return QName.createQName(qName, namespaceService);
		}
		throw new RuntimeException("No @AlfType annotation in class");
	}

	@Override
	public QName readQName(Method readMethod) {
		String qName = readMethod.getAnnotation(AlfQname.class).qname();
		QName fieldQname = QName.createQName(qName, namespaceService);
		return fieldQname;
	}

	@SuppressWarnings("unchecked")
	private <R, Z> Map<QName, R> readValueMap(Z entity, Class<? extends Annotation> annotationClass, Class<?> returnType) {
		Map<QName, R> ret = new HashMap<>();
		
			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
			for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();
				if (readMethod != null) {
					if (readMethod.isAnnotationPresent(annotationClass) && readMethod.isAnnotationPresent(AlfQname.class) && !readMethod.isAnnotationPresent(AlfReadOnly.class)) {
						Object o = evaluateObject(pd.getPropertyType(), beanWrapper.getPropertyValue(pd.getName()));
						QName qname = readQName(readMethod);
						if (o != null) {
							if (returnType.isAssignableFrom(o.getClass())) {
								ret.put(qname, (R) returnType.cast(o));
							} else {
								logger.debug("Cannot cast from :" + o.getClass().getName() + " to " + returnType.getName() + " for " + qname);
							}
						} else {
							ret.put(qname, null);
						}
					}
				}
			}
		return ret;
	}

	private Object evaluateObject(Class<?> propertyType, Object o) {
		if (o != null && Enum.class.isAssignableFrom(propertyType)) {
			return o.toString();
		}
		return o;
	}

	@Override
	public Class<T> getEntityClass(QName type) {
		return domainMapping.get(type);
	}

	@Override
	public QName getDefaultPivoAssocName(QName entityDataListQname) {
		Class<T> entityClass = getEntityClass(entityDataListQname);
		if (entityClass == null) {
			throw new IllegalArgumentException("Type is not registered : " + entityDataListQname);
		}

		
		BeanWrapper beanWrapper = new BeanWrapperImpl(entityClass);

		for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();
			if (readMethod != null) {
				if (readMethod.isAnnotationPresent(DataListIdentifierAttr.class) && readMethod.isAnnotationPresent(AlfQname.class) 
						&& readMethod.getAnnotation(DataListIdentifierAttr.class).isDefaultPivotAssoc()
					 ) {
					return readQName(readMethod);
				}
			}
		}
		logger.warn("No default pivot assoc found for "+entityDataListQname);
		return null;
	}

	@Override
	public boolean isMultiLevelDataList(QName dataListItemType) {
		Class<T> entityClass = getEntityClass(dataListItemType);
		return entityClass != null && entityClass.isAnnotationPresent(MultiLevelDataList.class);
	}

	@Override
	public boolean isMultiLevelLeaf(QName entityType) {
		Class<T> entityClass = getEntityClass(entityType);
		return entityClass != null && entityClass.isAnnotationPresent(MultiLevelLeaf.class);
	}

}
