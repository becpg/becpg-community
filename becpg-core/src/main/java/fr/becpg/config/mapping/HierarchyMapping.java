package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;

/**
 * Class that represent the mapping for importing a hierarchy
 * 
 * <column id="lkvValue2" type="Hierarchy" attribute="bcpg:lkvValue"
 * parentLevel="lkvValue1" parentLevelAttribute="bcpg:parentLevel" />
 * 
 * @author matthieu
 * 
 */
public class HierarchyMapping extends AbstractAttributeMapping {

	private String parentLevelColumn;

	private ClassAttributeDefinition parentLevelAttribute;

	public String getParentLevelColumn() {
		return parentLevelColumn;
	}

	public void setParentLevelColumn(String parentLevelColumn) {
		this.parentLevelColumn = parentLevelColumn;
	}

	public ClassAttributeDefinition getParentLevelAttribute() {
		return parentLevelAttribute;
	}

	public void setParentLevelAttribute(ClassAttributeDefinition parentLevelAttribute) {
		this.parentLevelAttribute = parentLevelAttribute;
	}

	public HierarchyMapping(String id, ClassAttributeDefinition attribute, String parentLevelColumn, ClassAttributeDefinition parentLevelAttribute) {
		super(id, attribute);
		this.parentLevelColumn = parentLevelColumn;
		this.parentLevelAttribute = parentLevelAttribute;
	}
}
