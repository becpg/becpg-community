/*
 * 
 */
package fr.becpg.repo.report.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving birtPlatform events.
 * The class that is interested in processing a birtPlatform
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addBirtPlatformListener<code> method. When
 * the birtPlatform event occurs, that object's appropriate
 * method is invoked.
 *
 * @see BirtPlatformEvent
 */
public class BirtPlatformListener {

	/** The logger. */
	private static Log logger = LogFactory.getLog(BirtPlatformListener.class);
	
	/** The engine config. */
	 private EngineConfig engineConfig;
  	
	  /** The report engine. */
	  private IReportEngine reportEngine;
  	
	  
	  

	public BirtPlatformListener() {
		super();
		engineConfig = new EngineConfig();
	}

	/**
	   * Must be called before using the BIRT APIs. After retrieving the configuration from
	   * {@link XReportConfiguration} will start the BIRT {@link Platform}.
	   */
	  public void start() {
		  if (logger.isDebugEnabled()) {
			  logger.debug("Starting Eclipse BIRT platform");
		  }

		  try {
			  Platform.startup(engineConfig);
		  } 
		  catch (BirtException be) {
			  logger.debug("Failure starting BIRT platform. Error: " + be.getMessage());
			  throw new IllegalArgumentException("Failure starting BIRT platform", be);
		  }
		  catch (Exception e) {
			  logger.debug("Failure starting BIRT platform. Error: " + e.getMessage());
		  }
	  }

	  /**
	   * Must be called after finishing use of the Eclipse BIRT APIs. E.g. at application shutdown
	   * (hint, look at the destroy-method property of spring beans).
	   * 
	   */
	  public void shutdown() {
	    if (logger.isDebugEnabled()) {
	      logger.debug("Shutting down Eclipse BIRT platform");
	    }

	    if (reportEngine != null) {
	      if (logger.isDebugEnabled()) {
	        logger.debug("Shutting down report engine instance.");
	      }
	      reportEngine.destroy();
	    }

	    // Just call shutdown
	    Platform.shutdown();
	  }

	  /**
	   * Create or return the cached instance of the {@link IReportEngine}.
	   * 
	   * @return instance of {@link IReportEngine}
	   */
	  public IReportEngine getReportEngine() {
	    if (reportEngine == null) {
	      if (logger.isDebugEnabled()) {
	        logger.debug("Creating new instance of report engine.");
	      }
	      try{
	    	  
		      IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		      reportEngine = factory.createReportEngine(engineConfig);
		      
	      }catch(Exception e){
	    	  logger.error("Failed to create report engine.", e);
	      }
	    }

	    return reportEngine;
	  }
}
