package fr.becpg.repo.audit.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditFilter;
import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.AuditScopeListener;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.audit.service.DatabaseAuditService;

/**
 * <p>BeCPGAuditServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("beCPGAuditService")
public class BeCPGAuditServiceImpl implements BeCPGAuditService, AuditScopeListener {

	/** Constant <code>NOT_DATABASE_PLUGIN="Audit plugin for type '%s' is not a dat"{trunked}</code> */
	private static final String NOT_DATABASE_PLUGIN = "Audit plugin for type '%s' is not a database plugin";
	
	@Autowired
	private AuditPlugin[] auditPlugins;
	
	@Autowired
	private DatabaseAuditService databaseAuditService;
	
	private ThreadLocal<AuditScope> threadLocalScope = new ThreadLocal<>();
	
	/** {@inheritDoc} */
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(plugin, databaseAuditService, this, plugin.getAuditedClass(), plugin.getClass().getSimpleName()).start();
	}
	
	/** {@inheritDoc} */
	@SuppressWarnings("resource")
	@Override
	public AuditScope startAudit(AuditType auditType, Class<?> auditClass, String scopeName) {
		
		AuditPlugin plugin = getPlugin(auditType);
		
		return new AuditScope(plugin, databaseAuditService, this, auditClass, scopeName).start();
	}

	/**
	 * {@inheritDoc}
	 *
	 * The filters are first ordered on the filter hierarchy of the plugin.
	 */
	@Override
	public List<JSONObject> listAuditEntries(AuditType type, AuditQuery auditQuery) {
		return AuthenticationUtil.runAsSystem(() -> {
			DatabaseAuditPlugin plugin = getDatabasePlugin(type);
			return databaseAuditService.listAuditEntries(plugin, prioritizeFilters(plugin, auditQuery));
		});
	}

	/**
	 * {@inheritDoc}
	 *
	 * The filters are first ordered on the filter hierarchy of the plugin.
	 */
	@Override
	public AuditPage listAuditPage(AuditType type, AuditQuery auditQuery) {
		return AuthenticationUtil.runAsSystem(() -> {
			DatabaseAuditPlugin plugin = getDatabasePlugin(type);
			return databaseAuditService.listAuditPage(plugin, prioritizeFilters(plugin, auditQuery));
		});
	}

	/**
	 * A copy of the query whose filters are ordered on the filter hierarchy of the plugin, so that
	 * the database reads with the most selective one and the other ones are applied in memory.
	 *
	 * The identifier matches a single entry: it comes first, unless the plugin places it in its
	 * hierarchy. The filters on a key missing from the hierarchy keep their order, after the other
	 * ones.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @throws fr.becpg.repo.audit.exception.BeCPGAuditException if a filter has a wrong syntax
	 */
	private AuditQuery prioritizeFilters(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		List<String> filters = new ArrayList<>(auditQuery.getFilters());
		if (filters.size() < 2) {
			return auditQuery;
		}
		List<String> hierarchy = plugin.getFilterHierarchy();
		filters.sort(Comparator.comparingInt(filter -> filterRank(hierarchy, AuditFilter.parse(filter).key())));
		return auditQuery.copy().filters(filters);
	}

	private int filterRank(List<String> hierarchy, String filterKey) {
		int rank = hierarchy.indexOf(filterKey);
		if (rank >= 0) {
			return rank;
		}
		return AuditPlugin.ID.equals(filterKey) ? -1 : Integer.MAX_VALUE;
	}

	/**
	 * <p>getDatabasePlugin.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @return a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @throws fr.becpg.repo.audit.exception.BeCPGAuditException if the plugin of the type does not record in the database
	 */
	private DatabaseAuditPlugin getDatabasePlugin(AuditType type) {
		AuditPlugin plugin = getPlugin(type);
		if (plugin.isDatabaseEnable() && (plugin instanceof DatabaseAuditPlugin databasePlugin)) {
			return databasePlugin;
		}
		throw new BeCPGAuditException(String.format(NOT_DATABASE_PLUGIN, type));
	}

	/** {@inheritDoc} */
	@Override
	public void completeAuditEntry(AuditType auditType, String filterKey, String filterValue) {
		AuthenticationUtil.runAsSystem(() -> {
			AuditPlugin plugin = getPlugin(auditType);
			if (plugin.isDatabaseEnable()) {
				databaseAuditService.completeAuditEntry((DatabaseAuditPlugin) plugin, filterKey, filterValue);
			}
			return null;
		});
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAuditEntries(AuditType type, Long fromId, Long toId) {
		AuditPlugin plugin = getPlugin(type);
		
		if (plugin.isDatabaseEnable()) {
			databaseAuditService.deleteAuditEntries((DatabaseAuditPlugin) plugin, fromId, toId);
		} else {
			throw new BeCPGAuditException(String.format(NOT_DATABASE_PLUGIN, type));
		}
	}
	
	/**
	 * <p>getPlugin.</p>
	 *
	 * @param type a {@link fr.becpg.repo.audit.model.AuditType} object
	 * @return a {@link fr.becpg.repo.audit.plugin.AuditPlugin} object
	 */
	private AuditPlugin getPlugin(AuditType type) {
		for (AuditPlugin auditPlugin : auditPlugins) {
			if (auditPlugin.applyTo(type)) {
				return auditPlugin;
			}
		}
		
		throw new BeCPGAuditException("Audit plugin for type '" + type + "' is not implemented yet");
	}
	
	/** {@inheritDoc} */
	@Override
	public void onStart(AuditScope auditScope) {
		auditScope.setParentScope(threadLocalScope.get());
		threadLocalScope.set(auditScope);
	}

	/** {@inheritDoc} */
	@Override
	public void onClose(AuditScope auditScope) {
		threadLocalScope.remove();
		threadLocalScope.set(auditScope.getParentScope());
	}

}
