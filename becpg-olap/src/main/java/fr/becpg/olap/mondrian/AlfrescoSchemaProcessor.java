/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.olap.mondrian;

import mondrian.olap.Util.PropertyList;
import mondrian.spi.DynamicSchemaProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.becpg.olap.authentication.AlfrescoUserDetails;
import fr.becpg.olap.http.GetMondrianSchemaCommand;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;

public class AlfrescoSchemaProcessor implements DynamicSchemaProcessor {

	private static final Log logger = LogFactory.getLog(AlfrescoSchemaProcessor.class);

	InstanceManager instanceManager;

	@Override
	public String processSchema(String schemaUrl, PropertyList connectInfo) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieve mondrian schema for: " + schemaUrl);
			logger.debug("Connection info: " + connectInfo);
		}

		// retrieve instance manager from webapp ctx
		if (instanceManager == null) {
			instanceManager = MondrianApplicationContextProvider.getApplicationContext().getBean("instanceManager", InstanceManager.class);
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getPrincipal() != null && (auth.getPrincipal() instanceof AlfrescoUserDetails)) {
			Instance instance = ((AlfrescoUserDetails) auth.getPrincipal()).getInstance();
			try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
				HttpClientContext httpContext = HttpClientContext.create();

				String ticket = ((AlfrescoUserDetails) auth).getPassword();
				
				if(ticket==null || ticket.isEmpty()){
					UsernamePasswordCredentials creds = new UsernamePasswordCredentials(instance.getTenantUser(), instance.getTenantPassword());
					CredentialsProvider credsProvider = new BasicCredentialsProvider();
					credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
					httpContext.setCredentialsProvider(credsProvider);
				}
				GetMondrianSchemaCommand schemaCommand = new GetMondrianSchemaCommand(instance.getInstanceUrl());

				return schemaCommand.getSchema(httpClient, httpContext, instance.getId(), ticket);
			}

		}

		return "<Schema name=\"beCPG OLAP Schema\"></Schema>";

	}

}
