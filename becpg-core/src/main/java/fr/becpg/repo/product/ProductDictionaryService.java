/*
 * 
 */
package fr.becpg.repo.product;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.SystemProductType;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductDictionaryService.
 *
 * @author querephi
 */
public interface ProductDictionaryService {


	/**
	 * Gets the display name.
	 *
	 * @param systemProductType the system product type
	 * @return the display name
	 */
	public String getDisplayName(SystemProductType systemProductType);
	
	/**
	 * Gets the folder name.
	 *
	 * @param systemProductType the system product type
	 * @return the folder name
	 */
	public String getFolderName(SystemProductType systemProductType);	

	public QName getWUsedList(NodeRef childNodeRef);
	

}
