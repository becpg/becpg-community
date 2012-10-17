package fr.becpg.repo.entity.impl;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.repo.entity.EntityDictionaryService;

@Service
public class EntityDictionaryServiceImpl implements EntityDictionaryService {

	@Override
	public QName getWUsedList(QName entityType) {
		
		QName wUsedList = null;
		
		if(entityType == null){
			
		}
		else if(entityType.getLocalName().equals(BeCPGModel.TYPE_RAWMATERIAL.getLocalName()) ||
				entityType.getLocalName().equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName()) ||
				entityType.getLocalName().equals(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName()) ||
				entityType.getLocalName().equals(BeCPGModel.TYPE_FINISHEDPRODUCT.getLocalName())){
			wUsedList = BeCPGModel.TYPE_COMPOLIST;
		}
		else if(entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGMATERIAL.getLocalName()) ||
				entityType.getLocalName().equals(BeCPGModel.TYPE_PACKAGINGKIT.getLocalName())){
			wUsedList = BeCPGModel.TYPE_PACKAGINGLIST;
		} else if(entityType.getLocalName().equals(BeCPGModel.TYPE_RESOURCEPRODUCT.getLocalName())){
			wUsedList = MPMModel.TYPE_PROCESSLIST;
		}
		return wUsedList;
	}
	

	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {

		
		if(BeCPGModel.TYPE_COMPOLIST.equals(dataListItemType)){
			return BeCPGModel.ASSOC_COMPOLIST_PRODUCT;
		} else if(BeCPGModel.TYPE_PACKAGINGLIST.equals(dataListItemType)){
			return BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT;
		} else if(MPMModel.TYPE_PROCESSLIST.equals(dataListItemType)){
			return MPMModel.ASSOC_PL_RESOURCE;
		} 
		
		return null;
	}


}
