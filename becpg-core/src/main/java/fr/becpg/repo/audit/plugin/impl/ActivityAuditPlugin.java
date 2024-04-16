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

	public static final String ENTITY_NODEREF = "entityNodeRef";
	public static final String PROP_CM_CREATED = "prop_cm_created";
	public static final String PROP_BCPG_AL_DATA = "prop_bcpg_alData";
	public static final String PROP_BCPG_AL_TYPE = "prop_bcpg_alType";
	public static final String PROP_BCPG_AL_USER_ID = "prop_bcpg_alUserId";

	static {
		KEY_MAP.put(PROP_BCPG_AL_USER_ID, AuditDataType.STRING);
		KEY_MAP.put(PROP_BCPG_AL_TYPE, AuditDataType.STRING);
		KEY_MAP.put(PROP_BCPG_AL_DATA, AuditDataType.STRING);
		KEY_MAP.put(PROP_CM_CREATED, AuditDataType.DATE);
		KEY_MAP.put(ENTITY_NODEREF, AuditDataType.STRING);
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
	public Class<?> getAuditedClass() {
		return EntityActivityService.class;
	}
	
	@Override
	@Value("${becpg.audit.activity}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		// nothing
	}

	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		// nothing
	}

}
