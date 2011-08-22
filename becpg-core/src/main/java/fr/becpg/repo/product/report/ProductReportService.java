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
	 * Load node attributes.
	 * @param nodeRef
	 * @return
	 */
	public Map<ClassAttributeDefinition, String> loadNodeAttributes(NodeRef nodeRef);
	
	/**
	 * Gets the image.
	 *
	 * @param nodeRef the node ref
	 * @return the image
	 */
	public byte[] getImage(NodeRef nodeRef);
	
	/**
	 * Gets the product image.
	 *
	 * @param productNodeRef the product node ref
	 * @param imgName the img name
	 * @return the product image
	 */
	public NodeRef getProductImage(NodeRef productNodeRef, String imgName);
	
	/**
	 * Checks if is report up to date.
	 *
	 * @param productNodeRef the product node ref
	 * @return true, if is report up to date
	 */
	public boolean isReportUpToDate(NodeRef productNodeRef);	
	
	/**
	 * Gets the document content writer.
	 *
	 * @param productNodeRef the product node ref
	 * @param tplNodeRef the tpl node ref
	 * @return the document content writer
	 */
	public ContentWriter getDocumentContentWriter(NodeRef productNodeRef, NodeRef tplNodeRef);
	
	/**
	 * load data lists.
	 *
	 * @param productData the product data
	 * @param dataListsElt the data lists elt
	 * @return the element
	 */
	public Element loadDataLists(ProductData productData, Element dataListsElt);
	
	/**
	 * Generate reports.
	 *
	 * @param productNodeRef the product node ref
	 * @param tplsNodeRef the tpls node ref
	 * @param productElt the product elt
	 * @param images the images
	 */
	public void generateReports(NodeRef productNodeRef, List<NodeRef> tplsNodeRef, Element productElt, Map<String, byte[]> images);
		
	/**
	 * Get the fields organized by sets
	 * @param productNodeRef
	 * @param reportFormConfigPath
	 * @return
	 */
	public Map<String, List<String>> getFieldsBySets(NodeRef productNodeRef, String reportFormConfigPath);
		
		
}
