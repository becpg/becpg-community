/*
 * 
 */
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.NodeVisitor;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProductService.
 *
 * @author querephi
 */
public interface ProductService {
		   	       
    /**
     * Formulate.
     *
     * @param productNodeRef the product node ref
     */
    public void formulate(NodeRef productNodeRef);
    
    /**
	 * Check if the system should generate the report for this product
	 * @param productNodeRef
	 * @return
	 */
	public boolean IsReportable(NodeRef productNodeRef);
	
    /**
     * Sets the product report visitor.
     *
     * @param productReportVisitor the new product report visitor
     */
    public void setProductReportVisitor(NodeVisitor productReportVisitor);    	
    
    /**
     * Generate report.
     *
     * @param productNodeRef the product node ref
     */
    public void generateReport(NodeRef productNodeRef);
        
    /**
     * Classify product.
     *
     * @param containerNodeRef the container node ref
     * @param productNodeRef the product node ref
     */
    public void classifyProduct(NodeRef containerNodeRef, NodeRef productNodeRef);    
    
    /**
     * Gets the WUsed product of the compoList
     *
     * @param productNodeRef the product node ref
     * @return the WUsed product
     */
    public List<CompoListDataItem> getWUsedCompoList(NodeRef productNodeRef);
    
    /**
     * Gets the WUsed product of the packagingList
     *
     * @param productNodeRef the product node ref
     * @return the WUsed product
     */
    public List<PackagingListDataItem> getWUsedPackagingList(NodeRef productNodeRef);
}
