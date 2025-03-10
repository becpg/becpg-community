/*
 * 
 */
package fr.becpg.config.mapping;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;



/**
 * Class that represent the mapping for importing a characteristic
 *
 *<pre>
 * {@code
 * 	<column id="produit.jpg" attribute="cm:content" path="cm:Images" type="File" />
 * }
 * </pre>
 *
 * @author querephi
 * @version $Id: $Id
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(path);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileMapping other = (FileMapping) obj;
		return Objects.equals(path, other.path);
	}
	
	
}
