/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.repo.entity.remote.extractor;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

public class RemoteHelper {
	
	@Deprecated
	//Slow
	public static QName getPropName(QName type, DictionaryService dictionaryService) {
		if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)){
			return BeCPGModel.PROP_LKV_VALUE;
		} else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)){
		    return BeCPGModel.PROP_CHARACT_NAME;
		} else if(ContentModel.TYPE_PERSON.equals(type)){
			return ContentModel.PROP_USERNAME;
		} else if(ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)){
			return ContentModel.PROP_AUTHORITY_NAME;
		}
		return ContentModel.PROP_NAME;
	}

	public static QName getPropName(QName type, EntityDictionaryService dictionaryService) {
		if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_LINKED_VALUE)){
			return BeCPGModel.PROP_LKV_VALUE;
		} else if(dictionaryService.isSubClass(type, BeCPGModel.TYPE_CHARACT)){
		    return BeCPGModel.PROP_CHARACT_NAME;
		} else if(ContentModel.TYPE_PERSON.equals(type)){
			return ContentModel.PROP_USERNAME;
		} else if(ContentModel.TYPE_AUTHORITY_CONTAINER.equals(type)){
			return ContentModel.PROP_AUTHORITY_NAME;
		}
		return ContentModel.PROP_NAME;
	}
	
}
