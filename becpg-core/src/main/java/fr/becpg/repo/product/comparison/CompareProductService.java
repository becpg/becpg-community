/*
 * 
 */
package fr.becpg.repo.product.comparison;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Interface CompareProductService.
 *
 * @author querephi
 */
public interface CompareProductService {

	/**
	 * Compare some products.
	 *
	 * @param product1 the product1
	 * @param products the products
	 * @return the list
	 */
	public List<CompareResultDataItem> compare(NodeRef product1, List<NodeRef> products);
	
	/**
	 * Do a structural comparison.
	 *
	 * @param product1 the product1
	 * @param product2 the product2
	 * @param datalistType the datalist type
	 * @param pivotProperty the pivot property
	 * @return the list
	 */
	public List<StructCompareResultDataItem> compareStructDatalist(NodeRef product1, NodeRef product2, QName datalistType, QName pivotProperty);
		
}
