package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

public interface DatabaseAuditService {
	
	List<JSONObject> listAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter);

	int recordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry);

	void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId);

}
