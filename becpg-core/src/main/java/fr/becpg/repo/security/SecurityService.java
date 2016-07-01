/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.security;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public interface SecurityService {

	/**
	 * Access status
	 */
	int NONE_ACCESS = 0;
	int READ_ACCESS = 1;
	int WRITE_ACCESS = 2;
	
	/**
	 * Compute access mode for the given field name on a specific type
	 * @param nodeType
	 * @param name
	 * @return Access Mode status
	 */
	int computeAccessMode(QName nodeType, String name);
	
	/**
	 * Refresh ACLS cache per tenant
	 */
	void refreshAcls();

	/**
	 * Extract props list based on existing ACL_GROUPS
	 * @param item
	 * @return
	 */
	List<String> getAvailablePropNames();

}
