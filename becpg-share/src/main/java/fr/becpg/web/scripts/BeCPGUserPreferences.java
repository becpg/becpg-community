/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG.
 *
 *  This file is part of beCPG
 *
 *  beCPG is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  beCPG is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.web.scripts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

import org.alfresco.web.scripts.UserPreferences;

/**
 * Hardened replacement for Share's <code>preferences</code> script root object.
 *
 * <p>
 * {@link org.alfresco.web.scripts.UserPreferences#getValue()} only skips the remote preference read for the guest user; when no
 * user at all is bound to the request context it still calls
 * <code>URLEncoder.encode(requestContext.getUserId())</code>, which throws a {@link java.lang.NullPointerException} on a null id.
 * Any web script reading <code>preferences.value</code> then answers a 500 instead of degrading, which is what surfaces as the
 * random "Could not read Data List Column definitions" pop-up when a request is served while the AIMS filter is renewing the
 * Alfresco ticket.
 * </p>
 *
 * @author matthieu
 */
public class BeCPGUserPreferences extends UserPreferences {

	private static final Log logger = LogFactory.getLog(BeCPGUserPreferences.class);

	/** Empty preference set, also what the stock implementation returns for the guest user. */
	private static final String NO_PREFERENCES = "{}";

	/** {@inheritDoc} */
	@Override
	public String getValue() {
		RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();
		if ((requestContext == null) || (requestContext.getUserId() == null)) {
			// no authenticated user on this request: behave as if the user had no preference at all
			if (logger.isDebugEnabled()) {
				logger.debug("No user bound to the request context, returning an empty preference set");
			}
			return NO_PREFERENCES;
		}

		try {
			return super.getValue();
		} catch (RuntimeException e) {
			// a preference is never worth failing the whole web script for
			logger.warn("Unable to read the user preferences, falling back on an empty preference set", e);
			return NO_PREFERENCES;
		}
	}

}
