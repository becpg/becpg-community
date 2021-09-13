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
package fr.becpg.config.format;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <p>CSVPropertyFormats class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CSVPropertyFormats extends PropertyFormats {

	/**
	 * <p>Constructor for CSVPropertyFormats.</p>
	 *
	 * @param useDefaultLocal a boolean.
	 */
	public CSVPropertyFormats(boolean useDefaultLocal) {
		super(useDefaultLocal);

		if (Locale.FRENCH.equals(I18NUtil.getContentLocaleLang())) {
			dateFormat = "dd/MM/yyyy";
			datetimeFormat = "dd/MM/yyyy HH:mm:ss";
		} else {
			dateFormat = "MM/dd/yyyy";
			datetimeFormat = "MM/dd/yyyy HH:mm:ss";
		}
		
		
	}

}
