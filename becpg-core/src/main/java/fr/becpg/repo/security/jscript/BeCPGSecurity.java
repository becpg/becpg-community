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
package fr.becpg.repo.security.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.security.SecurityService;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class BeCPGSecurity extends BaseScopableProcessorExtension{

	private static final Log logger = LogFactory.getLog(BeCPGSecurity.class);
	
    protected ServiceRegistry services;
    
	private SecurityService securityService;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
		
	/**
	 * 
	 * @param entityNode
	 * @param dataListType
	 * @return
	 */
	public boolean hasWriteAccess(ScriptNode entityNode,String dataListType){
		logger.debug(" jscript - hasWriteAccess");
		return securityService.computeAccessMode(services.getNodeService().getType(entityNode.getNodeRef()), dataListType) 
				== SecurityService.WRITE_ACCESS;
		
	}
	

}
