package fr.becpg.repo.entity.comparison;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>CompareEntityReportService interface.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface CompareEntityReportService {

	/**
	 * Get the birt comparison report output.
	 *
	 * @param entity1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entities a {@link java.util.List} object.
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param out a {@link java.io.OutputStream} object.
	 */
	void getComparisonReport(NodeRef entity1, List<NodeRef> entities, NodeRef templateNodeRef, OutputStream out);

	/**
	 * <p>getXmlReportDataSource.</p>
	 *
	 * @param entities a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 * @param entity a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	String getXmlReportDataSource(NodeRef entity, List<NodeRef> entities);

	/**
	 * Guess report name based on template name
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param defaultName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getReportFileName(NodeRef templateNodeRef, String defaultName);
}
