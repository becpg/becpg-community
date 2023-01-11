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

import java.util.HashMap;
import java.util.Map;

/**
 * POJO to store list value entry
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AutoCompleteEntry {


	private String value;
	private String name;
	private String cssClass;
	private Map<String,String> metadatas ;

	
	/**
	 * <p>Constructor for AutoCompleteEntry.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param name a {@link java.lang.String} object.
	 * @param cssClass a {@link java.lang.String} object.
	 */
	public AutoCompleteEntry(String value, String name, String cssClass) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = new HashMap<>();
	}
	
	/**
	 * <p>Constructor for AutoCompleteEntry.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param name a {@link java.lang.String} object.
	 * @param cssClass a {@link java.lang.String} object.
	 * @param metadatas a {@link java.util.Map} object.
	 */
	public AutoCompleteEntry(String value, String name, String cssClass, Map<String,String> metadatas) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = metadatas;
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>cssClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * <p>Setter for the field <code>cssClass</code>.</p>
	 *
	 * @param cssClass a {@link java.lang.String} object.
	 */
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	/**
	 * <p>Getter for the field <code>metadatas</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getMetadatas() {
		return metadatas;
	}

	/**
	 * <p>Setter for the field <code>metadatas</code>.</p>
	 *
	 * @param metadatas a {@link java.util.Map} object.
	 */
	public void setMetadatas(Map<String, String> metadatas) {
		this.metadatas = metadatas;
	}

	
	
	
	
}
