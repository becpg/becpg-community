package fr.becpg.repo.system;

/**
 * 
 * @author matthieu
 * Store/Get system prop value in database
 */
public interface SystemConfigurationService {

	/**
	 * <p>confValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String confValue(String propKey);	
	void resetConfValue(String propKey);	
	void updateConfValue(String propKey, String value);
	
	
	
}
