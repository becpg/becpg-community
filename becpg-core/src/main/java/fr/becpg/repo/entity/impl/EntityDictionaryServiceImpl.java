/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
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

	private Map<QName,QName> propDefMapping = new HashMap<>();
	
	
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
	public boolean isMultiLevelLeaf(QName entityType) {
		return repositoryEntityDefReader.isMultiLevelLeaf(entityType);
	}
	
	@Override
	public QName getMultiLevelSecondaryPivot(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelSecondaryPivot(dataListItemType);
	}
	
   @Override
   public void registerPropDefMapping(QName orig, QName dest){
	   propDefMapping.put(orig, dest);
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
	public ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname) {
		
		if(propDefMapping.containsKey(fieldQname)){
			return getPropDef(propDefMapping.get(fieldQname));
		}
		
		if( fieldQname.getLocalName().contains(itemType.getLocalName())){
			QName newQname = QName.createQName(fieldQname.getNamespaceURI(),fieldQname.getLocalName().replace(itemType.getLocalName(), newItemType.getLocalName()) );
			ClassAttributeDefinition ret = getPropDef(newQname);
			if(ret!=null){
				return ret;
			}
		}
		
		return getPropDef(fieldQname);
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
	public boolean isSubClass(final QName className, final QName ofClassName) {
		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), className.toString() + "_" + ofClassName.toString() + ".isSubClass",
				new BeCPGCacheDataProviderCallBack<Boolean>() {
					public Boolean getData() {
						return dictionaryService.isSubClass(className, ofClassName);
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



	@Override
	public boolean isAssoc(QName assocName) {
		return dictionaryService.getAssociation(assocName)!=null;

	}

	
	@Override
	public TypeDefinition getType(QName type) {
		return dictionaryService.getType(type);

	}

	@Override
	public AspectDefinition getAspect(QName aspect) {
		return dictionaryService.getAspect(aspect);

	}

	@Override
	public PropertyDefinition getProperty(QName key) {
		return dictionaryService.getProperty(key);

	}

	@Override
	public AssociationDefinition getAssociation(QName qName) {
		return dictionaryService.getAssociation(qName);

	}

	@Override
	public DictionaryService getDictionaryService() {
		return dictionaryService;
	}

	@Override
	public ClassDefinition getClass(QName type) {
		return dictionaryService.getClass(type);
	}





}
