package fr.becpg.repo.migration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class EntityFolderMigrator {

	private static Log logger = LogFactory.getLog(EntityFolderMigrator.class);
	
	private BeCPGSearchService beCPGSearchService;
	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	
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

	@SuppressWarnings("deprecation")
	public void migrate(){
		
		// search for entities to migrate
		List<NodeRef> entitiesNodeRef = beCPGSearchService.search(LuceneHelper.mandatory(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY_V2)), null, RepoConsts.MAX_RESULTS_UNLIMITED, SearchService.LANGUAGE_LUCENE);
		
		// check parent is entityFolder and has the same name
		for(NodeRef entityNodeRef : entitiesNodeRef){
			
			if(nodeService.exists(entityNodeRef)){
				
				if(nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_CHECKED_OUT)){
					logger.error("Node is checked out " + entityNodeRef);
				}
				else if(nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_WORKING_COPY)){
					logger.error("Node is a working copy " + entityNodeRef);
				}
				else{
					
					// remove some properties on entity (CONTENT, thumbnail,...)
					logger.debug("remove aspects");
					if(nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION)){
						nodeService.removeAspect(entityNodeRef, ContentModel.ASPECT_THUMBNAIL_MODIFICATION);
					}							
					nodeService.removeProperty(entityNodeRef, ContentModel.PROP_CONTENT);
					if(nodeService.hasAspect(entityNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE)){
						nodeService.removeAspect(entityNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE);
					}
					if(nodeService.hasAspect(entityNodeRef, RenditionModel.ASPECT_RENDITIONED)){
						nodeService.removeAspect(entityNodeRef, RenditionModel.ASPECT_RENDITIONED);
					}
					
					NodeRef entityFolderNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
					QName parentType = nodeService.getType(entityFolderNodeRef);
					QName entityType = nodeService.getType(entityNodeRef);
					String entityName = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					String parentName = (String)nodeService.getProperty(entityFolderNodeRef, ContentModel.PROP_NAME);
														
					if(BeCPGModel.TYPE_ENTITY_FOLDER.equals(parentType)){
						
						if(entityName != null && !entityName.equals(parentName)){
							logger.warn("EntityName and entityFolderName are not the same. EntityName: " + entityName + " - entityFolderName: " + parentName);
						}
							
						logger.info("migrate entity " + entityName + " - " + entityNodeRef);
						
						try {
							
							// rename entityFolder, move entity as sibling of entityFolder and delete entityFolder
							nodeService.setProperty(entityFolderNodeRef, ContentModel.PROP_NAME, GUID.generate());
							NodeRef parentNodeRef = nodeService.getPrimaryParent(entityFolderNodeRef).getParentRef();
							fileFolderService.move(entityNodeRef, parentNodeRef, entityName);
							
							// move sub-folders of entityFolder under entity
							List<FileInfo> fileInfos = fileFolderService.list(entityFolderNodeRef);
							for(FileInfo fileInfo : fileInfos){	
								//is it another entity ?
								if(dictionaryService.isSubClass(nodeService.getType(fileInfo.getNodeRef()), BeCPGModel.TYPE_ENTITY_V2)){
									fileFolderService.move(fileInfo.getNodeRef(), parentNodeRef, fileInfo.getName());
								}
								else{
									fileFolderService.move(fileInfo.getNodeRef(), entityNodeRef, fileInfo.getName());
								}															
							}
							
							//delete reports that don't have the type rep:report
							List<AssociationRef> reports = nodeService.getTargetAssocs(entityFolderNodeRef, ReportModel.ASSOC_REPORTS);
							for(AssociationRef report : reports){
								if(!ReportModel.TYPE_REPORT.equals(nodeService.getType(report.getTargetRef()))){
									nodeService.deleteNode(report.getTargetRef());
									nodeService.removeAssociation(entityNodeRef, report.getTargetRef(), ReportModel.ASSOC_REPORTS);
								}
							}
							
							//delete entityFolder
							nodeService.deleteNode(entityFolderNodeRef);
							
						} catch (FileExistsException e) {
							logger.error("Failed to migrate entity " + entityName, e);
						} catch (FileNotFoundException e) {
							logger.error("Failed to migrate entity " + entityName, e);
						}			
					}
					
					// add aspect bcpg:entityVersionable
					if(!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_VERSIONABLE)){
						Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
						properties.put(ContentModel.PROP_AUTO_VERSION, false);
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_VERSIONABLE, properties);
					}
					
					// add aspect bcpg:entityTplRefAspect
					if((dictionaryService.isSubClass(entityType, BeCPGModel.TYPE_PRODUCT) ||
							dictionaryService.isSubClass(entityType, ProjectModel.TYPE_PROJECT)) && 
							!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)){
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF, null);
					}
				}				
			}			
		}		
	}
}
