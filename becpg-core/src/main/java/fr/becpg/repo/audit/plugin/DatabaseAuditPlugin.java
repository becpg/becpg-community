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

}
