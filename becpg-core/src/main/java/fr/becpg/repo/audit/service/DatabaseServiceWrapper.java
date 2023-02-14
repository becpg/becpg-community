package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

public class DatabaseServiceWrapper {
	
	private static final String ID = "id";
	private static final String STARTED_AT = "startedAt";
	private static final String COMPLETED_AT = "completedAt";
	private static final String DURATION = "duration";
	
	private DatabaseAuditService databaseAuditService;
	
	private DatabaseAuditPlugin auditPlugin;
	
	private Map<String, Serializable> auditValues = new HashMap<>();
	
	public DatabaseServiceWrapper(DatabaseAuditPlugin auditPlugin, DatabaseAuditService databaseAuditService) {
		this.databaseAuditService = databaseAuditService;
		this.auditPlugin = auditPlugin;
	}

	public void stop() {
		Date end = new Date();
		
		auditValues.put(COMPLETED_AT, end);
		
		Date start = (Date) auditValues.get(STARTED_AT);
		
		auditValues.put(DURATION, end.getTime() - start.getTime());

		databaseAuditService.recordAuditEntry(auditPlugin, auditValues, false);
	}

	public void putAttribute(String string, Object attribute) {
		if (attribute instanceof Serializable) {
			auditValues.put(string, (Serializable) attribute);
		}
	}

	public void start() {
		auditValues.put(STARTED_AT, new Date());
		int id = Objects.hash(auditValues);
		auditValues.put(ID, id);
	}

}
