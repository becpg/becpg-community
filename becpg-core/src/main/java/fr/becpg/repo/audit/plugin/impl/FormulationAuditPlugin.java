package fr.becpg.repo.audit.plugin.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AbstractAuditPlugin;
import fr.becpg.repo.audit.plugin.visitor.AuditModelVisitor;

@Service
public class FormulationAuditPlugin extends AbstractAuditPlugin {

	private static final String FORMULATION = "formulation";

	private static final String FORMULATION_AUDIT_ID = "beCPGFormulationAudit";
	
	private static final Map<String, String> FORMULATION_KEY_MAP = new HashMap<>();
	
	static {
		FORMULATION_KEY_MAP.put("entityNodeRef", "string");
		FORMULATION_KEY_MAP.put("entityName", "string");
		FORMULATION_KEY_MAP.put("duration", "int");
		FORMULATION_KEY_MAP.put("startedAt", "date");
		FORMULATION_KEY_MAP.put("completedAt", "date");
	}
	
	protected FormulationAuditPlugin(AuditModelVisitor auditModelVisitor) {
		super(auditModelVisitor);
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
	public boolean applyTo(AuditType type) {
		return AuditType.FORMULATION.equals(type);
	}
	
	@Override
	public Map<String, String> getStatisticsKeyMap() {
		return FORMULATION_KEY_MAP;
	}

	@Override
	protected int createHashCode(Map<String, Serializable> auditValues) {
		return Objects.hash(auditValues.get("formulation/startedAt"), auditValues.get("formulation/entityNodeRef"), auditValues.get("formulation/chainId"));
	}

}