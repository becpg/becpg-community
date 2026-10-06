package fr.becpg.repo.audit.service;

import java.util.List;

import org.json.JSONObject;

import fr.becpg.repo.audit.model.AuditPage;
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
	 * The filters but the most selective one are applied in memory, on the entries the database
	 * returns within the query size: with several filters, the list may hold less entries than
	 * the matching ones. {@link #listAuditPage(AuditType, AuditQuery)} reads them all, page by page.
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.util.List} object
	 */
	List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditFilter);

	/**
	 * <p>listAuditPage.</p>
	 *
	 * Read one page of audit entries by keyset paging, so that the database only ever walks
	 * through a bounded window of entry identifiers.
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditPage} object
	 */
	AuditPage listAuditPage(AuditType type, AuditQuery auditFilter);
	
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
	 * Complete the entry left open by an operation recorded on start, the one matching the given
	 * filter.
	 *
	 * @param auditType a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param filterKey the audit key correlating the entry to the completed operation
	 * @param filterValue the value of that key
	 */
	void completeAuditEntry(AuditType auditType, String filterKey, String filterValue);

	/**
	 * <p>deleteAuditEntries.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @param fromId a {@link java.lang.Long} object
	 * @param toId a {@link java.lang.Long} object
	 */
	void deleteAuditEntries(AuditType type, Long fromId, Long toId);
	
}
