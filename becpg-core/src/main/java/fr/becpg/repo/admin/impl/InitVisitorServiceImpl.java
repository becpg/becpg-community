/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.admin.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.admin.InitVisitorService;


@Service("initVisitorService")
public class InitVisitorServiceImpl implements InitVisitorService {

	private static Log logger = LogFactory.getLog(InitVisitorServiceImpl.class);
	
	@Autowired
	private InitVisitor[] initVisitors;
	

	@Override
	public void run(NodeRef companyHomeNodeRef) {
		for(InitVisitor initVisitor : initVisitors) {
			if(logger.isDebugEnabled()) {
				logger.debug("Run visitor : "+initVisitor.getClass().getName());
			}
			
			initVisitor.visitContainer(companyHomeNodeRef);
		}
		
	}
}
