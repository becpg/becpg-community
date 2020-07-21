package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author querephi
 */
public interface CompareEntityReportService {

	/**
	 * Get the birt comparison report output.
	 */
	void getComparisonReport(NodeRef entity1, List<NodeRef> entities, NodeRef templateNodeRef, OutputStream out);

	/**
	 * Guess report name based on template name
	 */
	String getReportFileName(NodeRef templateNodeRef, String defaultName);
}
