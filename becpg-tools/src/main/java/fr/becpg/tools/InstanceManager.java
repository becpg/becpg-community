/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.tools;

import java.io.Serializable;
import java.sql.Connection;
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

import fr.becpg.tools.helper.UserNameHelper;
import fr.becpg.tools.jdbc.JdbcConnectionManager;
import fr.becpg.tools.jdbc.JdbcUtils;

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
	
	private static final Log logger = LogFactory.getLog(InstanceManager.class);

	public enum InstanceState {
		UP,DOWN
	}
	
	public class Instance  implements Serializable {
		
		private static final long serialVersionUID = -6767708131173643111L;
		private final Long id;
		private Long batchId;
		private final String tenantUser;
		private final String tenantPassword;
		private final String tenantName;
		private final String instanceName;
		private final String instanceUrl;
		private InstanceState instanceState;
		private Date lastImport;

		public Instance(Long id, Long batchId, String tenantUser, String tenantPassword, String tenantName, String instanceName, String instanceUrl, Date lastImport, InstanceState instanceState) {
			super();
			this.id = id;
			this.batchId = batchId;
			this.tenantUser = tenantUser;
			this.tenantPassword = tenantPassword;
			this.tenantName = tenantName;
			this.instanceName = instanceName;
			this.instanceUrl = instanceUrl;
			this.lastImport = lastImport;
			this.instanceState = instanceState;
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

		
		public InstanceState getInstanceState() {
			return instanceState;
		}

		public void setInstanceState(InstanceState instanceState) {
			this.instanceState = instanceState;
		}

		@Override
		public String toString() {
			return "Instance [id=" + id + ", batchId=" + batchId + ", tenantUser=" + tenantUser + ", tenantPassword=" + tenantPassword + ", tenantName=" + tenantName
					+ ", instanceName=" + instanceName + ", instanceUrl=" + instanceUrl + ", instanceSate=" + instanceState + ", lastImport=" + lastImport + "]";
		}
		
		

	}

	public List<Instance> getAllInstances() throws SQLException {
		return jdbcConnectionManager.list(
				"SELECT `id`,`batch_id` ,`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`,`last_imported`,`instance_state`  FROM `becpg_instance`",
				new JdbcConnectionManager.RowMapper<Instance>() {
					public Instance mapRow(ResultSet rs, int line) throws SQLException {
						return new Instance(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8), InstanceState.valueOf(rs.getString(9)));

					}
				}, new Object[] {});

	}

	public Instance createBatch(Connection connection, Instance instance) throws SQLException {

		final Long batchId = JdbcUtils.update(connection,"INSERT INTO `becpg_batch`(`id`) VALUES(NULL)", new Object[] {});

		
		instance.setBatchId(batchId);

		return instance;

	}
	
	public void updateBatchAndDate(Connection connection,Instance instance) throws SQLException {

		instance.setLastImport(new Date());
		
		JdbcUtils.update(connection,"UPDATE `becpg_instance` SET `last_imported`=?, `batch_id`=? WHERE `id`=? ", new Object[] { instance.getLastImport(), instance.getBatchId(),
				instance.getId() });
	}
	
	public void updateInstanceState(Connection connection,Instance instance) throws SQLException {
		JdbcUtils.update(connection,"UPDATE `becpg_instance` SET `instance_state`=?  WHERE `id`=? ", new Object[] { instance.getInstanceState().toString(),
				instance.getId() });
	}
	

	public Instance findInstanceByUserName(String username) throws SQLException {
		Matcher ma = UserNameHelper.userNamePattern.matcher(username);
		if (ma.matches()) {
			List<Instance> instances = jdbcConnectionManager
					.list("SELECT `id`,`batch_id` ,`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`,`last_imported`,`instance_state`  FROM `becpg_instance` WHERE instance_name = ? and tenant_name = ?",
							new JdbcConnectionManager.RowMapper<Instance>() {
								public Instance mapRow(ResultSet rs, int line) throws SQLException {
									return new Instance(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs
											.getTimestamp(8), InstanceState.valueOf(rs.getString(9)));

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
