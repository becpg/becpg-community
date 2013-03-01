package fr.becpg.olap;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;

import fr.becpg.olap.helper.UserNameHelper;
import fr.becpg.olap.jdbc.JdbcConnectionManager;

/**
 * 
 * @author matthieu
 * 
 */
public class InstanceManager {

	JdbcConnectionManager jdbcConnectionManager;

	public void setJdbcConnectionManager(JdbcConnectionManager jdbcConnectionManager) {
		this.jdbcConnectionManager = jdbcConnectionManager;
	}
	
	private static Log logger = LogFactory.getLog(InstanceManager.class);

	public class Instance  implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6767708131173643111L;
		private Long id;
		private Long batchId;
		private String tenantUser;
		private String tenantPassword;
		private String tenantName;
		private String instanceName;
		private String instanceUrl;
		private Date lastImport;

		public Instance(Long id, Long batchId, String tenantUser, String tenantPassword, String tenantName, String instanceName, String instanceUrl, Date lastImport) {
			super();
			this.id = id;
			this.batchId = batchId;
			this.tenantUser = tenantUser;
			this.tenantPassword = tenantPassword;
			this.tenantName = tenantName;
			this.instanceName = instanceName;
			this.instanceUrl = instanceUrl;
			this.lastImport = lastImport;
		}

		public Long getId() {
			return id;
		}

		public String getTenantUser() {
			return tenantUser;
		}

		public String getTenantPassword() {
			return tenantPassword;
		}

		public String getTenantName() {
			return tenantName;
		}

		public String getInstanceName() {
			return instanceName;
		}

		public String getInstanceUrl() {
			return instanceUrl;
		}

		public Date getLastImport() {
			return lastImport;
		}

		public Long getBatchId() {
			return batchId;
		}

		public void setBatchId(Long batchId) {
			this.batchId = batchId;
		}

		public void setLastImport(Date lastImport) {
			this.lastImport = lastImport;
		}

	}

	public List<Instance> getAllInstances() throws SQLException {
		return jdbcConnectionManager.list(
				"SELECT `id`,`batch_id` ,`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`,`last_imported`  FROM `becpg_instance`",
				new JdbcConnectionManager.RowMapper<Instance>() {
					public Instance mapRow(ResultSet rs, int line) throws SQLException {
						return new Instance(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8));

					}
				}, new Object[] {});

	}

	public void createBatch(Instance instance) throws SQLException {

		final Long batchId = jdbcConnectionManager.update("INSERT INTO `becpg_batch`(`id`) VALUES(NULL)", new Object[] {});

		instance.setLastImport(new Date());
		instance.setBatchId(batchId);

		jdbcConnectionManager.update("UPDATE `becpg_instance` SET `last_imported`=?, `batch_id`=? WHERE `id`=? ", new Object[] { instance.getLastImport(), instance.getBatchId(),
				instance.getId() });

	}

	public Instance findInstanceByUserName(String username) throws SQLException {
		Matcher ma = UserNameHelper.userNamePattern.matcher(username);
		if (ma.matches()) {
			List<Instance> instances = jdbcConnectionManager
					.list("SELECT `id`,`batch_id` ,`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`,`last_imported`  FROM `becpg_instance` WHERE instance_name = ? and tenant_name = ?",
							new JdbcConnectionManager.RowMapper<Instance>() {
								public Instance mapRow(ResultSet rs, int line) throws SQLException {
									return new Instance(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs
											.getTimestamp(8));

								}
							}, new Object[] { ma.group(1), ma.group(3) });
			if (instances != null && instances.size() == 1) {
				return instances.get(0);
			}
			throw new IllegalStateException("Instance/Tenant not found : (" + ma.group(1) + "," + ma.group(3) + ")");
		}
		throw new IllegalStateException("Username : " + username + " doesn't match pattern");
	}

	public HttpClient createInstanceSession(Instance instance) {

		DefaultHttpClient httpclient = new DefaultHttpClient();

		if (instance.getTenantUser() != null && instance.getTenantUser() != null) {
			if(logger.isDebugEnabled()){
				logger.debug("Try to login with :"+instance.getTenantUser());
			}
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(instance.getTenantUser(), instance.getTenantPassword());
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
			httpclient.setCredentialsProvider(credsProvider);
			
			
		}

		return httpclient;
	}

}
