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
	
	public enum AssociationCriteriaFilterMode {
		RANGE, EQUALS, NOT_EQUALS, 
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
