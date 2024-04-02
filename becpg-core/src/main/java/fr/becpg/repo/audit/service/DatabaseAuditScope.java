package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.util.ISO8601DateFormat;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

public class DatabaseAuditScope implements AutoCloseable {
	
	private DatabaseAuditService databaseAuditService;
	
	private DatabaseAuditPlugin auditPlugin;
	
	private Map<String, Serializable> auditValues = new HashMap<>();
	
	public DatabaseAuditScope(DatabaseAuditPlugin auditPlugin, DatabaseAuditService databaseAuditService) {
		this.databaseAuditService = databaseAuditService;
		this.auditPlugin = auditPlugin;
	}
	
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

		databaseAuditService.recordAuditEntry(auditPlugin, auditValues, false);
	}

	public void putAttribute(String string, Object attribute) {
		if (attribute instanceof Serializable) {
			auditValues.put(string, (Serializable) attribute);
		}
	}

	public void start() {
		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.STARTED_AT)) {
			auditValues.put(AuditPlugin.STARTED_AT, ISO8601DateFormat.format(new Date()));
		}
		int id = Objects.hash(auditValues);
		auditValues.put(AuditPlugin.ID, id);
	}

}
