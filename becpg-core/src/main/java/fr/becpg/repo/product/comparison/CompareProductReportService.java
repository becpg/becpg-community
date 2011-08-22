/*
 * 
 */
package fr.becpg.repo.product.comparison;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface CompareProductReportService.
 *
 * @author querephi
 */
public interface CompareProductReportService {

	/**
	 * Get the birt comparison report output.
	 *
	 * @param product1 the product1
	 * @param products the products
	 * @param outputStream the output stream
	 * @return the comparison report
	 */
	public OutputStream getComparisonReport(NodeRef product1, List<NodeRef> products, OutputStream outputStream);
}
