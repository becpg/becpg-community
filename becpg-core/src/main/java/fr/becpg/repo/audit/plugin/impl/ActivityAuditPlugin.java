package fr.becpg.repo.audit.plugin.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;

@Service
public class ActivityAuditPlugin extends AbstractAuditPlugin {

	static {
		KEY_MAP.put("userId", AuditDataType.STRING);
		KEY_MAP.put("activityType", AuditDataType.STRING);
		KEY_MAP.put("activityData", AuditDataType.STRING);
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

}
