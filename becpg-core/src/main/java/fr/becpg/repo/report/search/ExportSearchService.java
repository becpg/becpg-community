/*
 * 
 */
package fr.becpg.repo.report.search;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.report.client.ReportFormat;

// TODO: Auto-generated Javadoc
/**
 * The Interface ExportSearchService.
 *
 * @author querephi
 */
public interface ExportSearchService {

	/**
	 * Gets the report.
	 *
	 * @param reportName the report name
	 * @param searchResults the search result
	 * @param outputStream the output stream to update (out value, updated)
	 * @param ReportFormat the format of the report
	 * @return the report
	 */
	public void getReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream);	
}
