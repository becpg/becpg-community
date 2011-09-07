/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface CompareEntityReportService.
 *
 * @author querephi
 */
public interface CompareEntityReportService {

	/**
	 * Get the birt comparison report output.
	 *
	 * @param product1 the entity1
	 * @param entitys the entitys
	 * @param outputStream the output stream
	 * @return the comparison report
	 */
	public OutputStream getComparisonReport(NodeRef entity1, List<NodeRef> entities, OutputStream outputStream);
}
