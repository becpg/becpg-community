package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.batch.BatchInfo;

@Service
public class BatchAuditPlugin extends AbstractAuditPlugin {

	private static final String BATCH = "batch";

	private static final String BATCH_AUDIT_ID = "beCPGBatchAudit";
	
	private static final Map<String, String> BATCH_KEY_MAP = new HashMap<>();
	
	static {
		BATCH_KEY_MAP.put("batchId", "string");
		BATCH_KEY_MAP.put("isCompleted", "boolean");
		BATCH_KEY_MAP.put("totalItems", "int");
		BATCH_KEY_MAP.put("duration", "int");
		BATCH_KEY_MAP.put("startedAt", "date");
		BATCH_KEY_MAP.put("completedAt", "date");
	}
	
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.BATCH.equals(type);
	}

	@Override
	public int recordAuditEntry(Map<String, Serializable> auditValues, boolean updateEntry) {
		
		AuthenticationUtil.pushAuthentication();
		
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get("batch/batchUser"));
		
		int hashCode = super.recordAuditEntry(auditValues, updateEntry);
		
		AuthenticationUtil.popAuthentication();
		
		return hashCode;
	
	}

	@Override
	protected String getAuditApplicationId() {
		return BATCH_AUDIT_ID;
	}

	@Override
	protected String getAuditApplicationPath() {
		return BATCH;
	}

	@Override
	protected Map<String, String> getStatisticsKeyMap() {
		return BATCH_KEY_MAP;
	}

	@Override
	protected int createHashCode(Map<String, Serializable> auditValues) {
		return Objects.hash(auditValues.get("batch/batchUser"), auditValues.get("batch/batchId"), auditValues.get("batch/startedAt"));

	}

	@Override
	protected Map<String, Serializable> extractModelValues(Object auditModel) {

		Map<String, Serializable> auditValues = new HashMap<>();
		
		if (auditModel instanceof BatchInfo) {
			BatchInfo batchInfo = (BatchInfo) auditModel;
			
			
			int batchHashCode = Objects.hash(batchInfo.hashCode(), batchInfo.getStartTime());
			
			auditValues.put("batch/hashCode", batchHashCode);
			auditValues.put("batch/batchUser", batchInfo.getBatchUser());
			auditValues.put("batch/batchId", batchInfo.getBatchId());
			auditValues.put("batch/totalItems", batchInfo.getTotalItems());
			auditValues.put("batch/startedAt", batchInfo.getStartTime());
			auditValues.put("batch/isCompleted", batchInfo.getIsCompleted());
			if (batchInfo.getEndTime() != null)  {
				auditValues.put("batch/completedAt", batchInfo.getEndTime());
				auditValues.put("batch/duration", batchInfo.getEndTime().getTime() - batchInfo.getStartTime().getTime());
			}
		}
		
		return auditValues;
	}

}