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
package fr.becpg.olap;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

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
public class RemoteETLClient {
	private static final Log logger = LogFactory.getLog(RemoteETLClient.class);
	
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
			jdbcConnectionManager.doInTransaction( new JdbcConnectionManagerCallBack() {

				@Override
				public void execute(Connection connection) throws Exception {
					
					instanceManager.createBatch(connection,instance);
		
					InstanceImporter remoteETLClient = new InstanceImporter(instance.getInstanceUrl());
				
					remoteETLClient.setEntityToDBXmlVisitor(new EntityToDBXmlVisitor(connection, instance));
					try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
						remoteETLClient.loadEntities(remoteETLClient.buildQuery(instance.getLastImport()), httpClient, instance.createHttpContext());
					} 
					
					instanceManager.updateBatchAndDate(connection,instance);
				}
			});

		}

	}


}
