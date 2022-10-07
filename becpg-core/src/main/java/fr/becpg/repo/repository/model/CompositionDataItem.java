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
package fr.becpg.repo.repository.model;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * <p>CompositionDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface CompositionDataItem extends RepositoryEntity, CopiableDataItem, EffectiveDataItem, VariantDataItem {

	/**
	 * <p>getComponent.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getComponent();
	
	/**
	 * <p>getComponentAssocName.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	QName getComponentAssocName();

	/**
	 * <p>setComponent.</p>
	 *
	 * @param targetItem a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void setComponent(NodeRef targetItem);

	/**
	 * <p>getQty.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	Double getQty();

	/**
	 * <p>setQty.</p>
	 *
	 * @param d a {@link java.lang.Double} object.
	 */
	void setQty(Double d);
	
	/**
	 * <p>getLossPerc.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	Double getLossPerc();

	/**
	 * <p>setLossPerc.</p>
	 *
	 * @param d a {@link java.lang.Double} object.
	 */
	void setLossPerc(Double d);


}
