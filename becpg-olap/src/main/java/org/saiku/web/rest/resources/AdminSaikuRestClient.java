/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.saiku.web.rest.resources;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import fr.becpg.olap.InstanceCleaner;
import fr.becpg.olap.InstanceImporter;
import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.jdbc.JdbcConnectionManager;
import fr.becpg.tools.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;

/**
 *
 * @author matthieu
 *
 */
@Component
@Path("/saiku/becpg/admin")
@XmlAccessorType(XmlAccessType.NONE)
public class AdminSaikuRestClient {

	private static final Log logger = LogFactory.getLog(AdminSaikuRestClient.class);

	InstanceManager instanceManager;

	JdbcConnectionManager jdbcConnectionManager;

	public void setJdbcConnectionManager(JdbcConnectionManager jdbcConnectionManager) {
		this.jdbcConnectionManager = jdbcConnectionManager;
	}

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	@GET
	@Produces({ "text/plain" })
	@Path("/import")
	public Response launchImport() throws Exception {
		for (final Instance instance : instanceManager.getAllInstances()) {
			if (logger.isInfoEnabled()) {
				logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/"
						+ instance.getTenantName());
			}
			jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
				@Override
				public void execute(Connection connection) throws Exception {

					instanceManager.createBatch(connection, instance);

					InstanceImporter remoteETLClient = new InstanceImporter(instance.getInstanceUrl());

					remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(connection, instance));

					try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

						remoteETLClient.loadEntities(remoteETLClient.buildQuery(instance.getLastImport()), httpClient, instance.createHttpContext());
					}

					instanceManager.updateBatchAndDate(connection, instance);

				}
			});

		}

		if (logger.isInfoEnabled()) {
			logger.info("Running post feed All procs");
		}
		jdbcConnectionManager.doInTransaction(new JdbcConnectionManagerCallBack() {
			@Override
			public void execute(Connection connection) throws Exception {
				// CALL feed all to recreate tables
				try (Statement statement = connection.createStatement()) {
					statement.executeQuery("CALL feed_all();");
				}

			}
		});

		return Response.ok().build();
	}

	@GET
	@Produces({ "text/plain" })
	@Path("/purge")
	public Response launchPurge() throws Exception {
		for (final Instance instance : instanceManager.getAllInstances()) {
			if (logger.isInfoEnabled()) {
				logger.info("Start purging nodes from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/"
						+ instance.getTenantName());
			}

			InstanceCleaner instanceCleaner = new InstanceCleaner(jdbcConnectionManager);

			instanceCleaner.purgeEntities(instance);

		}

		return Response.ok().build();
	}

	@GET
	@Produces({ "text/plain" })
	@Path("/purge/historic/datalists")
	public Response purgeDataListHistoric() throws Exception {
		for (final Instance instance : instanceManager.getAllInstances()) {
			if (logger.isInfoEnabled()) {
				logger.info("Start purging datalists from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/"
						+ instance.getTenantName());
			}
			InstanceCleaner instanceCleaner = new InstanceCleaner(jdbcConnectionManager);

			instanceCleaner.purgeDataListHistoric(instance);

		}
		return Response.ok().build();
	}

	@GET
	@Produces({ "text/plain" })
	@Path("/purge/historic/entities")
	public Response purgeEntityHistoric() throws Exception {
		for (final Instance instance : instanceManager.getAllInstances()) {
			if (logger.isInfoEnabled()) {
				logger.info("Start purging entities historic from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/"
						+ instance.getTenantName());
			}
			InstanceCleaner instanceCleaner = new InstanceCleaner(jdbcConnectionManager);

			instanceCleaner.purgeEntityHistoric(instance);

		}
		return Response.ok().build();
	}

	@GET
	@Produces({ "text/plain" })
	@Path("/purge/statistics")
	public Response purgeStatistics() throws Exception {
		for (final Instance instance : instanceManager.getAllInstances()) {
			if (logger.isInfoEnabled()) {
				logger.info("Start purging statistics from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/"
						+ instance.getTenantName());
			}
			InstanceCleaner instanceCleaner = new InstanceCleaner(jdbcConnectionManager);

			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -6);

			instanceCleaner.purgeStatistics(instance, cal.getTime());

		}
		return Response.ok().build();
	}
}
