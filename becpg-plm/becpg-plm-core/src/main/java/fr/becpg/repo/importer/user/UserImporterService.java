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
package fr.becpg.repo.importer.user;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.importer.ImporterException;

/**
 * <p>UserImporterService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface UserImporterService {

	/**
	 * <p>importUser.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	void importUser(NodeRef nodeRef) throws ImporterException;


}
