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
package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>ListValuePlugin interface.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface AutoCompletePlugin {


	/**
	 * <p>getHandleSourceTypes.</p>
	 *
	 * @return sourceType that the plugin handle
	 */
	String[] getHandleSourceTypes();

	/**
	 * <p>suggest.</p>
	 *
	 * @param sourceType a {@link java.lang.String} object.
	 * @param query a {@link java.lang.String} object.
	 * @param pageNum a {@link java.lang.Integer} object.
	 * @param pageSize a {@link java.lang.Integer} object.
	 * @param props a {@link java.util.Map} object.
	 * @return Suggested values page
	 */
	AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props);

	
}
