package fr.becpg.repo.audit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditModel;
import fr.becpg.repo.audit.model.AuditType;

public interface BeCPGAuditService {

	List<JSONObject> buildAuditStatistics(AuditType type, Integer maxResults, String sortBy, String filter);

	int recordAuditEntry(AuditType type, Map<String, Serializable> auditValues, boolean updateEntry);
	
	int recordAuditEntry(AuditType type, AuditModel auditModel, boolean updateEntry);

}
