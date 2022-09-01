package fr.becpg.repo.audit;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.audit.impl.BeCPGAuditServiceImpl.AuditScopeBuilder;
import fr.becpg.repo.audit.model.DatabaseAuditType;

public interface BeCPGAuditService {
	
	AuditScopeBuilder createAudit();

	List<JSONObject> buildAuditStatistics(DatabaseAuditType type, Integer maxResults, String sortBy, String filter);

}
