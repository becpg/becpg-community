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

import java.util.Objects;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;

/**
 * Class that represent the mapping for importing a hierarchy
 *
 *<pre>
 * {@code
 * <column id="lkvValue2" type="Hierarchy" attribute="bcpg:lkvValue"
 * parentLevel="lkvValue1" parentLevelAttribute="bcpg:parentLevel" />
 * }
 * </pre>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class HierarchyMapping extends AbstractAttributeMapping {

	private String parentLevelColumn;
	
	private String path;
	
	private String key;


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
	 * <p>Getter for the field <code>key</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getKey() {
		return key;
	}

	/**
	 * <p>Setter for the field <code>key</code>.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 */
	public void setKey(String key) {
		this.key = key;
	}

	
	/**
	 * <p>Constructor for HierarchyMapping.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object.
	 * @param parentLevelColumn a {@link java.lang.String} object.
	 * @param path a {@link java.lang.String} object.
	 * @param key a {@link java.lang.String} object
	 */
	public HierarchyMapping(String id, ClassAttributeDefinition attribute, String parentLevelColumn, String path,  String key) {
		super(id, attribute);
		this.parentLevelColumn = parentLevelColumn;
		this.key = key;
		this.path = path;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(key, parentLevelColumn, path);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HierarchyMapping other = (HierarchyMapping) obj;
		return Objects.equals(key, other.key) && Objects.equals(parentLevelColumn, other.parentLevelColumn) && Objects.equals(path, other.path);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "HierarchyMapping [parentLevelColumn=" + parentLevelColumn + ", path=" + path + ", key=" + key + ", id=" + id + "]";
	}
	
	

}
