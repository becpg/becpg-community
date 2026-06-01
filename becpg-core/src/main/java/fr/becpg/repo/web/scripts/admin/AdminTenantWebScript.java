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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.admin;

import java.io.File;
import java.util.Map;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * The Class AdminTenantWebScript.
 *
 * @author matthieu
 * @version $Id: $Id
 * @deprecated since 4.2, see TenantPost
 */
@Deprecated(since = "4.2", forRemoval = false)
public class AdminTenantWebScript extends AbstractWebScript {

	
	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(AdminTenantWebScript.class);

	// request parameter names

	/** Constant <code>PARAM_ACTION="action"</code> */
	private static final String PARAM_ACTION = "action";

	/** Constant <code>PARAM_DOMAIN="tenantDomain"</code> */
	private static final String PARAM_DOMAIN = "tenantDomain";

	/** Constant <code>PARAM_PASSWORD="tenantPassword"</code> */
	private static final String PARAM_PASSWORD = "tenantPassword";

	/** Constant <code>PARAM_ROOT_CONTENT="rootContentStoreDir"</code> */
	private static final String PARAM_ROOT_CONTENT = "rootContentStoreDir";
	
	/** Constant <code>PARAM_DIRECTORY_DESTINATION="directoryDestination"</code> */
	private static final String PARAM_DIRECTORY_DESTINATION = "directoryDestination";

	/** Constant <code>ACTION_CREATE="create"</code> */
	private static final String ACTION_CREATE = "create";
	/** Constant <code>ACTION_EXPORT="export"</code> */
	private static final String ACTION_EXPORT = "export";

	private TenantAdminService tenantAdminService;
	private TenantService tenantService;
	private AuthorityService authorityService;
	private AuthenticationService authenticationService;

	//Spring IoC
	
	/**
	 * <p>Setter for the field <code>tenantAdminService</code>.</p>
	 *
	 * @param tenantAdminService a {@link org.alfresco.repo.tenant.TenantAdminService} object.
	 */
	@Deprecated
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	/**
	 * <p>Setter for the field <code>tenantService</code>.</p>
	 *
	 * @param tenantService a {@link org.alfresco.repo.tenant.TenantService} object.
	 */
	@Deprecated
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object.
	 */
	@Deprecated
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.service.cmr.security.AuthenticationService} object.
	 */
	@Deprecated
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.
	 * springframework.extensions.webscripts.WebScriptRequest,
	 * org.springframework.extensions.webscripts.WebScriptResponse)
	 */
	/** {@inheritDoc} */
	@Deprecated
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {

		String username = authenticationService.getCurrentUserName();
		
		// must be "super" admin for tenant administration
		if ((username != null) && (authorityService.isAdminAuthority(username)) && (!tenantService.isTenantUser(username))) {

			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

			String action = templateArgs.get(PARAM_ACTION);
			String tenant = req.getParameter(PARAM_DOMAIN);
			
			// Check arg			
			if (action == null || action.isEmpty()){
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'action' argument cannot be null or empty");
			}
						
			if (tenant == null || tenant.isEmpty()) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'tenantDomain' argument cannot be null or empty");
			}

			logger.debug("Call admin tenant webscript for action :" + action);

			if (ACTION_CREATE.equals(action)) {

				
				String tenantAdminRawPassword = req.getParameter(PARAM_PASSWORD);
				String rootContentStoreDir = req.getParameter(PARAM_ROOT_CONTENT);
				
				if (tenantAdminRawPassword == null || tenantAdminRawPassword.isEmpty()) {
					throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'tenantPassword' argument cannot be null or empty");
				}

				if (rootContentStoreDir == null || rootContentStoreDir.isEmpty()) {
					throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'rootContentStoreDir' argument cannot be null or empty");
				}

				tenantAdminService.createTenant(tenant, tenantAdminRawPassword.toCharArray(), rootContentStoreDir);

			}
			else if(ACTION_EXPORT.equals(action)){
				    
				String directoryDestination = req.getParameter(PARAM_DIRECTORY_DESTINATION);
				if (directoryDestination == null || directoryDestination.isEmpty()) {
					throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'directoryDestination' argument cannot be null or empty");
				}
					            
				File directoryDestinationFile = new File(directoryDestination);
	            tenantAdminService.exportTenant(tenant, directoryDestinationFile);
			}
    		else{
    			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'action'. action = " + action);
    		}    	

		} else {
			throw new WebScriptException(Status.STATUS_FORBIDDEN, "Bad username " + username);
		}

	}
}
