package fr.becpg.repo.audit.service;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditFilter;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;

public interface BeCPGAuditService {
	
	List<JSONObject> getAuditStatistics(AuditType type, AuditFilter auditFilter);
	
	AuditScope startAudit(AuditType auditType);
	
	void deleteAuditStatitics(AuditType type, Long fromId, Long toId);

}
