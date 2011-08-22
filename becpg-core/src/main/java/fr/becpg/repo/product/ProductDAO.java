/*
 * 
 */
package fr.becpg.repo.product;

import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.data.ProductData;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductDAO.
 *
 * @author querephi
 */
public interface ProductDAO {

	/**
	 * Creates the.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 * @return the node ref
	 */
	public NodeRef create(NodeRef parentNodeRef, ProductData productData, Collection<QName> dataLists);
	
	/**
	 * Update.
	 *
	 * @param productNodeRef the product node ref
	 * @param productData the product data
	 * @param dataLists the data lists
	 */
	public void update(NodeRef productNodeRef, ProductData productData, Collection<QName> dataLists);	
	
	/**
	 * Find.
	 *
	 * @param productNodeRef the product node ref
	 * @param dataLists the data lists
	 * @return the product data
	 */
	public ProductData find(NodeRef productNodeRef, Collection<QName> dataLists);
	
	/**
	 * Delete.
	 *
	 * @param productNodeRef the product node ref
	 */
	public void delete(NodeRef productNodeRef);
	
	/**
	 * Get the product list container.
	 *
	 * @param productNodeRef the product node ref
	 * @return the list container
	 */
	public NodeRef getListContainer(NodeRef productNodeRef);
	
	/**
	 * Get the product list NodeRef.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param productListQName : type of the product list
	 * @return the list
	 */
	public NodeRef getList(NodeRef listContainerNodeRef, QName productListQName);
	
	public Set<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef);
	
	public Set<QName> getExistingListsQName(NodeRef listContainerNodeRef);
	
	/**
	 * Get the link node of a product list that has the nodeRef stored in the propertyQName.
	 *
	 * @param listNodeRef the list node ref
	 * @param propertyQName the property q name
	 * @param nodeRef the node ref
	 * @return the link
	 */
	public NodeRef getLink(NodeRef listNodeRef, QName propertyQName, NodeRef nodeRef);
	
	/**
	 * Create the product list container.
	 *
	 * @param productNodeRef the product node ref
	 * @return the node ref
	 */
	public NodeRef createListContainer(NodeRef productNodeRef);
	
	/**
	 * Create the product list NodeRef.
	 *
	 * @param listContainerNodeRef the list container node ref
	 * @param productListQName : type of the product list
	 * @return the node ref
	 */
	public NodeRef createList(NodeRef listContainerNodeRef, QName productListQName);
}
