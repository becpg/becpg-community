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
	 * Render a report
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param searchResults a {@link java.util.List} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 */
	default void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {
		renderReport(templateNodeRef, searchResults, reportFormat, outputStream, null);
	}

	/**
	 * Render a report with extra parameters
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param searchResults a {@link java.util.List} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @since 25.3.0.34
	 */
	void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream, String[] parameters);

	/**
	 * is applicable
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @return a boolean.
	 */
	boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat);

	/**
	 * execute action
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param downloadNode a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 */
	default void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat) {
		executeAction(templateNodeRef, downloadNode, reportFormat, null);
	}

	/**
	 * execute action with extra parameters
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param downloadNode a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @since 25.3.0.34
	 */
	void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat, String[] parameters);


}
