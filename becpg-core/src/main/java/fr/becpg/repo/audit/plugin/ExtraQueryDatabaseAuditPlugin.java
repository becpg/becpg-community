package fr.becpg.repo.audit.plugin;

import fr.becpg.repo.audit.model.AuditQuery;

public interface ExtraQueryDatabaseAuditPlugin extends DatabaseAuditPlugin {

	AuditQuery extraQuery(AuditQuery auditQuery);
	
}
