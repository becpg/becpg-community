package fr.becpg.repo.audit.model;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.util.StopWatch;

import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.StopWatchAuditService;
import fr.becpg.repo.audit.plugin.TracerAuditService;
import io.opencensus.trace.AttributeValue;

public class AuditScope implements AutoCloseable {

	private DatabaseAuditPlugin databaseAuditPlugin;
	private TracerAuditService tracerAuditService;
	private StopWatchAuditService stopWatchAuditService;
	private String stopWatchName;
	private Map<String, Serializable> auditValues;
	private String tracerScopeName;
	private AutoCloseable tracerScope;
	private StopWatch stopWatch;
	private Log logger;
	
	public AuditScope(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, TracerAuditService tracerAuditPlugin, String tracerScopeName, StopWatchAuditService loggerAuditPlugin, String stopWatchName, Log logger) {
		this.databaseAuditPlugin = auditPlugin;
		this.tracerAuditService = tracerAuditPlugin;
		this.auditValues = auditValues;
		this.tracerScopeName = tracerScopeName;
		this.stopWatchAuditService = loggerAuditPlugin;
		this.stopWatchName = stopWatchName;
		this.logger = logger;
	}
	
	public void auditValue(String key, Serializable value) {
		auditValues.put(key, value);
	}

	public void open() {
		if (databaseAuditPlugin != null) {
			databaseAuditPlugin.recordAuditEntry(auditValues, true);
		}
		
		if (tracerAuditService != null) {
			tracerScope = tracerAuditService.start(tracerScopeName);
		}
		
		if (stopWatchAuditService != null) {
			stopWatch = stopWatchAuditService.start(logger);
		}
	}
	
	@Override
	public void close() {
		if (databaseAuditPlugin != null) {
			databaseAuditPlugin.recordAuditEntry(auditValues, false);
		}
		if (tracerAuditService != null) {
			tracerAuditService.stop(tracerScope);
		}
		if (stopWatchAuditService != null) {
			stopWatchAuditService.stop(logger, stopWatch, stopWatchName);
		}
	}

	public void putAttribute(String string, AttributeValue stringAttributeValue) {
		if (tracerAuditService != null) {
			tracerAuditService.putAttribute(string, stringAttributeValue);
		}
	}

	public void addAnnotation(String string) {
		if (tracerAuditService != null) {
			tracerAuditService.addAnnotation(string);
		}
	}

}