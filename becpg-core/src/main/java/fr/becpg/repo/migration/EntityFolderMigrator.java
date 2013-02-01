package fr.becpg.repo.migration;

import java.util.List;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

public class EntityFolderMigrator {

	private static Log logger = LogFactory.getLog(EntityFolderMigrator.class);
	
	private BeCPGSearchService beCPGSearchService;
	private FileFolderService fileFolderService;
	private NodeService nodeService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void migrate(){
		
		// search for entities to migrate
		List<NodeRef> entitiesNodeRef = beCPGSearchService.search(LuceneHelper.getCondType(BeCPGModel.TYPE_ENTITY, null), null, RepoConsts.MAX_RESULTS_UNLIMITED, SearchService.LANGUAGE_LUCENE);
		
		// check parent is entityFolder and has the same name
		for(NodeRef entityNodeRef : entitiesNodeRef){
			
			NodeRef entityFolderNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
			QName parentType = nodeService.getType(entityFolderNodeRef);
			String entityName = (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
			String parentName = (String)nodeService.getProperty(entityFolderNodeRef, ContentModel.PROP_NAME);
			
			if(BeCPGModel.TYPE_ENTITY_FOLDER.equals(parentType)){
				
				if(entityName != null && entityName.equals(parentName)){
					
					logger.info("migrate entity " + entityName + " - " + entityNodeRef);
					
					try {
						
						// move sub-folders of entityFolder under entity
						List<FileInfo> fileInfos = fileFolderService.list(entityFolderNodeRef);
						for(FileInfo fileInfo : fileInfos){							
							fileFolderService.move(fileInfo.getNodeRef(), entityNodeRef, fileInfo.getName());							
						}
						
						// rename entityFolder, move entity as sibling as entityFolder and delete entityFolder
						nodeService.setProperty(entityFolderNodeRef, ContentModel.PROP_NAME, GUID.generate());
						NodeRef parentNodeRef = nodeService.getPrimaryParent(entityFolderNodeRef).getParentRef();
						fileFolderService.move(entityNodeRef, parentNodeRef, entityName);
						nodeService.deleteNode(entityFolderNodeRef);
						
						// remove some properties on entity (CONTENT, thumbnail,...)
						nodeService.removeProperty(entityNodeRef, ContentModel.PROP_CONTENT);
						nodeService.removeAspect(entityNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE);
						nodeService.removeAspect(entityNodeRef, RenditionModel.ASPECT_RENDITIONED);
					
					} catch (FileExistsException e) {
						logger.error("Failed to migrate entity " + entityName, e);
					} catch (FileNotFoundException e) {
						logger.error("Failed to migrate entity " + entityName, e);
					}
				}
				else{
					logger.warn("EntityName and entityFolderName are not the same. EntityName: " + entityName + " - entityFolderName: " + parentName);
				}				
			}
		}		
	}
}
