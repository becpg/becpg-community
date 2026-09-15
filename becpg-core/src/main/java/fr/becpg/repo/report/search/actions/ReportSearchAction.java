
package fr.becpg.repo.report.search.actions;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.search.impl.ReportServerSearchRenderer;
import fr.becpg.report.client.ReportFormat;

/**
 * {@link org.alfresco.repo.action.executer.ActionExecuter} for creating an
 * excel file containing content from the repository.
 *
 * The maximum total size of the content which can be downloaded is controlled
 * by the maximumContentSie property. -1 indicates no limit.
 *
 * @author matthieu form Alex Miller code
 * @version $Id: $Id
 */
public class ReportSearchAction extends AbstractExportSearchAction {

	/** Constant <code>NAME="reportSearchAction"</code> */
	public static final String NAME = "reportSearchAction";
	
	
	private ReportServerSearchRenderer reportServerSearchRenderer;
	
	
	/**
	 * <p>Setter for the field <code>reportServerSearchRenderer</code>.</p>
	 *
	 * @param reportServerSearchRenderer a {@link fr.becpg.repo.report.search.impl.ReportServerSearchRenderer} object
	 */
	public void setReportServerSearchRenderer(ReportServerSearchRenderer reportServerSearchRenderer) {
		this.reportServerSearchRenderer = reportServerSearchRenderer;
	}


	/** {@inheritDoc} */
	@Override
	protected AbstractSearchDownloadExporter createHandler(Action action, NodeRef actionedUponNodeRef, NodeRef templateNodeRef, long nodeCount,
			ReportFormat format) {
		String[] parameters = (String[]) action.getParameterValue(PARAM_PARAMETERS);
		return new ReportSearchDownloadExporter(transactionHelper, updateService, downloadStorage, reportServerSearchRenderer,
				actionedUponNodeRef, templateNodeRef, nodeCount, format, parameters);
	}


}
