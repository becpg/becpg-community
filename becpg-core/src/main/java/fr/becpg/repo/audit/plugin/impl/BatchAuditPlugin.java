package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.model.AuditEntry;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;

@Service
public class BatchAuditPlugin extends AbstractAuditPlugin {

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
	public String getAuditApplicationId() {
		return "beCPGBatchAudit";
	}

	@Override
	public String getAuditApplicationPath() {
		return "/beCPGBatchAudit/batch";
	}

	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.BATCH.equals(type);
	}
	
	@Override
	public Map<String, String> getStatisticsKeyMap() {
		return BATCH_KEY_MAP;
	}

	@Override
	public void recordAuditEntry(Map<String, Serializable> auditValues) {
		
		AuthenticationUtil.pushAuthentication();
		
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get("batch/batchUser"));
		
		AuditEntry entryToDelete = null;
		
		// delete the old entry
		if ((boolean) auditValues.get("batch/isCompleted")) {
			
			Collection<AuditEntry> entries = listAuditEntries(10, "hashCode=" + auditValues.get("batch/hashCode"));
			
			if (!entries.isEmpty()) {
				entryToDelete = entries.iterator().next();
			}
		}
		
		super.recordAuditEntry(auditValues);
		
		if (entryToDelete != null) {
			auditComponent.deleteAuditEntries(Arrays.asList(entryToDelete.getId()));
		}
		
		AuthenticationUtil.popAuthentication();

	}

}