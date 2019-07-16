package fr.becpg.repo.helper;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.AutoNumService;

@Service
public class AutoNumHelper implements InitializingBean{


	@Autowired
	@Qualifier("autoNumService")
	private AutoNumService autoNumService;
	
	@Autowired
	private NamespaceService namespaceService;
	
	
    private static 	AutoNumHelper INSTANCE = null;
    
    

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;	
	}

	
	/**
	 * SPEL HELPER
	 * T(fr.becpg.repo.helper.AutoNumHelper).getAutoNumValue("bcpg:finishedProduct","bcpg:eanCode")
	 * T(fr.becpg.repo.helper.AutoNumHelper).getOrCreateCode(nodeRef,"bcpg:eanCode")
	 */
	public static String getAutoNumValue(String className, String propertyName) {
	  return INSTANCE.autoNumService.getAutoNumValue( QName.createQName(className, INSTANCE.namespaceService), QName.createQName(propertyName, INSTANCE.namespaceService));
	}
	
	public static String getOrCreateCode( NodeRef nodeRef,String propertyName) {
	  return INSTANCE.autoNumService.getOrCreateCode(nodeRef, QName.createQName(propertyName, INSTANCE.namespaceService));
	}

	
}
