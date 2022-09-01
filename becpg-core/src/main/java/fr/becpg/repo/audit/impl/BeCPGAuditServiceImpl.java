package fr.becpg.repo.audit.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.BeCPGAuditService;
import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.DatabaseAuditType;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.StopWatchAuditService;
import fr.becpg.repo.audit.plugin.TracerAuditService;

@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService {

	@Autowired
	private DatabaseAuditPlugin[] databaseAuditPlugins;
	
	@Autowired(required = false)
	private TracerAuditService tracerAuditService;
	
	@Autowired(required = false)
	private StopWatchAuditService stopWatchAuditService;
	
	@Override
	public List<JSONObject> buildAuditStatistics(DatabaseAuditType type, Integer maxResults, String sortBy, String filter) {
		return getPlugin(type).buildAuditStatistics(maxResults, sortBy, filter);
	}

	private DatabaseAuditPlugin getPlugin(DatabaseAuditType type) {
		for (DatabaseAuditPlugin auditPlugin : databaseAuditPlugins) {
			if (auditPlugin.applyTo(type)) {
				return auditPlugin;
			}
		}
		
		throw new BeCPGAuditException("Audit plugin for type '" + type + "' is not implemented yet");
	}

	@Override
	public AuditScopeBuilder createAudit() {
		return new AuditScopeBuilder();
	}

	public class AuditScopeBuilder {
		
		private DatabaseAuditPlugin databaseAuditPlugin;
		
		private TracerAuditService tracerAuditService;
		
		private StopWatchAuditService stopWatchAuditService;
		
		private String tracerScopeName;
		
		private String stopWatchName;
		
		private Map<String, Serializable> auditValues = new HashMap<>();

		private Log logger;
		
		private AuditScopeBuilder() {
		}
		
		public AuditScopeBuilder auditValue(String key, Serializable value) {
			auditValues.put(key, value);
			return this;
		}
		
		public AuditScope startAudit() {
			AuditScope auditScope =  new AuditScope(databaseAuditPlugin, auditValues, tracerAuditService, tracerScopeName, stopWatchAuditService, stopWatchName, logger);
			
			auditScope.open();
			
			return auditScope;
		}
		
		public AuditScopeBuilder withDatabaseRecords(DatabaseAuditType type) {
			this.databaseAuditPlugin = getPlugin(type);
			return this;
		}
		
		public AuditScopeBuilder withTracer(String tracerScopeName) {
			this.tracerAuditService = BeCPGAuditServiceImpl.this.tracerAuditService;
			this.tracerScopeName = tracerScopeName;
			return this;
		}
		
		public AuditScopeBuilder withStopWatch(Log logger, String stopWatchName) {
			this.stopWatchAuditService = BeCPGAuditServiceImpl.this.stopWatchAuditService;
			this.stopWatchName = stopWatchName;
			this.logger = logger;
			return this;
		}
	}

}
