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

import java.util.HashSet;
import java.util.Set;

import org.dom4j.Element;

/**
 * <p>EntityReportData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityReportData {
	
	private Element xmlDataSource;
	
	private Set<EntityImageInfo> images = new HashSet<>();
	
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
	
	
	public Set<EntityImageInfo> getImages() {
		return images;
	}
	
	public void setImages(Set<EntityImageInfo> images) {
		this.images = images;
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
