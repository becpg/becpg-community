package fr.becpg.repo.audit.model;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
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
	
	private ThreadLocal<AuditScope> threadLocalScope;
	
	private AuditScope parentScope;
	
	public AuditScope(ThreadLocal<AuditScope> threadLocalScope, AuditPlugin plugin, DatabaseAuditService databaseAuditService,
			StopWatchAuditService stopWatchAuditService, TracerAuditService tracerAuditService, Class<?> callerClass, String scopeName) {
		
		this.threadLocalScope = threadLocalScope;
		
		parentScope = threadLocalScope.get();
		
		if (plugin.isDatabaseEnable()) {
			databaseService = new DatabaseServiceWrapper((DatabaseAuditPlugin) plugin, databaseAuditService);
		}
		
		if (plugin.isStopWatchEnable()) {
			stopWatchService = new StopWatchServiceWrapper(stopWatchAuditService, plugin.getClass().getSimpleName() + " : " + callerClass.getName(), LogFactory.getLog(callerClass));
		}
		
		if (plugin.isTracerEnable()) {
			tracerService = new TracerServiceWrapper(tracerAuditService, scopeName);
		}
	}

	public AuditScope start() {
		
		threadLocalScope.set(this);
		
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

	@Override
	public void close() {
		
		threadLocalScope.remove();
		
		threadLocalScope.set(parentScope);
		
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

	public void addAnnotation(String annotation) {
		if (stopWatchService != null) {
			stopWatchService.addAnnotation(annotation);
		}
		if (tracerService != null) {
			tracerService.addAnnotation(annotation);
		}
	}
	
	public void addAnnotation(String description, Map<String, String> attributes) {
		if (tracerService != null) {
			tracerService.addAnnotation(description, attributes);
		}
	}

}