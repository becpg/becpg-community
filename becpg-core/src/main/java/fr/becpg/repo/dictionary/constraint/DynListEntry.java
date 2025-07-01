package fr.becpg.repo.dictionary.constraint;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;

/**
 * <p>DynListEntry class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DynListEntry {
	
	private String code;
	private MLText values;
	private List<String> groups;
	private Boolean isDeleted;
	
	
	
	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getCode() {
		return code;
	}

	/**
	 * <p>Setter for the field <code>code</code>.</p>
	 *
	 * @param code a {@link java.lang.String} object
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>values</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public MLText getValues() {
		return values;
	}

	/**
	 * <p>Setter for the field <code>values</code>.</p>
	 *
	 * @param values a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setValues(MLText values) {
		this.values = values;
	}

	/**
	 * <p>Getter for the field <code>groups</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * <p>Setter for the field <code>groups</code>.</p>
	 *
	 * @param groups a {@link java.util.List} object
	 */
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	/**
	 * <p>Getter for the field <code>isDeleted</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * <p>Setter for the field <code>isDeleted</code>.</p>
	 *
	 * @param isDeleted a {@link java.lang.Boolean} object
	 */
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(code, groups, isDeleted, values);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynListEntry other = (DynListEntry) obj;
		return Objects.equals(code, other.code) && Objects.equals(groups, other.groups) && Objects.equals(isDeleted, other.isDeleted)
				&& Objects.equals(values, other.values);
	}

	@Override
	public String toString() {
		return "DynListEntry [code=" + code + ", values=" + values + ", groups=" + groups + ", isDeleted=" + isDeleted + "]";
	}
	
	
	
	
		
}
