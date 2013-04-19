package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import fr.becpg.model.ECMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

//TODO
public class ECMPatch extends AbstractPatch {

	private static Log logger = LogFactory.getLog(ECMPatch.class);
	
	private static int BATCH_SIZE = 50;
	
	private BeCPGSearchService beCPGSearchService;
	
	private DictionaryService dictionaryService;
	
	
	@Override
	protected String applyInternal() throws Exception {
	
		//Remove simulation Item
		//add aspect <aspect>rep:reportEntityAspect</aspect>

		
		
		addMandatoryAspect(ECMModel.TYPE_ECO, ReportModel.ASPECT_REPORT_ENTITY);
			
		
		return "Ecm patch success";
	}
	
	private void addMandatoryAspect(QName type, final QName aspect) {

		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(type))
				+ LuceneHelper.exclude(LuceneHelper.getCondAspect(aspect));

		List<NodeRef> nodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + nodeRefs.size() + " node of type " + type + " without mandatory aspect " + aspect);

		if (!nodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(nodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								for (NodeRef nodeRef : batchList) {
									if (nodeService.exists(nodeRef)) {
										if (!nodeService.hasAspect(nodeRef, aspect)) {
											nodeService.addAspect(nodeRef, aspect, null);
											//look for other mandatory aspects
											TypeDefinition typeDef = dictionaryService.getType(nodeService.getType(nodeRef));
											for(QName defaultAspect : typeDef.getDefaultAspectNames()){
												if (!nodeService.hasAspect(nodeRef, defaultAspect)) {
													logger.debug("Add other default aspect " + defaultAspect + " for node " + nodeRef);
													nodeService.addAspect(nodeRef, defaultAspect, null);
												}
											}
										}
									}
								}
								return true;
							}
						}, false, true);
			}
		}
	}

	
}
