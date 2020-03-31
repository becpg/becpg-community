package fr.becpg.repo.importer;

import fr.becpg.config.mapping.MappingException;

public interface MappingLoader {

	
	
	/**
	 * Load class mapping.
	 *
	 * @param mapping the XML element or the annotations array
	 * @param importContext the import context
	 * @return the import context
	 * @throws ImporterException the exception that can be raised
	 */
	
	ImportContext loadClassMapping(Object mapping, ImportContext importContext) throws MappingException ;
	
	boolean applyTo(MappingType mappingType);
	
	
	
}
