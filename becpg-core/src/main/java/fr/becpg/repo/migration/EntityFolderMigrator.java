package fr.becpg.repo.migration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.search.impl.lucene.LuceneFunction;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
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
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class EntityFolderMigrator {

	private static Log logger = LogFactory.getLog(EntityFolderMigrator.class);

	private BeCPGSearchService beCPGSearchService;
	
	private FileFolderService fileFolderService;
	
	private NodeService nodeService;
	
	private DictionaryService dictionaryService;

	private TransactionService transactionService;
	
	private EntityTplService entityTplService;
	
	private EntityService entityService;
	
	private EntityListDAO entityListDAO;

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

	@SuppressWarnings("deprecation")
	public void migrate() {

		// search for entity and entityTplAspect
		String query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY)) + 
				LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITY_TPL));
		
		List<NodeRef> entityTplNodeRefs = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entityTplNodeRefs.size() + " entity templates to migrate");
		
		if (!entityTplNodeRefs.isEmpty()) {
			
			for(NodeRef entityTplNodeRef : entityTplNodeRefs){
				if(nodeService.exists(entityTplNodeRef)){
					
					nodeService.setProperty(entityTplNodeRef, ContentModel.PROP_NAME, GUID.generate());
					QName type = (QName)nodeService.getProperty(entityTplNodeRef, BeCPGModel.PROP_ENTITY_TPL_CLASS_NAME);
			        
					NodeRef newEntityTplNodeRef = entityTplService.createEntityTpl(nodeService.getPrimaryParent(entityTplNodeRef).getParentRef(), 
							type, true, null, null);
					
					entityListDAO.moveDataLists(entityTplNodeRef, newEntityTplNodeRef);					
					nodeService.deleteNode(entityTplNodeRef);
					
					// folder tpl to migrate
					List<NodeRef> entityFolderTplNodeRefs = beCPGSearchService.luceneSearch(String.format(" +TYPE:\"bcpg:entityFolder\" +@bcpg\\:entityTplClassName:\"%s\" +@bcpg\\:entityTplEnabled:true", type));
					NodeRef entityFolderTplNodeRef = entityFolderTplNodeRefs!=null && !entityFolderTplNodeRefs.isEmpty() ? entityFolderTplNodeRefs.get(0) : null;
					if(entityFolderTplNodeRef != null){
						entityService.moveFiles(entityFolderTplNodeRef, newEntityTplNodeRef);						
						nodeService.deleteNode(entityFolderTplNodeRef);
					}					
				}							
			}
		}
		
		// remove system folders
		List<NodeRef> folderNodeRefs = beCPGSearchService.luceneSearch("+PATH:\"/app:company_home/cm:System/cm:FolderTemplates\"", RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		if (!folderNodeRefs.isEmpty()) {
			nodeService.deleteNode(folderNodeRefs.get(0));
		}		
				
		query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY_V2))
				 	  +LuceneHelper.exclude(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY_FOLDER));

		// search for entities to migrate
		List<NodeRef> entitiesNodeRef = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entitiesNodeRef.size() + " nodes to migrate");

		if (!entitiesNodeRef.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(entitiesNodeRef, 100)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
					public Boolean execute() throws Exception {

						return doMigrate(batchList);

					}
				}, false, true);

			}
		}
		
		// bug fix bcpg:entityListsAspect on bcpg:entityListItem
		query = LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITYLIST_ITEM)) + 
				LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS));
				
		List<NodeRef> entityListItems = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		logger.info("Found " + entityListItems.size() + " entityListItems to fix");
		
		if (!entityListItems.isEmpty()) {

			for (final List<NodeRef> batchList : Lists.partition(entityListItems, 100)) {
				transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {
					public Boolean execute() throws Exception {

						for(NodeRef n : batchList){
							if(nodeService.exists(n)){
								nodeService.removeAspect(n, BeCPGModel.ASPECT_ENTITYLISTS);
							}							
						}
						return true;
					}
				}, false, true);
			}
		}
		
	}

	@SuppressWarnings("deprecation")
	private Boolean doMigrate(List<NodeRef> entitiesNodeRef) {

		// check parent is entityFolder and has the same name
		for (NodeRef entityNodeRef : entitiesNodeRef) {
			if (nodeService.exists(entityNodeRef) ) {

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
							logger.warn("EntityName and entityFolderName are not the same. EntityName: " + entityName + " - entityFolderName: " + parentName);
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
								if (dictionaryService.isSubClass(nodeService.getType(fileInfo.getNodeRef()), BeCPGModel.TYPE_ENTITY_V2)) {
									fileFolderService.move(fileInfo.getNodeRef(), parentNodeRef, fileInfo.getName());
								} else {
									fileFolderService.move(fileInfo.getNodeRef(), entityNodeRef, fileInfo.getName());
								}
							}

							// delete reports that don't have the type
							// rep:report
							List<AssociationRef> reports = nodeService.getTargetAssocs(entityFolderNodeRef, ReportModel.ASSOC_REPORTS);
							for (AssociationRef report : reports) {
								if (!ReportModel.TYPE_REPORT.equals(nodeService.getType(report.getTargetRef()))) {
									nodeService.deleteNode(report.getTargetRef());
									nodeService.removeAssociation(entityNodeRef, report.getTargetRef(), ReportModel.ASSOC_REPORTS);
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

					// add aspect bcpg:entityVersionable
					if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_VERSIONABLE)) {
						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						properties.put(ContentModel.PROP_AUTO_VERSION, false);
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_VERSIONABLE, properties);
					}

					// add aspect bcpg:entityTplRefAspect
					if ((dictionaryService.isSubClass(entityType, BeCPGModel.TYPE_PRODUCT) || dictionaryService.isSubClass(entityType, ProjectModel.TYPE_PROJECT))
							&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)) {
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF, null);
					}
				}
			}

		}
		return true;
	}
}
