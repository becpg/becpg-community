package fr.becpg.repo.entity.datalist.impl;

import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.download.AbstractDownloadExporter;

public class ExcelDataListDownloadExporter extends AbstractDownloadExporter {
	

	public ExcelDataListDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage,
			NodeRef downloadNodeRef, Long nbOfLines) {
		 super(transactionHelper, updateService, downloadStorage, downloadNodeRef, nbOfLines);

	}


}
