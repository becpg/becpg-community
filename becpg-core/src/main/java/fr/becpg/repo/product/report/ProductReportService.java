/*
 * 
 */
package fr.becpg.repo.product.report;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

import fr.becpg.repo.product.data.ProductData;

/**
 * Class used to manage product report instances (used by report visitors classes)
 *
 * @author querephi
 */
public interface ProductReportService {	
	
	/**
	 * load data lists.
	 *
	 * @param productData the product data
	 * @param dataListsElt the data lists elt
	 * @return the element
	 */
	public Element loadDataLists(ProductData productData, Element dataListsElt);
	
		
}
