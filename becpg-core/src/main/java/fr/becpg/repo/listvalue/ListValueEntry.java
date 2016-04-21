/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.listvalue;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO to store list value entry 
 * @author matthieu
 *
 */
public class ListValueEntry {


	private String value;
	private String name;
	private String cssClass;
	private Map<String,String> metadatas ;

	
	public ListValueEntry(String value, String name, String cssClass) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = new HashMap<>();
	}
	
	public ListValueEntry(String value, String name, String cssClass, Map<String,String> metadatas) {
		super();
		this.value = value;
		this.name = name;
		this.cssClass = cssClass;
		this.metadatas = metadatas;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public Map<String, String> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, String> metadatas) {
		this.metadatas = metadatas;
	}

	
	
	
	
}
