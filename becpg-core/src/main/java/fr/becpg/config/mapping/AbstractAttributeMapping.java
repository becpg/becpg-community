/*
 * 
 */
package fr.becpg.config.mapping;

import java.util.Objects;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;


/**
 * Class that represent the mapping for importing a property or an association (node or characteristic or file)
 *
 * @author querephi
 * @version $Id: $Id
 */
public abstract class AbstractAttributeMapping{

	/** The id. */
	protected String id;
	
	/** The attribute. */
	protected ClassAttributeDefinition attribute;	

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
	protected AbstractAttributeMapping(String id, ClassAttributeDefinition attribute){
		this.id = id;
		this.attribute = attribute;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(attribute, id);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAttributeMapping other = (AbstractAttributeMapping) obj;
		return Objects.equals(attribute, other.attribute) && Objects.equals(id, other.id);
	}

	
	
}
