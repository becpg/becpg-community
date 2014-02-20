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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * @author querephi
 */
public class ReportTplWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(ReportTplWebScript.class);

	private static final String ACTION_REFRESH = "refresh";
	private static final String ACTION_DISABLE = "disable";
	private static final String ACTION_ENABLE = "enable";
	private static final String ACTION_DELETE_REPORTS = "deleteReports";
	private static final String ACTION_UPDATE_PERMISSIONS = "updatePermissions";

	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private NodeService nodeService;

	private EntityReportService entityReportService;

	private BeCPGSearchService beCPGSearchService;

	private AssociationService associationService;

	private EntityReportAsyncGenerator entityReportAsyncGenerator;

	public void setEntityReportAsyncGenerator(EntityReportAsyncGenerator entityReportAsyncGenerator) {
		this.entityReportAsyncGenerator = entityReportAsyncGenerator;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
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
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_DISABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, true);
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_ENABLE.equals(action)) {
				nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED, false);
				updateReports(nodeRef, ACTION_REFRESH);
			} else if (ACTION_DELETE_REPORTS.equals(action)) {
				deleteReports(nodeRef);
			} else if (ACTION_UPDATE_PERMISSIONS.equals(action)) {
				updateReports(nodeRef, ACTION_UPDATE_PERMISSIONS);
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}
		}
	}

	private void updateReports(NodeRef nodeRef, String action) {

		Boolean isSystem = (Boolean) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);
		QName classType = (QName) nodeService.getProperty(nodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME);

		if (isSystem != null && isSystem && classType != null) {

			String query = LuceneHelper.mandatory(LuceneHelper.getCondType(classType)) + LuceneHelper.mandatory(LuceneHelper.getCondAspect(ReportModel.ASPECT_REPORT_ENTITY))
					+ LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));

			// TODO : duplicate code
			List<NodeRef> refs = null;
			int page = 1;
			List<NodeRef> tmp = beCPGSearchService.lucenePaginatedSearch(query, LuceneHelper.getSort(ContentModel.PROP_MODIFIED,false), page, RepoConsts.MAX_RESULTS_256);
			
			if (tmp!=null && !tmp.isEmpty()) {
				logger.info(" - Page 1:"+tmp.size());
				refs = tmp;
			}
			while(tmp!=null && tmp.size() == RepoConsts.MAX_RESULTS_256 ){
				page ++;
				tmp	=  beCPGSearchService.lucenePaginatedSearch(query, LuceneHelper.getSort(ContentModel.PROP_MODIFIED,false), page, RepoConsts.MAX_RESULTS_256);
				if (tmp!=null && !tmp.isEmpty()) {
					logger.info(" - Page "+page+":"+tmp.size());
					refs.addAll(tmp);
				}
			}

			logger.info("Refresh reports of " + refs.size() + " entities. action: " + action);

			if (ACTION_REFRESH.equals(action)) {
				entityReportAsyncGenerator.queueNodes(refs);
			} else if (ACTION_UPDATE_PERMISSIONS.equals(action)) {
				for (NodeRef entityNodeRef : refs) {
					List<NodeRef> reports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS);
					for (NodeRef report : reports) {
						NodeRef tplNodeRef = associationService.getTargetAssoc(report, ReportModel.ASSOC_REPORT_TPL);
						if (nodeRef.equals(tplNodeRef)) {
							entityReportService.setPermissions(nodeRef, report);
						}
					}
				}
			}

			logger.info("Refresh reports done.");
		}
	}

	private void deleteReports(NodeRef nodeRef) {

		List<AssociationRef> assocRefs = nodeService.getSourceAssocs(nodeRef, ReportModel.ASSOC_REPORT_TPL);

		logger.info("Delete " + assocRefs.size() + " reports.");

		for (AssociationRef assocRef : assocRefs) {
			if (logger.isDebugEnabled()) {
				logger.debug("Delete report " + assocRef.getSourceRef() + " - name: " + nodeService.getProperty(assocRef.getSourceRef(), ContentModel.PROP_NAME));
			}
			nodeService.addAspect(assocRef.getSourceRef(), ContentModel.ASPECT_TEMPORARY, null);
			nodeService.deleteNode(assocRef.getSourceRef());
		}

		logger.info("Reports deleted.");
	}
}
