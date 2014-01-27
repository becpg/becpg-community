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

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.namespace.QName;

public interface EntityDictionaryService {

	QName getDefaultPivotAssoc(QName dataListItemType);
	
	boolean isMultiLevelDataList(QName dataListItemType);

	List<AssociationDefinition> getPivotAssocDefs(QName sourceType);

	QName getTargetType(QName createQName);
	
	Collection<QName> getSubTypes(QName typeQname);

	ClassAttributeDefinition getPropDef(QName fieldQname);

	boolean isSubClass(QName fieldQname, QName typeEntitylistItem);
	
}
