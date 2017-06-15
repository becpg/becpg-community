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

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import fr.becpg.tools.InstanceManager.Instance;
/**
 * 
 * @author matthieu
 *
 */
public class AlfrescoUserDetails extends User implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3854416878397851938L;
	private final Instance instance;
	private final String sessionId;
	
	
	public AlfrescoUserDetails(String username, String password, boolean enabled,
			Collection<? extends GrantedAuthority> authorities, Instance instance) {
		super(username, password, enabled, true,true,true, authorities);
		this.instance = instance;
		this.sessionId =  UUID.randomUUID().toString();
		
	}

	public Instance getInstance() {
		return instance;
	}

	public String getSessionId() {
		return sessionId;
	}

	
}
