/*
 *
 */
package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import fr.becpg.util.MutexFactory;

/**
 * Class used to render the result of a search in a report
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("exportSearchService")
public class ExportSearchServiceImpl implements ExportSearchService {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ExportSearchServiceImpl.class);

	private static final String EXPORT_MUTEX_PREFIX = "exportSearch-";

	private static final int DOWNLOAD_BATCH_SIZE = 500;

	@Autowired
	private SearchReportRenderer[] searchReportRenderers;

	@Autowired
	protected RetryingTransactionHelper retryingTransactionHelper;

	@Autowired
	private DownloadStorage downloadStorage;
	
	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private MutexFactory mutexFactory;

	/** {@inheritDoc} */
	@Override
	public void createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat,
			OutputStream outputStream) {
		createReport(nodeType, templateNodeRef, searchResults, reportFormat, outputStream, (String[]) null);
	}

	/** {@inheritDoc} */
	@Override
	public void createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat,
			OutputStream outputStream, String[] parameters) {

		if (templateNodeRef != null) {

			SearchReportRenderer searchReportRender = getSearchReportRender(templateNodeRef, reportFormat);
			if (searchReportRender != null) {
				searchReportRender.renderReport(templateNodeRef, searchResults, reportFormat, outputStream, parameters);
			} else {
				logger.error("No search report renderer found for : " + reportFormat.toString() + " " + templateNodeRef);
			}

		}
	}

	/**
	 * <p>getSearchReportRender.</p>
	 *
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object
	 * @return a {@link fr.becpg.repo.report.search.SearchReportRenderer} object
	 */
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
		return createReport(nodeType, templateNodeRef, searchResults, reportFormat, (String[]) null);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef createReport(QName nodeType, NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat,
			String[] parameters) {

		ParameterCheck.mandatory("templateNodeRef", templateNodeRef);
		

		String mutexKey = EXPORT_MUTEX_PREFIX + AuthenticationUtil.getRunAsUser();
		ReentrantLock lock = mutexFactory.getMutex(mutexKey);
		lock.lock();

		NodeRef downloadNode;
		try {
			downloadNode = retryingTransactionHelper.doInTransaction(() -> downloadStorage.createDownloadNode(false), false, true);
			addNodesToDownload(downloadNode, searchResults);
		} finally {
			lock.unlock();
			mutexFactory.removeMutex(mutexKey, lock);
		}

		SearchReportRenderer searchReportRender = getSearchReportRender(templateNodeRef, reportFormat);
		if (searchReportRender != null) {
			searchReportRender.executeAction(templateNodeRef, downloadNode, reportFormat, parameters);
		} else {
			logger.error("No search report renderer found for : " + reportFormat.toString() + " " + templateNodeRef);
		}

		return downloadNode;
	}

	/**
	 * Attach the search results to the download node, one batch of associations per transaction.
	 *
	 * A single transaction over a large result set saturates the transactional caches and holds its
	 * database locks for the whole request: an export of tens of thousands of entities then takes
	 * minutes before the asynchronous rendering even starts.
	 *
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param searchResults a {@link java.util.List} object
	 */
	private void addNodesToDownload(NodeRef downloadNodeRef, List<NodeRef> searchResults) {
		List<NodeRef> distinctResults = new ArrayList<>(new LinkedHashSet<>(searchResults));

		if (logger.isDebugEnabled()) {
			logger.debug("Adding " + distinctResults.size() + " node(s) to download " + downloadNodeRef);
		}

		for (int fromIndex = 0; fromIndex < distinctResults.size(); fromIndex += DOWNLOAD_BATCH_SIZE) {
			List<NodeRef> batch = distinctResults.subList(fromIndex, Math.min(fromIndex + DOWNLOAD_BATCH_SIZE, distinctResults.size()));

			retryingTransactionHelper.doInTransaction(() -> {
				for (NodeRef nodeRef : batch) {
					if (nodeService.exists(nodeRef)) {
						downloadStorage.addNodeToDownload(downloadNodeRef, nodeRef);
					}
				}
				return null;
			}, false, true);
		}
	}

}
