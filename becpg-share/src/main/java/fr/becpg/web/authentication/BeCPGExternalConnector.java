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
import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.webscripts.RequestCachingConnector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.RemoteClient;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extends the {@link org.springframework.extensions.webscripts.connector.AlfrescoConnector} to allow the connection from Share
 * to the Alfresco Repository to use a configurable HTTP header for the
 * authenticated user name. Allows both Share and Repository to use the same
 * HTTP header, when an external SSO is unable to provide the user name in the
 * default Alfresco Repository header {@code "X-Alfresco-Remote-User"}.
 * <p>
 * The user name from the header is also used by {@link org.alfresco.web.site.servlet.SSOAuthenticationFilter}
 * for incoming request to Share to return the user name from
 * {@link jakarta.servlet.http.HttpServletRequest#getRemoteUser}.
 * <p>
 * The name of the header to be used is defined in the userHeader element of the
 * Alfresco Connector definition (see share-config-custom.xml.sample). Also note
 * the class element specifies this class. For example:
 * <pre>
 * &lt;connector&gt;
 *   &lt;id&gt;alfrescoCookie&lt;/id&gt;
 *   &lt;name&gt;Alfresco Connector&lt;/name&gt;
 *   &lt;description&gt;Connects to an Alfresco instance using cookie-based authentication&lt;/description&gt;
 *   &lt;class&gt;org.alfresco.web.site.servlet.SlingshotAlfrescoConnector&lt;/class&gt;
 *   &lt;userHeader&gt;SsoUserHeader&lt;/userHeader&gt;
 *   &lt;userIdPattern&gt;&lt;/userIdPattern&gt;
 * &lt;/connector&gt;
 * </pre>
 * The Alfresco global property {@code external.authentication.proxyHeader} still needs to
 * be configured on the Repository side to define which header will be used. For example:
 * <pre>
 * authentication.chain=MySso:external,alfrescoNtlm1:alfrescoNtlm
 * external.authentication.proxyUserName=
 * external.authentication.proxyHeader=SsoUserHeader
 * </pre>
 * The {@code userIdPattern} element should mirror the value set on the Repository if used:
 * <pre>
 * external.authentication.userIdPattern=
 * </pre>
 *
 * When using the default Alfresco Repository header (X-Alfresco-Remote-User") Share and the
 * Alfresco Repository must be protected against direct access from other clients. The same is
 * true when using a configurable header. The reason is that Share and Alfresco just accept the
 * header value as valid. Without this protection, it would be possible to log in as any user
 * simply by setting the header.
 *
 * @author adavis
 * @author kroast
 * @version $Id: $Id
 */
public class BeCPGExternalConnector extends RequestCachingConnector {

	private static Log logger = LogFactory.getLog(BeCPGExternalConnector.class);

	/**
	 * The name of the element in the {@link ConnectorDescriptor}
	 * ({@code <connector>...<userHeader>...</userHeader></connector>}) that
	 * contains the name of the HTTP header used by an external SSO
	 * to provide the authenticated user name.
	 */
	private static final String CD_USER_HEADER = "userHeader";

	/**
	 * The name of the element in the {@link ConnectorDescriptor}
	 * ({@code <connector>...<userIdPattern>...</userIdPattern></connector>}) that
	 * contains the optional regex used by the external SSO to pattern match authenticated
	 * user names. This value must match that used by Alfresco External Auth settings.
	 */
	private static final String CD_USER_ID_PATTERN = "userIdPattern";

	/**
	 * The name of the property in the {@link ConnectorSession} that
	 * contains the name of the HTTP header used by an external SSO
	 * to provide the authenticated user name.
	 */
	public static final String CS_PARAM_USER_HEADER = "userHeader";

	/**
	 * The name of the property in the {@link ConnectorSession} that
	 * contains the optional regex used by the external SSO to pattern match authenticated
	 * user names.
	 */
	public static final String CS_PARAM_USER_ID_PATTERN = "userIdPattern";

	/**
	 * <p>Constructor for BeCPGExternalConnector.</p>
	 *
	 * @param descriptor a {@link org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor} object.
	 * @param endpoint a {@link java.lang.String} object.
	 */
	public BeCPGExternalConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	private String getUserHeader() {
		String userHeader = descriptor.getStringProperty(CD_USER_HEADER);
		if ((userHeader != null) && (userHeader.isBlank())) {
			userHeader = null;
		}
		return userHeader;
	}

	private String getUserIdPattern() {
		String userIdPattern = descriptor.getStringProperty(CD_USER_ID_PATTERN);
		if ((userIdPattern != null) && (userIdPattern.isBlank())) {
			userIdPattern = null;
		}
		return userIdPattern;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides super method to set the CS_PARAM_USER_HEADER. This method is
	 * always called at the end of ConnectorService#getConnector when
	 * it constructs a connector
	 */
	@Override
	public void setConnectorSession(ConnectorSession connectorSession) {
		super.setConnectorSession(connectorSession);
		connectorSession.setParameter(CS_PARAM_USER_HEADER, getUserHeader());
		connectorSession.setParameter(CS_PARAM_USER_ID_PATTERN, getUserIdPattern());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Overrides the super method to add the HTTP header used by an external SSO
	 * to provide the authenticated user name when calling alfresco from share.
	 */
	@Override
	protected void applyRequestHeaders(RemoteClient remoteClient, ConnectorContext context) {
		// Need to override the headers set on the remoteClient to include the 'userHeader'
		// The following duplicates much of the code in the super method. Creating a new
		// context with the userHeader is even more complex.

		// copy in cookies that have been stored back as part of the connector session
		ConnectorSession connectorSession = getConnectorSession();
		if (connectorSession != null) {
			Map<String, String> cookies = new HashMap<>(8);
			for (String cookieName : connectorSession.getCookieNames()) {
				cookies.put(cookieName, connectorSession.getCookie(cookieName));
			}
			remoteClient.setCookies(cookies);
		}

		Map<String, String> headers = new HashMap<>(8);
		if (context != null) {
			headers.putAll(context.getHeaders());
		}

		// Proxy the authenticated user name if we have password-less credentials (indicates SSO auth over a secure connection)
		if (getCredentials() != null) {
			String userHeader = getUserHeader();
			if (userHeader != null) {
				HttpServletRequest req = ServletUtil.getRequest();
				if (req == null) {
					req = MTAuthenticationFilter.getCurrentServletRequest();
				}
				// MNT-15866: In some cases req can be null so we need to check it before getHeader from it
				String user = null;
				if (req != null) {
					user = req.getHeader(userHeader);
					if (user == null) {
						// MNT-15795
						user = req.getRemoteUser();
					}
				}
				if (user != null) {

					// MNT-11041 Share SSOAuthenticationFilter and non-ascii username strings
					if (!org.apache.commons.codec.binary.Base64.isBase64(user)) {
						user = org.apache.commons.codec.binary.Base64.encodeBase64String(
								(new String(user.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
						headers.put("Remote-User-Encode", Boolean.TRUE.toString());
					}

					if (logger.isDebugEnabled()) {
						logger.debug("beCPG setting user : " + user + " into " + userHeader);
					}

					headers.put(userHeader, user);
				}
			}
		}

		// stamp all headers onto the remote client
		if (headers.size() != 0) {
			remoteClient.setRequestProperties(headers);
		}
	}
}
