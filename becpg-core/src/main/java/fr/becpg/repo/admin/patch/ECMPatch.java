package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ECMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class ECMPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(ECMPatch.class);

//	private InitVisitor initRepoVisitor;

	private BeCPGSearchService beCPGSearchService;
	

	// simulationEntityAspect
	public static final QName ASPECT_SIMULATION_ENTITY = QName.createQName(ECMModel.ECM_URI, "simulationEntityAspect");

	public static final QName ASSOC_SIMULATION_SOURCE_ITEM = QName.createQName(ECMModel.ECM_URI, "simulationSourceItem");

//	public void setInitRepoVisitor(InitVisitor initRepoVisitor) {
//		this.initRepoVisitor = initRepoVisitor;
//	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	

	@Override
	protected String applyInternal() throws Exception {

		// Remove simulation Item
		// add aspect <aspect>rep:reportEntityAspect</aspect>

		// Change template to ECO_REPORT_PATH, ReportType.Document,
		// ReportFormat.PDF, ECMModel.TYPE_ECO, true, true, false);
		// Delete template abd redo initrepo

		NodeRef oldWFNodeRef = searchFolder("app:company_home/cm:System/cm:Reports/cm:ECOReports/.");

		if (oldWFNodeRef != null) {
			logger.info("Delete old OM report template");
			nodeService.deleteNode(oldWFNodeRef);
		}

		// Delete ALL OM
		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(ECMModel.TYPE_ECO));

		List<NodeRef> nodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("Delete " + nodeRefs.size() + " OM ");
		for (NodeRef nodeRef : nodeRefs) {
			nodeService.deleteNode(nodeRef);
		}

		query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(ASPECT_SIMULATION_ENTITY));

		// Delete SIMULATION NODE
		nodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("Delete " + nodeRefs.size() + " simulation nodes ");
		for (NodeRef nodeRef : nodeRefs) {
			nodeService.deleteNode(nodeRef);
		}
		
		//Doesn't work in junit test 
		//initRepoVisitor.visitContainer(repository.getCompanyHome());

		
		return "ECM patch success";
	}

}
