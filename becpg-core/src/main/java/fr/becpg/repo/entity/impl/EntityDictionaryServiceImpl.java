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
package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;

/**
 * 
 * @author matthieu Fast and cached access to dataDictionnary
 */
@Service("entityDictionaryService")
public class EntityDictionaryServiceImpl implements EntityDictionaryService {

	@Autowired
	public DictionaryService dictionaryService;

	@Autowired
	public BeCPGCacheService beCPGCacheService;

	@Autowired
	public RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;

	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {
		return repositoryEntityDefReader.getDefaultPivoAssocName(dataListItemType);
	}

	@Override
	public boolean isMultiLevelDataList(QName dataListItemType) {
		return repositoryEntityDefReader.isMultiLevelDataList(dataListItemType);
	}

	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType) {
		List<AssociationDefinition> ret = new ArrayList<>();
		for (QName assocQName : dictionaryService.getAllAssociations()) {
			AssociationDefinition assocDef = dictionaryService.getAssociation(assocQName);
			if (isSubClass(assocDef.getTargetClass().getName(), sourceType)) {
				ret.add(assocDef);
			}
		}
		return ret;
	}

	@Override
	public QName getTargetType(QName assocName) {
		return dictionaryService.getAssociation(assocName).getTargetClass().getName();
	}

	@Override
	public ClassAttributeDefinition getPropDef(final QName fieldQname) {

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), fieldQname.toString() + ".propDef",
				new BeCPGCacheDataProviderCallBack<ClassAttributeDefinition>() {
					public ClassAttributeDefinition getData() {
						ClassAttributeDefinition propDef = dictionaryService.getProperty(fieldQname);
						if (propDef == null) {
							propDef = dictionaryService.getAssociation(fieldQname);
						}

						return propDef;
					}
				});
	}

	@Override
	public boolean isSubClass(final QName fieldQname, final QName typeEntitylistItem) {
		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), fieldQname.toString() + "_" + typeEntitylistItem.toString() + ".isSubClass",
				new BeCPGCacheDataProviderCallBack<Boolean>() {
					public Boolean getData() {
						return dictionaryService.isSubClass(fieldQname, typeEntitylistItem);
					}
				});
	}

	@Override
	public Collection<QName> getSubTypes(final QName typeQname) {
		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), typeQname.toString() + ".getSubTypes",
				new BeCPGCacheDataProviderCallBack<Collection<QName>>() {
					public Collection<QName> getData() {
						return dictionaryService.getSubTypes(typeQname, true);
					}
				});
	}

}
