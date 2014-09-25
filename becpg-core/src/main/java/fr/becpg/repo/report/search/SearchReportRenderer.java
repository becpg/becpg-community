package fr.becpg.repo.report.search;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.report.client.ReportFormat;

public interface SearchReportRenderer {

	void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream);

	boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat);

}
