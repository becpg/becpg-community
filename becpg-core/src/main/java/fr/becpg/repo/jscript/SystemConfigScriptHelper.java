package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SystemConfigScriptHelper class.</p>
 *
 * @author matthieu
 */
public class SystemConfigScriptHelper extends BaseScopableProcessorExtension {

	SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}
	
	
	/**
	 * <p>confValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String confValue(String propKey) {
		return systemConfigurationService.confValue(propKey);
	}
	
	/**
	 * <p>updateConfValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 */
	public void updateConfValue(String propKey, String value) {
		 systemConfigurationService.updateConfValue(propKey, value);
	}
}
