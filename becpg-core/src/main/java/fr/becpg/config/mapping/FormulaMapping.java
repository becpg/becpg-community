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
package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;


/**
 * This class represents the mapping for importing either a property formula
 * or a node association.
 *
 * Example usage:
 * <pre>
 * {@code
 * <column id="labelClaimFormula" type="Formula" attribute="bcpg:labelClaimFormula" />
 * }
 * </pre>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class FormulaMapping extends AbstractAttributeMapping {

	/**
	 * <p>Constructor for FormulaMapping.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	public FormulaMapping(String id, ClassAttributeDefinition attribute) {
		super(id, attribute);
	}

}
