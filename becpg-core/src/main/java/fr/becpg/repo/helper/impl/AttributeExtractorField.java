package fr.becpg.repo.helper.impl;

import java.util.NoSuchElementException;
import java.util.Objects;

public class AttributeExtractorField {
	private String fieldName;
	private String fieldLabel;

	private int pos;
	private final int len;
	private int pos2;
	private final int len2;
	private String delim = "|";

	public AttributeExtractorField(String fieldName, String fieldLabel) {
		super();
		this.fieldName = fieldName;
		this.fieldLabel = fieldLabel;
		this.len = fieldName != null ? fieldName.length() : 0;
		this.len2 = fieldLabel != null ? fieldLabel.length() : 0;
		this.pos = 0;
		this.pos2 = 0;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public boolean isNested() {
		return fieldName.contains("|");
	}

	public AttributeExtractorField nextToken() {

		String id = null;
		String label = null;

		if ((pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) {
			while ((++pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) {

			}
		}
		if (pos < len) {
			int start = pos;
			while ((++pos < len) && (delim.indexOf(fieldName.charAt(pos)) < 0)) {

			}

			id = fieldName.substring(start, pos);
		}

		if (id == null) {
			throw new NoSuchElementException();
		}

		if ((pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) >= 0)) {
			while ((++pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) >= 0)) {

			}
		}
		if (pos2 < len2) {
			int start = pos2;
			while ((++pos2 < len2) && (delim.indexOf(fieldLabel.charAt(pos2)) < 0)) {

			}

			label = fieldLabel.substring(start, pos2);
		}

		return new AttributeExtractorField(id, label);
	}

	public boolean hasMoreTokens() {

		while ((pos < len) && (delim.indexOf(fieldName.charAt(pos)) >= 0)) {
			pos++;
		}

		return pos < len;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldLabel, fieldName);
	}

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

	public AttributeExtractorField prefixed(String prefix) {

		return new AttributeExtractorField(prefix + fieldName.replaceFirst(":", "_"), fieldLabel);

	}

}