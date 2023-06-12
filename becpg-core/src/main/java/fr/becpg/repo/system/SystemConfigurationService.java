package fr.becpg.repo.system;

/**
 * 
 * @author matthieu
 * Store/Get system prop value in database
 */
public interface SystemConfigurationService {

	String confValue(String propKey);	
	void updateConfValue(String propKey, String value);
	
}
