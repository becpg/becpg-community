/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.Arrays;

import org.alfresco.service.namespace.QName;


/**
 * The Class CompareResultDataItem.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class CompareResultDataItem {

	private QName entityList;
	
	private QName property;
	
	private String[] values;
	
	private String pivotKey;
	
	private String charactName;
	
	private boolean isDifferent = false;

	
	/**
	 * <p>Getter for the field <code>entityList</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getEntityList() {
		return entityList;
	}
	
	/**
	 * <p>setProductList.</p>
	 *
	 * @param entityList a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setProductList(QName entityList) {
		this.entityList = entityList;
	}
	
	/**
	 * <p>Getter for the field <code>property</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getProperty() {
		return property;
	}
	
	/**
	 * <p>Setter for the field <code>property</code>.</p>
	 *
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setProperty(QName property) {
		this.property = property;
	}
	
	/**
	 * <p>Getter for the field <code>values</code>.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getValues() {
		return values;
	}

	/**
	 * <p>Setter for the field <code>values</code>.</p>
	 *
	 * @param values an array of {@link java.lang.String} objects.
	 */
	public void setValues(String[] values) {
		this.values = values; 
	}

	/**
	 * <p>isDifferent.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDifferent() {
		return isDifferent; 
	}

	/**
	 * <p>setDifferent.</p>
	 *
	 * @param isDifferent a boolean.
	 */
	public void setDifferent(boolean isDifferent) {
		this.isDifferent = isDifferent;
	}

	/**
	 * <p>Getter for the field <code>pivotKey</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPivotKey() {
		return pivotKey;
	}

	/**
	 * <p>Setter for the field <code>pivotKey</code>.</p>
	 *
	 * @param pivotKey a {@link java.lang.String} object.
	 */
	public void setPivotKey(String pivotKey) {
		this.pivotKey = pivotKey;
	}
	
	

	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCharactName() {
		return charactName;
	}

	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link java.lang.String} object.
	 */
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	/**
	 * <p>Constructor for CompareResultDataItem.</p>
	 *
	 * @param entityList a {@link org.alfresco.service.namespace.QName} object.
	 * @param charactName a {@link java.lang.String} object.
	 * @param pivotKey a {@link java.lang.String} object.
	 * @param property a {@link org.alfresco.service.namespace.QName} object.
	 * @param values an array of {@link java.lang.String} objects.
	 */
	public CompareResultDataItem(QName entityList,String charactName,  String pivotKey, QName property, String[] values){
		setProductList(entityList);
		setPivotKey(pivotKey);
		setCharactName(charactName);
		setProperty(property);
		setValues(values);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entityList == null) ? 0 : entityList.hashCode());
		result = prime * result + (isDifferent ? 1231 : 1237);
		result = prime * result + ((pivotKey == null) ? 0 : pivotKey.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompareResultDataItem other = (CompareResultDataItem) obj;
		if (entityList == null) {
			if (other.entityList != null)
				return false;
		} else if (!entityList.equals(other.entityList))
			return false;
		if (isDifferent != other.isDifferent)
			return false;
		if (pivotKey == null) {
			if (other.pivotKey != null)
				return false;
		} else if (!pivotKey.equals(other.pivotKey))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	
	
	
}
