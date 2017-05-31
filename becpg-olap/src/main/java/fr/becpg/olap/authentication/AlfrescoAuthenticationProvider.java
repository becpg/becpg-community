/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import fr.becpg.olap.http.LoginCommand;
import fr.becpg.tools.InstanceManager;
import fr.becpg.tools.InstanceManager.Instance;
import fr.becpg.tools.helper.UserNameHelper;

/**
 * 
 * @author matthieu
 *
 */
public class AlfrescoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final Log logger = LogFactory.getLog(AlfrescoAuthenticationProvider.class);

	private UserDetailsService userDetailsService;

	InstanceManager instanceManager;

	public void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

	}

	protected void doAfterPropertiesSet() throws Exception {
		Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
	}

	protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

		if (authentication.getCredentials() == null) {
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}

		try {
			Instance instance = instanceManager.findInstanceByUserName(username);

			String presentedPassword = authentication.getCredentials().toString();
			String presentedLogin = UserNameHelper.extractLogin(username);

			LoginCommand loginCommand = new LoginCommand(instance.getInstanceUrl());
			String alfTicket = loginCommand.getAlfTicket(presentedLogin, presentedPassword);
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieving alfTicket :" + alfTicket);
			}

			if (alfTicket == null || alfTicket.isEmpty()) {
				throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
			}

			UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(UserNameHelper.buildAuthToken(username,alfTicket));

			if (loadedUser == null) {
				throw new AuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
			}
			return loadedUser;

		} catch (SQLException e) {
			throw new UsernameNotFoundException(e.getMessage());
		} catch (DataAccessException repositoryProblem) {
			throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
		}
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	protected UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

}
