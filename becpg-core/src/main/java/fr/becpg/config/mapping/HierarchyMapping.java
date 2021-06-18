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
package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;

/**
 * Class that represent the mapping for importing a hierarchy
 *
 * <column id="lkvValue2" type="Hierarchy" attribute="bcpg:lkvValue"
 * parentLevel="lkvValue1" parentLevelAttribute="bcpg:parentLevel" />
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HierarchyMapping extends AbstractAttributeMapping {

	private String parentLevelColumn;
	
	private String path;

	private ClassAttributeDefinition parentLevelAttribute;

	/**
	 * <p>Getter for the field <code>parentLevelColumn</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getParentLevelColumn() {
		return parentLevelColumn;
	}

	/**
	 * <p>Setter for the field <code>parentLevelColumn</code>.</p>
	 *
	 * @param parentLevelColumn a {@link java.lang.String} object.
	 */
	public void setParentLevelColumn(String parentLevelColumn) {
		this.parentLevelColumn = parentLevelColumn;
	}

	
	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public void setPath(String path) {
		this.path = path;
	}


	/**
	 * <p>Getter for the field <code>parentLevelAttribute</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	public ClassAttributeDefinition getParentLevelAttribute() {
		return parentLevelAttribute;
	}

	/**
	 * <p>Setter for the field <code>parentLevelAttribute</code>.</p>
	 *
	 * @param parentLevelAttribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	public void setParentLevelAttribute(ClassAttributeDefinition parentLevelAttribute) {
		this.parentLevelAttribute = parentLevelAttribute;
	}

	/**
	 * <p>Constructor for HierarchyMapping.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 * @param parentLevelColumn a {@link java.lang.String} object.
	 * @param path a {@link java.lang.String} object.
	 * @param parentLevelAttribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 */
	public HierarchyMapping(String id, ClassAttributeDefinition attribute, String parentLevelColumn, String path,  ClassAttributeDefinition parentLevelAttribute) {
		super(id, attribute);
		this.parentLevelColumn = parentLevelColumn;
		this.parentLevelAttribute = parentLevelAttribute;
		this.path = path;
	}

}
