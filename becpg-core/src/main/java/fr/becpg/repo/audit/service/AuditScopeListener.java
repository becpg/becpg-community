package fr.becpg.repo.audit.service;

import fr.becpg.repo.audit.model.AuditScope;

public interface AuditScopeListener {

	void onStart(AuditScope auditScope);
	
	void onClose(AuditScope auditScope);
	
}
