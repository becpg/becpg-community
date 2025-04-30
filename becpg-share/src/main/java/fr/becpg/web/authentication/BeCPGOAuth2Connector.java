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

import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;

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
