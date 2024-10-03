
package fr.becpg.repo.report.search.actions;

import org.alfresco.service.cmr.download.DownloadRequest;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ExcelSearchAction class.</p>
 *
 * @author matthieu
 */
public class ExcelSearchAction extends AbstractExportSearchAction {

	/** Constant <code>NAME="excelSearchAction"</code> */
	public static final String NAME = "excelSearchAction";
	
	private ExcelReportSearchRenderer excelReportSearchRenderer;

	
	/**
	 * <p>Setter for the field <code>excelReportSearchRenderer</code>.</p>
	 *
	 * @param excelReportSearchRenderer a {@link fr.becpg.repo.report.search.impl.ExcelReportSearchRenderer} object
	 */
	public void setExcelReportSearchRenderer(ExcelReportSearchRenderer excelReportSearchRenderer) {
		this.excelReportSearchRenderer = excelReportSearchRenderer;
	}

	/** {@inheritDoc} */
	@Override
	protected AbstractSearchDownloadExporter createHandler(NodeRef actionedUponNodeRef, NodeRef templateNodeRef, DownloadRequest downloadRequest, ReportFormat format) {
		return new ExcelSearchDownloadExporter(transactionHelper, updateService, downloadStorage, contentService, excelReportSearchRenderer,
				actionedUponNodeRef, templateNodeRef, Long.valueOf(downloadRequest.getRequetedNodeRefs().length));
	}


}
