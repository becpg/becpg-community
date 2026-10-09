/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.security.authentication.identityservice;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link org.alfresco.repo.security.authentication.external.RemoteUserMapper} implementation that detects and validates JWTs
 * issued by the Alfresco Identity Service.
 *
 * @author Gavin Cornwell
 */
public class IdentityServiceRemoteUserMapper implements RemoteUserMapper, ActivateableBean {
	private static final Log LOGGER = LogFactory.getLog(IdentityServiceRemoteUserMapper.class);

	/** Is the mapper enabled */
	private boolean isEnabled;

	/** Are token validation failures handled silently? */
	private boolean isValidationFailureSilent;

	private BearerTokenResolver bearerTokenResolver;

	//beCPG
	/** The header containing the ID of a proxied user. */
	private static final String PROXY_HEADER = "X-Alfresco-Remote-User";

	private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

	/**
	 * Sets the active flag
	 *
	 * @param isEnabled true to enable the subsystem
	 */
	public void setActive(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Determines whether token validation failures are silent
	 *
	 * @param silent true to silently fail, false to throw an exception
	 */
	public void setValidationFailureSilent(boolean silent) {
		this.isValidationFailureSilent = silent;
	}

	/**
	 * <p>Setter for the field <code>bearerTokenResolver</code>.</p>
	 *
	 * @param bearerTokenResolver a {@link org.springframework.security.oauth2.server.resource.web.BearerTokenResolver} object
	 */
	public void setBearerTokenResolver(BearerTokenResolver bearerTokenResolver) {
		this.bearerTokenResolver = bearerTokenResolver;
	}

	/**
	 * <p>Setter for the field <code>jitProvisioningHandler</code>.</p>
	 *
	 * @param jitProvisioningHandler a {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceJITProvisioningHandler} object
	 */
	public void setJitProvisioningHandler(IdentityServiceJITProvisioningHandler jitProvisioningHandler) {
		this.jitProvisioningHandler = jitProvisioningHandler;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.web.app.servlet.RemoteUserMapper#getRemoteUser(jakarta.servlet.http.HttpServletRequest)
	 */
	/** {@inheritDoc} */
	@Override
	public String getRemoteUser(HttpServletRequest request) {

		if (!this.isEnabled) {
			LOGGER.debug("IdentityServiceRemoteUserMapper is disabled, returning null.");
			return null;
		}
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("beCPG - Try retrieving username from http request " + request.getPathInfo());
			}

			String normalizedUserId = extractUserFromHeader(request);

			if (normalizedUserId != null) {
				// Normalize the user ID taking into account case sensitivity settings
				LOGGER.trace("Found userId: " + AuthenticationUtil.maskUsername(normalizedUserId));

				return normalizedUserId;
			}
		} catch (IdentityServiceFacadeException e) {
			if (!isValidationFailureSilent) {
				throw new AuthenticationException("Failed to extract username from token: " + e.getMessage(), e);
			}
			LOGGER.error("Failed to authenticate user using IdentityServiceRemoteUserMapper: " + e.getMessage(), e);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to authenticate user using IdentityServiceRemoteUserMapper: " + e.getMessage(), e);
		}
		LOGGER.trace("Could not identify a userId. Returning null.");
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
	 */
	/**
	 * <p>isActive.</p>
	 *
	 * @return a boolean
	 */
	@Override
	public boolean isActive() {
		return this.isEnabled;
	}

	/**
	 * Extracts the user name from the JWT in the given request.
	 * 
	 * @param request The request containing the JWT
	 * @return The username or null if it can not be determined
	 */
	private String extractUserFromHeader(HttpServletRequest request) {
		//beCPG (To use Share SSO with IDS we need to active remote user support)

		String userId = request.getHeader(PROXY_HEADER);
		if (userId != null) {
			// MNT-11041 Share SSOAuthenticationFilter and non-ascii username strings
			boolean isEncode = Boolean.parseBoolean(request.getHeader("Remote-User-Encode"));

			if (isEncode) {
				userId = new String(org.apache.commons.codec.binary.Base64.decodeBase64(userId), StandardCharsets.UTF_8);
			} else if (!org.apache.commons.codec.binary.Base64.isBase64(userId)) {
				userId = new String(userId.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			}
			LOGGER.debug("The header user id is: " + userId);

			return userId.isBlank() ? null : userId.trim();

		} else {
			String remoteUserId = request.getRemoteUser();
			if (remoteUserId != null) {
				LOGGER.debug("The remote user id is: " + remoteUserId);
				return remoteUserId;
			}
		}

		// try authenticating with bearer token first
		LOGGER.debug("Trying bearer token...");

		final String bearerToken;
		try {
			bearerToken = bearerTokenResolver.resolve(request);
		} catch (OAuth2AuthenticationException e) {
			LOGGER.debug("Failed to resolve Bearer token.", e);
			return null;
		}

		final Optional<String> possibleUsername = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(bearerToken)
				.map(OIDCUserInfo::username);

		if (possibleUsername.isEmpty()) {
			LOGGER.debug("User could not be authenticated by IdentityServiceRemoteUserMapper.");
			return null;
		}

		String normalizedUsername = possibleUsername.get();
		LOGGER.trace("Extracted username: " + AuthenticationUtil.maskUsername(normalizedUsername));

		return normalizedUsername;
	}

}
