package fr.becpg.repo.audit.model;

import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.AuditScopeListener;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditScope;
import fr.becpg.repo.audit.service.StopWatchScope;

/**
 * <p>AuditScope class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AuditScope implements AutoCloseable {

	private DatabaseAuditScope databaseScope;
	
	private StopWatchScope stopWatchScope;
	
	private AuditScopeListener auditScopeListener;
	
	private AuditScope parentScope;
	
	/**
	 * <p>Constructor for AuditScope.</p>
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.AuditPlugin} object
	 * @param databaseAuditService a {@link fr.becpg.repo.audit.service.DatabaseAuditService} object
	 * @param auditScopeListener a {@link fr.becpg.repo.audit.service.AuditScopeListener} object
	 * @param callerClass a {@link java.lang.Class} object
	 * @param scopeName a {@link java.lang.String} object
	 */
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
	
	/**
	 * <p>Setter for the field <code>parentScope</code>.</p>
	 *
	 * @param parentScope a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	public void setParentScope(AuditScope parentScope) {
		this.parentScope = parentScope;
	}
	
	/**
	 * <p>Getter for the field <code>parentScope</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	public AuditScope getParentScope() {
		return parentScope;
	}

	/**
	 * <p>start.</p>
	 *
	 * @return a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
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

	/** {@inheritDoc} */
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

	/**
	 * <p>putAttribute.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param attribute a {@link java.lang.Object} object
	 */
	public void putAttribute(String key, Object attribute) {
		if (databaseScope != null) {
			databaseScope.putAttribute(key, attribute);
		}
	}

	/**
	 * <p>addCheckpoint.</p>
	 *
	 * @param checkpointName a {@link java.lang.String} object
	 */
	public void addCheckpoint(String checkpointName) {
		if (stopWatchScope != null) {
			stopWatchScope.addCheckpoint(checkpointName);
		}
	}
	
	/**
	 * <p>disable.</p>
	 */
	public void disable() {
		if (databaseScope != null) {
			databaseScope.disableAuditRecord();
		}
	}
	
}
