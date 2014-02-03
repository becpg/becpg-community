/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


// TODO: Auto-generated Javadoc
/**
 * The Class StructCompareResultDataItem.
 *
 * @author querephi
 */
public class StructCompareResultDataItem {

	/** The entity list. */
	private QName entityList;	
	
	/** The depth level. */
	private int depthLevel;
	
	/** The operator. */
	private StructCompareOperator operator;
	
	private QName pivotProperty;
	
	/** The characteristic1. */
	private NodeRef characteristic1;
	
	/** The characteristic2. */
	private NodeRef characteristic2;
	
	/** The properties1. */
	private Map<QName, String> properties1;
	
	/** The properties2. */
	private Map<QName, String> properties2;
		
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
	public void setEntityList(QName entityList) {
		this.entityList = entityList;
	}
	
	/**
	 * Gets the depth level.
	 *
	 * @return the depth level
	 */
	public int getDepthLevel() {
		return depthLevel;
	}
	
	/**
	 * Sets the depth level.
	 *
	 * @param depthLevel the new depth level
	 */
	public void setDepthLevel(int depthLevel) {
		this.depthLevel = depthLevel;
	}
	
	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	public StructCompareOperator getOperator() {
		return operator;
	}
	
	public QName getPivotProperty() {
		return pivotProperty;
	}

	public void setPivotProperty(QName pivotProperty) {
		this.pivotProperty = pivotProperty;
	}

	/**
	 * Sets the operator.
	 *
	 * @param operator the new operator
	 */
	public void setOperator(StructCompareOperator operator) {
		this.operator = operator;
	}
	
	/**
	 * Gets the characteristic1.
	 *
	 * @return the characteristic1
	 */
	public NodeRef getCharacteristic1() {
		return characteristic1;
	}
	
	/**
	 * Sets the characteristic1.
	 *
	 * @param characteristic1 the new characteristic1
	 */
	public void setCharacteristic1(NodeRef characteristic1) {
		this.characteristic1 = characteristic1;
	}
	
	/**
	 * Gets the characteristic2.
	 *
	 * @return the characteristic2
	 */
	public NodeRef getCharacteristic2() {
		return characteristic2;
	}
	
	/**
	 * Sets the characteristic2.
	 *
	 * @param characteristic2 the new characteristic2
	 */
	public void setCharacteristic2(NodeRef characteristic2) {
		this.characteristic2 = characteristic2;
	}
	
	/**
	 * Gets the properties1.
	 *
	 * @return the properties1
	 */
	public Map<QName, String> getProperties1() {
		return properties1;
	}
	
	/**
	 * Sets the properties1.
	 *
	 * @param properties1 the properties1
	 */
	public void setProperties1(Map<QName, String> properties1) {
		this.properties1 = properties1;
	}
	
	/**
	 * Gets the properties2.
	 *
	 * @return the properties2
	 */
	public Map<QName, String> getProperties2() {
		return properties2;
	}
	
	/**
	 * Sets the properties2.
	 *
	 * @param properties2 the properties2
	 */
	public void setProperties2(Map<QName, String> properties2) {
		this.properties2 = properties2;
	}
	
	/**
	 * Instantiates a new struct compare result data item.
	 *
	 * @param entityList the entity list
	 * @param depthLevel the depth level
	 * @param operator the operator
	 * @param characteristic1 the characteristic1
	 * @param characteristic2 the characteristic2
	 * @param properties1 the properties1
	 * @param properties2 the properties2
	 */
	public StructCompareResultDataItem(QName entityList, int depthLevel, StructCompareOperator operator, QName pivotProperty, NodeRef characteristic1, NodeRef characteristic2, Map<QName, String> properties1, Map<QName, String> properties2){
		this.setEntityList(entityList);
		this.setCharacteristic1(characteristic1);
		this.setCharacteristic2(characteristic2);
		this.setOperator(operator);
		this.setPivotProperty(pivotProperty);
		this.setDepthLevel(depthLevel);
		this.setProperties1(properties1);
		this.setProperties2(properties2);
	}
}
