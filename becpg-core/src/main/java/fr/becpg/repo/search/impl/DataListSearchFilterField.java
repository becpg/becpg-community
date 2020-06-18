package fr.becpg.repo.search.impl;

import org.alfresco.service.namespace.QName;

public class DataListSearchFilterField {

	private String operator = "or";
	private String value;
	private String htmlId;
	private QName attributeQname;
	
	
	public String getOperator() {
		return operator;
	}
	public String getValue() {
		return value;
	}
	public String getHtmlId() {
		return htmlId;
	}
	public QName getAttributeQname() {
		return attributeQname;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setHtmlId(String htmlId) {
		this.htmlId = htmlId;
	}
	public void setAttributeQname(QName attributeQname) {
		this.attributeQname = attributeQname;
	}
	
	

}
