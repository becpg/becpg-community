/*
 * 
 */
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductService.
 *
 * @author querephi
 */
@Service
public interface ProductService {
		   	       
    /**
     * Formulate the product (update DB)
     *
     * @param productNodeRef the product node ref
     */
    public void formulate(NodeRef productNodeRef) throws FormulateException;
    

    /**
     * Use fast chain formulation handler if fast param is true
     * @param productNodeRef
     * @param fast
     * @throws FormulateException
     */
	public void formulate(NodeRef productNodeRef, boolean fast)   throws FormulateException;
    
    
    /**
     * Formulate the product (don't update DB)
     * @param productData
     * @return
     * @throws FormulateException
     */
    public ProductData formulate(ProductData productData) throws FormulateException;
     
    
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
