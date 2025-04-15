package fr.becpg.repo.helper.impl;

import org.alfresco.service.namespace.QName;

/**
 * <p>AssociationCriteriaFilter class.</p>
 *
 * @author valentin, matthieu
 * @version $Id: $Id
 */
public class AssociationCriteriaFilter {

	private String fromRange;
	private String toRange;
	private String value;
	private QName attributeQname;
	private AssociationCriteriaFilterMode mode = AssociationCriteriaFilterMode.EQUALS;
	private boolean isEntityFilter = false;
	
	/**
	 * Types of filter modes for association criteria.
	 */
	public enum AssociationCriteriaFilterMode {
		/**
		 * Represents a range filter mode.
		 */
		RANGE, 
		/**
		 * Represents an equals filter mode.
		 */
		EQUALS, 
		/**
		 * Represents a not equals filter mode.
		 */
		NOT_EQUALS, 
	}
	
	/**
	 * <p>Constructor for AssociationCriteriaFilter.</p>
	 *
	 * @param attributeQName a {@link org.alfresco.service.namespace.QName} object
	 * @param criteriaValue a {@link java.lang.String} object
	 */
	public AssociationCriteriaFilter(QName attributeQName, String criteriaValue) {
		this(attributeQName,criteriaValue,AssociationCriteriaFilterMode.EQUALS);
	}

	/**
	 * <p>setEntityFilter.</p>
	 *
	 * @param isEntityFilter a boolean
	 */
	public void setEntityFilter(boolean isEntityFilter) {
		this.isEntityFilter = isEntityFilter;
	}
	
	/**
	 * <p>isEntityFilter.</p>
	 *
	 * @return a boolean
	 */
	public boolean isEntityFilter() {
		return isEntityFilter;
	}
	
	/**
	 * <p>Constructor for AssociationCriteriaFilter.</p>
	 *
	 * @param attributeQName a {@link org.alfresco.service.namespace.QName} object
	 * @param criteriaValue a {@link java.lang.String} object
	 * @param mode a {@link fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode} object
	 */
	public AssociationCriteriaFilter(QName attributeQName, String criteriaValue, AssociationCriteriaFilterMode mode) {
		this.attributeQname = attributeQName;
		this.mode  = mode;
		if (AssociationCriteriaFilterMode.RANGE.equals(mode)) {
			String[] splitted = criteriaValue.split("\\|");
			
			if(splitted.length > 0 && !splitted[0].isBlank()) {
				fromRange = splitted[0];
			}
			if(splitted.length>1 && !splitted[1].isBlank()) {
				toRange = splitted[1];
			}
		} else {
			this.value = criteriaValue;
		}
	}
	
	/**
	 * Sets the value for the filter.
	 *
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Sets the mode for the filter.
	 *
	 * @param mode the mode to set
	 */
	public void setMode(AssociationCriteriaFilterMode mode) {
		this.mode = mode;
	}
	
	/**
	 * Sets the from range value for the filter.
	 *
	 * @param fromRange the from range value to set
	 */
	public void setFromRange(String fromRange) {
		this.fromRange = fromRange;
	}
	
	/**
	 * Sets the to range value for the filter.
	 *
	 * @param toRange the to range value to set
	 */
	public void setToRange(String toRange) {
		this.toRange = toRange;
	}

	/**
	 * <p>Getter for the field <code>mode</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.helper.impl.AssociationCriteriaFilter.AssociationCriteriaFilterMode} object
	 */
	public AssociationCriteriaFilterMode getMode() {
		return mode;
	}
	
	/**
	 * <p>Getter for the field <code>attributeQname</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object
	 */
	public QName getAttributeQname() {
		return attributeQname;
	}

	/**
	 * <p>Getter for the field <code>fromRange</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFromRange() {
		return fromRange;
	}

	/**
	 * <p>Getter for the field <code>toRange</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getToRange() {
		return toRange;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <p>hasValue.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasValue() {
		return (value != null) || (fromRange != null && !isMinMax(fromRange)) || (toRange != null && !isMinMax(toRange)) ;
	}

	/**
	 * <p>isMinMax.</p>
	 *
	 * @param range a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean isMinMax(String range) {
		return "MIN".equalsIgnoreCase(range)  || "MAX".equalsIgnoreCase(range);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "AssociationCriteriaFilter [fromRange=" + fromRange + ", toRange=" + toRange + ", value=" + value + ", attributeQname="
				+ attributeQname + "]";
	}
}
