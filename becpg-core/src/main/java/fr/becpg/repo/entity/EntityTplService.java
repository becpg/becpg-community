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
package fr.becpg.repo.entity;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;


public interface EntityTplService {

	NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, String entityTplName,  boolean enabled, Set<QName> entityLists, Set<String> subFolders);
	
	NodeRef getEntityTpl(QName nodeType);
	
	NodeRef createWUsedList(NodeRef entityTplNodeRef, QName typeQName, QName assocQName);
	
	void synchronizeEntities(NodeRef tplNodeRef);
	
	void formulateEntities(NodeRef tplNodeRef) throws FormulateException;

	void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);

}
