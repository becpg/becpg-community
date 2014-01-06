package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Add IngParentLevelPatch
 */
public class IngParentLevelPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(IngParentLevelPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.ingParentLevelPatch.result";

	private BeCPGSearchService beCPGSearchService;

	private AssociationService associationService;

	private BehaviourFilter policyBehaviourFilter;


	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Apply IngParentLevelPatch");
		
		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:ingList\" NOT ASPECT:\"bcpg:depthLevelAspect\" ", RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("add depthLevel in bcpg:entityListItem, size: " + dataListNodeRefs.size());

		// depthLevel
		int i = 0;
		List<List<NodeRef>> batches = Lists.partition(dataListNodeRefs, 500);
		for (final List<NodeRef> subList : batches) {
			try {
				i++;
				logger.info("Batch depthLevel " + i + " over " + batches.size());
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

					@Override
					public Void execute() throws Throwable {

						policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
						policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);

						try {
							for (NodeRef dataListNodeRef : subList) {
								if (nodeService.exists(dataListNodeRef)) {
									Map<QName, Serializable> properties = new HashMap<>();
									properties.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
									nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, properties);	
									
									// check processing aid
									if(nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_ING_LIST_IS_PROCESSING_AID) == null){
										nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_ING_LIST_IS_PROCESSING_AID, false);
									}
								} else {
									logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
								}
							}
							return null;

						} finally {
							policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
							policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_SORTABLE_LIST);
						}

					}

				}, false, true);
			} catch (Throwable e) {
				logger.warn(e, e);
			}
		}
		
		// remove bcpg:ingListSubIng
		i = 0;
		dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:ingList\" ", RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("remove bcpg:ingListSubIng: " + dataListNodeRefs.size());
		batches = Lists.partition(dataListNodeRefs, 500);
		for (final List<NodeRef> subList : batches) {
			try {
				i++;
				logger.info("Batch remove bcpg:ingListSubIng " + i + " over " + batches.size());
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

					@Override
					public Void execute() throws Throwable {
						for (NodeRef dataListNodeRef : subList) {
							if (nodeService.exists(dataListNodeRef)) {
								
								List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(dataListNodeRef, BeCPGModel.ASSOC_INGLIST_SUBING);
								if(!targetNodeRefs.isEmpty()){	
									
									logger.info("remove bcpg:ingListSubIng for ingList " + dataListNodeRef);
									for(NodeRef targetNodeRef : targetNodeRefs){
										
										NodeRef parentNodeRef = nodeService.getPrimaryParent(dataListNodeRef).getParentRef();
										Map<QName, Serializable> properties = new HashMap<>();
										String name = GUID.generate();
										properties.put(ContentModel.PROP_NAME, name);
										NodeRef ingList = nodeService.createNode(parentNodeRef, 
												ContentModel.ASSOC_CONTAINS, 
												QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
														QName.createValidLocalName(name)), 
														BeCPGModel.TYPE_INGLIST, properties).getChildRef();
										associationService.update(ingList, BeCPGModel.ASSOC_INGLIST_ING, targetNodeRef);
										nodeService.setProperty(ingList, BeCPGModel.PROP_PARENT_LEVEL, dataListNodeRef);
									}			
									associationService.update(dataListNodeRef, BeCPGModel.ASSOC_INGLIST_SUBING, new ArrayList<NodeRef>());
								}																									
							} else {
								logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
							}
						}
						return null;
					}

				}, false, true);
			} catch (Throwable e) {
				logger.warn(e, e);
			}
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}
}
