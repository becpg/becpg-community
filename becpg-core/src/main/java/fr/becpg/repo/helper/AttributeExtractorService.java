package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.config.format.PropertyFormats;

/**
 * Helper used to manage a property
 * @author querephi
 *
 */
public interface AttributeExtractorService {

	/**
	 * Get the property value formated for UI (share, comparison, etc...)
	 * @param propertyDef
	 * @param value
	 * @param systemFormat use the locale of the system
	 * @return
	 */
	public String getStringValue(PropertyDefinition propertyDef, Serializable value, PropertyFormats propertyFormats);

	/**
	 * 
	 * @param userId
	 * @return
	 */
	public String getPersonDisplayName(String userId);

	/**
	 * 
	 * @param nodeRef
	 * @param propQname
	 * @return
	 */
	public Serializable getProperty(NodeRef nodeRef, QName propQname);

	/**
	 * 
	 * @param nodeRef
	 * @param itemType
	 * @param metadataFields
	 * @return
	 */
	public Map<String, Object> extractNodeData(NodeRef nodeRef, QName itemType, List<String> metadataFields, boolean isSearch);

	/**
	 * 
	 * @param nodeRef
	 * @return
	 */
	public String getDisplayPath(NodeRef nodeRef);

	/**
	 * 
	 * @param nodeRef
	 * @return
	 */
	public String[] getTags(NodeRef nodeRef);


	/**
	 * 
	 * @param date
	 * @return
	 */
	public String formatDate(Date date);

	/**
	 * 
	 * @param type
	 * @param nodeRef
	 * @return
	 */
	public String extractMetadata(QName type, NodeRef nodeRef);

	/**
	 * 
	 * @return
	 */
	public PropertyFormats getPropertyFormats();

	
}
