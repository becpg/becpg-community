package fr.becpg.repo.audit.service.impl;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.AuditScopeListener;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditService;

@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService, AuditScopeListener {

	private static final String NOT_DATABASE_PLUGIN = "Audit plugin for type '%s' is not a database plugin";
	
	@Autowired
	private AuditPlugin[] auditPlugins;
	
	@Autowired
	private DatabaseAuditService databaseAuditService;
	
	private ThreadLocal<AuditScope> threadLocalScope = new ThreadLocal<>();
	
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(plugin, databaseAuditService, this, plugin.getAuditedClass(), plugin.getClass().getSimpleName()).start();
	}
	
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType, Class<?> auditClass, String scopeName) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(plugin, databaseAuditService, this, auditClass, scopeName).start();
	}

	@Override
	public List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditQuery) {
		
		AuditPlugin plugin = getPlugin(type);
		
		if (plugin.isDatabaseEnable()) {
			return databaseAuditService.listAuditEntries((DatabaseAuditPlugin) plugin, auditQuery);
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
	
	private AuditPlugin getPlugin(AuditType type) {
		for (AuditPlugin auditPlugin : auditPlugins) {
			if (auditPlugin.applyTo(type)) {
				return auditPlugin;
			}
		}
		
		throw new BeCPGAuditException("Audit plugin for type '" + type + "' is not implemented yet");
	}
	
	@Override
	public void onStart(AuditScope auditScope) {
		auditScope.setParentScope(threadLocalScope.get());
		threadLocalScope.set(auditScope);
	}

	@Override
	public void onClose(AuditScope auditScope) {
		threadLocalScope.remove();
		threadLocalScope.set(auditScope.getParentScope());
	}

}
