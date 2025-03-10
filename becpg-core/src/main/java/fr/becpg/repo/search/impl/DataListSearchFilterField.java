package fr.becpg.repo.search.impl;

import org.alfresco.service.namespace.QName;

/**
 * <p>DataListSearchFilterField class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListSearchFilterField {

	private String operator = "or";
	private String value;
	private String htmlId;
	private QName attributeQname;
	private QName sourceTypeQname;
	
	
	/**
	 * <p>Getter for the field <code>operator</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOperator() {
		return operator;
	}
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * <p>Getter for the field <code>htmlId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getHtmlId() {
		return htmlId;
	}
	/**
	 * <p>Getter for the field <code>attributeQname</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getAttributeQname() {
		return attributeQname;
	}
	/**
	 * <p>Setter for the field <code>operator</code>.</p>
	 *
	 * @param operator a {@link java.lang.String} object.
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * <p>Setter for the field <code>htmlId</code>.</p>
	 *
	 * @param htmlId a {@link java.lang.String} object.
	 */
	public void setHtmlId(String htmlId) {
		this.htmlId = htmlId;
	}
	/**
	 * <p>Setter for the field <code>attributeQname</code>.</p>
	 *
	 * @param attributeQname a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setAttributeQname(QName attributeQname) {
		this.attributeQname = attributeQname;
	}
	/**
	 * <p>Getter for the field <code>sourceTypeQname</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object
	 */
	public QName getSourceTypeQname() {
		return sourceTypeQname;
	}
	/**
	 * <p>Setter for the field <code>sourceTypeQname</code>.</p>
	 *
	 * @param sourceTypeQname a {@link org.alfresco.service.namespace.QName} object
	 */
	public void setSourceTypeQname(QName sourceTypeQname) {
		this.sourceTypeQname = sourceTypeQname;
	}
	
	

}
