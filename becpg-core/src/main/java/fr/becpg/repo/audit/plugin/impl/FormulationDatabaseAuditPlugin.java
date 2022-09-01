package fr.becpg.repo.audit.plugin.impl;

import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.DatabaseAuditType;
import fr.becpg.repo.audit.plugin.AbstractDatabaseAuditPlugin;

@Service
public class FormulationDatabaseAuditPlugin extends AbstractDatabaseAuditPlugin {

	private static final String FORMULATION = "formulation";

	private static final String FORMULATION_AUDIT_ID = "beCPGFormulationAudit";
	
	static {
		KEY_MAP.put("chainId", "string");
		KEY_MAP.put("entityNodeRef", "string");
		KEY_MAP.put("entityName", "string");
	}
	
	@Override
	public boolean applyTo(DatabaseAuditType type) {
		return DatabaseAuditType.FORMULATION.equals(type);
	}

	@Override
	protected String getAuditApplicationId() {
		return FORMULATION_AUDIT_ID;
	}

	@Override
	protected String getAuditApplicationPath() {
		return FORMULATION;
	}

}