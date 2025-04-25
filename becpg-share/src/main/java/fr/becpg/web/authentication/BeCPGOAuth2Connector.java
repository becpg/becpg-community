/*******************************************************************************
 * Copyright (C) 2010-2025 beCPG. 
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

package fr.becpg.web.authentication;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.web.site.servlet.MTAuthenticationFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.webscripts.RequestCachingConnector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.RemoteClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import fr.becpg.web.authentication.identity.IdentityServiceFacade;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extends the {@link RequestCachingConnector} to implement OAuth2 authentication for the connection
 * from Share to the Alfresco Repository. This connector handles OAuth2 token acquisition and passes
 * the token as Authorization header to the repository.
 * <p>
 * The configuration for this connector should be defined in the share-config-custom.xml file:
 * <pre>
 * &lt;connector&gt;
 *   &lt;id&gt;alfrescoOAuth2&lt;/id&gt;
 *   &lt;name&gt;Alfresco OAuth2 Connector&lt;/name&gt;
 *   &lt;description&gt;Connects to an Alfresco instance using OAuth2 authentication&lt;/description&gt;
 *   &lt;class&gt;fr.becpg.web.authentication.BeCPGAuth2Connector&lt;/class&gt;
 *   &lt;resource&gt;alfresco&lt;/resource&gt;
 * &lt;/connector&gt;
 * </pre>
 * 
 * @author BeCPG Team
 */
public class BeCPGOAuth2Connector extends BeCPGExternalConnector {
    
	/**
	 * <p>Constructor for BeCPGExternalConnector.</p>
	 *
	 * @param descriptor a {@link org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor} object.
	 * @param endpoint a {@link java.lang.String} object.
	 */
	public BeCPGOAuth2Connector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

}
