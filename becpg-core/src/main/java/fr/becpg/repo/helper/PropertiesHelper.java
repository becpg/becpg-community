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
package fr.becpg.repo.helper;

public class PropertiesHelper {

	/**
	 * remove invalid characters.
	 *
	 * @param name the name
	 * @return the string
	 */
	public static String cleanName(String name) {
		/*(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$) */
		return name!=null? name.replaceAll("([\"*\\><?/:|])", "-").trim(): null;
	}	
	
	public static String cleanFolderName(String name) {
		String ret = cleanName(name);
		return ret!=null ? ret.replaceAll("\\.","-"): null;
	}	
	
	
	/**
	 * remove invalid characters (trim).
	 *
	 * @param value the value
	 * @return the string
	 */
	public static String cleanValue(String value) {		
		return value!=null? value.trim(): null;		
	}
	
}
