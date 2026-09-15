package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.web.scripts.report.ExportSearchWebScript;

/**
 * <p>ExportSearchAuditPlugin class.</p>
 *
 * @author matthieu
 */
@Service
public class ExportSearchAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	/** Constant <code>FILENAME="filename"</code> */
	public static final String FILENAME = "filename";
	/** Constant <code>USERNAME="username"</code> */
	public static final String USERNAME = "username";
	/** Constant <code>TEMPLATE="template"</code> */
	public static final String TEMPLATE = "template";
	/** Constant <code>RESULTS_SIZE="resultsSize"</code> */
	public static final String RESULTS_SIZE = "resultsSize";
	/** Constant <code>ASYNC="async"</code> */
	public static final String ASYNC = "async";
	/** Constant <code>DOWNLOAD_NODE_REF="downloadNodeRef"</code> */
	public static final String DOWNLOAD_NODE_REF = "downloadNodeRef";
	
	static {
		KEY_MAP.put(STARTED_AT, AuditDataType.DATE);
		KEY_MAP.put(COMPLETED_AT, AuditDataType.DATE);
		KEY_MAP.put(DURATION, AuditDataType.INTEGER);
		KEY_MAP.put(FILENAME, AuditDataType.STRING);
		KEY_MAP.put(USERNAME, AuditDataType.STRING);
		KEY_MAP.put(TEMPLATE, AuditDataType.STRING);
		KEY_MAP.put(RESULTS_SIZE, AuditDataType.INTEGER);
		KEY_MAP.put(ASYNC, AuditDataType.BOOLEAN);
		KEY_MAP.put(DOWNLOAD_NODE_REF, AuditDataType.STRING);
		KEY_MAP.put(IS_COMPLETED, AuditDataType.BOOLEAN);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * An export is heavy enough to bring the server down: its entry is recorded when the export
	 * starts so that the user who requested it stays traceable even when it never completes.
	 */
	@Override
	public boolean isRecordOnStart() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.EXPORT_SEARCH.equals(type);
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationId() {
		return "beCPGExportSearchAudit";
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationPath() {
		return "exportSearch";
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getAuditedClass() {
		return ExportSearchWebScript.class;
	}
	
	/** {@inheritDoc} */
	@Override
	@Value("${becpg.audit.exportSearch}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The entry of an asynchronous export is completed by the thread running it, as system: the
	 * export is credited to the user who requested it, not to the one recording the entry.
	 */
	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.pushAuthentication();

		Serializable username = auditValues.get(USERNAME);
		if ((username != null) && !username.toString().isBlank()) {
			AuthenticationUtil.setFullyAuthenticatedUser(username.toString());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.popAuthentication();
	}

}
