package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

import fr.becpg.repo.system.SystemConfigurationService;

public class SystemConfigScriptHelper extends BaseScopableProcessorExtension {

	SystemConfigurationService systemConfigurationService;

	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}
	
	
	public String confValue(String propKey) {
		return systemConfigurationService.confValue(propKey);
	}
	
	public void updateConfValue(String propKey, String value) {
		 systemConfigurationService.updateConfValue(propKey, value);
	}
}
