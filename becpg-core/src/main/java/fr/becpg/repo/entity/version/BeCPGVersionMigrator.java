package fr.becpg.repo.entity.version;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

@SuppressWarnings("deprecation")
public class BeCPGVersionMigrator {

	private static Log logger = LogFactory.getLog(BeCPGVersionMigrator.class);
	
	private EntityVersionService entityVersionService;
	
	private NodeService nodeService;
	
	private VersionService versionService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	private NodeService dbNodeService;	
	
	private BeCPGSearchService beCPGSearchService;
	
	private TransactionService transactionService;
	
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setDbNodeService(NodeService dbNodeService) {
		this.dbNodeService = dbNodeService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void migrateVersionHistory(){
		//remove compositeVersion on entity
		String query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS))+
				LuceneHelper.mandatory(LuceneHelper.getCondAspect(ContentModel.ASPECT_VERSIONABLE))+
				LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)) +
				" -PATH:\"/bcpg:entitiesHistory//*\"";
		
		List<NodeRef> entityVersionableToFixList = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("remove compositeVersion on entities. Found " + entityVersionableToFixList.size());
		
		for(NodeRef entityVersionableToFix : entityVersionableToFixList){
			if(nodeService.exists(entityVersionableToFix)){
				nodeService.removeAspect(entityVersionableToFix, BeCPGModel.ASPECT_COMPOSITE_VERSION);
			}
		}
		
		logger.debug("migrateVersionHistory");
		NodeRef versionHistoryNodeRef = entityVersionService.getEntitiesHistoryFolder();
		
		if (versionHistoryNodeRef == null) {
			logger.info("No version to migrate, since versionHistoryNodeRef is null");
			return;
		}
		
		// look for versionHistory of entity
		List<ChildAssociationRef> folderChilAssocs = nodeService.getChildAssocs(versionHistoryNodeRef);
		
		logger.info("entity to migrate size: " + folderChilAssocs.size());
		
		VersionNumber prevVersionNumber = new VersionNumber("0.0");
		for(ChildAssociationRef folderChildAssoc : folderChilAssocs){
			
			NodeRef folderNodeRef = folderChildAssoc.getChildRef();
			logger.info("folder: " + folderNodeRef);
			
			// folders for versions
			NodeRef entityNodeRef = new NodeRef(RepoConsts.SPACES_STORE, (String)nodeService.getProperty(folderNodeRef, ContentModel.PROP_NAME));
			if(nodeService.exists(entityNodeRef)){
				logger.info("entityNodeRef: " + entityNodeRef);
								
				List<NodeRef> evNodeRefList = buildVersionHistory(folderNodeRef, entityNodeRef);
							
				for(NodeRef evNodeRef : evNodeRefList){
					
					logger.info("evNodeRef: " + evNodeRef);				
					
					if(nodeService.hasAspect(evNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
						
						String versionLabel = (String)nodeService.getProperty(evNodeRef, BeCPGModel.PROP_VERSION_LABEL);												
						logger.info("versionLabel: " + versionLabel);
						
						if(versionLabel != null){
						
							// has already a version ?							
							VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
							Version version = null;							
							try{
								if(versionHistory !=null){
									version = versionHistory.getVersion(versionLabel);
								}								
							}
							catch(VersionDoesNotExistException e){								
								logger.info("Version doesn't exist, we will create it");
							}
							
							// create version
							if(version == null){
							
								version = createVersion(entityNodeRef, evNodeRef, prevVersionNumber, versionLabel);
								prevVersionNumber = new VersionNumber(version.getVersionLabel());
								
								migrateSystemProperties(version.getFrozenStateNodeRef(), evNodeRef);
							}														
						}						
						else{
							logger.warn("Entity doesn't exist: " + entityNodeRef);
						}					
					}
				}				
			}			
		}
		
		
//		// migrate audit props
//		query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS))+
//				LuceneHelper.mandatory(LuceneHelper.getCondAspect(ContentModel.ASPECT_VERSIONABLE))+
//				LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));
//
//		List<NodeRef> entityVersionableToFix = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
//		logger.info("migrate audit props. Found " + entityVersionableToFix.size());
//		
//		if (!entityVersionableToFix.isEmpty()) {
//
//			int batchId=1;
//			
//			for (final List<NodeRef> batchList : Lists.partition(entityVersionableToFix, 50)) {
//				
//				logger.info("entityListItems to fix, batch " + batchId);
//				batchId++;
//				
//				transactionService.getRetryingTransactionHelper().doInTransaction(
//						new RetryingTransactionCallback<Boolean>() {
//							public Boolean execute() throws Exception {
//
//								try {
//									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
//									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
//									
//									for (NodeRef n : batchList) {
//										if (nodeService.exists(n)) {
//											
//											logger.info("entity " + n);
//											
//											VersionHistory versionHistory = versionService.getVersionHistory(n);								
//
//											// if 1 version or null
//											if (versionHistory == null
//													|| versionHistory.getAllVersions().size() == 1) {
//												logger.warn("This node has no version or only one " + n);
//												break;
//											}
//											
//											NodeRef entityVersionHistory = entityVersionService.getVersionHistoryNodeRef(n);
//											if(entityVersionHistory==null){
//												logger.warn("This node has no entityVersionHistory  " + n);
//												break;
//											}
//											
//											List<NodeRef> entityVersionNodeRefs = entityVersionService.buildVersionHistory(entityVersionHistory, n);
//											
//											if(versionHistory.getAllVersions().size() != entityVersionNodeRefs.size()){
//												logger.warn("versionHistory.getAllVersions().size() != entityVersions.size() :  " + n + " - " + 
//														versionHistory.getAllVersions().size() + " - " +
//														entityVersionNodeRefs.size());
//											}
//											else{
//												// migrate
//												int i=versionHistory.getAllVersions().size()-1;
//												
//												for(Version version : versionHistory.getAllVersions()){
//													
//													NodeRef entityVersionNodeRef = entityVersionNodeRefs.get(i);
////													logger.debug("Entity " + n + " entityVersion " + entityVersionNodeRef + " set bcpg:versionLabel " + version.getVersionLabel());													
////													nodeService.setProperty(entityVersionNodeRef, BeCPGModel.PROP_VERSION_LABEL, version.getVersionLabel());
//													
//													NodeRef nodeRef = new NodeRef("workspace", "version2Store", version.getFrozenStateNodeRef().getId());
//													dbNodeService.setProperty(nodeRef, ContentModel.PROP_CREATED, 
//															nodeService.getProperty(entityVersionNodeRef, ContentModel.PROP_CREATED));
//													i--;
//												}												
//											}
//										}
//									}
//
//								} finally {
//									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
//									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
//								}
//								return true;
//							}
//						}, false, true);
//			}
//		}
	}
	
	private Version createVersion(NodeRef entityNodeRef, NodeRef evNodeRef, VersionNumber prevVersionNumber, String versionLabel){
		
		Version version;
		QName type = nodeService.getType(entityNodeRef);
		
		try{
			// disable policy on type to disable version policies
			policyBehaviourFilter.disableBehaviour(type);	
			policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);	
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
			
            VersionNumber versionNumber = new VersionNumber(versionLabel);
			VersionType versionType = (prevVersionNumber.getPart(0) == versionNumber.getPart(0)) ? VersionType.MINOR : VersionType.MAJOR;
			
			
			Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
			versionProperties.put(VersionModel.PROP_VERSION_TYPE, versionType);
			versionProperties.put(VersionModel.PROP_DESCRIPTION, (String)nodeService.getProperty(evNodeRef, BeCPGModel.PROP_VERSION_DESCRIPTION));
							
			version = versionService.createVersion(entityNodeRef, versionProperties);
			logger.info("create version: " + entityNodeRef + " - versionType: " + versionType 
					+ " - VersionLabel: " + version.getVersionLabel()
					+ " - FrozenStateNodeRef: " + version.getFrozenStateNodeRef()
					+ " - VersionedNodeRef: " + version.getVersionedNodeRef());	
			
			logger.debug("nodeService exists " + nodeService.exists(version.getFrozenStateNodeRef()));
			logger.debug("dbNodeService exists " + dbNodeService.exists(version.getFrozenStateNodeRef()));
			
			// add/remove system props on entity version
			nodeService.setProperty(evNodeRef, ContentModel.PROP_VERSION_LABEL, (String)nodeService.getProperty(evNodeRef, BeCPGModel.PROP_VERSION_LABEL));
			nodeService.removeProperty(evNodeRef, BeCPGModel.PROP_VERSION_DESCRIPTION);			
			
		}
		finally{
			policyBehaviourFilter.enableBehaviour(type);
			policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);	
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
		}
		
		return version;
	}
	
	private void migrateSystemProperties(NodeRef versionNodeRef, NodeRef evNodeRef){
		
		/*
		 * setProperty : this operation is not supported by a version store implementation of the node service
		 * we cannot do anything to update system properties
		 */
		
		Map<QName, Serializable> props = nodeService.getProperties(evNodeRef);
		
		for(Map.Entry<QName, Serializable> kv : props.entrySet()){
			
			if(kv.getKey().equals(ContentModel.PROP_NODE_REF) || 
					kv.getKey().equals(ContentModel.PROP_NODE_DBID) ||
					kv.getKey().equals(ContentModel.PROP_NODE_UUID) ||
					kv.getKey().equals(ContentModel.PROP_STORE_IDENTIFIER) ||
					kv.getKey().equals(ContentModel.PROP_STORE_NAME) ||
					kv.getKey().equals(ContentModel.PROP_STORE_PROTOCOL) ||
					kv.getKey().equals(ContentModel.PROP_CONTENT) ||
					kv.getKey().equals(ContentModel.PROP_VERSION_LABEL) ||
					kv.getKey().equals(ContentModel.PROP_AUTO_VERSION) ||
					kv.getKey().equals(ContentModel.PROP_AUTO_VERSION_PROPS)){									
				
				continue;
				}
			
			
			QName key = kv.getKey();
			
			if(key.isMatch(BeCPGModel.PROP_FROZEN_ACCESSED)){
				key = Version2Model.PROP_QNAME_FROZEN_ACCESSED;
			}
			else if(key.isMatch(BeCPGModel.PROP_FROZEN_CREATED)){
				key = Version2Model.PROP_QNAME_FROZEN_CREATED;
			}
			else if(key.isMatch(BeCPGModel.PROP_FROZEN_CREATOR)){
				key = Version2Model.PROP_QNAME_FROZEN_CREATOR;
			}
			else if(key.isMatch(BeCPGModel.PROP_FROZEN_MODIFIED)){
				key = Version2Model.PROP_QNAME_FROZEN_MODIFIED;
			}
			else if(key.isMatch(BeCPGModel.PROP_FROZEN_MODIFIER)){
				key = Version2Model.PROP_QNAME_FROZEN_MODIFIER;
			}
			else if(key.isMatch(BeCPGModel.PROP_FROZEN_NODE_DBID)){
				key = Version2Model.PROP_QNAME_FROZEN_NODE_DBID;
			}
//			else if(key.isMatch(BeCPGModel.PROP_FROZEN_NODE_REF)){
//				key = Version2Model.PROP_QNAME_FROZEN_NODE_REF;
//			}
			else if(key.isMatch(BeCPGModel.PROP_INITIAL_VERSION)){
				continue;
			}
			else if(key.isMatch(BeCPGModel.PROP_VERSION_LABEL)){
				continue;
			}	    											
			
			logger.debug("force prop: " + key + " - " + kv.getValue());	
			NodeRef nodeRef = new NodeRef("workspace", "version2Store", versionNodeRef.getId());
			dbNodeService.setProperty(nodeRef, key, kv.getValue());
		}
	}
	
	/**
	 * Get the versions sort by date and node-ide.
	 * 
	 * @param versionHistoryRef
	 *            the version history ref
	 * @param nodeRef
	 *            the node ref
	 * @return the list
	 */
	private List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef) {

		List<ChildAssociationRef> versionAssocs = nodeService.getChildAssocs(versionHistoryRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL,true);
		List<NodeRef> versionRefs = new ArrayList<NodeRef>();

		for (ChildAssociationRef versionAssoc : versionAssocs) {

			versionRefs.add(versionAssoc.getChildRef());
		}

		// sort versions by node id
		Collections.sort(versionRefs, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef v1, NodeRef v2) {
				Date modifiedDateV1 = (Date) nodeService.getProperty(v1, BeCPGModel.PROP_FROZEN_MODIFIED);
				Date modifiedDateV2 = (Date) nodeService.getProperty(v2, BeCPGModel.PROP_FROZEN_MODIFIED);
				int result = 0;
				if(modifiedDateV1 != null && modifiedDateV2 != null){
					result = modifiedDateV1.compareTo(modifiedDateV2);
					if (result == 0) {
						Long dbid1 = (Long) nodeService.getProperty(v1, ContentModel.PROP_NODE_DBID);
						Long dbid2 = (Long) nodeService.getProperty(v2, ContentModel.PROP_NODE_DBID);

						if (dbid1 != null && dbid2 != null) {
							result = dbid1.compareTo(dbid2);
						} else {
							result = 0;

							if (logger.isWarnEnabled()) {
								logger.warn("node-dbid property is missing for versions: " + v1.toString() + " or "
										+ v2.toString());
							}
						}
					}					
				}
				
				return result;				
			}

		});

		return versionRefs;

	}
}
