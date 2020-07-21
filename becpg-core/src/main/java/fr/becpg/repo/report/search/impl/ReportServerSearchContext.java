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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.search.impl;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;

/**
 * <p>ReportServerSearchContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportServerSearchContext {
	
	private List<AttributeMapping> attributeColumns = new ArrayList<>();
	
	private List<CharacteristicMapping> characteristicsColumns = new ArrayList<>();
	
	private List<FileMapping> fileColumns = new ArrayList<>();
	
	/**
	 * <p>Getter for the field <code>attributeColumns</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<AttributeMapping> getAttributeColumns() {
		return attributeColumns;
	}

	/**
	 * <p>Setter for the field <code>attributeColumns</code>.</p>
	 *
	 * @param attributeColumns a {@link java.util.List} object.
	 */
	public void setAttributeColumns(List<AttributeMapping> attributeColumns) {
		this.attributeColumns = attributeColumns;
	}

	/**
	 * <p>Getter for the field <code>characteristicsColumns</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<CharacteristicMapping> getCharacteristicsColumns() {
		return characteristicsColumns;
	}

	/**
	 * <p>Setter for the field <code>characteristicsColumns</code>.</p>
	 *
	 * @param characteristicsColumns a {@link java.util.List} object.
	 */
	public void setCharacteristicsColumns(
			List<CharacteristicMapping> characteristicsColumns) {
		this.characteristicsColumns = characteristicsColumns;
	}

	/**
	 * <p>Getter for the field <code>fileColumns</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<FileMapping> getFileColumns() {
		return fileColumns;
	}

	/**
	 * <p>Setter for the field <code>fileColumns</code>.</p>
	 *
	 * @param fileColumns a {@link java.util.List} object.
	 */
	public void setFileColumns(List<FileMapping> fileColumns) {
		this.fileColumns = fileColumns;
	}
	
}
