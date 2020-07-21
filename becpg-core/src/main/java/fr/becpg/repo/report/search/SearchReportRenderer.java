package fr.becpg.repo.report.search;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.report.client.ReportFormat;

/**
 * <p>SearchReportRenderer interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SearchReportRenderer {

	/**
	 * <p>renderReport.</p>
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param searchResults a {@link java.util.List} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 */
	void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream);

	/**
	 * <p>isApplicable.</p>
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @return a boolean.
	 */
	boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat);

	/**
	 * <p>executeAction.</p>
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param downloadNode a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 */
	void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat);

}
