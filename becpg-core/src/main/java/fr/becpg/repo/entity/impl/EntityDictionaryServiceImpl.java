package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;

@Service
/**
 * 
 * @author matthieu
 * Fast and cached access to dataDictionnary
 */
public class EntityDictionaryServiceImpl implements EntityDictionaryService {

	public DictionaryService dictionaryService;
	
	public BeCPGCacheService beCPGCacheService;
	
	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public QName getWUsedList(QName entityType) {

		QName wUsedList = null;

		if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_RAWMATERIAL.getLocalName())
				|| entityType.getLocalName().equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName())
				|| entityType.getLocalName().equals(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName())
				|| entityType.getLocalName().equals(BeCPGModel.TYPE_FINISHEDPRODUCT.getLocalName())) {
			wUsedList = BeCPGModel.TYPE_COMPOLIST;
		} else if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGMATERIAL.getLocalName())
				|| entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGKIT.getLocalName())) {
			wUsedList = BeCPGModel.TYPE_PACKAGINGLIST;
		} else if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_RESOURCEPRODUCT.getLocalName())) {
			wUsedList = MPMModel.TYPE_PROCESSLIST;
		}
		return wUsedList;
	}

	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {

		if (BeCPGModel.TYPE_COMPOLIST.equals(dataListItemType)) {
			return BeCPGModel.ASSOC_COMPOLIST_PRODUCT;
		} else if (BeCPGModel.TYPE_PACKAGINGLIST.equals(dataListItemType)) {
			return BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT;
		} else if (MPMModel.TYPE_PROCESSLIST.equals(dataListItemType)) {
			return MPMModel.ASSOC_PL_RESOURCE;
		}

		return null;
	}
	
	
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType){
		List<AssociationDefinition> ret = new ArrayList<>();
		for(QName assocQName :  dictionaryService.getAllAssociations()){
			AssociationDefinition assocDef = dictionaryService.getAssociation(assocQName);
			if(isSubClass(assocDef.getTargetClass().getName(),sourceType)){
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

		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), fieldQname.toString() + ".propDef",
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
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), fieldQname.toString() + "_" + typeEntitylistItem.toString() + ".isSubClass",
				new BeCPGCacheDataProviderCallBack<Boolean>() {
					public Boolean getData() {
						return dictionaryService.isSubClass(fieldQname, typeEntitylistItem);
					}
				});
	}

	@Override
	public Collection<QName> getSubTypes(final QName typeQname) {
		return beCPGCacheService.getFromCache(AttributeExtractorService.class.getName(), typeQname.toString() + ".getSubTypes",
				new BeCPGCacheDataProviderCallBack<Collection<QName>>() {
					public Collection<QName> getData() {
						return dictionaryService.getSubTypes(typeQname, true);
					}
				});
	}
	

}
