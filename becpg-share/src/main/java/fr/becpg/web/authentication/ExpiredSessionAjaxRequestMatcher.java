/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.web.authentication;

import org.springframework.extensions.surf.site.AuthenticationUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Recognises a background request of the Share client that reached the AIMS filter without any
 * authenticated user, typically after the HTTP session expired or was invalidated by a refused
 * token refresh.
 *
 * Only full page requests can go through the identity provider. A background request left to run
 * anonymously reaches a webscript that fails with a 500, which the client reports as an opaque
 * error instead of reloading the page. Answering 401 to it lets the client reload the page, which
 * replays the SSO login.
 *
 * The YUI connection manager sends the X-Requested-With header on every request, but Alfresco
 * reuses it to carry the content type, so only its presence is checked. Script tags and plain
 * links do not send it, which keeps the resources loaded by the login page untouched.
 *
 * Stateless and thread-safe.
 *
 * @author matthieu
 */
public class ExpiredSessionAjaxRequestMatcher {

    private static final String REQUESTED_WITH_HEADER = "X-Requested-With";

    private static final String SERVICE_PATH = "/service/";

    private static final String PROXY_PATH = "/proxy/";

    private static final String NOAUTH_MARKER = "noauth";

    private final String servicePrefix;

    private final String proxyPrefix;

    /**
     * <p>Constructor for ExpiredSessionAjaxRequestMatcher.</p>
     *
     * @param shareContext the Share context path, for instance <code>/share</code>
     */
    public ExpiredSessionAjaxRequestMatcher(String shareContext) {
        this.servicePrefix = shareContext + SERVICE_PATH;
        this.proxyPrefix = shareContext + PROXY_PATH;
    }

    /**
     * Tells whether the request is a background request that must be answered with a 401.
     *
     * @param request the incoming request
     * @return true for a background service or proxy request carrying no authenticated Share user
     */
    public boolean matches(HttpServletRequest request) {
        return isBackgroundRequest(request) && targetsProtectedEndpoint(request.getRequestURI())
                && !AuthenticationUtil.isAuthenticated(request);
    }

    private boolean isBackgroundRequest(HttpServletRequest request) {
        return request.getHeader(REQUESTED_WITH_HEADER) != null;
    }

    private boolean targetsProtectedEndpoint(String requestUri) {
        if (requestUri == null || requestUri.contains(NOAUTH_MARKER)) {
            return false;
        }
        return requestUri.startsWith(servicePrefix) || requestUri.startsWith(proxyPrefix);
    }
}
