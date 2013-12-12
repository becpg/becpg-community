
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;

/**
 * @author querephi
 */
@Service
public interface ProductService {
		   	       
    /**
     * Formulate the product (update DB)
     */
    public void formulate(NodeRef productNodeRef) throws FormulateException;
    
    /**
     * Use fast chain formulation handler if fast param is true
     */
	public void formulate(NodeRef productNodeRef, boolean fast)   throws FormulateException;
    
    /**
     * Formulate the product (don't update DB)
     */
    public ProductData formulate(ProductData productData) throws FormulateException;
     
    
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName dataType, String dataListName, List<NodeRef> elements) throws FormulateException;


	public boolean shouldFormulate(NodeRef product);


   
}
