package fr.becpg.repo.solr;

import org.alfresco.repo.search.TypeIndexFilter;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>BeCPGIndexFilter class.</p>
 *
 * @author matthieu
 */
public class BeCPGIndexFilter extends TypeIndexFilter {

	private static final String CONF_KEY = "beCPG.solr.enableIndexForTypes";
	
	private static Log logger = LogFactory.getLog(BeCPGIndexFilter.class);
	
	
	SystemConfigurationService systemConfigurationService;

	
	
	 /**
	  * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	  *
	  * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	  */
	 public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}


	/** {@inheritDoc} */
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
