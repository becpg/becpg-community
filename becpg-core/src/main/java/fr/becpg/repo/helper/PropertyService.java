package fr.becpg.repo.helper;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;

import fr.becpg.config.format.PropertyFormats;

/**
 * Helper used to manage a property
 * @author querephi
 *
 */
public interface PropertyService {

	/**
	 * Get the property value formated for UI (share, comparison, etc...)
	 * @param propertyDef
	 * @param value
	 * @param systemFormat use the locale of the system
	 * @return
	 */
	public String getStringValue(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);
	
}
