/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
 * @author matthieu
 * @version $Id: $Id
 */
public interface RepositoryEntityDefReader<T> {
	
	/**
	 * <p>getEntityProperties.</p>
	 *
	 * @param entity a T object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<QName, T> getEntityProperties(T entity);
	/**
	 * <p>getProperties.</p>
	 *
	 * @param entity a T object.
	 * @return a {@link java.util.Map} object.
	 */
	Map<QName, Serializable> getProperties(T entity);
    /**
     * <p>getSingleAssociations.</p>
     *
     * @param entity a T object.
     * @return a {@link java.util.Map} object.
     */
    Map<QName, NodeRef> getSingleAssociations(T entity);	
    /**
     * <p>getMultipleAssociations.</p>
     *
     * @param entity a T object.
     * @return a {@link java.util.Map} object.
     */
    Map<QName, List<NodeRef>> getMultipleAssociations(T entity);
    /**
     * <p>getDataLists.</p>
     *
     * @param entity a R object.
     * @param <R> a R object.
     * @return a {@link java.util.Map} object.
     */
    <R> Map<QName, List<? extends RepositoryEntity>> getDataLists(R entity);
    /**
     * <p>getSingleEntityAssociations.</p>
     *
     * @param entity a T object.
     * @return a {@link java.util.Map} object.
     */
    Map<QName, T> getSingleEntityAssociations(T entity);
    /**
     * <p>getDataListViews.</p>
     *
     * @param entity a T object.
     * @return a {@link java.util.Map} object.
     */
    Map<QName, ?> getDataListViews(T entity);
    /**
     * <p>getIdentifierAttributes.</p>
     *
     * @param entity a T object.
     * @return a {@link java.util.Map} object.
     */
    Map<QName, Serializable> getIdentifierAttributes(T entity);
    
	/**
	 * <p>getType.</p>
	 *
	 * @param clazz a {@link java.lang.Class} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getType(Class<? extends RepositoryEntity> clazz);
	/**
	 * <p>readQName.</p>
	 *
	 * @param method a {@link java.lang.reflect.Method} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName readQName(Method method);
	/**
	 * <p>getEntityClass.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.Class} object.
	 */
	Class<T> getEntityClass(QName type);
	
	/**
	 * <p>getDefaultPivoAssocName.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getDefaultPivoAssocName(QName dataListItemType);
	/**
	 * <p>isMultiLevelDataList.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMultiLevelDataList(QName dataListItemType);
	/**
	 * <p>isMultiLevelLeaf.</p>
	 *
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isMultiLevelLeaf(QName entityType);
	/**
	 * <p>getMultiLevelSecondaryPivot.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getMultiLevelSecondaryPivot(QName dataListItemType);
	
	/**
	 * <p>getMultiLevelGroupProperty.</p>
	 *
	 * @param dataListItemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getMultiLevelGroupProperty(QName dataListItemType);
	
	
	/**
	 * isRegisteredQName
	 * @param qname
	 * @return
	 */
	boolean isRegisteredQName(RepositoryEntity entity, QName qname);
	
	
	
}
