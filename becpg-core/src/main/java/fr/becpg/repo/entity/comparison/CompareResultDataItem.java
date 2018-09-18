/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class CompareResultDataItem.
 *
 * @author querephi
 */
public class CompareResultDataItem {

	private QName entityList;
	
	private QName property;
	
	private String[] values;
	
	private String pivotKey;
	
	private String charactName;
	
	private boolean isDifferent = false;

	
	public QName getEntityList() {
		return entityList;
	}
	
	public void setProductList(QName entityList) {
		this.entityList = entityList;
	}
	
	public QName getProperty() {
		return property;
	}
	
	public void setProperty(QName property) {
		this.property = property;
	}
	
	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values; 
	}

	public boolean isDifferent() {
		return isDifferent; 
	}

	public void setDifferent(boolean isDifferent) {
		this.isDifferent = isDifferent;
	}

	public String getPivotKey() {
		return pivotKey;
	}

	public void setPivotKey(String pivotKey) {
		this.pivotKey = pivotKey;
	}
	
	

	public String getCharactName() {
		return charactName;
	}

	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}

	public CompareResultDataItem(QName entityList,String charactName,  String pivotKey, QName property, String[] values){
		setProductList(entityList);
		setPivotKey(pivotKey);
		setCharactName(charactName);
		setProperty(property);
		setValues(values);
	}

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
