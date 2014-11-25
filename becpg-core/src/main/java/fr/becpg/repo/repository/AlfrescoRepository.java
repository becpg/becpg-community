/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
 * @author matthieu
 *
 * @param <T>
 * @since 1.5
 */
public interface AlfrescoRepository<T extends RepositoryEntity>  extends CrudRepository<T, NodeRef> {

	boolean hasDataList(RepositoryEntity entity, QName datalistContainerQname);
	boolean hasDataList(NodeRef entityNodeRef, QName datalistContainerQname);
	
	List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname);
	
	T create(NodeRef parentNodeRef, T entity);
	NodeRef getOrCreateDataListContainer(T entity);
	void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType, List<? extends RepositoryEntity> dataList);
	boolean isRegisteredType(QName type);
	

}
