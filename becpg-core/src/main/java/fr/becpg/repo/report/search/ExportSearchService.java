/*
 * 
 */
package fr.becpg.repo.report.search;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface ExportSearchService.
 *
 * @author querephi
 */
public interface ExportSearchService {

	/**
	 * Gets the report tpls.
	 *
	 * @return the report tpls
	 */
	public List<NodeRef> getReportTpls();
	
	/**
	 * Gets the report.
	 *
	 * @param reportName the report name
	 * @param searchResults the search result
	 * @param outputStream the output stream
	 * @return the report
	 */
	public void getReport(String reportName, List<NodeRef> searchResults, OutputStream outputStream);	
}
