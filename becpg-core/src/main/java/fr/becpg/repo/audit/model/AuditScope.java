package fr.becpg.repo.audit.model;

import java.util.Map;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.AuditScopeListener;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.DatabaseScope;
import fr.becpg.repo.audit.service.StopWatchScope;
import fr.becpg.repo.audit.service.TracerAuditService;
import fr.becpg.repo.audit.service.TracerScope;

public class AuditScope implements AutoCloseable {

	private DatabaseScope databaseScope;
	
	private StopWatchScope stopWatchScope;
	
	private TracerScope tracerScope;
	
	private AuditScopeListener auditScopeListener;
	
	private AuditScope parentScope;
	
	public AuditScope(AuditPlugin plugin, DatabaseAuditService databaseAuditService,
			TracerAuditService tracerAuditService, AuditScopeListener auditScopeListener, Class<?> callerClass, String scopeName) {
		
		this.auditScopeListener = auditScopeListener;
		
		if (plugin.isDatabaseEnable()) {
			databaseScope = new DatabaseScope((DatabaseAuditPlugin) plugin, databaseAuditService);
		}
		
		if (plugin.isStopWatchEnable()) {
			stopWatchScope = new StopWatchScope(scopeName, LogFactory.getLog(callerClass));
		}
		
		if (plugin.isTracerEnable()) {
			tracerScope = new TracerScope(tracerAuditService, scopeName);
		}
	}
	
	public void setParentScope(AuditScope parentScope) {
		this.parentScope = parentScope;
	}
	
	public AuditScope getParentScope() {
		return parentScope;
	}

	public AuditScope start() {
		
		if (auditScopeListener != null) {
			auditScopeListener.onStart(this);
		}
		
		if (databaseScope != null) {
			databaseScope.start();
		}
		
		if (stopWatchScope != null) {
			stopWatchScope.start();
		}
		
		if (tracerScope != null) {
			tracerScope.start();
		}
		
		return this;
	}

	@Override
	public void close() {
		
		if (databaseScope != null) {
			databaseScope.stop();
		}
		
		if (stopWatchScope != null) {
			stopWatchScope.stop();
		}
		
		if (tracerScope != null) {
			tracerScope.stop();
		}
		
		if (auditScopeListener != null) {
			auditScopeListener.onClose(this);
		}
		
	}

	public void putAttribute(String string, Object attribute) {
		if (databaseScope != null) {
			databaseScope.putAttribute(string, attribute);
		}
		if (tracerScope != null) {
			tracerScope.putAttribute(string, attribute);
		}
	}

	public void addAnnotation(String annotation) {
		if (stopWatchScope != null) {
			stopWatchScope.addAnnotation(annotation);
		}
		if (tracerScope != null) {
			tracerScope.addAnnotation(annotation);
		}
	}
	
	public void addAnnotation(String description, Map<String, String> attributes) {
		if (tracerScope != null) {
			tracerScope.addAnnotation(description, attributes);
		}
	}

}