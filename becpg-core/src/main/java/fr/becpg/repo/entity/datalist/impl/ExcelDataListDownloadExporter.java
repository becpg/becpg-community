package fr.becpg.repo.entity.datalist.impl;

import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.download.AbstractDownloadExporter;

/**
 * <p>ExcelDataListDownloadExporter class.</p>
 *
 * @author matthieu
 */
public class ExcelDataListDownloadExporter extends AbstractDownloadExporter {
	

	/**
	 * <p>Constructor for ExcelDataListDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nbOfLines a {@link java.lang.Long} object
	 */
	public ExcelDataListDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage,
			NodeRef downloadNodeRef, Long nbOfLines) {
		 super(transactionHelper, updateService, downloadStorage, downloadNodeRef, nbOfLines);

	}


}
