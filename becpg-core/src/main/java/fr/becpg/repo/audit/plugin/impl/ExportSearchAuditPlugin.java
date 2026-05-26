package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

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
	
	static {
		KEY_MAP.put(STARTED_AT, AuditDataType.DATE);
		KEY_MAP.put(COMPLETED_AT, AuditDataType.DATE);
		KEY_MAP.put(DURATION, AuditDataType.INTEGER);
		KEY_MAP.put(FILENAME, AuditDataType.STRING);
		KEY_MAP.put(USERNAME, AuditDataType.STRING);
		KEY_MAP.put(TEMPLATE, AuditDataType.STRING);
		KEY_MAP.put(RESULTS_SIZE, AuditDataType.INTEGER);
		KEY_MAP.put(ASYNC, AuditDataType.BOOLEAN);
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

	/** {@inheritDoc} */
	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		// nothing
	}

	/** {@inheritDoc} */
	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		// nothing
	}

}
