/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;

public class ReportServerSearchContext {

	private PropertyFormats propertyFormats;
	
	private List<AttributeMapping> attributeColumns = new ArrayList<>();
	
	private List<CharacteristicMapping> characteristicsColumns = new ArrayList<>();
	
	private List<FileMapping> fileColumns = new ArrayList<>();
	
	public PropertyFormats getPropertyFormats() {
		return propertyFormats;
	}

	public void setPropertyFormats(PropertyFormats propertyFormats) {
		this.propertyFormats = propertyFormats;
	}

	public List<AttributeMapping> getAttributeColumns() {
		return attributeColumns;
	}

	public void setAttributeColumns(List<AttributeMapping> attributeColumns) {
		this.attributeColumns = attributeColumns;
	}

	public List<CharacteristicMapping> getCharacteristicsColumns() {
		return characteristicsColumns;
	}

	public void setCharacteristicsColumns(
			List<CharacteristicMapping> characteristicsColumns) {
		this.characteristicsColumns = characteristicsColumns;
	}

	public List<FileMapping> getFileColumns() {
		return fileColumns;
	}

	public void setFileColumns(List<FileMapping> fileColumns) {
		this.fileColumns = fileColumns;
	}
	
	public ReportServerSearchContext(){
		propertyFormats = new PropertyFormats(false);
	}
}
