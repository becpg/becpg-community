package fr.becpg.repo.audit.model;

import java.io.Serializable;

import fr.becpg.repo.audit.exception.BeCPGAuditException;

/**
 * An equality criterion on one audit key, written "key=value".
 *
 * @param key the audit key the criterion reads
 * @param value the value the audit key must hold
 * @author valentin
 */
public record AuditFilter(String key, String value) {

	private static final String KEY_VALUE_SEPARATOR = "=";

	/**
	 * Parse a filter written "key=value". Only the first separator splits the filter, so that the
	 * value may itself hold one, as a node reference query string would.
	 *
	 * @param filter the filter to parse
	 * @return the parsed filter, key and value trimmed
	 * @throws fr.becpg.repo.audit.exception.BeCPGAuditException if the filter carries no separator
	 */
	public static AuditFilter parse(String filter) {
		String[] splitted = filter.split(KEY_VALUE_SEPARATOR, 2);
		if (splitted.length < 2) {
			throw new BeCPGAuditException("statistics filter '" + filter + "' has wrong syntax");
		}
		return new AuditFilter(splitted[0].trim(), splitted[1].trim());
	}

	/**
	 * Tell whether an audit value satisfies the filter. The value is compared on its text, the way
	 * the audit query of the database compares it.
	 *
	 * @param auditValue the value an audit entry holds for the key, null when it holds none
	 * @return true when the entry holds the expected value
	 */
	public boolean matches(Serializable auditValue) {
		return (auditValue != null) && value.equals(auditValue.toString());
	}
}
