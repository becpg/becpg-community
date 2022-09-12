package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.plugin.AuditPlugin;

public interface DatabaseAuditService {
	
	List<JSONObject> getAuditStatistics(AuditPlugin plugin, Integer maxResults, String sortBy, String filter);

	int recordAuditEntry(AuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean b);
}
