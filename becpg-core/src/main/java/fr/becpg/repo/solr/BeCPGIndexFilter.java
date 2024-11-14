package fr.becpg.repo.solr;

import org.alfresco.repo.search.TypeIndexFilter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.system.SystemConfigurationService;

public class BeCPGIndexFilter extends TypeIndexFilter {

	private static final String CONF_KEY = "beCPG.solr.enableIndexForTypes";
	
	private static Log logger = LogFactory.getLog(BeCPGIndexFilter.class);
	
	
	SystemConfigurationService systemConfigurationService;

	
	
	 public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}


    @Override
	public boolean shouldBeIgnored(QName nodeType)
	    {
    		boolean ret = super.shouldBeIgnored(nodeType);
    	
			if(ret && systemConfigurationService.confValue(CONF_KEY).contains(nodeType.toPrefixString(namespaceService))) {
				ret = false;
			}
			
			if(ret) {
				logger.info("Ignore index for type : "+nodeType);
			}
			
			
	        return ret;
	    }
}
