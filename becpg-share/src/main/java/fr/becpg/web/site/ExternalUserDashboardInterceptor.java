/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package fr.becpg.web.site;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.alfresco.web.site.servlet.MTAuthenticationFilter;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.mvc.AbstractWebFrameworkInterceptor;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;

/**
 * Framework interceptor responsible for constructing user dashboards if one has
 * not already been initialised and persisted for the current user.
 *
 * @author Kevin Roast, ML
 * @version $Id: $Id
 */
public class ExternalUserDashboardInterceptor extends AbstractWebFrameworkInterceptor {
	private static final Pattern PATTERN_DASHBOARD_PATH = Pattern.compile(".*/user/([^/]*)/dashboard");
	
	//beCPG
	private static final Pattern PATTERN_FORBIDDEN_PATH = Pattern.compile("(.*/page/customise-user-dashboard)"
			+ "|(.*/page/start-workflow)"
			+ "|(.*/page/site/.*)"
			+ "|(.*/page/people-finder)"
			+ "|(.*/page/start-workflow)"
			+ "|(.*/page/advsearch)"
			+ "|(.*/page/create-content)"
			+ "|(.*/page/repository)"
			+ "|(.*/page/documentlibrary)"
            + "|(.*/page/wused)"
			+ "|(.*/page/bulk-edit)"
			+ "|(.*/page/nc-list)"
			+ "|(.*/page/model-designer)"
			+ "|(.*/page/project-list)"
			+ "|(.*/page/entity-data-lists)"
			+ "|(.*/page/user/admin/profile)"
			+ "|(.*/page/context/mine/entity-data-lists)");

	private static final String IS_BECPG_EXTERNAL_USER = "isbeCPGExternalUser";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.context.request.WebRequestInterceptor#preHandle
	 * (org.springframework.web.context.request.WebRequest)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("deprecation")
	@Override
	public void preHandle(WebRequest request) throws Exception {
		final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
		final String pathInfo = rc.getUri();
		Matcher matcher;
		if (pathInfo != null && (matcher = PATTERN_DASHBOARD_PATH.matcher(pathInfo)).matches()) {
			HttpServletRequest req = MTAuthenticationFilter.getCurrentServletRequest();
			if (req != null) {
				try {
					// init the user object so we can test the current user ID
					// against the page uri
					ServletUtil.setRequest(req);
					RequestContextUtil.populateRequestContext(rc, req);
					final String userid = rc.getUserId();

					// test user dashboard page exists?
					if (userid != null && userid.equals(URLDecoder.decode(matcher.group(1)))) {
						WebFrameworkServiceRegistry serviceRegistry = rc.getServiceRegistry();
						if (serviceRegistry.getModelObjectService().getPage("user/" + userid + "/dashboard") == null) {
								// no dashboard found! create initial dashboard
								// for this user...
								Map<String, String> tokens = new HashMap<String, String>(2);
								tokens.put("userid", userid);
								//beCPG
								if (!isExternalUser(rc.getUser())) {
									serviceRegistry.getPresetsManager().constructPreset("user-dashboard", tokens);
								} else {
									serviceRegistry.getPresetsManager().constructPreset("external-dashboard", tokens);
								}
							}
					} else {
						// reset the user context to ensure Guest or similar is
						// not applied - this will avoid
						// issues with SSO filters or similar that expect empty
						// user after interceptor execution
						rc.setUser(null);
						rc.setPage(ThreadLocalRequestContext.getRequestContext().getRootPage());
					}
				} catch (UserFactoryException uerr) {
					// unable to generate the user dashboard - the user can
					// still do this by visiting the index page
				}
			}
		} else if((pathInfo != null && (matcher = PATTERN_FORBIDDEN_PATH.matcher(pathInfo)).matches() && !pathInfo.contains("discussions-topicview")
				&& rc.getParameter("resetLocale") == null)){
			HttpServletRequest req = MTAuthenticationFilter.getCurrentServletRequest();
			if (req != null) {
				try {
					// init the user object so we can test the current user ID
					// against the page uri
					ServletUtil.setRequest(req);
					RequestContextUtil.populateRequestContext(rc, req);
					if(isExternalUser(rc.getUser())){
						throw new AuthenticationException("External user not allowed here");
					}
					
				} catch (UserFactoryException uerr) {
					// unable to generate the user dashboard - the user can
					// still do this by visiting the index page
				}
			}
		}
	}

	private boolean isExternalUser(User user) {
		return user !=null && user.getCapabilities().containsKey(IS_BECPG_EXTERNAL_USER)
				&& Boolean.TRUE.equals(user.getCapabilities().get(IS_BECPG_EXTERNAL_USER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.context.request.WebRequestInterceptor#postHandle
	 * (org.springframework.web.context.request.WebRequest,
	 * org.springframework.ui.ModelMap)
	 */
	/** {@inheritDoc} */
	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.context.request.WebRequestInterceptor#afterCompletion
	 * (org.springframework.web.context.request.WebRequest, java.lang.Exception)
	 */
	/** {@inheritDoc} */
	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
	}
}
