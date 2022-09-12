package fr.becpg.repo.audit.service.impl;

import java.util.List;

import org.alfresco.rest.api.Audit;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.StopWatchAuditService;
import fr.becpg.repo.audit.service.TracerAuditService;

@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService {

	@Autowired
	private AuditPlugin[] auditPlugins;
	
	@Autowired
	private DatabaseAuditService databaseAuditService;
	
	@Autowired(required = false)
	private StopWatchAuditService stopWatchAuditService;
	
	@Autowired(required = false)
	private TracerAuditService tracerAuditService;
	
	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(plugin, databaseAuditService, stopWatchAuditService, tracerAuditService).start();
	}

	@Override
	public List<JSONObject> getAuditStatistics(AuditType type, Integer maxResults, String sortBy, String filter) {
		return databaseAuditService.getAuditStatistics(getPlugin(type), maxResults, sortBy, filter);
		
	}

	private AuditPlugin getPlugin(AuditType type) {
		for (AuditPlugin auditPlugin : auditPlugins) {
			if (auditPlugin.applyTo(type)) {
				return auditPlugin;
			}
		}
		
		throw new BeCPGAuditException("Audit plugin for type '" + type + "' is not implemented yet");
	}
	
}
