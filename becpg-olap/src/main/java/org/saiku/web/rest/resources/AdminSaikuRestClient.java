package org.saiku.web.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;

import fr.becpg.olap.InstanceImporter;
import fr.becpg.olap.InstanceManager;
import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.jdbc.JdbcConnectionManager;
import fr.becpg.olap.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;

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
			if(logger.isInfoEnabled()){
				logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/" + instance.getTenantName());
			}
			JdbcConnectionManager.doInTransaction(jdbcConnectionManager, new JdbcConnectionManagerCallBack() {

				@Override
				public void execute(JdbcConnectionManager jdbcConnectionManager) throws Exception {
	

					instanceManager.createBatch(instance);

					InstanceImporter remoteETLClient = new InstanceImporter(instance.getInstanceUrl());

					remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(jdbcConnectionManager, instance));
					HttpClient httpClient = instanceManager.createInstanceSession(instance);
					try {
						remoteETLClient.loadEntities(remoteETLClient.buildQuery(instance.getLastImport()), httpClient);
					} finally {
						httpClient.getConnectionManager().shutdown();

					}
					
					instanceManager.updateBatchAndDate(instance);
				}
			});

		}
	
		return Response.ok().build();
	}


}
