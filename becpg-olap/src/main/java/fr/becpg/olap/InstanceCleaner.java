package fr.becpg.olap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.http.CheckEntityCommand;
import fr.becpg.tools.jdbc.JdbcConnectionManager;
import fr.becpg.tools.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;
import fr.becpg.tools.jdbc.JdbcUtils;

/**
 * Warning Not scalable at ALL
 *
 * @author matthieu
 *
 */
public class InstanceCleaner {

	private static Log logger = LogFactory.getLog(InstanceCleaner.class);

	JdbcConnectionManager jdbcConnectionManager;

	public InstanceCleaner(JdbcConnectionManager jdbcConnectionManager) {
		this.jdbcConnectionManager = jdbcConnectionManager;
	}

	public void purgeEntities(final Instance instance) throws Exception {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpContext httpContext = instance.createHttpContext();
			for (final String entityId : getAllEntities(instance)) {
				if (!exists(entityId, instance, httpClient, httpContext)) {
					logger.info("Deleting entity: " + entityId + " for instance: " + instance.getInstanceName());

					jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
						@Override
						public void execute(Connection connection) throws Exception {

						JdbcUtils.update(connection, "DELETE FROM `becpg_entity` where instance_id = ? AND entity_id = ?",
								new Object[] { instance.getId(), entityId });

					}});
				}
			}
		}

	}

	public void purgeEntityHistoric(final Instance instance) throws Exception {

		jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
			@Override
			public void execute(Connection connection) throws Exception {
			JdbcUtils.update(connection, "DELETE FROM `becpg_entity` where instance_id = ? AND is_last_version = ?",
					new Object[] { instance.getId(), false });

		}});
	}

	public void purgeDataListHistoric(final Instance instance) throws Exception {
		jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
			@Override
			public void execute(Connection connection) throws Exception {
			JdbcUtils.update(connection, "DELETE FROM `becpg_datalist` where instance_id = ? AND is_last_version = ?",
					new Object[] { instance.getId(), false });
		}});
	}

	public void purgeStatistics(final Instance instance, final Date date) throws Exception {
		jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
			@Override
			public void execute(Connection connection) throws Exception {
			JdbcUtils.update(connection, "DELETE FROM `becpg_statistics` where instance_id = ? AND statistics_date < ?",
					new Object[] { instance.getId(), new java.sql.Date(date.getTime()) });
		}});
	}

	private boolean exists(String nodeRef, Instance instance, CloseableHttpClient client, HttpContext context) throws IOException {
		CheckEntityCommand checkEntityCommand = new CheckEntityCommand(instance.getInstanceUrl());
		try (CloseableHttpResponse resp = checkEntityCommand.runCommand(client, context, nodeRef)) {
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				if ("KO".equals(EntityUtils.toString(entity, "UTF-8"))) {
					return false;
				}
			}
		}
		return true;
	}

	public List<String> getAllEntities(Instance instance) throws SQLException {
		return jdbcConnectionManager.list("SELECT `entity_id`  FROM `becpg_entity` WHERE instance_id = ? GROUP BY `entity_id`",
				new JdbcUtils.RowMapper<String>() {
						public String mapRow(ResultSet rs, int line) throws SQLException {
							return rs.getString(1);
						}
					}, new Object[] { instance.getId() });
	}

}
