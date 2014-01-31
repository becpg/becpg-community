/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.monitors;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import fr.becpg.monitors.extractor.StatisticsVisitor;
import fr.becpg.monitors.http.GetStaticticsCommand;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.InstanceManager.InstanceState;
import fr.becpg.tools.jdbc.JdbcConnectionManager;
import fr.becpg.tools.jdbc.JdbcConnectionManager.JdbcConnectionManagerCallBack;

/**
 * 
 * @author matthieu
 * 
 */
public class MonitorsClient {
	private static Log logger = LogFactory.getLog(MonitorsClient.class);
	
	
	JdbcConnectionManager jdbcConnectionManager;
	
	InstanceManager instanceManager;
	
	Properties properties;
	
	List<Instance> instancesToNotify = new ArrayList<>();
	
	
	public MonitorsClient(Properties properties) {
		super();
		this.properties = properties;
		
		jdbcConnectionManager = new JdbcConnectionManager((String) properties.get("jdbc.user"), (String) properties.get("jdbc.password"),
				(String) properties.get("jdbc.url"));
		
		instanceManager  = new InstanceManager();
		
		instanceManager.setJdbcConnectionManager(jdbcConnectionManager);
	}




	public void setJdbcConnectionManager(JdbcConnectionManager jdbcConnectionManager) {
		this.jdbcConnectionManager = jdbcConnectionManager;
	}




	public static void main(String[] args) throws Exception {

		Options options = new Options();

		options.addOption("file", true, "Load properties file");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);
		Properties props = new Properties();
		if (cmd.hasOption("file")) {
			props.load(new FileInputStream(cmd.getOptionValue("file")));
		} else {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("monitors.properties"));
		}

		
		MonitorsClient monitorsClient = new MonitorsClient(props);
		
	
		
		monitorsClient.run();
		
		

	}




	/**
	 * @throws Exception 
	 * 
	 */
	private void run() throws Exception {
	
		for (final Instance instance : instanceManager.getAllInstances()) {
			logger.info("Start importing from instance/tenant: " + instance.getId() + "/" + instance.getInstanceName() + "/" + instance.getTenantName());
			if (logger.isDebugEnabled()) {
				logger.debug(" - Login: " + instance.getTenantUser());
				logger.debug(" - Password: " + instance.getTenantPassword());
			}
			jdbcConnectionManager.doInTransaction( new JdbcConnectionManagerCallBack() {

				@Override
				public void execute(Connection connection) throws Exception {
					
					
					
					StatisticsVisitor visitor = new StatisticsVisitor(connection, instance);
					
					HttpClient httpClient = instanceManager.createInstanceSession(instance);
					try {
						GetStaticticsCommand getStaticticsCommand = new GetStaticticsCommand(instance.getInstanceUrl());

						try (InputStream in = getStaticticsCommand.runCommand(httpClient)) {
							visitor.visit(in);
						}
						if(!InstanceState.UP.equals(instance.getInstanceState())) {
							instance.setInstanceState(InstanceState.UP);
							instanceManager.updateInstanceState(connection, instance);
							instancesToNotify.add(instance);
						}
						
					} catch(Exception e) {
						if(!InstanceState.DOWN.equals(instance.getInstanceState())) {
							logger.error("Cannot retrive statistics for instance :"+instance.toString(),e);
							instance.setInstanceState(InstanceState.DOWN);
							instanceManager.updateInstanceState(connection, instance);
							instancesToNotify.add(instance);
						
						} else {
							logger.info("Instance still DOWN: "+instance.toString());
							logger.debug(e,e);
						}
					} finally {
						
						
						
						httpClient.getConnectionManager().shutdown();

					}
				}
			});

		}
		
		
		if(instancesToNotify.size()>0) {
			sendMail();
		}
		
	}




	/**
	 * @param instancesToNotify
	 */
	private void sendMail() {
	        Session session = Session.getDefaultInstance(properties, null);

	        try {
	        
		        StringBuilder message = new StringBuilder();
		        message.append("<html><head>");
		        
		         message.append("</head><body><h1>Status of instances has changed</h1><br/>");
		        
		        message.append("<div class=\"datagrid\" style=\"font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden; border: 1px solid #8C8C8C; -webkit-border-radius: 3px; -moz-border-radius: 3px; border-radius: 3px; \"><table style=\"border-collapse: collapse; text-align: left; width: 100%;\">");
		        message.append("<thead><tr style=\"background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #8C8C8C), color-stop(1, #7D7D7D) );background:-moz-linear-gradient( center top, #8C8C8C 5%, #7D7D7D 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#8C8C8C', endColorstr='#7D7D7D');background-color:#8C8C8C; color:#FFFFFF; font-size: 15px; font-weight: bold; border-left: 1px solid #A3A3A3;\"><th>Instance Name</th><th>Instance URL</th><th>New State</th></tr></thead><tbody>");
		        int i=0;
		        for(Instance instance : instancesToNotify) {
		        	message.append("<td"+(i%2==0?" class=\"alt\"":"")+"><b>");
		        	 message.append(instance.getInstanceName());
		        	 message.append("</b></td>");
		        	 message.append("<td><a href=\"");
		        	 message.append(instance.getInstanceUrl());
		        	 message.append("\">");
		        	 message.append(instance.getInstanceUrl());
		        	 message.append("</a></td>");
		        	 message.append("<td>");
		        	 message.append("<span><b><font color=\""+(InstanceState.DOWN.equals(instance.getInstanceState())?"red":"green")+"\">");
		        	 message.append(instance.getInstanceState());
		        	 message.append("</font>");
		        	 message.append("</b></span></td></tr>");
		        	 i++;
		        }
		        message.append("</tbody><tfoot></tfoot></table></div></body></html>");


		        MimeMessage msg = new MimeMessage(session);
	            msg.setFrom(new InternetAddress(properties.getProperty("monitors.notify.from")));
	            msg.addRecipient(Message.RecipientType.TO,
	            		    new InternetAddress(properties.getProperty("monitors.notify.to")));
	            msg.setSubject("becpg-monitors ["+InetAddress.getLocalHost().getHostName()+"]");
	            msg.setText(message.toString(), "utf-8", "html");
	            Transport.send(msg);

	        } catch (Exception e) {
	        	logger.error(e);
	        }
		
	}

	
	
	

}
