/*
 * 
 */
package fr.becpg.repo.report.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class BirtConfiguration.
 *
 * @author querephi
 */
public class BirtConfiguration {

	/** The logger. */
	private static Log logger = LogFactory.getLog(BirtConfiguration.class);
	
	/** The birt runtime location. */
	private String birtRuntimeLocation;
	
	/** The engine config. */
	private EngineConfig engineConfig;
	
	/**
	 * Sets the birt runtime location.
	 *
	 * @param birtRuntimeLocation the new birt runtime location
	 */
	public void setBirtRuntimeLocation(String birtRuntimeLocation) {
	    this.birtRuntimeLocation = birtRuntimeLocation;
	  }
	  
	/**
	 * Gets the engine config.
	 *
	 * @return the engine config
	 */
	public EngineConfig getEngineConfig() {
		if (engineConfig == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new instance of EngineConfig");
				logger.debug("birtRuntimeLocation : " + birtRuntimeLocation);
			}
	 			
			engineConfig = new EngineConfig();
			engineConfig.setBIRTHome(birtRuntimeLocation);
	      	}
	 
	    	return engineConfig;
	}
}
