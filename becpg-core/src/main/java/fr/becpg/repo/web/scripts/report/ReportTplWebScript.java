/*
 * 
 */
package fr.becpg.repo.web.scripts.report;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * @author querephi
 */
@Service
public class ReportTplWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ReportTplWebScript.class);

	private static final int BATCH_SIZE = 25;
	private static final String ACTION_REFRESH = "refresh";
	private static final String ACTION_DISABLE = "disable";
	private static final String ACTION_ENABLE = "enable";
	private static final String ACTION_DELETE_REPORTS = "deleteReports";

	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private NodeService nodeService;

	private EntityReportService entityReportService;

	private BeCPGSearchService beCPGSearchService;

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		logger.debug("start report webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String action = templateArgs.get(PARAM_ACTION);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

		if (nodeService.exists(nodeRef)) {
			if (ACTION_REFRESH.equals(action)) {
				refreshReports(nodeRef);
			} else if (ACTION_DISABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
				refreshReports(nodeRef);
			} else if (ACTION_ENABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);
				refreshReports(nodeRef);
			} else if (ACTION_DELETE_REPORTS.equals(action)) {
				deleteReports(nodeRef);
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}
		}
	}

	private void refreshReports(NodeRef nodeRef) {

		Boolean isSystem = (Boolean) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
		QName classType = (QName) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME);

		if (isSystem != null && isSystem && classType != null) {

			String query = LuceneHelper.mandatory(LuceneHelper.getCondType(classType))
					+ LuceneHelper.mandatory(LuceneHelper.getCondAspect(ReportModel.ASPECT_REPORT_ENTITY))
					+ LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));

			List<NodeRef> entityNodeRefs = beCPGSearchService.luceneSearch(query);
			
			logger.info("Refresh reports of " + entityNodeRefs.size() + " entities.");

			for (List<NodeRef> batch : Lists.partition(entityNodeRefs, BATCH_SIZE)) {
				for (NodeRef entityNodeRef : batch) {
					entityReportService.generateReport(entityNodeRef);
				}
			}
			
			logger.info("Refresh reports done.");
		}
	}

	private void deleteReports(NodeRef nodeRef) {

		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, ReportModel.ASSOC_REPORT_TPL);
		
		logger.info("Delete " + assocRefs.size() + " reports.");

		for (AssociationRef assocRef : assocRefs) {
			if (!nodeService.hasAspect(assocRef.getSourceRef(), ContentModel.ASPECT_PENDING_DELETE)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Delete report " + assocRef.getSourceRef() + " - name: "
							+ nodeService.getProperty(assocRef.getSourceRef(), ContentModel.PROP_NAME));
				}
				nodeService.addAspect(assocRef.getSourceRef(), ContentModel.ASPECT_TEMPORARY, null);
				nodeService.deleteNode(assocRef.getSourceRef());
			}
		}
		
		logger.info("Reports deleted.");
	}
}
