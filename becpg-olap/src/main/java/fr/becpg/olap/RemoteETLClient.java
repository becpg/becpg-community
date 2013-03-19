package fr.becpg.olap;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.extractor.EntityToDBXmlVisitor;
import fr.becpg.olap.jdbc.JdbcConnectionManager;
import fr.becpg.olap.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;

/**
 * 
 * @author matthieu
 * 
 */
public class RemoteETLClient {
	private static Log logger = LogFactory.getLog(RemoteETLClient.class);
	
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		options.addOption("file", true, "Load properties file");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);
		Properties props = new Properties();
		if (cmd.hasOption("file")) {
			props.load(new FileInputStream(cmd.getOptionValue("file")));
		} else {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("jdbc.properties"));
		}

		final JdbcConnectionManager jdbcConnectionManager = new JdbcConnectionManager((String) props.get("jdbc.user"), (String) props.get("jdbc.password"),
				(String) props.get("jdbc.url"));
		final InstanceManager instanceManager = new InstanceManager();
		instanceManager.setJdbcConnectionManager(jdbcConnectionManager);
		for (final Instance instance : instanceManager.getAllInstances()) {
			logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/" + instance.getTenantName());
			if (logger.isDebugEnabled()) {
				logger.debug(" - Login: " + instance.getTenantUser());
				logger.debug(" - Password: " + instance.getTenantPassword());
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

	}


}
