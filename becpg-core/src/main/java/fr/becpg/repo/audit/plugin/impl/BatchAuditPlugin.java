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

/**
 * <p>BatchAuditPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BatchAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	/** Constant <code>TOTAL_ITEMS="totalItems"</code> */
	public static final String TOTAL_ITEMS = "totalItems";
	/** Constant <code>TOTAL_ERRORS="totalErrors"</code> */
	public static final String TOTAL_ERRORS = "totalErrors";
	/** Constant <code>IS_COMPLETED="isCompleted"</code> */
	public static final String IS_COMPLETED = "isCompleted";
	/** Constant <code>BATCH_USER="batchUser"</code> */
	public static final String BATCH_USER = "batchUser";
	/** Constant <code>BATCH_ID="batchId"</code> */
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
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.BATCH.equals(type);
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationId() {
		return BATCH_AUDIT_ID;
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationPath() {
		return BATCH;
	}
	
	/** {@inheritDoc} */
	@Override
	@Value("${becpg.audit.batch}")
	protected void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getAuditedClass() {
		return BatchQueueService.class;
	}

	/** {@inheritDoc} */
	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.pushAuthentication();
		AuthenticationUtil.setFullyAuthenticatedUser((String) auditValues.get(BATCH_USER));
	}

	/** {@inheritDoc} */
	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		AuthenticationUtil.popAuthentication();
	}

}
