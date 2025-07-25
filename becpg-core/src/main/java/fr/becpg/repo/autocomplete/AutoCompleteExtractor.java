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

import java.util.List;

/**
 * <p>ListValueExtractor interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@FunctionalInterface
public interface AutoCompleteExtractor<T> {

	/**
	 * <p>extract.</p>
	 *
	 * @param values a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	List<AutoCompleteEntry> extract(List<T> values);
	
	/**
	 * <p>extract.</p>
	 *
	 * @param values a {@link java.util.List} object.
	 * @param characNameFormat a {@link java.lang.String} object
	 * @return a {@link java.util.List} object.
	 */
	default List<AutoCompleteEntry> extract(List<T> values, String characNameFormat) {
		return extract(values);
	}

}
