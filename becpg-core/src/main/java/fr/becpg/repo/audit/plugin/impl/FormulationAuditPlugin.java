package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.formulation.impl.FormulationServiceImpl;

/**
 * <p>FormulationAuditPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class FormulationAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	/** Constant <code>ENTITY_NAME="entityName"</code> */
	public static final String ENTITY_NAME = "entityName";
	/** Constant <code>ENTITY_NODE_REF="entityNodeRef"</code> */
	public static final String ENTITY_NODE_REF = "entityNodeRef";
	/** Constant <code>CHAIN_ID="chainId"</code> */
	public static final String CHAIN_ID = "chainId";

	private static final String FORMULATION = "formulation";
	private static final String FORMULATION_AUDIT_ID = "beCPGFormulationAudit";
	
	static {
		KEY_MAP.put(STARTED_AT, AuditDataType.DATE);
		KEY_MAP.put(COMPLETED_AT, AuditDataType.DATE);
		KEY_MAP.put(DURATION, AuditDataType.INTEGER);
		KEY_MAP.put(CHAIN_ID, AuditDataType.STRING);
		KEY_MAP.put(ENTITY_NODE_REF, AuditDataType.STRING);
		KEY_MAP.put(ENTITY_NAME, AuditDataType.STRING);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.FORMULATION.equals(type);
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationId() {
		return FORMULATION_AUDIT_ID;
	}

	/** {@inheritDoc} */
	@Override
	public String getAuditApplicationPath() {
		return FORMULATION;
	}

	/** {@inheritDoc} */
	@Override
	@Value("${becpg.audit.formulation}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	/** {@inheritDoc} */
	@Override
	public Class<?> getAuditedClass() {
		return FormulationServiceImpl.class;
	}

	/** {@inheritDoc} */
	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

	/** {@inheritDoc} */
	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

}
