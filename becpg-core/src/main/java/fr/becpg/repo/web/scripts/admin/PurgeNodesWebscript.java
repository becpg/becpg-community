package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Purge all nodes in specific folderNodeRef
 *
 * @author matthieu
 * @version $Id: $Id
 * TODO use batch Service
 */
@Deprecated
public class PurgeNodesWebscript extends AbstractWebScript {

	private static  final  Log logger = LogFactory.getLog(PurgeNodesWebscript.class);

	private static final  String PARAM_FOLDER_NODEREF = "folderNodeRef";

	private TransactionService transactionService;

	private RuleService ruleService;

	private BehaviourFilter policyBehaviourFilter;

	private EntityDictionaryService entityDictionaryService;
	
	private TenantService tenantService;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>transactionService</code>.</p>
	 *
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>Setter for the field <code>tenantService</code>.</p>
	 *
	 * @param tenantService a {@link org.alfresco.repo.tenant.TenantService} object.
	 */
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}



	private static final int BATCH_THREADS = 3;
	private static final int BATCH_SIZE = 40;

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String nodeRef = req.getParameter(PARAM_FOLDER_NODEREF);
		if ((nodeRef != null) && (nodeRef.length() > 0)) {
			NodeRef parentNodeRef = new NodeRef(nodeRef);
			if (nodeService.exists(parentNodeRef)) {
				
				final String currentUserDomain = tenantService.getCurrentUserDomain();

				BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
					List<NodeRef> result = new ArrayList<>();
					int currentPage = 1;

					@Override
					public int getTotalEstimatedWorkSize() {
						return result.size();
					}
					
					@Override
					public long getTotalEstimatedWorkSizeLong() {
						return getTotalEstimatedWorkSize();
					}

					@Override
					public Collection<NodeRef> getNextWork() {

						int skipOffset = (currentPage - 1) * BATCH_SIZE;
						int requestTotalCountMax = skipOffset + BATCH_SIZE;

						result.clear();

						PagingRequest pageRequest = new PagingRequest(skipOffset, BATCH_SIZE, null);
						pageRequest.setRequestTotalCountMax(requestTotalCountMax);

						BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().parent(parentNodeRef);

						for (QName subType : entityDictionaryService.getSubTypes(BeCPGModel.TYPE_ENTITY_V2)) {
							query.inType(subType);
						}

						PagingResults<NodeRef> page = query.childFileFolders(pageRequest);

						if (page != null) {
							result.addAll(page.getPage());
						}
						currentPage++;

						return result;

					}
				};

				BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("PurgeNodesWebscript",
						transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE, null, logger, 1000);

				BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

					@Override
					public void afterProcess() throws Throwable {
						
					}

					@Override
					public void beforeProcess() throws Throwable {
					
					}

					@Override
					public String getIdentifier(NodeRef entry) {
						return entry.toString();
					}

					@Override
					public void process(NodeRef nodeRef) throws Throwable {
						
		                   AuthenticationUtil.runAs(new RunAsWork<String>()
		                    {
		                        public String doWork() throws Exception
		                        {
		                        	ruleService.disableRules();
		    						if (nodeService.exists(nodeRef)) {
		    							//TODO bug here disabling behaviour make association cache not working
		    							policyBehaviourFilter.disableBehaviour();
		    							try {
		    								nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, new HashMap<>());
		    								nodeService.deleteNode(nodeRef);
		    							} finally {
		    								policyBehaviourFilter.enableBehaviour();
		    							}
		    						} else {
		    							logger.warn("nodeRef doesn't exist : " + nodeRef);
		    						}
		    						ruleService.enableRules();
		    						
		    						return "OK";
		    						
		                        }
		                    }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), currentUserDomain));          
						
						

					}

				};

				batchProcessor.process(worker, true);

			} else {
				throw new WebScriptException("folderNodeRef is mandatory");
			}

		}

	}

}
