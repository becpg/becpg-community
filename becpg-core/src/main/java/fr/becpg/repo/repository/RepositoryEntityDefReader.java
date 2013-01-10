package fr.becpg.repo.repository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Read informations from annotations
 * 
 */
public interface RepositoryEntityDefReader<T> {
	
	Map<QName, T> getEntityProperties(T entity);
	Map<QName, Serializable> getProperties(T entity);
    Map<QName, NodeRef> getSingleAssociations(T entity);	
    Map<QName, List<NodeRef>> getMultipleAssociations(T entity);
    <R> Map<QName, List<? extends RepositoryEntity>> getDataLists(R entity);
    Map<QName, T> getSingleEntityAssociations(T entity);
    Map<QName, ?> getDataListViews(T entity);
    
	QName getType(Class<? extends RepositoryEntity> clazz);
	QName readQName(Method method);
	
	
	
}
