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
import fr.becpg.tools.jdbc.JdbcUtils;

/**
 * Warning Not scalable at ALL
 * @author matthieu
 *
 */
public class InstanceCleaner {

	private static Log logger = LogFactory.getLog(InstanceCleaner.class);

	public void purgeEntities(Connection connection, Instance instance) throws Exception {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpContext httpContext = instance.createHttpContext();
			for (final String entityId : getAllEntities(connection, instance)) {
				if (!exists(entityId, instance, httpClient, httpContext)) {
					logger.info("Deleting entity: " + entityId + " for instance: " + instance.getInstanceName());
					JdbcUtils.update(connection, "DELETE FROM `becpg_entity` where instance_id = ? AND entity_id = ?", new Object[] { instance.getId(), entityId });
				}
			}
		}

	}
	
	public void purgeEntityHistoric(Connection connection, Instance instance) throws SQLException{
		JdbcUtils.update(connection, "DELETE FROM `becpg_entity` where instance_id = ? AND is_last_version = ?", new Object[] { instance.getId(), false });
	}
	
	public void purgeDataListHistoric(Connection connection, Instance instance) throws SQLException{
		JdbcUtils.update(connection, "DELETE FROM `becpg_datalist` where instance_id = ? AND is_last_version = ?", new Object[] { instance.getId(), false });
	}

	public void purgeStatistics(Connection connection, Instance instance, Date date) throws SQLException{
		JdbcUtils.update(connection, "DELETE FROM `becpg_statistics` where instance_id = ? AND statistics_date < ?", new Object[] { instance.getId(), new java.sql.Date(date.getTime()) });
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

	public List<String> getAllEntities(Connection connection, Instance instance) throws SQLException {
		return JdbcUtils.list(connection, "SELECT `entity_id`  FROM `becpg_entity` WHERE instance_id = ? GROUP BY `entity_id`", new JdbcUtils.RowMapper<String>() {
			public String mapRow(ResultSet rs, int line) throws SQLException {
				return rs.getString(1);
			}
		}, new Object[] { instance.getId() });
	}

}
