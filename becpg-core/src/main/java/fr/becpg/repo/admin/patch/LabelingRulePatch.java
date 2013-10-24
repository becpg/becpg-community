package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class LabelingRulePatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(LabelingRulePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.labelingRulePatch.result";

	private BeCPGSearchService beCPGSearchService;

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		List<NodeRef> dataListNodeRefs = new ArrayList<>();
		int page = 1;
		String runnedQuery = "+TYPE:\"bcpg:ing\"";
		List<NodeRef> tmp = beCPGSearchService.lucenePaginatedSearch(runnedQuery, LuceneHelper.getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);

		if (tmp != null && !tmp.isEmpty()) {
			logger.info(" - Page 1:" + tmp.size());
			dataListNodeRefs = tmp;
		}
		while (tmp != null && tmp.size() == RepoConsts.MAX_RESULTS_256) {
			page++;
			tmp = beCPGSearchService.lucenePaginatedSearch(runnedQuery, LuceneHelper.getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);
			if (tmp != null && !tmp.isEmpty()) {
				logger.info(" - Page " + page + ":" + tmp.size());
				dataListNodeRefs.addAll(tmp);
			}
		}

		logger.info("Migrate ingType, size: " + dataListNodeRefs.size());
		String encodedPath = LuceneHelper.encodePath("System/Lists/bcpg:entityLists/IngTypes");

		for (NodeRef dataListNodeRef : dataListNodeRefs) {
			if (nodeService.exists(dataListNodeRef)) {
				try{
					String queryPath = String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE, encodedPath, nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_ING_TYPE));
	
					List<NodeRef> resultSet = beCPGSearchService.luceneSearch(queryPath, RepoConsts.MAX_RESULTS_SINGLE_VALUE);
	
					if (!resultSet.isEmpty()) {
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_ING_TYPE_V2, resultSet.get(0));
						nodeService.removeProperty(dataListNodeRef, BeCPGModel.PROP_ING_TYPE);
						logger.info("Update ing type for " + nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME));
					} else {
						logger.warn("Type not found for : " + queryPath);
					}
				} catch (Exception e){
					if(e instanceof ConcurrencyFailureException){
						throw e;
					}
					logger.error(e,e);
				}

			} else {
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}
		}
		dataListNodeRefs = beCPGSearchService.luceneSearch(String.format(RepoConsts.PATH_QUERY_SUGGEST_VALUE_ALL, encodedPath));
		for (NodeRef dataListNodeRef : dataListNodeRefs) {
			if (nodeService.exists(dataListNodeRef)) {
				logger.info("Change node type for " + nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME));
				nodeService.setType(dataListNodeRef, BeCPGModel.TYPE_ING_TYPE_ITEM);

				nodeService.setProperty(nodeService.getPrimaryParent(dataListNodeRef).getParentRef(), DataListModel.PROP_DATALISTITEMTYPE,
						BeCPGModel.TYPE_ING_TYPE_ITEM.toPrefixString(namespaceService));
			} else {
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
