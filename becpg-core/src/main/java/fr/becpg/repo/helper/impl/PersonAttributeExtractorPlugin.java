/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
*/
package fr.becpg.repo.helper.impl;

import java.util.Arrays;
import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * @author matthieu
 *
 */
@Service
public class PersonAttributeExtractorPlugin implements AttributeExtractorPlugin {

	@Autowired
	NodeService nodeService;
	
	@Autowired 
	PersonService personService;
	
	@Autowired
	BeCPGCacheService beCPGCacheService; 
	
	@Autowired
	NamespaceService namespaceService;
	 
	
	@Override
	public Collection<QName> getMatchingTypes() {
	
		return Arrays.asList(ContentModel.TYPE_PERSON,ContentModel.TYPE_AUTHORITY_CONTAINER);
	}
	

	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
    	if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
			return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
		} 
		return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
	}

	
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
			return type.toPrefixString(namespaceService).split(":")[1];
		} 
		return getPersonDisplayName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
	}

	
	public String getPersonDisplayName(final String userId) {
		if (userId == null) {
			return "";
		}
		if (userId.equalsIgnoreCase(AuthenticationUtil.getSystemUserName())) {
			return userId;
		}
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), userId + ".person", new BeCPGCacheDataProviderCallBack<String>() {
			public String getData() {
				String displayName = "";
				try {
					NodeRef personNodeRef = personService.getPerson(userId);
					if (personNodeRef != null) {
						displayName = nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
					}
				} catch (NoSuchPersonException e){
					//Case person was deleted
					return userId;
				}
				return displayName;
			}
		});

	}

}
