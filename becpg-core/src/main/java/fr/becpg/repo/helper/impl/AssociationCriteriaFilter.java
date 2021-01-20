package fr.becpg.repo.helper.impl;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * <p>DataListSearchFilterField class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class AssociationCriteriaFilter {

	private Integer minRange;
	private Integer maxRange;
	private String stringValue;
	private String booleanValue;
	private QName attributeQname;

	public AssociationCriteriaFilter(QName attributeQName, DataTypeDefinition dataType, String criteriaValue, boolean isRange) {
		this.attributeQname = attributeQName;
		if (DataTypeDefinition.BOOLEAN.equals(dataType.getName())) {
			booleanValue = criteriaValue;
		} else if (DataTypeDefinition.DOUBLE.equals(dataType.getName())) {
			if (isRange) {

				String[] splitted = criteriaValue.split("\\|");

				if (splitted.length >= 1) {
					try {
						Integer min = Integer.parseInt(criteriaValue.split("\\|")[0]);
						this.minRange = min;
					} catch (NumberFormatException e) {
						// no minRange set
					}
					if (splitted.length == 2) {
						try {
							Integer max = Integer.parseInt(criteriaValue.split("\\|")[1]);
							this.maxRange = max;
						} catch (NumberFormatException e) {
							// no maxRange set
						}
					}
				}
			}
		} else {
			stringValue = criteriaValue;
		}

	}

	public QName getAttributeQname() {
		return attributeQname;
	}

	public Integer getMinRange() {
		return minRange;
	}

	public Integer getMaxRange() {
		return maxRange;
	}

	public String getStringValue() {
		return stringValue;
	}

	public String getBooleanValue() {
		return booleanValue;
	}

}
