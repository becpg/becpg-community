/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
 *
 * @author quere
 * @version $Id: $Id
 */
public class CheckInEntityEvent extends ApplicationEvent {

	private static final long serialVersionUID = 6872600105098661778L;	
	private final NodeRef entityNodeRef;
	
	/**
	 * <p>Getter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	/**
	 * <p>Constructor for CheckInEntityEvent.</p>
	 *
	 * @param source a {@link java.lang.Object} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public CheckInEntityEvent(Object source, NodeRef entityNodeRef) {
		super(source);
		
		this.entityNodeRef = entityNodeRef;
	}

}
