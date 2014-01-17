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
package fr.becpg.repo.report.entity;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

public class EntityReportData {
	
	private Element xmlDataSource;
	
	private Map<String, byte[]> dataObjects = new HashMap<String, byte[]>();
	/**
	 * @return the xmlDataSource
	 */
	public Element getXmlDataSource() {
		return xmlDataSource;
	}
	/**
	 * @param xmlDataSource the xmlDataSource to set
	 */
	public void setXmlDataSource(Element xmlDataSource) {
		this.xmlDataSource = xmlDataSource;
	}
	/**
	 * @return the dataObjects
	 */
	public Map<String, byte[]> getDataObjects() {
		return dataObjects;
	}
	/**
	 * @param dataObjects the dataObjects to set
	 */
	public void setDataObjects(Map<String, byte[]> dataObjects) {
		this.dataObjects = dataObjects;
	}
	
	
}
