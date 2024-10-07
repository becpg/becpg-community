package fr.becpg.repo.activity;

import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.behaviour.FieldBehaviourRegistry;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>PlmEntityActivityPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PlmEntityActivityPlugin extends DefaultEntityActivityPlugin implements EntityActivityPlugin, InitializingBean {	
	
	@Autowired
	EntityDictionaryService entityDictionaryService;
	
	
	/** {@inheritDoc} */
	public boolean isMatchingStateProperty(QName propName){
		return PLMModel.PROP_PRODUCT_STATE.isMatch(propName) || PLMModel.PROP_SUPPLIER_STATE.isMatch(propName)
				|| QualityModel.PROP_NC_STATE.isMatch(propName);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMatchingEntityType(QName entityType) {
		
		return entityDictionaryService.isSubClass(entityType, PLMModel.TYPE_PRODUCT) || PLMModel.TYPE_SUPPLIER.isMatch(entityType);
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		FieldBehaviourRegistry.registerIgnoredActivityFields(PLMModel.PROP_ILL_LOG_VALUE);
	}


}
