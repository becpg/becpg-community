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
package fr.becpg.repo.entity;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;


public interface EntityTplService {

	/**
	 * Create the entityTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists, Set<String> subFolders);
	
	/**
	 * Look for the entityTpl
	 * @param isContainer
	 * @param nodeType
	 * @return
	 */
	public NodeRef getEntityTpl(QName nodeType);
	
	public void synchronizeEntities(NodeRef tplNodeRef);
	
	public void formulateEntities(NodeRef tplNodeRef) throws FormulateException;
}
