package fr.becpg.repo.entity.version;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("versionCleanerService")
public class VersionCleanerServiceImpl implements VersionCleanerService {

	private static final Log logger = LogFactory.getLog(VersionCleanerServiceImpl.class);

	@Autowired
	private EntityFormatService entityFormatService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private TenantAdminService tenantAdminService;

	@Autowired
	private LockService lockService;
	
	@Autowired
	private DictionaryService dictionaryService;
	
	@Autowired
	private BatchQueueService batchQueueService;

	@Override
	public boolean cleanVersions(int maxProcessedNodes) {
		AuthenticationUtil.runAsSystem(() -> internalCleanVersions(maxProcessedNodes));

		if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				AuthenticationUtil.runAs(() -> internalCleanVersions(maxProcessedNodes), tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
			}
		}
		
		return true;
	}
	
	private boolean internalCleanVersions(int maxProcessedNodes) {
		
		String tenantName = "default";
		
		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
			tenantName = tenantAdminService.getTenant(tenantAdminService.getCurrentUserDomain()).getTenantDomain();
		}

		cleanOrphanVersions(maxProcessedNodes, tenantName);
		
		cleanTemporaryNodes(maxProcessedNodes, tenantName);
		
		convertOldVersions(maxProcessedNodes, tenantName);
		
		return true;
	}

	private BatchProcessWorkProvider<NodeRef> createCleanOrphanVersionsProcessWorkProvider(int maxProcessedNodes) {
		List<NodeRef> entityNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef versionRootNode = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
	
			List<ChildAssociationRef> versionChildAssocs = nodeService.getChildAssocs(versionRootNode, Version2Model.CHILD_QNAME_VERSION_HISTORIES,
					RegexQNamePattern.MATCH_ALL);
			
			List<NodeRef> results = new ArrayList<>();
			
			for (ChildAssociationRef childAssoc : versionChildAssocs) {
				String name = (String) nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME);
				NodeRef nodeToTest = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", name);
	
				if (nodeService.getChildAssocs(childAssoc.getChildRef()).isEmpty()) {
					results.add(childAssoc.getChildRef());
				} else if (!nodeService.exists(nodeToTest)) {
					List<ChildAssociationRef> versionAssocs = nodeService.getChildAssocs(childAssoc.getChildRef(), Version2Model.CHILD_QNAME_VERSIONS,
							RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef versionAssoc : versionAssocs) {
						if (dictionaryService.isSubClass(nodeService.getType(versionAssoc.getChildRef()), BeCPGModel.TYPE_ENTITY_V2)) {
							results.add(childAssoc.getChildRef());
							break;
						}
					}
	
				} else if(nodeService.hasAspect(nodeToTest, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
					results.add(childAssoc.getChildRef());
				} 
				
				if (results.size() >= maxProcessedNodes) {
					break;
				}
				
			}
			
			NodeRef entityHistoryFolder= BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
					RepoConsts.ENTITIES_HISTORY_XPATH);
			
			List<ChildAssociationRef> entityHistoryChildAssocs = nodeService.getChildAssocs(entityHistoryFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
			
			for (ChildAssociationRef childAssoc : entityHistoryChildAssocs) {
				if (nodeService.getChildAssocs(childAssoc.getChildRef()).isEmpty()) {
					results.add(childAssoc.getChildRef());
				}
				
				if (results.size() >= maxProcessedNodes) {
					break;
				}
			}
			
			return results;
		}, true, true);
	
		return new EntityListBatchProcessWorkProvider<>(entityNodeRefs);
	
	}

	private void cleanOrphanVersions(int maxProcessedNodes, String tenantName) {
		
		BatchInfo batchInfo = new BatchInfo("cleanOrphanVersions", "becpg.batch.versionCleaner.cleanOrphanVersions." + tenantName);
		batchInfo.setRunAsSystem(true);
	
		BatchProcessWorkProvider<NodeRef> workProvider = createCleanOrphanVersionsProcessWorkProvider(maxProcessedNodes);
		
		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
	
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				NodeRef nodeToTest = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", name);
	
				if (nodeService.getChildAssocs(entityNodeRef).isEmpty()) {
					logger.info("delete folder because it is empty : " + entityNodeRef + ", tenant : " + tenantName);
				} else if (!nodeService.exists(nodeToTest)) {
					logger.info("reference node  doesn't exist : " + nodeToTest + ", delete version folder : " + entityNodeRef + ", tenant : " + tenantName);
				} else if(nodeService.hasAspect(nodeToTest, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
					logger.info("Removing unneeded version folder : " + entityNodeRef + " for Composite version : " + nodeToTest + ", tenant : " + tenantName);
				}
				
				deleteNode(entityNodeRef);
				
			}
			
		};
		
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
	
	}

	private BatchProcessWorkProvider<NodeRef> createCleanTemporaryNodesProcessWorkProvider(int maxProcessedNodes) {
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_YEAR, -1);
			
				List<NodeRef> entityNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					return BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
							.withAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage().maxResults(maxProcessedNodes)
							.andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'")
							.list();
				}, false, true);
			
				return new EntityListBatchProcessWorkProvider<>(entityNodeRefs);
		
			}

	private void cleanTemporaryNodes(int maxProcessedNodes, String tenantName) {
		
		BatchInfo batchInfo = new BatchInfo("cleanTemporaryNodes", "becpg.batch.versionCleaner.cleanTemporaryNodes." + tenantName);
		batchInfo.setRunAsSystem(true);
	
		BatchProcessWorkProvider<NodeRef> workProvider = createCleanTemporaryNodesProcessWorkProvider(maxProcessedNodes);
		
		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
	
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				deleteTemporaryNode(tenantName, entityNodeRef);
			}
			
		};
		
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
	
	}

	private BatchProcessWorkProvider<NodeRef> createConvertOldVersionsProcessWorkProvider(int maxProcessedNodes) {
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -1);
		
			List<NodeRef> entityNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				
				List<NodeRef> result = new ArrayList<>();
				
				List<NodeRef> queryResult = BeCPGQueryBuilder.createQuery().withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
						.excludeAspect(BeCPGModel.ASPECT_ENTITY_FORMAT).excludeAspect(ContentModel.ASPECT_TEMPORARY).inDB().ftsLanguage()
						.andBetween(ContentModel.PROP_MODIFIED, "MIN", "'" + ISO8601DateFormat.format(cal.getTime()) + "'")
						.list();
				
					for (NodeRef node : queryResult) {
						if (result.size() >= maxProcessedNodes) {
							break;
						}
						if (entityFormatService.checkWhereUsedBeforeConversion(node)) {
							result.add(node);
						}
					}
				
					return result;
			}, false, true);
		
			return new EntityListBatchProcessWorkProvider<>(entityNodeRefs);
		
		}

	private void convertOldVersions(int maxProcessedNodes, String tenantName) {
		
		BatchInfo batchInfo = new BatchInfo("convertOldVersions", "becpg.batch.versionCleaner.convertOldVersions." + tenantName);

		batchInfo.setRunAsSystem(true);
	
		BatchProcessWorkProvider<NodeRef> workProvider = createConvertOldVersionsProcessWorkProvider(maxProcessedNodes);
		
		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {
	
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				convertNode(tenantName, entityNodeRef);
			}
			
		};
		
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
	
	}

	private void deleteNode(NodeRef nodeRef) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			nodeService.deleteNode(nodeRef);
			return null;
		}, false, true);
	}
	
	private void deleteTemporaryNode(String tenantName, NodeRef temporaryNode) {
		long start = System.currentTimeMillis();
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			if (nodeService.exists(temporaryNode)) {
				
				NodeRef parentNode = nodeService.getPrimaryParent(temporaryNode).getParentRef();
				
				String name = (String) nodeService.getProperty(temporaryNode, ContentModel.PROP_NAME);
				
				if (lockService.isLocked(temporaryNode)) {
					lockService.unlock(temporaryNode);
				}
				nodeService.deleteNode(temporaryNode);
				long timeElapsed = System.currentTimeMillis() - start;
				logger.info("deleted temporary version node : '" + name + "', tenant : " + tenantName + ", time elapsed : " + timeElapsed + " ms");
				
				if (parentNode != null && nodeService.exists(parentNode) && nodeService.getChildAssocs(parentNode).isEmpty()) {
					if (lockService.isLocked(parentNode)) {
						lockService.unlock(parentNode);
					}
					nodeService.deleteNode(parentNode);
					logger.info("also deleted parent folder of '" + name + "' because it was empty, tenant : " + tenantName);
				}
			}
			
			return null;
		}, false, true);
	
	}

	private void convertNode(String tenantName, NodeRef notConvertedNode) {
		long start = System.currentTimeMillis();
		
		String name = (String) nodeService.getProperty(notConvertedNode, ContentModel.PROP_NAME);
		
		NodeRef parentNode = nodeService.getPrimaryParent(notConvertedNode).getParentRef();
		
		String parentName = (String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME);
		
		NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);
		
		if (nodeService.exists(originalNode)) {
			logger.info("Converting node " + notConvertedNode + ", tenant : " + tenantName);
			NodeRef convertedNode = entityFormatService.convertVersionHistoryNodeRef(notConvertedNode);
			if (convertedNode != null) {
				long timeElapsed = System.currentTimeMillis() - start;
				logger.info("Converted entity '" + name + "', from " + notConvertedNode + " to " + convertedNode + ", tenant : " + tenantName
						+ ", time elapsed : " + timeElapsed + " ms");
			}
		} else {
			logger.info("deleting version history node : '" + name + "' because the original node doesn't exist anymore, tenant : " + tenantName);
			if (lockService.isLocked(notConvertedNode)) {
				lockService.unlock(notConvertedNode);
			}
			
			deleteNode(notConvertedNode);
		}
	}

}
