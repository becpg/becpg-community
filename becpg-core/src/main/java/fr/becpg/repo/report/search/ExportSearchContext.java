package fr.becpg.repo.report.search;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.config.mapping.CharacteristicMapping;
import fr.becpg.config.mapping.FileMapping;

public class ExportSearchContext {

	private PropertyFormats propertyFormats;
	
	private List<AttributeMapping> attributeColumns = new ArrayList<AttributeMapping>();
	
	private List<CharacteristicMapping> characteristicsColumns = new ArrayList<CharacteristicMapping>();
	
	private List<FileMapping> fileColumns = new ArrayList<FileMapping>();
	
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
	
	public ExportSearchContext(){
		propertyFormats = new PropertyFormats(false);
	}
}
