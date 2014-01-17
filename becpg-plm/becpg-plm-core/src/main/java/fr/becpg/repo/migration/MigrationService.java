/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.migration;

import org.alfresco.service.namespace.QName;

/**
 * Service to migrate model and data
 * @author quere
 *
 */
public interface MigrationService {

	//MT
	public void addMandatoryAspectInMt(final QName type, final QName aspect);
	public void removeAspectInMt(final QName type, final QName aspect);
	public void migrateAssociationInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
	public void migratePropertyInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
	
	//Non MT
	public void addMandatoryAspect(QName type, QName aspect);
	void migrateAssociation(QName classQName, QName sourceAssoc, QName targetAssoc);
}
