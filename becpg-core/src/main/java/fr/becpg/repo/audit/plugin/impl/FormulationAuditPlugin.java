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

@Service
public class FormulationAuditPlugin extends AbstractAuditPlugin implements DatabaseAuditPlugin {

	public static final String ENTITY_NAME = "entityName";
	public static final String ENTITY_NODE_REF = "entityNodeRef";
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
	
	@Override
	public boolean applyTo(AuditType type) {
		return AuditType.FORMULATION.equals(type);
	}

	@Override
	public String getAuditApplicationId() {
		return FORMULATION_AUDIT_ID;
	}

	@Override
	public String getAuditApplicationPath() {
		return FORMULATION;
	}

	@Override
	@Value("${becpg.audit.formulation}")
	public void setAuditParameters(String auditParameters) {
		super.setAuditParameters(auditParameters);
	}

	@Override
	public Class<?> getAuditedClass() {
		return FormulationServiceImpl.class;
	}

	@Override
	public void beforeRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

	@Override
	public void afterRecordAuditEntry(Map<String, Serializable> auditValues) {
		
	}

}