package fr.becpg.repo.importer.annotation;

public class Hierarchy extends Annotation {

	private String path;
	private String parentLevelColumn;
	private String parentLevelAttribute;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getParentLevelColumn() {
		return parentLevelColumn;
	}

	public void setParentLevelColumn(String parentLevelColumn) {
		this.parentLevelColumn = parentLevelColumn;
	}

	public String getParentLevelAttribute() {
		return parentLevelAttribute;
	}

	public void setParentLevelAttribute(String parentLevelAttribute) {
		this.parentLevelAttribute = parentLevelAttribute;
	}

	@Override
	public String toString() {
		return "Hierarchy [path=" + path + ", parentLevelColumn=" + parentLevelColumn + ", parentLevelAttribute="
				+ parentLevelAttribute + ", id=" + id + ", attribute=" + attribute + ", targetClass=" + targetClass
				+ ", targetKey=" + targetKey + "]";
	}
	
	

}
