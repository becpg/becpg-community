package fr.becpg.repo.report.search.impl;

import java.io.OutputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.repo.report.search.actions.ZipSearchAction;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ZipSearchRenderer class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ZipSearchRenderer  implements SearchReportRenderer {

	@Autowired
	NodeService nodeService;
	
	@Autowired
	DownloadService downloadService;
	
	@Autowired
	ActionService actionService;
	
	
	/** {@inheritDoc} */
	@Override
	public void renderReport(NodeRef templateNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {
      throw new IllegalStateException("Please use async method");
		
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ReportFormat.ZIP.equals(reportFormat) && ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME))
				.endsWith(ReportTplService.PARAM_VALUE_XMLREPORT_EXTENSION);
	}

	/** {@inheritDoc} */
	@Override
	public void executeAction(NodeRef templateNodeRef, NodeRef downloadNode, ReportFormat reportFormat) {
		    Action action = actionService.createAction(ZipSearchAction.NAME);
	        action.setExecuteAsynchronously(true);
	        action.setParameterValue(ZipSearchAction.PARAM_TPL_NODEREF, templateNodeRef);
	        actionService.executeAction(action, downloadNode);
	}
	
	

}
