package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.Map;

import fr.becpg.repo.audit.model.AuditDataType;

/**
 * <p>DatabaseAuditPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DatabaseAuditPlugin extends AuditPlugin {

	/**
	 * <p>beforeRecordAuditEntry.</p>
	 *
	 * @param auditValues a {@link java.util.Map} object
	 */
	void beforeRecordAuditEntry(Map<String, Serializable> auditValues);

	/**
	 * <p>afterRecordAuditEntry.</p>
	 *
	 * @param auditValues a {@link java.util.Map} object
	 */
	void afterRecordAuditEntry(Map<String, Serializable> auditValues);

	/**
	 * <p>getAuditApplicationId.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getAuditApplicationId();
	
	/**
	 * <p>getAuditApplicationPath.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getAuditApplicationPath();
	
	/**
	 * <p>getKeyMap.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	Map<String, AuditDataType> getKeyMap();

	/**
	 * Whether the entry has to be recorded when the audited operation starts and completed when it
	 * ends, instead of being recorded once at the end.
	 *
	 * An operation heavy enough to bring the server down leaves no trace at all when its entry is
	 * only written on completion. Recording it upfront keeps the author, the volume and the start
	 * time available even when the operation never returns: such an entry simply stays flagged as
	 * not completed.
	 *
	 * @return a boolean
	 */
	default boolean isRecordOnStart() {
		return false;
	}

}
