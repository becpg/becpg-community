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
package fr.becpg.repo.entity.event;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.context.ApplicationEvent;

/**
 * CheckIn entity event
 * @author quere
 *
 */
public class CheckInEntityEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6872600105098661778L;	
	private final NodeRef entityNodeRef;
	
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public CheckInEntityEvent(Object source, NodeRef entityNodeRef) {
		super(source);
		
		this.entityNodeRef = entityNodeRef;
	}

}
