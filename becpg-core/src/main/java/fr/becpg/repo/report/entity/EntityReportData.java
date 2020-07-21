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
package fr.becpg.repo.report.entity;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

/**
 * <p>EntityReportData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityReportData {
	
	private Element xmlDataSource;
	
	private Map<EntityImageInfo, byte[]> dataObjects = new HashMap<>();
	/**
	 * <p>Getter for the field <code>xmlDataSource</code>.</p>
	 *
	 * @return the xmlDataSource
	 */
	public Element getXmlDataSource() {
		return xmlDataSource;
	}
	/**
	 * <p>Setter for the field <code>xmlDataSource</code>.</p>
	 *
	 * @param xmlDataSource the xmlDataSource to set
	 */
	public void setXmlDataSource(Element xmlDataSource) {
		this.xmlDataSource = xmlDataSource;
	}
	/**
	 * <p>Getter for the field <code>dataObjects</code>.</p>
	 *
	 * @return the dataObjects
	 */
	public Map<EntityImageInfo, byte[]> getDataObjects() {
		return dataObjects;
	}
	/**
	 * <p>Setter for the field <code>dataObjects</code>.</p>
	 *
	 * @param dataObjects the dataObjects to set
	 */
	public void setDataObjects(Map<EntityImageInfo, byte[]> dataObjects) {
		this.dataObjects = dataObjects;
	}
	
	
	/**
	 * <p>setParameters.</p>
	 *
	 * @param reportParameters a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 */
	public void setParameters(EntityReportParameters reportParameters) {
		reportParameters.updateDataSource(xmlDataSource);
	}
	
	
}
