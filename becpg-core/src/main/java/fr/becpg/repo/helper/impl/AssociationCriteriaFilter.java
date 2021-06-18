package fr.becpg.repo.helper.impl;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>DataListSearchFilterField class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class AssociationCriteriaFilter {

	private static Log logger = LogFactory.getLog(AssociationCriteriaFilter.class);

	private Double minRange;
	private Double maxRange;
	private String stringValue;
	private String booleanValue;
	private QName attributeQname;

	public AssociationCriteriaFilter(QName attributeQName, DataTypeDefinition dataType, String criteriaValue, boolean isRange) {
		this.attributeQname = attributeQName;
		if (DataTypeDefinition.BOOLEAN.equals(dataType.getName())) {
			booleanValue = criteriaValue;
		} else if (DataTypeDefinition.DOUBLE.equals(dataType.getName())) {
			try {
				if (isRange) {

					String[] splitted = criteriaValue.split("\\|");

					if (splitted.length >= 1) {
						if (splitted[0].length() > 0) {
							this.minRange = Double.valueOf(splitted[0]);
						}

						if (splitted.length == 2 && splitted[1].length() > 0) {
							this.maxRange = Double.valueOf(splitted[1]);
						}
					}
				} else {
					this.minRange = Double.valueOf(criteriaValue);
				}
			} catch (NumberFormatException e) {
				logger.debug("Cannot parse search criteria: " + criteriaValue);
			}
		} else {
			stringValue = criteriaValue;
		}

	}

	public QName getAttributeQname() {
		return attributeQname;
	}

	public Double getMinRange() {
		return minRange;
	}

	public Double getMaxRange() {
		return maxRange;
	}

	public String getStringValue() {
		return stringValue;
	}

	public String getBooleanValue() {
		return booleanValue;
	}

	public boolean hasValue() {
		return (booleanValue != null) || (stringValue != null) || (minRange != null) || (maxRange != null);
	}
}
