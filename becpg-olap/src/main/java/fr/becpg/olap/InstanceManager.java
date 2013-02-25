package fr.becpg.olap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import fr.becpg.olap.jdbc.JdbcConnectionManager;

/**
 * 
 * @author matthieu
 *
 */
public class InstanceManager {

	JdbcConnectionManager jdbcConnectionManager;

	public class Instance {
		private Long id;
		private Long batchId;
		private String tenantUser;
		private String tenantPassword;
		private String tenantName;
		private String instanceName;
		private String instanceUrl;
		private Date lastImport;

		public Instance(Long id,Long batchId ,  String tenantUser, String tenantPassword, String tenantName, String instanceName, String instanceUrl, Date lastImport) {
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

	public InstanceManager(JdbcConnectionManager jdbcConnectionManager) {
		this.jdbcConnectionManager =  jdbcConnectionManager;
	}

	List<Instance> getAllInstances() throws SQLException {
		return jdbcConnectionManager.list("SELECT `id`,`batch_id` ,`tenant_username`,`tenant_password`,`tenant_name`,`instance_name`,`instance_url`,`last_imported`  FROM `becpg_instance`",
				new JdbcConnectionManager.RowMapper<Instance>() {
					public Instance mapRow(ResultSet rs, int line) throws SQLException {
						return new Instance(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getTimestamp(8));

					}
				});

	}

	public void createBatch(Instance instance) throws SQLException {
		
	
		
		final Long batchId  = jdbcConnectionManager.update("INSERT INTO `becpg_batch`(`id`) VALUES(NULL)", new Object[]{});
		
		instance.setLastImport(new Date());
		instance.setBatchId(batchId);
		
		jdbcConnectionManager.update("UPDATE `becpg_instance` SET `last_imported`=?, `batch_id`=? WHERE `id`=? ", new Object[] {instance.getLastImport() , instance.getBatchId() ,instance.getId() });
		
	}

	
	

}
