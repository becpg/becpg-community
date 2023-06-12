package fr.becpg.repo.audit.model;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.AuditScopeListener;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditScope;
import fr.becpg.repo.audit.service.StopWatchScope;

public class AuditScope implements AutoCloseable {

	private DatabaseAuditScope databaseScope;
	
	private StopWatchScope stopWatchScope;
	
	private AuditScopeListener auditScopeListener;
	
	private AuditScope parentScope;
	
	public AuditScope(AuditPlugin plugin, DatabaseAuditService databaseAuditService,
			AuditScopeListener auditScopeListener, Class<?> callerClass, String scopeName) {
		
		this.auditScopeListener = auditScopeListener;
		
		if (plugin.isDatabaseEnable()) {
			databaseScope = new DatabaseAuditScope((DatabaseAuditPlugin) plugin, databaseAuditService);
		}
		
		if (plugin.isStopWatchEnable()) {
			stopWatchScope = new StopWatchScope(scopeName, LogFactory.getLog(callerClass));
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
		
		return this;
	}

	@Override
	public void close() {
		
		if (databaseScope != null) {
			databaseScope.close();
		}
		
		if (stopWatchScope != null) {
			stopWatchScope.close();
		}
		
		if (auditScopeListener != null) {
			auditScopeListener.onClose(this);
		}
		
	}

	public void putAttribute(String string, Object attribute) {
		if (databaseScope != null) {
			databaseScope.putAttribute(string, attribute);
		}
	}

	public void addCheckpoint(String checkpointName) {
		if (stopWatchScope != null) {
			stopWatchScope.addCheckpoint(checkpointName);
		}
	}
	
}