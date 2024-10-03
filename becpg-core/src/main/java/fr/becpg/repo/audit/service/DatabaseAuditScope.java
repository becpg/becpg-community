package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.util.ISO8601DateFormat;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

/**
 * <p>DatabaseAuditScope class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DatabaseAuditScope implements AutoCloseable {
	
	private DatabaseAuditService databaseAuditService;
	
	private DatabaseAuditPlugin auditPlugin;
	
	private Map<String, Serializable> auditValues = new HashMap<>();
	
	private boolean shouldRecordAudit = true;
	
	/**
	 * <p>Constructor for DatabaseAuditScope.</p>
	 *
	 * @param auditPlugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param databaseAuditService a {@link fr.becpg.repo.audit.service.DatabaseAuditService} object
	 */
	public DatabaseAuditScope(DatabaseAuditPlugin auditPlugin, DatabaseAuditService databaseAuditService) {
		this.databaseAuditService = databaseAuditService;
		this.auditPlugin = auditPlugin;
	}
	
	/**
	 * <p>disableAuditRecord.</p>
	 */
	public void disableAuditRecord() {
		this.shouldRecordAudit = false;
	}
	
	/** {@inheritDoc} */
	@Override
	public void close() {
		Date end = new Date();
		
		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.COMPLETED_AT)) {
			auditValues.put(AuditPlugin.COMPLETED_AT, ISO8601DateFormat.format(end));
		}
		
		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.STARTED_AT) && auditPlugin.getKeyMap().containsKey(AuditPlugin.DURATION)) {
			Date start = ISO8601DateFormat.parse(auditValues.get(AuditPlugin.STARTED_AT).toString());
			auditValues.put(AuditPlugin.DURATION, end.getTime() - start.getTime());
		}
		
		if (shouldRecordAudit) {
			databaseAuditService.recordAuditEntry(auditPlugin, auditValues, false);
		}
	}

	/**
	 * <p>putAttribute.</p>
	 *
	 * @param string a {@link java.lang.String} object
	 * @param attribute a {@link java.lang.Object} object
	 */
	public void putAttribute(String string, Object attribute) {
		if (attribute instanceof Serializable) {
			auditValues.put(string, (Serializable) attribute);
		}
	}

	/**
	 * <p>start.</p>
	 */
	public void start() {
		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.STARTED_AT)) {
			auditValues.put(AuditPlugin.STARTED_AT, ISO8601DateFormat.format(new Date()));
		}
		int id = Objects.hash(auditValues);
		auditValues.put(AuditPlugin.ID, id);
	}

}
