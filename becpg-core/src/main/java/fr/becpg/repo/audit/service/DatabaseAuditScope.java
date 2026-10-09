package fr.becpg.repo.audit.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.alfresco.util.ISO8601DateFormat;

import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

/**
 * <p>DatabaseAuditScope class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DatabaseAuditScope implements AutoCloseable {
	
	private DatabaseAuditService databaseAuditService;
	
	private DatabaseAuditPlugin auditPlugin;
	
	private Map<String, Serializable> auditValues = new HashMap<>();
	
	private boolean shouldRecordAudit = true;
	
	private boolean startRecorded = false;
	
	private boolean completedElsewhere = false;
	
	/**
	 * <p>Constructor for DatabaseAuditScope.</p>
	 *
	 * @param auditPlugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param databaseAuditService a {@link fr.becpg.repo.audit.service.DatabaseAuditService} object
	 */
	public DatabaseAuditScope(DatabaseAuditPlugin auditPlugin, DatabaseAuditService databaseAuditService) {
		this.databaseAuditService = databaseAuditService;
		this.auditPlugin = auditPlugin;
	}
	
	/**
	 * <p>disableAuditRecord.</p>
	 */
	public void disableAuditRecord() {
		this.shouldRecordAudit = false;
	}
	
	/**
	 * Record the entry of an operation that may never complete, before running it.
	 *
	 * The entry is flagged as not completed and is replaced by the completed one when the scope is
	 * closed. Only the plugins declaring {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin#isRecordOnStart()}
	 * take part in this two phase recording.
	 */
	public void recordStart() {
		if (!shouldRecordAudit || startRecorded || !auditPlugin.isRecordOnStart()) {
			return;
		}

		auditValues.put(AuditPlugin.IS_COMPLETED, false);
		databaseAuditService.recordAuditEntry(auditPlugin, auditValues, false);
		startRecorded = true;
	}

	/**
	 * Hand the completion of the entry over to the thread that actually runs the operation.
	 *
	 * Closing the scope then only records the entry of the started operation: the duration of an
	 * asynchronous export measured by the thread that requested it would be the duration of the
	 * request, not the one of the export.
	 */
	public void deferCompletion() {
		this.completedElsewhere = true;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		if (!shouldRecordAudit) {
			return;
		}

		if (completedElsewhere) {
			recordStart();
			return;
		}

		completeAuditValues();

		databaseAuditService.recordAuditEntry(auditPlugin, auditValues, startRecorded);
	}

	/**
	 * <p>putAttribute.</p>
	 *
	 * @param string a {@link java.lang.String} object
	 * @param attribute a {@link java.lang.Object} object
	 */
	public void putAttribute(String string, Object attribute) {
		if (attribute instanceof Serializable) {
			auditValues.put(string, (Serializable) attribute);
		}
	}

	/**
	 * <p>start.</p>
	 *
	 * The identifier a completed entry replaces its started one on is drawn at random: derived from
	 * the start date alone, two operations starting in the same millisecond would share it and
	 * completing one would delete the entry of the other.
	 */
	public void start() {
		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.STARTED_AT)) {
			auditValues.put(AuditPlugin.STARTED_AT, ISO8601DateFormat.format(new Date()));
		}
		int id = Objects.hash(auditValues, UUID.randomUUID());
		auditValues.put(AuditPlugin.ID, id);
	}

	private void completeAuditValues() {
		Date end = new Date();

		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.COMPLETED_AT)) {
			auditValues.put(AuditPlugin.COMPLETED_AT, ISO8601DateFormat.format(end));
		}

		if (auditPlugin.getKeyMap().containsKey(AuditPlugin.STARTED_AT) && auditPlugin.getKeyMap().containsKey(AuditPlugin.DURATION)) {
			Date start = ISO8601DateFormat.parse(auditValues.get(AuditPlugin.STARTED_AT).toString());
			auditValues.put(AuditPlugin.DURATION, end.getTime() - start.getTime());
		}

		if (auditPlugin.isRecordOnStart()) {
			auditValues.put(AuditPlugin.IS_COMPLETED, true);
		}
	}

}
