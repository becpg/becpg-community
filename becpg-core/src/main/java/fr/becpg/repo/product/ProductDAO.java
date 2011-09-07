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
}
