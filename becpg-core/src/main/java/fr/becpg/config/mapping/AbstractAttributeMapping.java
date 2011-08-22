/*
 * 
 */
package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;

// TODO: Auto-generated Javadoc
/**
 * Class that represent the mapping for importing a property or an association (node or characteristic or file)
 * 
 *
 * @author querephi
 */
public abstract class AbstractAttributeMapping{

	/** The id. */
	private String id;
	
	/** The attribute. */
	private ClassAttributeDefinition attribute;	

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}	
	
	/**
	 * Gets the attribute.
	 *
	 * @return the attribute
	 */
	public ClassAttributeDefinition getAttribute() {
		return attribute;
	}
	
	/**
	 * Sets the attribute.
	 *
	 * @param attribute the new attribute
	 */
	public void setAttribute(ClassAttributeDefinition attribute) {
		this.attribute = attribute;
	}
	
	/**
	 * Instantiates a new attribute mapping.
	 *
	 * @param id the id
	 * @param attribute the attribute
	 */
	public AbstractAttributeMapping(String id, ClassAttributeDefinition attribute){
		this.id = id;
		this.attribute = attribute;
	}
}
