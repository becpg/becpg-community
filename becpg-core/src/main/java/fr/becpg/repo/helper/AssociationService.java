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
package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

public interface AssociationService {

	void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs);
	void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef);
	NodeRef getTargetAssoc(NodeRef nodeRef, QName qName);
	NodeRef getTargetAssoc(NodeRef nodeRef, QName qName, boolean fromCache);
	List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName, boolean fromCache);
	List<NodeRef> getTargetAssocs(NodeRef nodeRef, QName qName);
	NodeRef getChildAssoc(NodeRef nodeRef, QName qName);
	List<NodeRef> getChildAssocs(NodeRef nodeRef, QName qName);
	List<NodeRef> getSourcesAssocs(NodeRef nodeRef, QNamePattern qName);
	String createCacheKey(NodeRef nodeRef, QName qName);
	
	
}
