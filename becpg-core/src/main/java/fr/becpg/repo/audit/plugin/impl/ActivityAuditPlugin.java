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

/**
 * <p>ActivityAuditPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ActivityAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	/** Constant <code>ENTITY_NODEREF="entityNodeRef"</code> */
	public static final String ENTITY_NODEREF = "entityNodeRef";
	/** Constant <code>PROP_CM_CREATED="prop_cm_created"</code> */
	public static final String PROP_CM_CREATED = "prop_cm_created";
	/** Constant <code>PROP_BCPG_AL_DATA="prop_bcpg_alData"</code> */
	public static final String PROP_BCPG_AL_DATA = "prop_bcpg_alData";
	/** Constant <code>PROP_BCPG_AL_TYPE="prop_bcpg_alType"</code> */
	public static final String PROP_BCPG_AL_TYPE = "prop_bcpg_alType";
	/** Constant <code>PROP_BCPG_AL_USER_ID="prop_bcpg_alUserId"</code> */
	public static final String PROP_BCPG_AL_USER_ID = "prop_bcpg_alUserId";

	static {
		KEY_MAP.put(PROP_BCPG_AL_USER_ID, AuditDataType.STRING);
		KEY_MAP.put(PROP_BCPG_AL_TYPE, AuditDataType.STRING);
		KEY_MAP.put(PROP_BCPG_AL_DATA, AuditDataType.STRING);
		KEY_MAP.put(PROP_CM_CREATED, AuditDataType.DATE);
		KEY_MAP.put(ENTITY_NODEREF, AuditDataType.STRING);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.ACTIVITY.equals(type);
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationId() {
		return "beCPGActivityAudit";
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationPath() {
		return "activity";
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getAuditedClass() {
		return EntityActivityService.class;
	}
	
	/** {@inheritDoc} */
	@Override
	@Value("${becpg.audit.activity}")
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
