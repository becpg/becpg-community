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
package fr.becpg.repo.formulation;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>FormulatedEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulatedEntity extends RepositoryEntity {

	/**
	 * <p>getReformulateCount.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	Integer getReformulateCount();

	/**
	 * <p>setReformulateCount.</p>
	 *
	 * @param reformulateCount a {@link java.lang.Integer} object.
	 */
	void setReformulateCount(Integer reformulateCount);

	/**
	 * <p>getCurrentReformulateCount.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	Integer getCurrentReformulateCount();

	/**
	 * <p>setCurrentReformulateCount.</p>
	 *
	 * @param currentReformulateCount a {@link java.lang.Integer} object.
	 */
	void setCurrentReformulateCount(Integer currentReformulateCount);

	/**
	 * <p>getFormulatedDate.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	Date getFormulatedDate();
	

	/**
	 * <p>setFormulatedDate.</p>
	 *
	 * @param formulatedDate a {@link java.util.Date} object.
	 */
	void setFormulatedDate(Date formulatedDate);

	/**
	 * <p>setFormulationChainId.</p>
	 *
	 * @param chainId a {@link java.lang.String} object.
	 */
	void setFormulationChainId(String chainId);
	
	/**
	 * <p>getFormulationChainId.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getFormulationChainId();
	
	/**
	 * <p>shouldUpdateFormulatedDate.</p>
	 *
	 * @return a boolean.
	 */
	boolean shouldUpdateFormulatedDate();
	
	/**
	 * <p>setUpdateFormulatedDate.</p>
	 *
	 * @param updateFormulatedDate a boolean.
	 */
	void setUpdateFormulatedDate(boolean updateFormulatedDate);
	
	/**
	 * <p>getFormulatedEntityTpl.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getFormulatedEntityTpl();
	
	/**
	 * <p>getRequirementChecksum.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRequirementChecksum();
	

}
