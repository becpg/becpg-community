package fr.becpg.repo.entity.impl;

import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

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
		}
			
		return wUsedList;
	}

}
