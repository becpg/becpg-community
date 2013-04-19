package fr.becpg.repo.migration.impl;

import java.util.List;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.entity.version.EntityVersion;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

//MOVE that to admin.patch
@Deprecated
public class EntityFolderMigrator {

	private static Log logger = LogFactory.getLog(EntityFolderMigrator.class);
	private static int BATCH_SIZE = 50;

	private BeCPGSearchService beCPGSearchService;

	private FileFolderService fileFolderService;

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private TransactionService transactionService;

	private EntityTplService entityTplService;

	private EntityService entityService;

	private EntityListDAO entityListDAO;

	private VersionService versionService;

	private BehaviourFilter policyBehaviourFilter;
	
	private EntityVersionService entityVersionService;

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	@SuppressWarnings("deprecation")
	public void migrate() {

		// search for entity and entityTplAspect
		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY))
				+ LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITY_TPL));

		List<NodeRef> entityTplNodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entityTplNodeRefs.size() + " entity templates to migrate");

		if (!entityTplNodeRefs.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(entityTplNodeRefs, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								try {
									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);

									for (NodeRef entityTplNodeRef : batchList) {
										if (nodeService.exists(entityTplNodeRef)) {

											nodeService.setProperty(entityTplNodeRef, ContentModel.PROP_NAME,
													GUID.generate());
											QName type = (QName) nodeService.getProperty(entityTplNodeRef,
													BeCPGModel.PROP_ENTITY_TPL_CLASS_NAME);

											NodeRef newEntityTplNodeRef = entityTplService.createEntityTpl(nodeService
													.getPrimaryParent(entityTplNodeRef).getParentRef(), type, true,
													null, null);

											entityListDAO.moveDataLists(entityTplNodeRef, newEntityTplNodeRef);
											nodeService.deleteNode(entityTplNodeRef);

											// folder tpl to migrate
											List<NodeRef> entityFolderTplNodeRefs = beCPGSearchService
													.luceneSearch(String
															.format(" +PATH:\"/app:company_home/cm:System/cm:FolderTemplates//*\" +TYPE:\"bcpg:entityFolder\" +@bcpg\\:entityTplClassName:\"%s\" +@bcpg\\:entityTplEnabled:true",
																	type));
											NodeRef entityFolderTplNodeRef = entityFolderTplNodeRefs != null
													&& !entityFolderTplNodeRefs.isEmpty() ? entityFolderTplNodeRefs.get(0)
													: null;
											if (entityFolderTplNodeRef != null) {
												entityService.moveFiles(entityFolderTplNodeRef, newEntityTplNodeRef);
												nodeService.deleteNode(entityFolderTplNodeRef);
											}
										}
									}
								} finally {
									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								}
								return true;
							}
						}, false, true);
			}

		}

		// remove system folders
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Exception {

				try {
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);

					List<NodeRef> folderNodeRefs = beCPGSearchService.luceneSearch(
							"+PATH:\"/app:company_home/cm:System/cm:FolderTemplates\"",
							RepoConsts.MAX_RESULTS_SINGLE_VALUE);
					if (!folderNodeRefs.isEmpty()) {
						nodeService.deleteNode(folderNodeRefs.get(0));
					}
				} finally {
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				}
				return true;
			}
		}, false, true);

		query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY_V2))
				+ LuceneHelper.exclude(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY_FOLDER))
				+ LuceneHelper.mandatory(LuceneHelper.getCondAspect(RenditionModel.ASPECT_RENDITIONED));

		// search for entities to migrate
		List<NodeRef> entitiesNodeRef = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entitiesNodeRef.size() + " nodes to migrate");

		if (!entitiesNodeRef.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(entitiesNodeRef, BATCH_SIZE)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								try {
									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);

									return doMigrate(batchList);

								} finally {
									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								}
							}
						}, false, true);

			}
		}

		// bug fix bcpg:entityListsAspect on bcpg:entityListItem
		query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITYLIST_ITEM))
				+ LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS));

		List<NodeRef> entityListItems = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entityListItems.size() + " entityListItems to fix");

		if (!entityListItems.isEmpty()) {

			int batchId=1;
			
			for (final List<NodeRef> batchList : Lists.partition(entityListItems, BATCH_SIZE)) {
				
				logger.info("entityListItems to fix, batch " + batchId);
				batchId++;
				
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								try {
									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);

									for (NodeRef n : batchList) {
										if (nodeService.exists(n)) {
											nodeService.removeAspect(n, BeCPGModel.ASPECT_ENTITYLISTS);
										}
									}

								} finally {
									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								}
								return true;
							}
						}, false, true);
			}
		}
		
		// fix bcpg:entityVersionable
		query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITY_VERSIONABLE));

		List<NodeRef> entityVersionableToFix = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entityVersionableToFix.size() + " entityVersionable to fix");

		if (!entityVersionableToFix.isEmpty()) {

			int batchId=1;
			
			for (final List<NodeRef> batchList : Lists.partition(entityVersionableToFix, BATCH_SIZE)) {
				
				logger.info("entityListItems to fix, batch " + batchId);
				batchId++;
				
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								try {
									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);

									for (NodeRef n : batchList) {
										if (nodeService.exists(n)) {

											// delete when there is one version on entity if it is not a composite version
											// because entityVersion has cm:versionable (due to copy, like alfresco version) and don't have a versionHistory
											if (!nodeService.hasAspect(n, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
												VersionHistory versionHistory = versionService.getVersionHistory(n);
												if (versionHistory == null
														|| (versionHistory != null && versionHistory.getAllVersions()
																.size() < 2)) {
													if (nodeService.hasAspect(n, ContentModel.ASPECT_VERSIONABLE)) {
														logger.debug("remove aspect versionable " + n);
														nodeService.removeAspect(n, ContentModel.ASPECT_VERSIONABLE);
													}
												}
											}

											if (nodeService.hasAspect(n, BeCPGModel.ASPECT_ENTITY_VERSIONABLE)) {
												logger.debug("remove aspect entity versionable " + n);
												nodeService.removeAspect(n, BeCPGModel.ASPECT_ENTITY_VERSIONABLE);
											}
										}
									}

								} finally {
									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								}
								return true;
							}
						}, false, true);
			}
		}

		// migrate cm:versionLabel to bcpg:versionLabel for compositeVersion
		query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS))+
				LuceneHelper.mandatory(LuceneHelper.getCondAspect(ContentModel.ASPECT_VERSIONABLE))+
				LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION));

		entityVersionableToFix = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);
		logger.info("migrate cm:versionLabel to bcpg:versionLabel. Found " + entityVersionableToFix.size());
		
		if (!entityVersionableToFix.isEmpty()) {

			int batchId=1;
			
			for (final List<NodeRef> batchList : Lists.partition(entityVersionableToFix, BATCH_SIZE)) {
				
				logger.info("entityListItems to fix, batch " + batchId);
				batchId++;
				
				transactionService.getRetryingTransactionHelper().doInTransaction(
						new RetryingTransactionCallback<Boolean>() {
							public Boolean execute() throws Exception {

								try {
									policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
									
									for (NodeRef n : batchList) {
										if (nodeService.exists(n)) {
											
											VersionHistory versionHistory = versionService.getVersionHistory(n);								

											// if 1 version or null
											if (versionHistory == null
													|| versionHistory.getAllVersions().size() == 1) {
												logger.warn("This node has no version or only one " + n);
												break;
											}
											
											NodeRef entityVersionHistory = entityVersionService.getVersionHistoryNodeRef(n);
											if(entityVersionHistory==null){
												logger.warn("This node has no entityVersionHistory  " + n);
												break;
											}
											
											List<NodeRef> entityVersionNodeRefs = entityVersionService.buildVersionHistory(entityVersionHistory, n);
											
											if(versionHistory.getAllVersions().size() != entityVersionNodeRefs.size()){
												logger.warn("versionHistory.getAllVersions().size() != entityVersions.size() :  " + n + " - " + 
														versionHistory.getAllVersions().size() + " - " +
														entityVersionNodeRefs.size());
											}
											else{
												// migrate
												int i=versionHistory.getAllVersions().size()-1;
												
												for(Version version : versionHistory.getAllVersions()){
													
													NodeRef entityVersionNodeRef = entityVersionNodeRefs.get(i);
													logger.debug("Entity " + n + " entityVersion " + entityVersionNodeRef + " set bcpg:versionLabel " + version.getVersionLabel());													
													nodeService.setProperty(entityVersionNodeRef, BeCPGModel.PROP_VERSION_LABEL, version.getVersionLabel());
													i--;
												}												
											}
										}
									}

								} finally {
									policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
									policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								}
								return true;
							}
						}, false, true);
			}
		}
		
		// some entityVersionHistory may exist even entity has been removed -> delete entityVersionHistory
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
			public Boolean execute() throws Exception {

				NodeRef entitiesHistoryNodeRef = entityVersionService.getEntitiesHistoryFolder();
				if (entitiesHistoryNodeRef == null) {
					logger.warn("No entitiesHistoryNodeRef exists");
					return null;
				}

				List<ChildAssociationRef> entityVersionHistoryChildAssocs = nodeService.getChildAssocs(entitiesHistoryNodeRef,
						ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

				for (ChildAssociationRef entityVersionHistoryChildAssoc : entityVersionHistoryChildAssocs) {
					NodeRef entityVersionHistoryNodeRef = entityVersionHistoryChildAssoc.getChildRef();
					
					NodeRef entityNodeRef = new NodeRef(RepoConsts.SPACES_STORE, (String) nodeService.getProperty(
							entityVersionHistoryNodeRef, ContentModel.PROP_NAME));

					if (!nodeService.exists(entityNodeRef)) {
						// remove folder since node doesn't exist anymore
						logger.debug("remove folder " + entityVersionHistoryNodeRef + " since node " + entityNodeRef
								+ " doesn't exist anymore");
						nodeService.deleteNode(entityVersionHistoryNodeRef);
					}
				}

				return true;
			}
		}, false, true);

	}

	private Boolean doMigrate(List<NodeRef> entitiesNodeRef) {

		// check parent is entityFolder and has the same name
		for (NodeRef entityNodeRef : entitiesNodeRef) {
			if (nodeService.exists(entityNodeRef)) {

				if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_CHECKED_OUT)) {
					logger.error("Node is checked out " + entityNodeRef);
				} else if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
					logger.error("Node is a working copy " + entityNodeRef);
				} else {

					// remove some properties on entity (CONTENT, thumbnail,...)
					logger.debug("remove aspects");
					if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION)) {
						nodeService.removeAspect(entityNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION);
					}
					nodeService.removeProperty(entityNodeRef, ContentModel.PROP_CONTENT);
					if (nodeService.hasAspect(entityNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE)) {
						nodeService.removeAspect(entityNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE);
					}
					if (nodeService.hasAspect(entityNodeRef, RenditionModel.ASPECT_RENDITIONED)) {
						nodeService.removeAspect(entityNodeRef, RenditionModel.ASPECT_RENDITIONED);
					}

					QName entityType = nodeService.getType(entityNodeRef);

					NodeRef entityFolderNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
					QName parentType = nodeService.getType(entityFolderNodeRef);

					String entityName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					String parentName = (String) nodeService.getProperty(entityFolderNodeRef, ContentModel.PROP_NAME);

					if (BeCPGModel.TYPE_ENTITY_FOLDER.equals(parentType)) {

						if (entityName != null && !entityName.equals(parentName)) {
							logger.warn("EntityName and entityFolderName are not the same. EntityName: " + entityName
									+ " - entityFolderName: " + parentName);
						}

						logger.info("migrate entity " + entityName + " - " + entityNodeRef);

						try {

							// rename entityFolder, move entity as sibling of
							// entityFolder and delete entityFolder
							nodeService.setProperty(entityFolderNodeRef, ContentModel.PROP_NAME, GUID.generate());
							NodeRef parentNodeRef = nodeService.getPrimaryParent(entityFolderNodeRef).getParentRef();
							fileFolderService.move(entityNodeRef, parentNodeRef, entityName);

							// move sub-folders of entityFolder under entity
							List<FileInfo> fileInfos = fileFolderService.list(entityFolderNodeRef);
							for (FileInfo fileInfo : fileInfos) {
								// is it another entity ?
								if (dictionaryService.isSubClass(nodeService.getType(fileInfo.getNodeRef()),
										BeCPGModel.TYPE_ENTITY_V2)) {
									fileFolderService.move(fileInfo.getNodeRef(), parentNodeRef, fileInfo.getName());
								} else {
									fileFolderService.move(fileInfo.getNodeRef(), entityNodeRef, fileInfo.getName());
								}
							}

							// delete reports that don't have the type
							// rep:report
							List<AssociationRef> reports = nodeService.getTargetAssocs(entityFolderNodeRef,
									ReportModel.ASSOC_REPORTS);
							for (AssociationRef report : reports) {
								if (!ReportModel.TYPE_REPORT.equals(nodeService.getType(report.getTargetRef()))) {
									nodeService.deleteNode(report.getTargetRef());
									nodeService.removeAssociation(entityNodeRef, report.getTargetRef(),
											ReportModel.ASSOC_REPORTS);
								}
							}

							// delete entityFolder
							nodeService.deleteNode(entityFolderNodeRef);

						} catch (FileExistsException e) {
							logger.error("Failed to migrate entity " + entityName, e);
						} catch (FileNotFoundException e) {
							logger.error("Failed to migrate entity " + entityName, e);
						}
					}

					// add aspect bcpg:entityTplRefAspect
					if ((dictionaryService.isSubClass(entityType, BeCPGModel.TYPE_PRODUCT) || dictionaryService
							.isSubClass(entityType, ProjectModel.TYPE_PROJECT))
							&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)) {
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF, null);
					}
				}
			}

		}
		return true;
	}
}
