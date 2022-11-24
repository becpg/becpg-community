package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;

public interface BeCPGAuditService {
	
	List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditFilter);
	
	AuditScope startAudit(AuditType auditType);
	
	void deleteAuditEntries(AuditType type, Long fromId, Long toId);
	
	void updateAuditEntry(AuditType type, Long id, Long time, Map<String, Serializable> values);

}
