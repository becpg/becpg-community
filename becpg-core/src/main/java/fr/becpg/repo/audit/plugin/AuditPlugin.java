package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditType;

public interface AuditPlugin {

	boolean applyTo(AuditType type);

	int recordAuditEntry(AuditType type, Object auditModel, boolean updateEntry);
	
	int recordAuditEntry(Map<String, Serializable> auditValues, boolean updateEntry);

	List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter);

}
