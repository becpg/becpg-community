/*
 * 
 */
package fr.becpg.config.mapping;

import java.util.List;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;


// TODO: Auto-generated Javadoc
/**
 * Class that represent the mapping for importing a characteristic
 * 
 * 		<column id="produit.jpg" attribute="cm:content" path="cm:Images" type="File" />
 *				
 * @author querephi
 *
 */
public class FileMapping extends AbstractAttributeMapping {

	/** The path. */
	private List<String> path;

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public List<String> getPath() {
		return path;
	}
	
	/**
	 * Sets the path.
	 *
	 * @param path the new path
	 */
	public void setPath(List<String> path) {
		this.path = path;
	}
	
	/**
	 * Instantiates a new file mapping.
	 *
	 * @param id the id
	 * @param attribute the attribute
	 * @param path the path
	 */
	public FileMapping(String id, ClassAttributeDefinition attribute, List<String> path){
		super(id, attribute);
		this.path = path;
	}
}
