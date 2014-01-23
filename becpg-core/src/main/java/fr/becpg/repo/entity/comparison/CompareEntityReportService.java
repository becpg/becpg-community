package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
	 * @param out the output stream
	 */
	public void getComparisonReport(NodeRef entity1, QName dataListTypeQName, List<NodeRef> entities, OutputStream out);
}
