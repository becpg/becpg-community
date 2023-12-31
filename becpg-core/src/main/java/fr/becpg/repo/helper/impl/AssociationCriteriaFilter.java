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
	
	public AssociationCriteriaFilter(QName attributeQName, String criteriaValue) {
		this(attributeQName,criteriaValue,AssociationCriteriaFilterMode.EQUALS);
	}

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

	public AssociationCriteriaFilterMode getMode() {
		return mode;
	}
	
	public QName getAttributeQname() {
		return attributeQname;
	}

	public String getFromRange() {
		return fromRange;
	}

	public String getToRange() {
		return toRange;
	}

	public String getValue() {
		return value;
	}

	public boolean hasValue() {
		return (value != null) || (fromRange != null && !isMinMax(fromRange)) || (toRange != null && !isMinMax(toRange)) ;
	}

	public boolean isMinMax(String range) {
		return "MIN".equalsIgnoreCase(range)  || "MAX".equalsIgnoreCase(range);
	}

	@Override
	public String toString() {
		return "AssociationCriteriaFilter [fromRange=" + fromRange + ", toRange=" + toRange + ", value=" + value + ", attributeQname="
				+ attributeQname + "]";
	}
}
