package fr.becpg.repo.audit.service;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;

/**
 * <p>BeCPGAuditService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BeCPGAuditService {
	
	/**
	 * <p>listAuditEntries.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.util.List} object
	 */
	List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditFilter);
	
	/**
	 * <p>startAudit.</p>
	 *
	 * @param auditType a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	AuditScope startAudit(AuditType auditType);
	
	/**
	 * <p>startAudit.</p>
	 *
	 * @param auditType a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param auditClass a {@link java.lang.Class} object
	 * @param scopeName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditScope} object
	 */
	AuditScope startAudit(AuditType auditType, Class<?> auditClass, String scopeName);
	
	/**
	 * <p>deleteAuditEntries.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param fromId a {@link java.lang.Long} object
	 * @param toId a {@link java.lang.Long} object
	 */
	void deleteAuditEntries(AuditType type, Long fromId, Long toId);
	
}
