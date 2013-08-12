package fr.becpg.repo.repository.impl;

import java.beans.Introspector;
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
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.annotation.AlfIdentAttr;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfReadOnly;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.annotation.DataListView;
import fr.becpg.repo.repository.model.BaseObject;

@Repository
public class RepositoryEntityDefReaderImpl<T> implements RepositoryEntityDefReader<T> {

	private static Log logger = LogFactory.getLog(RepositoryEntityDefReaderImpl.class);

	private NamespaceService namespaceService;

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
		return readValueMap(entity, AlfIdentAttr.class, Serializable.class);
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
		Map<QName, R> ret = new HashMap<QName, R>();
		try {
			for (PropertyDescriptor pd : Introspector.getBeanInfo(entity.getClass()).getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();
				if (readMethod != null) {
					if (readMethod.isAnnotationPresent(annotationClass) && readMethod.isAnnotationPresent(AlfQname.class) && !readMethod.isAnnotationPresent(AlfReadOnly.class)) {
						Object o = evaluateObject(pd.getPropertyType(), PropertyUtils.getProperty(entity, pd.getName()));
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
		} catch (Exception e) {
			logger.error(e, e);
		}
		return ret;
	}

	private Object evaluateObject(Class<?> propertyType, Object o) {
		if (o != null && Enum.class.isAssignableFrom(propertyType)) {
			return o.toString();
		}
		return o;
	}

}
