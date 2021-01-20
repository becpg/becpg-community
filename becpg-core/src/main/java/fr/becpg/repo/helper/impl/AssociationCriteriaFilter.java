package fr.becpg.repo.helper.impl;

import java.util.Map;

import org.alfresco.service.namespace.QName;

import fr.becpg.repo.search.impl.DataListSearchFilterField;

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
	
	private boolean isProp;
	private DataListSearchFilterField propFilter;
	private Map<String, String> criteria;
	
	public QName getAttributeQname() {
		return attributeQname;
	}
	public void setAttributeQname(QName attributeQname) {
		this.attributeQname = attributeQname;
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
	
	private void buildCriteriaPropFilter(DataListSearchFilterField propFilter) {
		
		String criteriaValue = propFilter.getValue();
		
		if (criteriaValue.equals("true") || criteriaValue.equals("false")) {
			this.booleanValue = criteriaValue;
		} else {
			this.stringValue = criteriaValue;
		}
	}
	
	private void buildCriteriaRangeFilter(Map<String, String> criteria, DataListSearchFilterField propFilter) {
		
		String criteriaValue = criteria.get(propFilter.getHtmlId());

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
	
	private void buildCriteriaStringFilter(Map<String, String> criteria, DataListSearchFilterField propFilter) {
		
		String criteriaValue = criteria.get(propFilter.getHtmlId());
		
		this.stringValue = criteriaValue;
	}
	
	public AssociationCriteriaFilter isProp() {
		this.isProp = true;
		return this;
	}
	
	public void build() {
		
		setAttributeQname(propFilter.getAttributeQname());
		
		if (isProp) {
			buildCriteriaPropFilter(propFilter);
		} else if (propFilter.getHtmlId().contains("-range")) {
			buildCriteriaRangeFilter(criteria, propFilter);
		} else {
			buildCriteriaStringFilter(criteria, propFilter);
		}
	}
	
	public AssociationCriteriaFilter propFilter(DataListSearchFilterField propFilter) {
		this.propFilter = propFilter;
		return this;
	}
	
	public AssociationCriteriaFilter criteria(Map<String, String> criteria) {
		this.criteria = criteria;
		return this;
	}

}
