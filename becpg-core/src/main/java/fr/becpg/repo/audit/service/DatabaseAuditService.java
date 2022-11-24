package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.AuditPlugin;

public interface DatabaseAuditService {
	
	List<JSONObject> listAuditEntries(AuditPlugin plugin, AuditQuery auditFilter);

	int recordAuditEntry(AuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean b);

	void deleteAuditEntries(AuditPlugin plugin, Long fromId, Long toId);

	void updateAuditEntry(AuditPlugin plugin, Long id, Long time, Map<String, Serializable> auditValues);
}
