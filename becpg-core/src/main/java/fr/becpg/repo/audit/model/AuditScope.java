package fr.becpg.repo.audit.model;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.DatabaseServiceWrapper;
import fr.becpg.repo.audit.service.StopWatchAuditService;
import fr.becpg.repo.audit.service.StopWatchServiceWrapper;
import fr.becpg.repo.audit.service.TracerAuditService;
import fr.becpg.repo.audit.service.TracerServiceWrapper;

public class AuditScope implements AutoCloseable {

	private DatabaseServiceWrapper databaseService;
	
	private StopWatchServiceWrapper stopWatchService;
	
	private TracerServiceWrapper tracerService;
	
	public AuditScope(AuditPlugin plugin, DatabaseAuditService databaseAuditService, StopWatchAuditService stopWatchAuditService, TracerAuditService tracerAuditService) {
		if (plugin.isDatabaseEnable()) {
			databaseService = new DatabaseServiceWrapper(plugin, databaseAuditService);
		}
		
		if (plugin.isStopWatchEnable()) {
			stopWatchService = new StopWatchServiceWrapper(stopWatchAuditService, plugin.getAuditApplicationPath() + " : " + plugin.getAuditClass().getName(), LogFactory.getLog(plugin.getAuditClass()));
		}
		
		if (plugin.isTracerEnable()) {
			tracerService = new TracerServiceWrapper(tracerAuditService, plugin.getAuditApplicationPath());
		}
	}

	@Override
	public void close() {
		
		if (databaseService != null) {
			databaseService.stop();
		}
		
		if (stopWatchService != null) {
			stopWatchService.stop();
		}
		
		if (tracerService != null) {
			tracerService.stop();
		}
		
	}

	public void putAttribute(String string, Object attribute) {
		if (databaseService != null) {
			databaseService.putAttribute(string, attribute);
		}
		if (tracerService != null) {
			tracerService.putAttribute(string, attribute);
		}
	}

	public void addAnnotation(String string) {
		if (stopWatchService != null) {
			stopWatchService.addAnnotation(string);
		}
		if (tracerService != null) {
			tracerService.addAnnotation(string);
		}
	}

	public AuditScope start() {
		
		if (databaseService != null) {
			databaseService.start();
		}
		
		if (stopWatchService != null) {
			stopWatchService.start();
		}
		
		if (tracerService != null) {
			tracerService.start();
		}
		
		return this;
	}

}