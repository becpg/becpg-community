package fr.becpg.repo.audit.service;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;

public interface BeCPGAuditService {
	
	List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditFilter);
	
	AuditScope startAudit(AuditType auditType);
	
	AuditScope startAudit(AuditType auditType, Class<?> auditClass, String scopeName);
	
	void deleteAuditEntries(AuditType type, Long fromId, Long toId);
	
}
