/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.activiti.engine.impl.util.json.JSONArray;
import org.alfresco.repo.batch.BatchMonitor;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.google.common.net.HttpHeaders;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>MonitorWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MonitorWebScript extends DeclarativeWebScript {

	private static final String SOLR_STATUS = "solr_status";

	private static final String VOLUMETRY_QUERY = "SELECT "
            + "ns.uri, "
            + "qn.local_name, "
            + "st.protocol, "
            + "st.identifier, "
            + "node_count "
            + "FROM ( "
            + "SELECT "
            + "n.type_qname_id, "
            + "n.store_id, "
            + "COUNT(1) AS node_count "
            + "FROM "
            + "alf_node n "
            + "GROUP BY "
            + "n.type_qname_id, "
            + "n.store_id "
            + ") AS counted_nodes "
            + "JOIN "
            + "alf_qname qn ON counted_nodes.type_qname_id = qn.id "
            + "JOIN "
            + "alf_store st ON counted_nodes.store_id = st.id "
            + "JOIN "
            + "alf_namespace ns ON qn.ns_id = ns.id "
            + "ORDER BY "
            + "node_count DESC;";

	
	private static final double BYTES_TO_MEGA_BYTES = 1048576d;

	private static final Log logger = LogFactory.getLog(MonitorWebScript.class);
	
	private ContentService contentService;
	
	private AbstractAuthenticationService authenticationService;

	private BeCPGLicenseManager licenseManager;

	private TenantAdminService tenantAdminService;

	private String becpgSchema;

	private AuthorityService authorityService;

	private BatchQueueService batchQueueService;
	
	private DataSource dataSource;
	
	private NamespaceService namespaceService;
	
	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	/**
	 * <p>Setter for the field <code>dataSource</code>.</p>
	 *
	 * @param dataSource a {@link javax.sql.DataSource} object
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		
		return AuthenticationUtil.runAsSystem(() -> {
			logger.debug("start monitor webscript");
			
			Map<String, Object> ret = new HashMap<>();
			
			fillMonitoringInformation(ret, true);
			
			if ("beCPG Monitors".equals(req.getHeader(HttpHeaders.USER_AGENT))) {
				ret.put("authenticated", true);
				try {
					List<NodeRef> result = BeCPGQueryBuilder.createQuery().inSite("valid", null).maxResults(1).list();
					if (!result.isEmpty()) {
						ret.put(SOLR_STATUS, "UP");
					} else {
						ret.put(SOLR_STATUS, "DOWN");
					}
				} catch (Exception e) {
					ret.put(SOLR_STATUS, "DOWN");
				}
				
				if ("true".equals(req.getParameter("volumetry"))) {
					fillVolumetry(ret);
				}
			} else {
				ret.clear();
			}
			
			ret.put("status", "SUCCESS");
			
			return ret;
		});
		
	}

	@SuppressWarnings("unchecked")
	private void fillVolumetry(Map<String, Object> ret) {
		JSONArray volumetryArray = new JSONArray();
		try (Connection con = dataSource.getConnection()) {

			try (PreparedStatement statement = con.prepareStatement(VOLUMETRY_QUERY)) {
				try (java.sql.ResultSet res = statement.executeQuery()) {
					while (res.next()) {
						JSONObject volumetryJson = new JSONObject();
						QName qname = QName.createQName("{" + res.getString("uri") + "}" + res.getString("local_name"));
						volumetryJson.put("qname", extractQNameString(qname));
						volumetryJson.put("identifier", res.getString("identifier"));
						volumetryJson.put("protocol", res.getString("protocol"));
						volumetryJson.put("node_count", res.getInt("node_count"));
						volumetryArray.put(volumetryJson);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error running : " + VOLUMETRY_QUERY, e);
			throw new BeCPGException(e.getMessage(), e);
		}
		ret.put("volumetry", volumetryArray.toString());
	}

	private String extractQNameString(QName qname) {
		try {
			return qname.toPrefixString(namespaceService);
		} catch (NamespaceException e) {
			return qname.toString();
		}
	}
	
	/**
	 * <p>fillMonitoringInformation.</p>
	 *
	 * @param ret a {@link java.util.Map} object
	 * @param includeTenantUsers a boolean
	 * @return a {@link java.util.Set} object
	 */
	protected Set<String> fillMonitoringInformation(Map<String, Object> ret, boolean includeTenantUsers) {
		
		long concurrentReadUsers = 0;
		long concurrentSupplierUsers = 0;
		long concurrentWriteUsers = 0;
		long namedReadUsers = 0;
		long namedWriteUsers = 0;
		long withoutLicenseUsers = 0;
	
		Set<String> users = new HashSet<>(authenticationService.getUsersWithTickets(true));
		for (Iterator<String> iterator = users.iterator(); iterator.hasNext();) {
			String user = iterator.next();
			if ((AuthenticationUtil.getGuestUserName().equals(user) || AuthenticationUtil.getSystemUserName().equals(user)
					|| user.contains("connector"))
					|| !includeTenantUsers && (tenantAdminService.isEnabled()
							&& !tenantAdminService.getCurrentUserDomain().equals(tenantAdminService.getUserDomain(user)))
					) {
				iterator.remove();
			}
		}
	
		for (String user : users) {
			if (!AuthenticationUtil.getAdminUserName().equals(user) && !user.endsWith("@becpg.fr")) {
				Set<String> userAuthorities = authorityService.getAuthoritiesForUser(user);
				if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser)
						&& userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseSupplierConcurrent)) {
					concurrentSupplierUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteNamed)) {
					namedWriteUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadNamed)) {
					namedReadUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseWriteConcurrent)) {
					concurrentWriteUsers++;
				} else if (userAuthorities.contains(PermissionService.GROUP_PREFIX + SystemGroup.LicenseReadConcurrent)) {
					concurrentReadUsers++;
				} else {
					withoutLicenseUsers++;
				}
			}
		}
	
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	
		Runtime runtime = Runtime.getRuntime();
	
		ret.put("diskFreeSpace", contentService.getStoreFreeSpace());
		ret.put("diskTotalSpace", contentService.getStoreTotalSpace());
		ret.put("totalMemory", runtime.totalMemory() / BYTES_TO_MEGA_BYTES);
		ret.put("freeMemory", runtime.freeMemory() / BYTES_TO_MEGA_BYTES);
		ret.put("maxMemory", runtime.maxMemory() / BYTES_TO_MEGA_BYTES);
		ret.put("nonHeapMemoryUsage", memoryMXBean.getNonHeapMemoryUsage().getUsed() / BYTES_TO_MEGA_BYTES);
		ret.put("connectedUsers", users.size());
		ret.put("concurrentReadUsers", concurrentReadUsers);
		ret.put("concurrentWriteUsers", concurrentWriteUsers);
		ret.put("concurrentSupplierUsers", concurrentSupplierUsers);
		ret.put("namedReadUsers", namedReadUsers);
		ret.put("namedWriteUsers", namedWriteUsers);
		ret.put("allowedConcurrentRead", licenseManager.getAllowedConcurrentRead());
		ret.put("allowedConcurrentWrite", licenseManager.getAllowedConcurrentWrite());
		ret.put("allowedConcurrentSupplier", licenseManager.getAllowedConcurrentSupplier());
		ret.put("allowedNamedWrite", licenseManager.getAllowedNamedWrite());
		ret.put("allowedNamedRead", licenseManager.getAllowedNamedRead());
		ret.put("licenseName", licenseManager.getLicenseName());
		ret.put("withoutLicenseUsers", withoutLicenseUsers);
		ret.put("becpgSchema", becpgSchema.replace("-SNAPSHOT",""));
		
		BatchMonitor lastRunningBatch = batchQueueService.getLastRunningBatch();
		
		boolean batchInProgress = false;
		
		batchInProgress = lastRunningBatch != null && !lastRunningBatch.getPercentComplete().contains("100");
		
		ret.put("batchCounts", batchQueueService.getBatchesInQueue().size() + (batchInProgress ? 1 : 0));
	
		return users;
		
	}

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.repo.security.authentication.AbstractAuthenticationService} object.
	 */
	public void setAuthenticationService(AbstractAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>tenantAdminService</code>.</p>
	 *
	 * @param tenantAdminService a {@link org.alfresco.repo.tenant.TenantAdminService} object
	 */
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	/**
	 * <p>Setter for the field <code>becpgSchema</code>.</p>
	 *
	 * @param becpgSchema a {@link java.lang.String} object
	 */
	public void setBecpgSchema(String becpgSchema) {
		this.becpgSchema = becpgSchema;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>batchQueueService</code>.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 */
	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	/**
	 * <p>Setter for the field <code>licenseManager</code>.</p>
	 *
	 * @param licenseManager a {@link fr.becpg.repo.license.BeCPGLicenseManager} object
	 */
	public void setLicenseManager(BeCPGLicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

}
