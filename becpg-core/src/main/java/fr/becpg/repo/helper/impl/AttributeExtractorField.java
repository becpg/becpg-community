package fr.becpg.repo.helper.impl;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * <p>AttributeExtractorField class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AttributeExtractorField {
	private String fieldName;
	private String fieldLabel;

	private int pos;
	private final int len;
	private int pos2;
	private final int len2;
	private String delim = "|";

	/**
	 * <p>Constructor for AttributeExtractorField.</p>
	 *
	 * @param fieldName a {@link java.lang.String} object
	 * @param fieldLabel a {@link java.lang.String} object
	 */
	public AttributeExtractorField(String fieldName, String fieldLabel) {
		super();
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		if(fieldLabel!=null && fieldLabel.startsWith(delim)) {
			this.fieldLabel = "nested"+fieldLabel;
		}
		this.len = this.fieldName != null ? this.fieldName.length() : 0;
		this.len2 = this.fieldLabel != null ? this.fieldLabel.length() : 0;
		this.pos = 0;
		this.pos2 = 0;
	}

	/**
	 * <p>Getter for the field <code>fieldName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * <p>Getter for the field <code>fieldLabel</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFieldLabel() {
		return fieldLabel;
	}

	/**
	 * <p>isNested.</p>
	 *
	 * @return a boolean
	 */
	public boolean isNested() {
		return fieldName.contains("|");
	}

	/**
	 * <p>nextToken.</p>
	 *
	 * @return a {@link fr.becpg.repo.helper.impl.AttributeExtractorField} object
	 */
	public AttributeExtractorField nextToken() {

		String id = null;
		String label = null;

		if ((pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) {
			while ((++pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) ;
		}
		if (pos < len) {
			int start = pos;
			while ((++pos < len) && (delim.indexOf(fieldName.charAt(pos)) < 0)) ;

			id = fieldName.substring(start, pos);
		}

		if (id == null) {
			throw new NoSuchElementException();
		}

		if ((pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) >= 0)) {
			while ((++pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) >= 0)) ;
		}
		if (pos2 < len2) {
			int start = pos2;
			while ((++pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) < 0));

			label = fieldLabel.substring(start, pos2);
		}

		return new AttributeExtractorField(id, "nested".equals(label) ? null : label);
	}

	/**
	 * <p>hasMoreTokens.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasMoreTokens() {

		while ((pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) {
			pos++;
		}

		return pos < len;
	}
	
	/**
	 * <p>resetPositions.</p>
	 */
	public void resetPositions() {
		pos = 0;
		pos2 = 0;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(fieldLabel, fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		AttributeExtractorField other = (AttributeExtractorField) obj;
		return Objects.equals(fieldLabel, other.fieldLabel) && Objects.equals(fieldName, other.fieldName);
	}

	/**
	 * <p>prefixed.</p>
	 *
	 * @param prefix a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.helper.impl.AttributeExtractorField} object
	 */
	public AttributeExtractorField prefixed(String prefix) {

		return new AttributeExtractorField(prefix + fieldName.replaceFirst(":", "_"), fieldLabel);

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AttributeExtractorField [fieldName=" + fieldName + ", fieldLabel=" + fieldLabel + "]";
	}
	
	

}
