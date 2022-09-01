package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.DatabaseAuditType;

public interface DatabaseAuditPlugin {

	boolean applyTo(DatabaseAuditType type);

	int recordAuditEntry(Map<String, Serializable> auditValues, boolean open);

	List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter);

}
