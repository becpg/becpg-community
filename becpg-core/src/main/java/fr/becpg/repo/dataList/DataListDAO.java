package fr.becpg.repo.dataList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * DAO interface to manage dataList
 * @author querephi
 *
 */
public interface DataListDAO {		
	
	/**
	 * Get the data list container.
	 *
	 * @param nodeRef
	 * @return the list container
	 */
	public NodeRef getListContainer(NodeRef nodeRef);
	
	/**
	 * Get the data list NodeRef.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param dataListQName : type of the data list
	 * @return the list
	 */
	public NodeRef getList(NodeRef listContainerNodeRef, QName dataListQName);	
		
	/**
	 * Create the data list container.
	 *
	 * @param dataNodeRef the data node ref
	 * @return the node ref
	 */
	public NodeRef createListContainer(NodeRef dataNodeRef);
	
	/**
	 * Create the data list NodeRef.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param dataListQName : type of the data list
	 * @return the node ref
	 */
	public NodeRef createList(NodeRef listContainerNodeRef, QName dataListQName);
}
