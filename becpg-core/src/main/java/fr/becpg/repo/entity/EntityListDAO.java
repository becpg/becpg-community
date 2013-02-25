package fr.becpg.repo.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface EntityListDAO {

	/**
	 * Get the data list container.
	 * 
	 * @param nodeRef
	 * @return the list container
	 */
	public NodeRef getListContainer(NodeRef nodeRef);

	/**
	 * Get dataList with specified name and type
	 * 
	 * @param listContainerNodeRef
	 * @param name
	 * @return
	 */
	public NodeRef getList(NodeRef listContainerNodeRef, String name);

	/**
	 * Create dataList with specified name and type
	 * 
	 * @param listContainerNodeRef
	 * @param name
	 * @param type
	 */
	public NodeRef createList(NodeRef listContainerNodeRef, String name, QName type);

	/**
	 * Get the data list NodeRef.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param dataListQName
	 *            : type of the data list
	 * @return the list
	 */
	public NodeRef getList(NodeRef listContainerNodeRef, QName dataListQName);

	/**
	 * Create the data list container.
	 * 
	 * @param dataNodeRef
	 *            the data node ref
	 * @return the node ref
	 */
	public NodeRef createListContainer(NodeRef dataNodeRef);

	/**
	 * Create the data list NodeRef.
	 * 
	 * @param listContainerNodeRef
	 *            the list container node ref
	 * @param dataListQName
	 *            : type of the data list
	 * @return the node ref
	 */
	public NodeRef createList(NodeRef listContainerNodeRef, QName dataListQName);

	/**
	 * Get the link node of a data list that has the nodeRef stored in the
	 * propertyQName.
	 * 
	 * @param listNodeRef
	 *            the list node ref
	 * @param propertyQName
	 *            the property q name
	 * @param nodeRef
	 *            the node ref
	 * @return the link
	 */
	public NodeRef getListItem(NodeRef listNodeRef, QName propertyQName, NodeRef nodeRef);
	
	/**
	 * Create the link node of a data list that has the nodeRef stored in the
	 * propertyQName.
	 * 
	 * @param listNodeRef
	 *            the list node ref
	 * @param propertyQName
	 *            the property q name
	 * @param nodeRef
	 *            the node ref
	 * @return the link
	 */
	public NodeRef createListItem(NodeRef listNodeRef, QName listType, Map<QName, Serializable> properties, Map<QName, List<NodeRef>>associations);

	/**
	 * 
	 * @param listContainerNodeRef
	 * @return
	 */
	public List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef);

	/**
	 * 
	 * @param listContainerNodeRef
	 * @return
	 */
	public List<QName> getExistingListsQName(NodeRef listContainerNodeRef);

	/**
	 * Copy all data lists.
	 * 
	 * @param sourceNodeRef
	 *            the source node ref
	 * @param targetNodeRef
	 *            the target node ref
	 * @param override
	 *            the override
	 */
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override);
	
	/**
	 * Copy all data lists
	 * @param sourceNodeRef
	 * @param targetNodeRef
	 * @param listQNames (if null, copy all)
	 * @param override
	 */
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, Collection<QName> listQNames, boolean override);
	
	/**
	 * Get list items
	 * 
	 * @param listContainerNodeRef
	 * @param listQName
	 * @return
	 */
	public List<NodeRef> getListItems(NodeRef listNodeRef, QName listQName);

	/**
	 * Move datalists
	 * @param sourceNodeRef
	 * @param targetNodeRef
	 */
	public void moveDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef);

}
