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

/**
 * The Share endpoints called in the background by the client that need an authenticated user: the web script
 * services and the repository proxy, except their <code>noauth</code> variants.
 *
 * Stateless and thread-safe.
 *
 * @author matthieu
 */
public class ShareBackgroundEndpoints {

    private static final String SERVICE_PATH = "/service/";

    private static final String PROXY_PATH = "/proxy/";

    private static final String NOAUTH_MARKER = "noauth";

    private final String servicePrefix;

    private final String proxyPrefix;

    /**
     * <p>Constructor for ShareBackgroundEndpoints.</p>
     *
     * @param shareContext the Share context path, for instance <code>/share</code>
     */
    public ShareBackgroundEndpoints(String shareContext) {
        this.servicePrefix = shareContext + SERVICE_PATH;
        this.proxyPrefix = shareContext + PROXY_PATH;
    }

    /**
     * Tells whether the URI targets a background endpoint that needs an authenticated user.
     *
     * @param requestUri the request URI, may be null
     * @return true for a service or proxy URI that is not a <code>noauth</code> one
     */
    public boolean isProtected(String requestUri) {
        if (requestUri == null || requestUri.contains(NOAUTH_MARKER)) {
            return false;
        }
        return requestUri.startsWith(servicePrefix) || requestUri.startsWith(proxyPrefix);
    }
}
