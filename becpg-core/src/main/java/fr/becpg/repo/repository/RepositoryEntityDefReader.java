/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 */
public interface RepositoryEntityDefReader<T> {
	
	Map<QName, T> getEntityProperties(T entity);
	Map<QName, Serializable> getProperties(T entity);
    Map<QName, NodeRef> getSingleAssociations(T entity);	
    Map<QName, List<NodeRef>> getMultipleAssociations(T entity);
    <R> Map<QName, List<? extends RepositoryEntity>> getDataLists(R entity);
    Map<QName, T> getSingleEntityAssociations(T entity);
    Map<QName, ?> getDataListViews(T entity);
    Map<QName, Serializable> getIdentifierAttributes(T entity);
    
	QName getType(Class<? extends RepositoryEntity> clazz);
	QName readQName(Method method);
	boolean isEnforced(Class<? extends RepositoryEntity> clazz, QName propName);
	Class<T> getEntityClass(QName type);
	
	QName getDefaultPivoAssocName(QName dataListItemType);
	boolean isMultiLevelDataList(QName dataListItemType);
	
	
	
}
