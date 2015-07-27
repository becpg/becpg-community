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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.olap.authentication;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.helper.UserNameHelper;
import fr.becpg.tools.http.RetrieveUserCommand;

/**
 * 
 * @author matthieu
 * 
 */
public class AlfrescoUserDetailsService implements UserDetailsService {

	private static final Log logger = LogFactory.getLog(AlfrescoUserDetailsService.class);

	InstanceManager instanceManager;

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {

		try {
			Instance instance = instanceManager.findInstanceByUserName(username);

			try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

				RetrieveUserCommand retrieveUserCommand = new RetrieveUserCommand(instance.getInstanceUrl());

				String presentedLogin = UserNameHelper.extractLogin(username);

				List<GrantedAuthority> authorities = new ArrayList<>();
				try (CloseableHttpResponse resp = retrieveUserCommand.runCommand(httpClient, instance.createHttpContext(), presentedLogin)) {
					HttpEntity entity = resp.getEntity();
					if (entity != null) {

						try (InputStream in = entity.getContent()) {

							JsonFactory jsonFactory = new JsonFactory();
							JsonParser jp = jsonFactory.createJsonParser(in);

							ObjectMapper mapper = new ObjectMapper();

							JsonNode rootNode = mapper.readTree(jp);

							for (JsonNode node : rootNode.path("groups")) {
								authorities.add(new GrantedAuthorityImpl(node.path("itemName").getTextValue()));
							}

							return new AlfrescoUserDetails(username, "no-password", rootNode.path("enabled").asBoolean(), authorities, instance);

						}
					} else {
						return null;
					}
				}

			}

		} catch (Exception e) {
			logger.error(e, e);
			throw new UsernameNotFoundException(e.getMessage());
		}

	}

}
