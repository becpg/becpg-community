/*
 * 
 */
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductService.
 *
 * @author querephi
 */
public interface ProductService {
		   	       
    /**
     * Formulate the product (update DB)
     *
     * @param productNodeRef the product node ref
     */
    public void formulate(NodeRef productNodeRef) throws FormulateException;
    
    
    /**
     * Formulate the product (don't update DB)
     * @param productData
     * @return
     * @throws FormulateException
     */
    public ProductData formulate(ProductData productData) throws FormulateException;
    
 
    /**
     * Classify product.
     *
     * @param containerNodeRef the container node ref
     * @param productNodeRef the product node ref
     */
    public void classifyProduct(NodeRef containerNodeRef, NodeRef productNodeRef);

    
    
    /**
     * 
     * @param productNodeRef
     * @param dataType 
     * @param dataListName
     * @param elements
     * @return 
     * @throws FormulateException
     */
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName dataType, String dataListName, List<NodeRef> elements) throws FormulateException;   
    
   
}
