package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.batch.BatchQueueService;

@Service
public class BatchAuditPlugin extends AbstractAuditPlugin {

	private static final String BATCH = "batch";

	private static final String BATCH_AUDIT_ID = "beCPGBatchAudit";
	
	static {
		KEY_MAP.put("batchId", AuditDataType.STRING);
		KEY_MAP.put("batchUser", AuditDataType.STRING);
		KEY_MAP.put("isCompleted", AuditDataType.BOOLEAN);
		KEY_MAP.put("totalItems", AuditDataType.INTEGER);
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
	public Class<?> getAuditClass() {
		return BatchQueueService.class;
	}

	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.pushAuthentication();
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get("batchUser"));
	}

	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.popAuthentication();
	}

}