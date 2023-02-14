package fr.becpg.repo.audit.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.Audit;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditService;
import fr.becpg.repo.audit.service.StopWatchAuditService;
import fr.becpg.repo.audit.service.TracerAuditService;

@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService {

	private static final String NOT_DATABASE_PLUGIN = "Audit plugin for type '%s' is not a database plugin";
	
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
	
	private ThreadLocal<AuditScope> threadLocalScope = new ThreadLocal<>();
	
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(threadLocalScope, plugin, databaseAuditService, stopWatchAuditService, tracerAuditService, plugin.getAuditClass(), plugin.getAuditClass().getSimpleName()).start();
		
	}
	
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType, Class<?> auditClass, String scopeName) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(threadLocalScope, plugin, databaseAuditService, stopWatchAuditService, tracerAuditService, auditClass, scopeName).start();
	}

	@Override
	public List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditFilter) {
		
		AuditPlugin plugin = getPlugin(type);
		
		if (plugin.isDatabaseEnable()) {
			return databaseAuditService.listAuditEntries((DatabaseAuditPlugin) plugin, auditFilter);
		}
		
		throw new BeCPGAuditException(String.format(NOT_DATABASE_PLUGIN, type));
		
	}

	@Override
	public void deleteAuditEntries(AuditType type, Long fromId, Long toId) {
		AuditPlugin plugin = getPlugin(type);
		
		if (plugin.isDatabaseEnable()) {
			databaseAuditService.deleteAuditEntries((DatabaseAuditPlugin) plugin, fromId, toId);
		} else {
			throw new BeCPGAuditException(String.format(NOT_DATABASE_PLUGIN, type));
		}
	}
	
	@Override
	public void updateAuditEntry(AuditType type, Long id, Long time, Map<String, Serializable> values) {
		AuditPlugin plugin = getPlugin(type);
		
		if (plugin.isDatabaseEnable()) {
			databaseAuditService.updateAuditEntry((DatabaseAuditPlugin) plugin, id, time, values);
		} else {
			throw new BeCPGAuditException(String.format(NOT_DATABASE_PLUGIN, type));
		}
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
	public void putAttribute(String string, Object attribute) {
		if (threadLocalScope.get() != null) {
			threadLocalScope.get().putAttribute(string, attribute);
		}
	}

	@Override
	public void addAnnotation(String annotation) {
		if (threadLocalScope.get() != null) {
			threadLocalScope.get().addAnnotation(annotation);
		}
	}
	
	@Override
	public void addAnnotation(String description, Map<String, String> attributes) {
		if (threadLocalScope.get() != null) {
			threadLocalScope.get().addAnnotation(description, attributes);
		}
	}

}
