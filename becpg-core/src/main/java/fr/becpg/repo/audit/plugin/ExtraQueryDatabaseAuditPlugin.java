package fr.becpg.repo.audit.plugin;

import fr.becpg.repo.audit.model.AuditQuery;

/**
 * <p>ExtraQueryDatabaseAuditPlugin interface.</p>
 *
 * @author matthieu
 */
public interface ExtraQueryDatabaseAuditPlugin extends DatabaseAuditPlugin {

	/**
	 * <p>extraQuery.</p>
	 *
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 */
	AuditQuery extraQuery(AuditQuery auditQuery);
	
}
