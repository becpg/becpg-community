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
package fr.becpg.repo.migration;

import org.alfresco.service.namespace.QName;

/**
 * Service to migrate model and data
 *
 * @author quere
 * @version $Id: $Id
 */
public interface MigrationService {

	//MT
	/**
	 * <p>addMandatoryAspectInMt.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param aspect a {@link org.alfresco.service.namespace.QName} object.
	 */
	void addMandatoryAspectInMt(final QName type, final QName aspect);
	/**
	 * <p>removeAspectInMt.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param aspect a {@link org.alfresco.service.namespace.QName} object.
	 */
	void removeAspectInMt(final QName type, final QName aspect);
	/**
	 * <p>migrateAssociationInMt.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param sourceAssoc a {@link org.alfresco.service.namespace.QName} object.
	 * @param targetAssoc a {@link org.alfresco.service.namespace.QName} object.
	 */
	void migrateAssociationInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
	/**
	 * <p>migratePropertyInMt.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param sourceAssoc a {@link org.alfresco.service.namespace.QName} object.
	 * @param targetAssoc a {@link org.alfresco.service.namespace.QName} object.
	 */
	void migratePropertyInMt(final QName classQName, final QName sourceAssoc, final QName targetAssoc);
	
	//Non MT
	/**
	 * <p>addMandatoryAspect.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 * @param aspect a {@link org.alfresco.service.namespace.QName} object.
	 */
	void addMandatoryAspect(QName type, QName aspect);
	/**
	 * <p>migrateAssociation.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param sourceAssoc a {@link org.alfresco.service.namespace.QName} object.
	 * @param targetAssoc a {@link org.alfresco.service.namespace.QName} object.
	 */
	void migrateAssociation(QName classQName, QName sourceAssoc, QName targetAssoc);
	/**
	 * <p>cleanOrphanVersion.</p>
	 */
	void cleanOrphanVersion();
}
