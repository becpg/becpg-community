package fr.becpg.repo.importer;

import fr.becpg.config.mapping.MappingException;

/**
 * <p>MappingLoader interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface MappingLoader {

	
	
	/**
	 * Load class mapping.
	 *
	 * @param mapping the XML element or the annotations array
	 * @param importContext the import context
	 * @return the import context
	 * @throws fr.becpg.config.mapping.MappingException if any.
	 */
	ImportContext loadClassMapping(Object mapping, ImportContext importContext) throws MappingException ;
	
	/**
	 * <p>applyTo.</p>
	 *
	 * @param mappingType a {@link fr.becpg.repo.importer.MappingType} object.
	 * @return a boolean.
	 */
	boolean applyTo(MappingType mappingType);
	
	
	
}
