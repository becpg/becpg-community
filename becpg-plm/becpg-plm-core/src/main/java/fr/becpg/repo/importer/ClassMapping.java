/*
 * 
 */
package fr.becpg.repo.importer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.QName;

import fr.becpg.config.mapping.AbstractAttributeMapping;

// TODO: Auto-generated Javadoc
/**
 * Class used to store the mapping of a type.
 *
 * @author querephi
 */
public class ClassMapping {
	
	/** The type. */
	private QName type;	
	
	/** The node column keys. */
	private List<QName> nodeColumnKeys = new ArrayList<>();
	
	/** The data list column keys. */
	private List<QName> dataListColumnKeys = new ArrayList<>();
	
	/** The columns. */
	private List<AbstractAttributeMapping> columns = new ArrayList<>();
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public QName getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(QName type) {
		this.type = type;
	}
	
	/**
	 * Gets the node column keys.
	 *
	 * @return the node column keys
	 */
	public List<QName> getNodeColumnKeys() {
		return nodeColumnKeys;
	}
	
	/**
	 * Sets the node column keys.
	 *
	 * @param nodeColumnKeys the new node column keys
	 */
	public void setNodeColumnKeys(List<QName> nodeColumnKeys) {
		this.nodeColumnKeys = nodeColumnKeys;
	}
	
	/**
	 * Gets the data list column keys.
	 *
	 * @return the data list column keys
	 */
	public List<QName> getDataListColumnKeys() {
		return dataListColumnKeys;
	}
	
	/**
	 * Sets the data list column keys.
	 *
	 * @param dataListColumnKeys the new data list column keys
	 */
	public void setDataListColumnKeys(List<QName> dataListColumnKeys) {
		this.dataListColumnKeys = dataListColumnKeys;
	}
	
	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	public List<AbstractAttributeMapping> getColumns() {
		return columns;
	}
	
	/**
	 * Sets the columns.
	 *
	 * @param columns the new columns
	 */
	public void setColumns(List<AbstractAttributeMapping> columns) {
		this.columns = columns;
	}
}
