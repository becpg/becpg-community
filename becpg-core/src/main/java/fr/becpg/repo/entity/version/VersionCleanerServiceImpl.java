package fr.becpg.repo.entity.version;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service("versionCleanerService")
public class VersionCleanerServiceImpl implements VersionCleanerService {

	private static final String DEFAULT = "default";

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
	
	@Autowired
	private AssociationService associationService;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;
	
	@Override
	public boolean cleanVersions(int maxProcessedNodes, String path) {
		
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		
		try {
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
			convertAndDeleteVersions(maxProcessedNodes, DEFAULT, path);
			
			if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
				@SuppressWarnings("deprecation")
				List<Tenant> tenants = tenantAdminService.getAllTenants();
				for (Tenant tenant : tenants) {
					String tenantDomain = tenant.getTenantDomain();
					AuthenticationUtil.clearCurrentSecurityContext();
					AuthenticationUtil.setFullyAuthenticatedUser(tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
					convertAndDeleteVersions(maxProcessedNodes, tenantDomain, path);
				}
			}
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
			if(currentUser!=null) {
				AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
			}
		}
		
		return true;
	}
	
	private class CleanVersionWorkProvider implements BatchProcessWorkProvider<NodeRef>{

		private int maxProcessedNodes;
		private List<NodeRef> initialList = new ArrayList<>();
		private Set<NodeRef> toTreat = new LinkedHashSet<>();
		private Set<NodeRef> treated = new LinkedHashSet<>();
		private List<NodeRef> nextWork = new ArrayList<>();
		private Calendar cal = Calendar.getInstance();
		private String path;
		
		public CleanVersionWorkProvider(int maxProcessedNodes, String path) {
			super();
			this.maxProcessedNodes = maxProcessedNodes;
			this.path = path;
			
			if (path == null) {
				path = RepoConsts.ENTITIES_HISTORY_XPATH;
			}
			
			NodeRef parentNode = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE), path);
			
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parentNode, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, maxProcessedNodes, false);
			
			for (ChildAssociationRef childAssoc : childAssocs) {
				
				List<ChildAssociationRef> subChildAssocs = nodeService.getChildAssocs(childAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, maxProcessedNodes, false);
				
				if (subChildAssocs.isEmpty()) {
					deleteNode(childAssoc.getChildRef());
					logger.debug("delete empty folder : " + childAssoc.getChildRef());
				}
				
				for (ChildAssociationRef subChildAssoc : subChildAssocs) {
					
					if (entityDictionaryService.isSubClass(nodeService.getType(subChildAssoc.getChildRef()), BeCPGModel.TYPE_ENTITY_V2)) {
						initialList.add(subChildAssoc.getChildRef());
					}
					
					if (initialList.size() >= maxProcessedNodes) {
						break;
					}
				}
				if (initialList.size() >= maxProcessedNodes) {
					break;
				}
			}
			
			cal.add(Calendar.DAY_OF_YEAR, -1);
		}

		@Override
		public int getTotalEstimatedWorkSize() {
			return initialList.size();
		}
		
		@Override
		public Collection<NodeRef> getNextWork() {
			
			nextWork.clear();
			
			for (NodeRef node : toTreat) {
				if (nextWork.size() >= BatchInfo.BATCH_SIZE) {
					break;
				}
				nextWork.add(node);
				treated.add(node);
			}
			
			toTreat.removeAll(nextWork);
			
			if (nextWork.size() < BatchInfo.BATCH_SIZE) {
				for (NodeRef initialNode : initialList) {
					
					if (nodeService.exists(initialNode)) {
						
						logger.trace("find convertible relatives of " + initialNode);
						
						Set<NodeRef> convertibleRelatives = entityFormatService.findConvertibleRelatives(initialNode, new HashSet<>(), nextWork, maxProcessedNodes, new AtomicInteger(treated.size() + toTreat.size()), path);
						
						for (NodeRef convertibleRelative : convertibleRelatives) {
							
							if (treated.size() + toTreat.size() >= maxProcessedNodes) {
								break;
							}
							
							if (!toTreat.contains(convertibleRelative) && !treated.contains(convertibleRelative)) {
								Date modified = (Date) nodeService.getProperty(convertibleRelative, ContentModel.PROP_MODIFIED);
								if (cal.getTime().compareTo(modified) > 0) {
									if (nextWork.size() < BatchInfo.BATCH_SIZE) {
										nextWork.add(convertibleRelative);
										treated.add(convertibleRelative);
									} else {
										toTreat.add(convertibleRelative);
									}
								}
							}
						}
					}
				}
			}
					
			return nextWork;
		}
	}



	private void convertAndDeleteVersions(int maxProcessedNodes, String tenantDomain, String path) {
		BatchInfo batchInfo = new BatchInfo("cleanVersions", "becpg.batch.versionCleaner.cleanVersions." + tenantDomain);
		batchInfo.setRunAsSystem(true);

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {

				if (nodeService.exists(entityNodeRef)) {
					if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_TEMPORARY)) {
						deleteTemporaryNode(entityNodeRef);
					} else {
						try {
							convertNode(entityNodeRef);
						} catch (Throwable t) {
							if (RetryingTransactionHelper.extractRetryCause(t) == null) {

								transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									
									entityFormatService.moveToImportToDoFolder(entityNodeRef);
									
									nodeService.removeAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION);
								
									return null;
								}, false, true);
							}
							throw t;
						}
							
					}
				} else {
					logger.debug("Node already deleted : " + entityNodeRef + ", tenant : " + tenantDomain);
				}

			}

		};

		batchQueueService.queueBatch(batchInfo, new CleanVersionWorkProvider(maxProcessedNodes, path), processWorker, null);

	}

	@Override
	public void cleanVersionStore() {
		
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
		
		try {
			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
			cleanOrphanVersions(DEFAULT);
			
			if ((tenantAdminService != null) && tenantAdminService.isEnabled()) {
				@SuppressWarnings("deprecation")
				List<Tenant> tenants = tenantAdminService.getAllTenants();
				for (Tenant tenant : tenants) {
					String tenantDomain = tenant.getTenantDomain();
					AuthenticationUtil.clearCurrentSecurityContext();
					AuthenticationUtil.setFullyAuthenticatedUser(tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
					cleanOrphanVersions(tenantDomain);
				}
			}
		} finally {
			AuthenticationUtil.clearCurrentSecurityContext();
			if(currentUser!=null) {
				AuthenticationUtil.setFullyAuthenticatedUser(currentUser);
			}
		}
		
	}

	private void cleanOrphanVersions(String tenantDomain) {
		
		BatchInfo batchInfo = new BatchInfo("cleanOrphanVersions", "becpg.batch.versionCleaner.cleanOrphanVersions." + tenantDomain);
		batchInfo.setRunAsSystem(true);
		
		List<NodeRef> entityNodeRefs = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			NodeRef versionRootNode = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
			
			List<NodeRef> childAssocs = associationService.getChildAssocs(versionRootNode, Version2Model.CHILD_QNAME_VERSION_HISTORIES);
			
			return childAssocs.subList(0, Math.min(childAssocs.size(), MAX_PROCESSED_NODES) - 1);
			
		}, false, true);

		BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(entityNodeRefs);

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				NodeRef nodeToTest = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", name);
	
				if (nodeService.getChildAssocs(entityNodeRef).isEmpty()) {
					logger.debug("delete folder because it is empty : " + entityNodeRef + ", tenant : " + tenantDomain);
					deleteNode(entityNodeRef);
				} else if (!nodeService.exists(nodeToTest)) {
					List<ChildAssociationRef> versionAssocs = nodeService.getChildAssocs(entityNodeRef, Version2Model.CHILD_QNAME_VERSIONS,
							RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef versionAssoc : versionAssocs) {
						if (dictionaryService.isSubClass(nodeService.getType(versionAssoc.getChildRef()), BeCPGModel.TYPE_ENTITY_V2)) {
							logger.debug("reference node  doesn't exist : " + nodeToTest + ", delete version folder : " + entityNodeRef + ", tenant : " + tenantDomain);
							deleteNode(entityNodeRef);
							break;
						}
					}
	
				} else if(nodeService.hasAspect(nodeToTest, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
					logger.debug("Removing unneeded version folder : " + entityNodeRef + " for Composite version : " + nodeToTest + ", tenant : " + tenantDomain);
					deleteNode(entityNodeRef);
				} 
			}
		};
		
		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
	
	}

	private void deleteNode(NodeRef nodeRef) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);

			nodeService.deleteNode(nodeRef);
			return null;
		}, false, true);
	}
	
	private void deleteTemporaryNode(NodeRef temporaryNode) {

		String tenantDomain = DEFAULT;

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
			tenantDomain = tenantAdminService.getTenant(tenantAdminService.getCurrentUserDomain()).getTenantDomain();
		}
		if (nodeService.exists(temporaryNode)) {

			NodeRef parentNode = nodeService.getPrimaryParent(temporaryNode).getParentRef();

			String name = (String) nodeService.getProperty(temporaryNode, ContentModel.PROP_NAME);

			if (lockService.isLocked(temporaryNode)) {
				lockService.unlock(temporaryNode);
			}
			deleteNode(temporaryNode);
			logger.debug("deleted temporary version node : '" + name + "', tenant : " + tenantDomain);

			if (parentNode != null && nodeService.exists(parentNode) && nodeService.getChildAssocs(parentNode).isEmpty()) {
				if (lockService.isLocked(parentNode)) {
					lockService.unlock(parentNode);
				}
				deleteNode(parentNode);
				logger.debug("also deleted parent folder of '" + name + "' because it was empty, tenant : " + tenantDomain);
			}
		}

	}

	private void convertNode(NodeRef notConvertedNode) {
		
		String tenantDomain = DEFAULT;

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
			tenantDomain = tenantAdminService.getTenant(tenantAdminService.getCurrentUserDomain()).getTenantDomain();
		}

		String name = (String) nodeService.getProperty(notConvertedNode, ContentModel.PROP_NAME);

		NodeRef parentNode = nodeService.getPrimaryParent(notConvertedNode).getParentRef();

		String parentName = (String) nodeService.getProperty(parentNode, ContentModel.PROP_NAME);

		NodeRef originalNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentName);

		if (nodeService.exists(originalNode)) {
			logger.debug("Converting node " + notConvertedNode + ", tenant : " + tenantDomain);
			entityFormatService.convertVersionHistoryNodeRef(notConvertedNode);
		} else {
			logger.debug("deleting version history node : '" + name + "' because the original node doesn't exist anymore, tenant : " + tenantDomain);
			if (lockService.isLocked(notConvertedNode)) {
				lockService.unlock(notConvertedNode);
			}

			deleteNode(notConvertedNode);
		}

		if (parentNode != null && nodeService.exists(parentNode) && nodeService.getChildAssocs(parentNode).isEmpty()) {
			deleteNode(parentNode);
			logger.debug("also deleted parent folder of '" + name + "' because it was empty, tenant : " + tenantDomain);
		}
	}
	
}
