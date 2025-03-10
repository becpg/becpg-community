package fr.becpg.repo.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;


/**
 * <p>SystemConfigurationRegistry class.</p>
 *
 * @author matthieu
 */
@Service("systemConfigurationRegistry")
public class SystemConfigurationRegistry {
	   private static SystemConfigurationService instance;

	    private final SystemConfigurationService systemConfigurationService;

	    @Autowired
	    /**
	     * <p>Constructor for SystemConfigurationRegistry.</p>
	     *
	     * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	     */
	    public SystemConfigurationRegistry(SystemConfigurationService systemConfigurationService) {
	        this.systemConfigurationService = systemConfigurationService;
	    }

	    @PostConstruct
	    private void init() {
	    	instance = this.systemConfigurationService;
	    }

	    /**
	     * <p>instance.</p>
	     *
	     * @return a {@link fr.becpg.repo.system.SystemConfigurationService} object
	     */
	    public static SystemConfigurationService instance() {
	        return instance;
	    }
}
