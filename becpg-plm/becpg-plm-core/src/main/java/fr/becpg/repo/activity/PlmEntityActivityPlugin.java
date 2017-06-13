package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;

@Service
public class PlmEntityActivityPlugin implements EntityActivityPlugin {	
	
	@Autowired
	EntityDictionaryService entityDictionaryService;
	
	
	public boolean isMatchingStateProperty(QName propName){
		return PLMModel.PROP_PRODUCT_STATE.isMatch(propName) || PLMModel.PROP_SUPPLIER_STATE.isMatch(propName);
	}

	@Override
	public boolean isMatchingEntityType(QName entityType) {
		
		return entityDictionaryService.isSubClass(entityType, PLMModel.TYPE_PRODUCT) || PLMModel.TYPE_SUPPLIER.isMatch(entityType);
	}


}
