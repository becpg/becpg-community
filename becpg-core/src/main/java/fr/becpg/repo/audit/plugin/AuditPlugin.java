package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditModel;
import fr.becpg.repo.audit.model.AuditType;

public interface AuditPlugin {

	String getAuditApplicationId();
	
	String getAuditApplicationPath();
	
	boolean applyTo(AuditType type);

	Map<String, String> getStatisticsKeyMap();

	int recordAuditEntry(Map<String, Serializable> auditValues, boolean updateEntry);
	
	List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter);

	int recordAuditEntry(AuditType type, AuditModel auditModel, boolean updateEntry);

}
