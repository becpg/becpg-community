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

//	@Override
//	public QName getWUsedList(QName entityType) {
//
//		QName wUsedList = null;
//
//		if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_RAWMATERIAL.getLocalName())
//				|| entityType.getLocalName().equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName())
//				|| entityType.getLocalName().equals(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName())
//				|| entityType.getLocalName().equals(BeCPGModel.TYPE_FINISHEDPRODUCT.getLocalName())) {
//			wUsedList = BeCPGModel.TYPE_COMPOLIST;
//		} else if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGMATERIAL.getLocalName())
//				|| entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGKIT.getLocalName())) {
//			wUsedList = BeCPGModel.TYPE_PACKAGINGLIST;
//		} else if (entityType != null && entityType.getLocalName().equals(BeCPGModel.TYPE_RESOURCEPRODUCT.getLocalName())) {
//			wUsedList = MPMModel.TYPE_PROCESSLIST;
//		}
//		return wUsedList;
//	}

	@Override
	//alfresco reporsitory
	public QName getDefaultPivotAssoc(QName dataListItemType) {

		if (BeCPGModel.TYPE_COMPOLIST.equals(dataListItemType)) {
			return fr.becpg.model.BeCPGModel.ASSOC_COMPOLIST_PRODUCT;
		} else if (BeCPGModel.TYPE_PACKAGINGLIST.equals(dataListItemType)) {
			return BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT;}
//		} else if (MPMModel.TYPE_PROCESSLIST.equals(dataListItemType)) {
//			return MPMModel.ASSOC_PL_RESOURCE;
//		}
		
//		//look for pivot
//		if(entityList.getLocalName().equals(BeCPGModel.TYPE_ALLERGENLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_ALLERGENLIST_ALLERGEN;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_COMPOLIST.getLocalName())){
//			isCompositeDL = true;
//			pivotProperty = BeCPGModel.ASSOC_COMPOLIST_PRODUCT;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_COSTLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_COSTLIST_COST;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_INGLABELINGLIST.getLocalName())){
//			//TODO
//			return;
//			//pivotProperty = BeCPGModel.PROP_ILL_GRP;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_INGLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_INGLIST_ING;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_MICROBIOLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_MICROBIOLIST_MICROBIO;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_NUTLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_NUTLIST_NUT;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_ORGANOLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_ORGANOLIST_ORGANO;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_PHYSICOCHEMLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_PRICELIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_PRICELIST_COST;
//		}
//		else if(entityList.getLocalName().equals(BeCPGModel.TYPE_LABELCLAIMLIST.getLocalName())){
//			pivotProperty = BeCPGModel.ASSOC_LCL_LABELCLAIM;
//		}
//		else{
			//TODO : specific entityLists ? Not implemented
		//	return;
	//	}

		return null;
	}
	

	@Override
	//TODO
	public boolean isMultiLevelDataList(QName dataListItemType) {
		// TODO Auto-generated method stub
		return false;
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
