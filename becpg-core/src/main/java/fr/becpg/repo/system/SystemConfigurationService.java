package fr.becpg.repo.system;

/**
 * <p>SystemConfigurationService interface.</p>
 *
 * @author matthieu
 * Store/Get system prop value in database
 * @version $Id: $Id
 */
public interface SystemConfigurationService {


	/**
	 * <p>confValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String confValue(String propKey);	
	/**
	 * <p>resetConfValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 */
	void resetConfValue(String propKey);	
	/**
	 * <p>updateConfValue.</p>
	 *
	 * @param propKey a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 */
	void updateConfValue(String propKey, String value);
	
	
	
}
