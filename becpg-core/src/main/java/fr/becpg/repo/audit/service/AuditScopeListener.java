package fr.becpg.repo.audit.service;

import fr.becpg.repo.audit.model.AuditScope;

/**
 * <p>AuditScopeListener interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface AuditScopeListener {

	/**
	 * <p>onStart.</p>
	 *
	 * @param auditScope a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	void onStart(AuditScope auditScope);
	
	/**
	 * <p>onClose.</p>
	 *
	 * @param auditScope a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	void onClose(AuditScope auditScope);
	
}
