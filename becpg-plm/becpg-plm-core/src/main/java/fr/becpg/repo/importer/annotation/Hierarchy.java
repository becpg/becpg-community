package fr.becpg.repo.importer.annotation;

import java.util.Objects;

/**
 * <p>Hierarchy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Hierarchy extends Annotation {

	private String path;
	private String parentLevelColumn;
	private String parentLevelAttribute;

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
	 * <p>Getter for the field <code>parentLevelAttribute</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getParentLevelAttribute() {
		return parentLevelAttribute;
	}

	/**
	 * <p>Setter for the field <code>parentLevelAttribute</code>.</p>
	 *
	 * @param parentLevelAttribute a {@link java.lang.String} object.
	 */
	public void setParentLevelAttribute(String parentLevelAttribute) {
		this.parentLevelAttribute = parentLevelAttribute;
	}

	
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(parentLevelAttribute, parentLevelColumn, path);
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
		Hierarchy other = (Hierarchy) obj;
		return Objects.equals(parentLevelAttribute, other.parentLevelAttribute) && Objects.equals(parentLevelColumn, other.parentLevelColumn)
				&& Objects.equals(path, other.path);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Hierarchy [path=" + path + ", parentLevelColumn=" + parentLevelColumn + ", parentLevelAttribute=" + parentLevelAttribute + ", id="
				+ id + ", key=" + key + "]";
	}
	
	

}
