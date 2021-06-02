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
package fr.becpg.repo.designer;

import org.alfresco.repo.dictionary.M2Model;

/**
 * This service is used to read metaModel and write it from xml
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface MetaModelService {
	
	
	/**
	 * extract M2Model from m2:model node
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.repo.dictionary.M2Model} object.
	 */
	M2Model extractM2Model(String nodeRef);
	
	/**
	 * create nodeRef form M2Model
	 *
	 * @param model a {@link org.alfresco.repo.dictionary.M2Model} object.
	 * @return a {@link java.lang.String} object.
	 */
	String createModelNodeRef(M2Model model);


}
