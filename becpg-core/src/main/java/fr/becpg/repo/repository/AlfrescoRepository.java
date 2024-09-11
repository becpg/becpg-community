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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Should implement Spring Data
 * Used to retrieve entity from repository
 *
 * @author matthieu
 * @param <T>
 * @since 1.5
 * @version $Id: $Id
 */
public interface AlfrescoRepository<T extends RepositoryEntity>  extends CrudRepository<T, NodeRef> {

	/**
	 * <p>hasDataList.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param datalistContainerQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean hasDataList(RepositoryEntity entity, QName datalistContainerQname);
	/**
	 * <p>hasDataList.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistContainerQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean hasDataList(NodeRef entityNodeRef, QName datalistContainerQname);
	/**
	 * <p>loadDataList.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistQname a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 * @param listName a {@link java.lang.String} object
	 */
	List<T> loadDataList(NodeRef entityNodeRef, String listName, QName datalistQname);
	
	

	/**
	 * <p>loadDataList.</p>
	 *
	 * @param dataListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param datalistQname a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.util.List} object
	 */
	List<T> loadDataList(NodeRef dataListNodeRef, QName datalistQname);

	/**
	 * <p>create.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entity a T object.
	 * @return a T object.
	 */
	T create(NodeRef parentNodeRef, T entity);
	/**
	 * <p>getOrCreateDataListContainer.</p>
	 *
	 * @param entity a T object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrCreateDataListContainer(T entity);
	/**
	 * <p>saveDataList.</p>
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListContainerType a {@link org.alfresco.service.namespace.QName} object.
	 * @param dataListType a {@link org.alfresco.service.namespace.QName} object.
	 * @param dataList a {@link java.util.List} object.
	 */
	void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType, List<? extends RepositoryEntity> dataList);
	/**
	 * <p>saveDataList.</p>
	 *
	 * @param listContainerNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListContainerType a {@link org.alfresco.service.namespace.QName} object.
	 * @param dataListName a {@link java.lang.String} object.
	 * @param dataList a {@link java.util.List} object.
	 */
	void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, String dataListName, List<? extends RepositoryEntity> dataList);
	/**
	 * <p>isRegisteredType.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean isRegisteredType(QName type);
	/**
	 * <p>isDirty.</p>
	 *
	 * @param entity a T object.
	 * @return a boolean.
	 */
	boolean isDirty(T entity);

        
	/**
	 * <p>getList.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param clazz a {@link java.lang.Class} object.
	 * @param <R> a R object.
	 * @return a {@link java.util.List} object.
	 */
	<R extends RepositoryEntity> List<R> getList(RepositoryEntity entity, Class<R> clazz);
	/**
	 * <p>getList.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param datalistContainerQname a {@link org.alfresco.service.namespace.QName} object.
	 * @param datalistQname a {@link org.alfresco.service.namespace.QName} object.
	 * @param <R> a R object.
	 * @return a {@link java.util.List} object.
	 */
	<R extends RepositoryEntity> List<R> getList(RepositoryEntity entity, QName datalistContainerQname, QName datalistQname);
	
	/**
	 * <p>clearCaches.</p>
	 *
	 * @param id a {@link java.lang.String} object
	 */
	void clearCaches(String id);

	
}
