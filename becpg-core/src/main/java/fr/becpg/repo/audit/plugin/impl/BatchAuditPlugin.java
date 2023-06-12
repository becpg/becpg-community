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
import fr.becpg.repo.batch.BatchQueueService;

@Service
public class BatchAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	public static final String TOTAL_ITEMS = "totalItems";
	public static final String TOTAL_ERRORS = "totalErrors";
	public static final String IS_COMPLETED = "isCompleted";
	public static final String BATCH_USER = "batchUser";
	public static final String BATCH_ID = "batchId";
	private static final String BATCH = "batch";
	private static final String BATCH_AUDIT_ID = "beCPGBatchAudit";
	
	static {
		KEY_MAP.put(STARTED_AT, AuditDataType.DATE);
		KEY_MAP.put(COMPLETED_AT, AuditDataType.DATE);
		KEY_MAP.put(DURATION, AuditDataType.INTEGER);
		KEY_MAP.put(BATCH_ID, AuditDataType.STRING);
		KEY_MAP.put(BATCH_USER, AuditDataType.STRING);
		KEY_MAP.put(IS_COMPLETED, AuditDataType.BOOLEAN);
		KEY_MAP.put(TOTAL_ITEMS, AuditDataType.INTEGER);
		KEY_MAP.put(TOTAL_ERRORS, AuditDataType.INTEGER);
	}
	
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.BATCH.equals(type);
	}

	@Override
	public String getAuditApplicationId() {
		return BATCH_AUDIT_ID;
	}

	@Override
	public String getAuditApplicationPath() {
		return BATCH;
	}
	
	@Override
	@Value("${becpg.audit.batch}")
	protected void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	@Override
	public Class<?> getAuditedClass() {
		return BatchQueueService.class;
	}

	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.pushAuthentication();
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get(BATCH_USER));
	}

	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.popAuthentication();
	}

}