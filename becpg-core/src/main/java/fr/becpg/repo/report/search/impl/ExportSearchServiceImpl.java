/*
 *
 */
package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.report.client.ReportFormat;

/**
 * Class used to render the result of a search in a report
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("exportSearchService")
public class ExportSearchServiceImpl implements ExportSearchService {

	private static final Log logger = LogFactory.getLog(ExportSearchServiceImpl.class);

	@Autowired
	private SearchReportRenderer[] searchReportRenderers;

	@Autowired
	protected RetryingTransactionHelper retryingTransactionHelper;

	@Autowired
	private DownloadStorage downloadStorage;
	
	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public void createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat,
			OutputStream outputStream) {

		if (templateNodeRef != null) {

			SearchReportRenderer searchReportRender = getSearchReportRender(templateNodeRef, reportFormat);
			if (searchReportRender != null) {
				searchReportRender.renderReport(templateNodeRef, searchResults, reportFormat, outputStream);
			} else {
				logger.error("No search report renderer found for : " + reportFormat.toString() + " " + templateNodeRef);
			}

		}
	}

	private SearchReportRenderer getSearchReportRender(NodeRef templateNodeRef, ReportFormat reportFormat) {
		if (searchReportRenderers != null) {
			for (SearchReportRenderer searchReportRenderer : searchReportRenderers) {
				if (searchReportRenderer.isApplicable(templateNodeRef, reportFormat)) {
					return searchReportRenderer;
				}
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat) {

		ParameterCheck.mandatory("templateNodeRef", templateNodeRef);
		

		NodeRef downloadNode = retryingTransactionHelper.doInTransaction(() -> {
			// Create a download node
			NodeRef downloadNode1 = downloadStorage.createDownloadNode(false);

			// Add requested nodes
			for (NodeRef node : new HashSet<>(searchResults)) {
				if (nodeService.exists(node)) {
					downloadStorage.addNodeToDownload(downloadNode1, node);
				}
			}

			return downloadNode1;
		}, false, true);

		SearchReportRenderer searchReportRender = getSearchReportRender(templateNodeRef, reportFormat);
		if (searchReportRender != null) {
			searchReportRender.executeAction(templateNodeRef, downloadNode, reportFormat);
		} else {
			logger.error("No search report renderer found for : " + reportFormat.toString() + " " + templateNodeRef);
		}

		// This is done in a new transaction to avoid node not found errors when
		// the zip creation occurs
		// on a remote transformation server.

		return downloadNode;
	}

}
