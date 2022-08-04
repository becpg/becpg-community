package fr.becpg.repo.audit.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.AuditModelVisitor;
import fr.becpg.repo.audit.BeCPGAuditService;
import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditModel;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;

@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService {

	@Autowired
	private AuditPlugin[] auditPlugins;
	
	@Autowired
	private AuditModelVisitor auditModelVisitor;

	@Override
	public void recordAuditEntry(AuditType type, Map<String, Serializable> auditValues) {
		getPlugin(type).recordAuditEntry(auditValues);
	}
	
	@Override
	public List<JSONObject> buildAuditStatistics(AuditType type, Integer maxResults, String sortBy, String filter) {
		return getPlugin(type).buildAuditStatistics(maxResults, sortBy, filter);
	}

	private AuditPlugin getPlugin(AuditType type) {
		for (AuditPlugin auditPlugin : auditPlugins) {
			if (auditPlugin.applyTo(type)) {
				return auditPlugin;
			}
		}
		
		throw new BeCPGAuditException("Audit plugin for type '" + type + "' is not implemented yet");
	}

	@Override
	public void recordAuditEntry(AuditType type, AuditModel auditModel) {
		recordAuditEntry(type, auditModel.accept(auditModelVisitor));
	}
	
}
