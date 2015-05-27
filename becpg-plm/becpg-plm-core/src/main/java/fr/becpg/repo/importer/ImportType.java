/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.importer;

/**
 * Define the type of import.
 *
 * @author querephi
 */
public enum ImportType {

	/** The node with QName properties and associations */
	Node, 
	/** The entity datalists as compoList, ingList (not characteristics) */
	EntityListItem,
	/** The entity with characteristics (costs, nuts, etc...) */
	EntityListAspect, 
	/** Add Comments to product */
	Comments
}
