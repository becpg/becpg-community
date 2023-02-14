package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;

@Service
public class ActivityAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	static {
		KEY_MAP.put("prop_bcpg_alUserId", AuditDataType.STRING);
		KEY_MAP.put("prop_bcpg_alType", AuditDataType.STRING);
		KEY_MAP.put("prop_bcpg_alData", AuditDataType.STRING);
		KEY_MAP.put("prop_cm_created", AuditDataType.DATE);
		KEY_MAP.put("entityNodeRef", AuditDataType.STRING);
	}
	
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.ACTIVITY.equals(type);
	}

	@Override
	public String getAuditApplicationId() {
		return "beCPGActivityAudit";
	}

	@Override
	public String getAuditApplicationPath() {
		return "activity";
	}

	@Override
	public Class<?> getAuditClass() {
		return EntityActivityService.class;
	}
	
	@Override
	@Value("${becpg.audit.activity}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

}
