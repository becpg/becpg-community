/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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

import java.util.regex.Pattern;

import org.alfresco.service.namespace.QName;

/**
 * <p>PropertiesHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PropertiesHelper {


	private PropertiesHelper() {
		//Do Nothing
	}
	
	/*(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$)*/
	
	/** Constant <code>namePattern</code> */
	public static final Pattern namePattern =  Pattern.compile("(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)");

	
	/**
	 * <p>testName.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean testName(String name) {
		
		return !(namePattern.matcher(name).find());
	}
	
	
	
	/**
	 * remove invalid characters.
	 *
	 * @param name the name
	 * @return the string
	 */
	public static String cleanName(String name) {
		return name!=null? name.replaceAll("([\"*\\><?/:|])", "-")
				.replaceAll("(\n)|(\")", " ")
				.replaceAll(Pattern.quote("*"), " ")
				.replaceAll("\\.$", "")
				.replace("/", "-")
				.replace("_x0020_", " ").trim(): null;
	}	
	
	/**
	 * <p>cleanFolderName.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String cleanFolderName(String name) {
		
		//Case name is a qName
		if(name!=null && QName.splitPrefixedQName(name).length>1){
			name = QName.splitPrefixedQName(name)[1];
		}
		
		return cleanName(name);
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
