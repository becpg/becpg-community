package fr.becpg.repo.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service("systemConfigurationRegistry")
public class SystemConfigurationRegistry {
	   private static SystemConfigurationService instance;

	    private final SystemConfigurationService systemConfigurationService;

	    @Autowired
	    public SystemConfigurationRegistry(SystemConfigurationService systemConfigurationService) {
	        this.systemConfigurationService = systemConfigurationService;
	    }

	    @PostConstruct
	    private void init() {
	    	instance = this.systemConfigurationService;
	    }

	    public static SystemConfigurationService instance() {
	        return instance;
	    }
}
