/*
 * 
 */
package fr.becpg.repo.importer;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

import fr.becpg.config.mapping.AbstractAttributeMapping;


/**
 * Class used to store the mapping of a type.
 *
 * @author querephi
 * @version $Id: $Id
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
	
	private Map<QName, String> paths = new HashMap<>();
	
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

	

	/**
	 * <p>Getter for the field <code>paths</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<QName, String> getPaths() {
		return paths;
	}

	/**
	 * <p>Setter for the field <code>paths</code>.</p>
	 *
	 * @param paths a {@link java.util.Map} object.
	 */
	public void setPaths(Map<QName, String> paths) {
		this.paths = paths;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ClassMapping [type=" + type + ", nodeColumnKeys=" + nodeColumnKeys + ", dataListColumnKeys="
				+ dataListColumnKeys + ", columns=" + columns + ", paths=" + paths + "]";
	}

	
	
	
}
