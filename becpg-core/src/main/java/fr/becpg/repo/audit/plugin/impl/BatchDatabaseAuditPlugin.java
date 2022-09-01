package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.DatabaseAuditType;
import fr.becpg.repo.audit.plugin.AbstractDatabaseAuditPlugin;

@Service
public class BatchDatabaseAuditPlugin extends AbstractDatabaseAuditPlugin {

	private static final String BATCH = "batch";

	private static final String BATCH_AUDIT_ID = "beCPGBatchAudit";
	
	static {
		KEY_MAP.put("batchId", "string");
		KEY_MAP.put("batchUser", "string");
		KEY_MAP.put("isCompleted", "boolean");
		KEY_MAP.put("totalItems", "int");
	}
	
	@Override
	public boolean applyTo(DatabaseAuditType type) {
		return DatabaseAuditType.BATCH.equals(type);
	}

	@Override
	public int recordAuditEntry(Map<String, Serializable> auditValues, boolean open) {
		
		AuthenticationUtil.pushAuthentication();
		
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get("batchUser"));
		
		int hashCode = super.recordAuditEntry(auditValues, open);
		
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

}