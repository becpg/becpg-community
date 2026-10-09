package fr.becpg.repo.audit.model;

import java.util.List;

import org.json.JSONObject;

/**
 * <p>AuditPage class.</p>
 *
 * One page of audit entries read by keyset paging, along with what a caller needs to carry the
 * scan on where this page stopped.
 *
 * @author matthieu
 * @version $Id: $Id
 * @param entries the entries of the page, in the database order
 * @param nextStartAfterId the entry identifier the next page starts after, null when the scan
 *            reached the end of the audit application
 * @param scanInterrupted true when the scan spent its window budget before filling the page
 */
public record AuditPage(List<JSONObject> entries, Long nextStartAfterId, boolean scanInterrupted) {

	/**
	 * <p>empty.</p>
	 *
	 * @return a {@link fr.becpg.repo.audit.model.AuditPage} object
	 */
	public static AuditPage empty() {
		return new AuditPage(List.of(), null, false);
	}

}
