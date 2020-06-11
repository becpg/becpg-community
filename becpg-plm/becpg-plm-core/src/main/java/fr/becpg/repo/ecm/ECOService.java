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
package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Engineering change order service
 * @author quere
 *
 */
public interface ECOService {	
	
	void calculateWUsedList(NodeRef ecoNodeRef, boolean selectToApply);
	
	boolean apply(NodeRef ecoNodeRef);
	
	boolean doSimulation(NodeRef ecoNodeRef);

	Boolean setInProgress(NodeRef ecoNodeRef);

	Boolean setInError(NodeRef ecoNodeRef, Exception e);


}
