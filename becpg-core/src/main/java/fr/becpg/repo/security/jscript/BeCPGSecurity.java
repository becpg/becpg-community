/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
package fr.becpg.repo.security.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;

import fr.becpg.repo.security.SecurityService;

/**
 * <p>BeCPGSecurity class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class BeCPGSecurity extends BaseScopableProcessorExtension{
	
    protected ServiceRegistry services;
    
	private SecurityService securityService;
	
	/**
	 * <p>setServiceRegistry.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
		
	
	/**
	 * <p>hasWriteAccess.</p>
	 *
	 * @param entityNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param dataListType a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean hasWriteAccess(ScriptNode entityNode,String dataListType){
		return securityService.computeAccessMode(entityNode.getNodeRef(), services.getNodeService().getType(entityNode.getNodeRef()), dataListType) 
				== SecurityService.WRITE_ACCESS;
		
	}
	

}
