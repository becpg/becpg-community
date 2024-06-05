package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.report.entity.impl.EntityReportServiceImpl;

/**
 * <p>ReportAuditPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ReportAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	/** Constant <code>ENTITY_NODE_REF="entityNodeRef"</code> */
	public static final String ENTITY_NODE_REF = "entityNodeRef";
	/** Constant <code>LOCALE="locale"</code> */
	public static final String LOCALE = "locale";
	/** Constant <code>FORMAT="format"</code> */
	public static final String FORMAT = "format";
	/** Constant <code>NAME="name"</code> */
	public static final String NAME = "name";
	/** Constant <code>DATASOURCE_SIZE="datasourceSize"</code> */
	public static final String DATASOURCE_SIZE = "datasourceSize";

	private static final String REPORT = "report";
	private static final String REPORT_AUDIT_ID = "beCPGReportAudit";
	
	static {
		KEY_MAP.put(STARTED_AT, AuditDataType.DATE);
		KEY_MAP.put(COMPLETED_AT, AuditDataType.DATE);
		KEY_MAP.put(DURATION, AuditDataType.INTEGER);
		KEY_MAP.put(FORMAT, AuditDataType.STRING);
		KEY_MAP.put(ENTITY_NODE_REF, AuditDataType.STRING);
		KEY_MAP.put(LOCALE, AuditDataType.STRING);
		KEY_MAP.put(NAME, AuditDataType.STRING);
		KEY_MAP.put(DATASOURCE_SIZE, AuditDataType.INTEGER);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.REPORT.equals(type);
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationId() {
		return REPORT_AUDIT_ID;
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationPath() {
		return REPORT;
	}

	/** {@inheritDoc} */
	@Override
	@Value("${becpg.audit.report}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getAuditedClass() {
		return EntityReportServiceImpl.class;
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
