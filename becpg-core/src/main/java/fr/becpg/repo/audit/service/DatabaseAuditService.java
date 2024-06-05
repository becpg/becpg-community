package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

/**
 * <p>DatabaseAuditService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DatabaseAuditService {
	
	/**
	 * <p>listAuditEntries.</p>
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.util.List} object
	 */
	List<JSONObject> listAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter);

	/**
	 * <p>recordAuditEntry.</p>
	 *
	 * @param auditPlugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditValues a {@link java.util.Map} object
	 * @param deleteOldEntry a boolean
	 * @return a int
	 */
	int recordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry);

	/**
	 * <p>deleteAuditEntries.</p>
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param fromId a {@link java.lang.Long} object
	 * @param toId a {@link java.lang.Long} object
	 */
	void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId);

}
