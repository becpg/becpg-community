
package fr.becpg.repo.report.search.actions;

import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer;
import fr.becpg.report.client.ReportFormat;

/**
 * @author matthieu 
 */
public class ExcelSearchAction extends AbstractExportSearchAction {

	public static final String NAME = "excelSearchAction";
	
	private ExcelReportSearchRenderer excelReportSearchRenderer;

	
	public void setExcelReportSearchRenderer(ExcelReportSearchRenderer excelReportSearchRenderer) {
		this.excelReportSearchRenderer = excelReportSearchRenderer;
	}

	@Override
	protected AbstractSearchDownloadExporter createHandler(NodeRef actionedUponNodeRef, NodeRef templateNodeRef, DownloadRequest downloadRequest, ReportFormat format) {
		return new ExcelSearchDownloadExporter(transactionHelper, updateService, downloadStorage, contentService, excelReportSearchRenderer,
				actionedUponNodeRef, templateNodeRef, Long.valueOf(downloadRequest.getRequetedNodeRefs().length));
	}


}
