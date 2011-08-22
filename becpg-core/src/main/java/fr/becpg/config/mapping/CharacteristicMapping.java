/*
 * 
 */
package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


// TODO: Auto-generated Javadoc
/**
 * Class that represent the mapping for importing a characteristic
 * 
 * <column id="coutMP" path="bcpg:productLists/bcpg:costList" charactQName="bcpg:costListCost" valueQName="bcpg:costListValue"
 * charactNodeRef="" charactName="Coût emballage" />.
 *
 * @author querephi
 */
public class CharacteristicMapping extends AbstractAttributeMapping{
	
	/** The data list q name. */
	private QName dataListQName;	
	
	/** The charact q name. */
	private QName charactQName;
	
	/** The charact node ref. */
	private NodeRef charactNodeRef;
		
	/**
	 * Gets the data list q name.
	 *
	 * @return the data list q name
	 */
	public QName getDataListQName() {
		return dataListQName;
	}
	
	/**
	 * Sets the data list q name.
	 *
	 * @param dataListQName the new data list q name
	 */
	public void setDataListQName(QName dataListQName) {
		this.dataListQName = dataListQName;
	}
	
	/**
	 * Gets the charact q name.
	 *
	 * @return the charact q name
	 */
	public QName getCharactQName() {
		return charactQName;
	}
	
	/**
	 * Sets the charact q name.
	 *
	 * @param charactQName the new charact q name
	 */
	public void setCharactQName(QName charactQName) {
		this.charactQName = charactQName;
	}
	
	/**
	 * Gets the charact node ref.
	 *
	 * @return the charact node ref
	 */
	public NodeRef getCharactNodeRef() {
		return charactNodeRef;
	}
	
	/**
	 * Sets the charact node ref.
	 *
	 * @param charactNodeRef the new charact node ref
	 */
	public void setCharactNodeRef(NodeRef charactNodeRef) {
		this.charactNodeRef = charactNodeRef;
	}
	
	/**
	 * Instantiates a new characteristic mapping.
	 *
	 * @param id the id
	 * @param attribute the attribute
	 * @param dataListQName the data list q name
	 * @param charactQName the charact q name
	 * @param charactNodeRef the charact node ref
	 */
	public CharacteristicMapping(String id, ClassAttributeDefinition attribute, QName dataListQName, QName charactQName, NodeRef charactNodeRef){
		super(id, attribute);
		this.dataListQName = dataListQName;
		this.charactQName = charactQName;
		this.charactNodeRef = charactNodeRef;
	}
}
