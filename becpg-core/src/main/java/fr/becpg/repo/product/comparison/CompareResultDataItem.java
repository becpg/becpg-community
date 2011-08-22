/*
 * 
 */
package fr.becpg.repo.product.comparison;

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

	/** The product list. */
	private QName productList;
	
	/** The charact path. */
	private List<NodeRef> charactPath;
	
	/** The characteristic. */
	private NodeRef characteristic;
	
	/** The property. */
	private QName property;
	
	/** The values. */
	private List<String> values;

	
	/**
	 * Gets the product list.
	 *
	 * @return the product list
	 */
	public QName getProductList() {
		return productList;
	}
	
	/**
	 * Sets the product list.
	 *
	 * @param productList the new product list
	 */
	public void setProductList(QName productList) {
		this.productList = productList;
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
	
	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<String> getValues() {
		return values;
	}
	
	/**
	 * Sets the values.
	 *
	 * @param values the new values
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	/**
	 * Instantiates a new compare result data item.
	 *
	 * @param productList the product list
	 * @param charactPath the charact path
	 * @param characteristic the characteristic
	 * @param property the property
	 * @param values the values
	 */
	public CompareResultDataItem(QName productList, List<NodeRef> charactPath, NodeRef characteristic, QName property, List<String> values){
		setProductList(productList);
		setCharactPath(charactPath);
		setCharacteristic(characteristic);
		setProperty(property);
		setValues(values);
	}
}
