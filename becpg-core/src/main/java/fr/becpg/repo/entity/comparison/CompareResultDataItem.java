/*
 * 
 */
package fr.becpg.repo.entity.comparison;

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

	/** The entity list. */
	private QName entityList;
	
	/** The charact path. */
	private List<NodeRef> charactPath;
	
	/** The characteristic. */
	private NodeRef characteristic;
	
	/** The property. */
	private QName property;
	
	/** The values. */
	private String[] values;
	
	private boolean isDifferent;

	
	/**
	 * Gets the entity list.
	 *
	 * @return the entity list
	 */
	public QName getEntityList() {
		return entityList;
	}
	
	/**
	 * Sets the entity list.
	 *
	 * @param entityList the new entity list
	 */
	public void setProductList(QName entityList) {
		this.entityList = entityList;
	}
	
	/**
	 * Gets the charact path.
	 *
	 * @return the charact path
	 */
	public List<NodeRef> getCharactPath() {
		return charactPath;
	}
	
	/**
	 * Sets the charact path.
	 *
	 * @param charactPath the new charact path
	 */
	public void setCharactPath(List<NodeRef> charactPath) {
		this.charactPath = charactPath;
	}
	
	/**
	 * Gets the characteristic.
	 *
	 * @return the characteristic
	 */
	public NodeRef getCharacteristic() {
		return characteristic;
	}
	
	/**
	 * Sets the characteristic.
	 *
	 * @param characteristic the new characteristic
	 */
	public void setCharacteristic(NodeRef characteristic) {
		this.characteristic = characteristic;
	}
	
	/**
	 * Gets the property.
	 *
	 * @return the property
	 */
	public QName getProperty() {
		return property;
	}
	
	/**
	 * Sets the property.
	 *
	 * @param property the new property
	 */
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

	/**
	 * Instantiates a new compare result data item.
	 *
	 * @param entityList the entity list
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param property the property
	 * @param values the values
	 */
	public CompareResultDataItem(QName entityList, List<NodeRef> charactPath, NodeRef characteristic, QName property, String[] values){
		setProductList(entityList);
		setCharactPath(charactPath);
		setCharacteristic(characteristic);
		setProperty(property);
		setValues(values);
	}

	@Override
	public String toString() {
		return "CompareResultDataItem [entityList=" + entityList + ", charactPath=" + charactPath + ", characteristic="
				+ characteristic + ", property=" + property + ", values=" + values + ", isDifferent=" + isDifferent
				+ "]";
	}
	
}
