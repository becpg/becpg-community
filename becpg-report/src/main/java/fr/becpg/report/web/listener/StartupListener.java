package fr.becpg.report.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import fr.becpg.report.services.BirtPlatformListener;

/**
 * Start and stop Birt Platform
 * @author matthieu
 *
 */
public class StartupListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		BirtPlatformListener.start();
		BirtPlatformListener.getReportEngine();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		BirtPlatformListener.shutdown();
		
	}

}
