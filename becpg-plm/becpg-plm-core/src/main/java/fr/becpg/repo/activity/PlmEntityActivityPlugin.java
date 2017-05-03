package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;

@Service
public class PlmEntityActivityPlugin implements EntityActivityPlugin {	
	
	public boolean isMatchingStateProperty(QName propName){
		return PLMModel.PROP_PRODUCT_STATE.isMatch(propName) || PLMModel.PROP_SUPPLIER_STATE.isMatch(propName);
	}

}
