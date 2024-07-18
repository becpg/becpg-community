package fr.becpg.repo.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigurationRegistry {
	
	private SystemConfigurationRegistry() {
		//Do Nothing
	}

	@Autowired
	private static SystemConfigurationService systemConfigurationService;
	
	
	public static SystemConfigurationService instance() {
		return systemConfigurationService;
	}
	
	
}
