/*
 * 
 */
package fr.becpg.repo.product;

import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.SystemProductType;
import fr.becpg.model.SystemState;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductDictionaryService.
 *
 * @author querephi
 */
public interface ProductDictionaryService {

	/**
	 * Gets the data lists.
	 *
	 * @return the data lists
	 */
	public Collection<QName> getDataLists();
	
	/**
	 * Gets the display name.
	 *
	 * @param systemState the system state
	 * @return the display name
	 */
	public String getDisplayName(SystemState systemState);
	
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
	 * @param systemState the system state
	 * @return the folder name
	 */
	public String getFolderName(SystemState systemState);
	
	/**
	 * Gets the folder name.
	 *
	 * @param systemProductType the system product type
	 * @return the folder name
	 */
	public String getFolderName(SystemProductType systemProductType);	
	
	/**
	 * Initialize repo hierarchy.
	 *
	 * @param containerNodeRef the container node ref
	 */
	public void initializeRepoHierarchy(NodeRef containerNodeRef);
	
	public QName getWUsedList(NodeRef childNodeRef);
}
