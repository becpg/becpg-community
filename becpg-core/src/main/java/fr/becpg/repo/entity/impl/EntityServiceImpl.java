package fr.becpg.repo.entity.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.util.Calendar;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.TranslateHelper;

/**
 * Entity Service implementation
 * @author querephi
 *
 */
public class EntityServiceImpl implements EntityService {

	/** The Constant QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE. */
	private static final String QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE = "(%s) AND +@cm\\:modified:[%s TO MAX]";	
	
	/** The Constant QUERY_OPERATOR_OR. */
	private static final String QUERY_OPERATOR_OR = " OR ";
	
	/** The Constant QUERY_PARENT. */
	private static final String QUERY_PARENT = " PARENT:\"%s\"";
		
	private static Log logger = LogFactory.getLog(EntityServiceImpl.class);
	
	private NodeService nodeService;
	
	private EntityListDAO entityListDAO;
	
	private SearchService searchService;
	
	private FileFolderService fileFolderService;
	
	private PermissionService permissionService;
	
	private EntityTplService entityTplService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	@Override
	public boolean hasDataListModified(NodeRef nodeRef) {
		
		Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);				
		
		// check data lists
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
		
		if(listContainerNodeRef != null){
			
			String queryParentsSearch = "";
			
			for(NodeRef listNodeRef : entityListDAO.getExistingListsNodeRef(listContainerNodeRef)){				
				
				// check list folder modified date
				Date dataListModified = (Date)nodeService.getProperty(listNodeRef, ContentModel.PROP_MODIFIED);
				logger.debug("list modified: " + ISO8601DateFormat.format(dataListModified) + " - modified: " + ISO8601DateFormat.format(modified));
				if(dataListModified.after(modified)){
					logger.debug("list folder has been modified");
					return true;
				}
				
				if(!queryParentsSearch.isEmpty()){
					queryParentsSearch += QUERY_OPERATOR_OR;
				}
				queryParentsSearch += String.format(QUERY_PARENT, listNodeRef);						
			}		
			
			// check list children modified date
			if(!queryParentsSearch.isEmpty()){
				
				String querySearch = String.format(QUERY_PRODUCTLIST_ITEMS_OUT_OF_DATE, queryParentsSearch, ISO8601DateFormat.format(modified));
				
				SearchParameters sp = new SearchParameters();
		        sp.addStore(RepoConsts.SPACES_STORE);
		        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		        sp.setQuery(querySearch);	        
		        sp.setLimitBy(LimitBy.FINAL_SIZE);
		        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);        
		        sp.setMaxItems(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
		        
		        ResultSet resultSet = null;
		        
		        try{
		        	
		        	logger.debug("queryPath: " + querySearch);	        		
			        resultSet = searchService.query(sp);		        
			        logger.debug("resultSet.length() : " + resultSet.length());
			        
			        if (resultSet.length() > 0){
			        			        	
			        	logger.debug("list children has been modified");	        	
			        	return true;
			        }		        		        
		        }
		        finally{
		        	if(resultSet != null)
		        		resultSet.close();
		        }			
			}
		}		
		
		return false;
	}
	
	/**
     * get the entity folder of the template and copy it for the entity.
     *
     * @param entityNodeRef the entity node ref
     */
	@Override
	public void initializeEntityFolder(NodeRef entityNodeRef) {
		
		logger.debug("initializeEntityFolder");				
		QName entityType = nodeService.getType(entityNodeRef);
		
		// entity => exit
		if(entityType.isMatch(BeCPGModel.TYPE_ENTITY)){
			return;
		}
		
		NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
		QName parentEntityType = nodeService.getType(parentEntityNodeRef);
		
		// Actual entity parent is not a entity folder
		if(!parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
			
			// look for folderTpl
			NodeRef folderTplNodeRef = entityTplService.getFolderTpl(entityType);
			
			if(folderTplNodeRef != null){
				
				logger.debug("folderTplNodeRef found");				

				FileInfo fileInfo = null;
				try{
				fileInfo = fileFolderService.copy(folderTplNodeRef, parentEntityNodeRef, GUID.generate());
				}
				catch(FileNotFoundException e){
					logger.error("initializeEntityFolder : Failed to copy template folder", e);
				}
				
				if(fileInfo != null){
					NodeRef entityFolderNodeRef = fileInfo.getNodeRef();
					
					// remove aspect entityTpl of folder
					nodeService.removeAspect(entityFolderNodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
					
					// set inherit parents permissions
					permissionService.deletePermissions(entityFolderNodeRef);
					permissionService.setInheritParentPermissions(entityFolderNodeRef, true);
					
					//move entity in entityfolder and rename entityfolder
					nodeService.moveNode(entityNodeRef, entityFolderNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getType(entityNodeRef));
					nodeService.setProperty(entityFolderNodeRef, 
											ContentModel.PROP_NAME, 
											nodeService.getProperty(entityNodeRef, 
																	ContentModel.PROP_NAME));
					
					// initialize permissions according to template
					for(FileInfo folder : fileFolderService.listFolders(folderTplNodeRef)){
						
						logger.debug("init permissions, folder: " + folder.getName());
						NodeRef subFolderTplNodeRef = folder.getNodeRef();
						NodeRef subFolderNodeRef = nodeService.getChildByName(entityFolderNodeRef, ContentModel.ASSOC_CONTAINS, folder.getName());
						
						if(subFolderNodeRef != null){
							
							if(nodeService.hasAspect(subFolderTplNodeRef, BeCPGModel.ASPECT_PERMISSIONS_TPL)){
								
								QName [] permissionGroupAssociations = {BeCPGModel.ASSOC_PERMISSIONS_TPL_CONSUMER_GROUPS, BeCPGModel.ASSOC_PERMISSIONS_TPL_EDITOR_GROUPS, BeCPGModel.ASSOC_PERMISSIONS_TPL_CONTRIBUTOR_GROUPS, BeCPGModel.ASSOC_PERMISSIONS_TPL_COLLABORATOR_GROUPS};
								String [] permissionNames = {RepoConsts.PERMISSION_CONSUMER, RepoConsts.PERMISSION_EDITOR, RepoConsts.PERMISSION_CONTRIBUTOR, RepoConsts.PERMISSION_COLLABORATOR};

								for(int cnt=0 ; cnt < permissionGroupAssociations.length ; cnt++){
									
									QName permissionGroupAssociation = permissionGroupAssociations[cnt];
									String permissionName = permissionNames[cnt];
									List<AssociationRef> groups = nodeService.getTargetAssocs(subFolderTplNodeRef, permissionGroupAssociation);
									
									if(groups.size() > 0){
										for(AssociationRef assocRef : groups){
											NodeRef groupNodeRef = assocRef.getTargetRef();
											String authorityName = (String)nodeService.getProperty(groupNodeRef, ContentModel.PROP_AUTHORITY_NAME);
											logger.debug("add permission, folder: " + folder.getName() + " authority: " + authorityName + " perm: " + permissionName);
											permissionService.setPermission(subFolderNodeRef, authorityName, permissionName, true);
											
											// remove 	association
											nodeService.removeAssociation(subFolderNodeRef, groupNodeRef, permissionGroupAssociation);
										}
									}									
								}
								
								//TODO
								// remove aspect when every association has been removed
								//nodeService.removeAspect(subFolderNodeRef, BeCPGModel.ASPECT_PERMISSIONS_TPL);	
							}
						}
					}
				}
			}
			
		}		
	}
	
	

	/**
	 * Load an image in the folder Images.
	 *
	 * @param nodeRef the node ref
	 * @param imgName the img name
	 * @return the image
	 */
	@Override
	public NodeRef getImage(NodeRef nodeRef, String imgName){		
		
		NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				
		NodeRef imagesFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES));		
		if(imagesFolderNodeRef == null){
			logger.debug("Folder 'Images' doesn't exist.");
			return null;
		}
		
		NodeRef imageNodeRef = null;		
		List<FileInfo> files = fileFolderService.listFiles(imagesFolderNodeRef);				
		for(FileInfo file : files){
			if(file.getName().toLowerCase().startsWith(imgName.toLowerCase())){
				imageNodeRef = file.getNodeRef();
			}
		}
		
		if(imageNodeRef == null){
			logger.debug("image not found. imgName: " + imgName);
			return null;
		}			
		
		return imageNodeRef;
	}

	@Override
	public void initializeEntity(NodeRef entityNodeRef) {
		logger.debug("initialyze entity");
		if(entityNodeRef!=null && nodeService.exists(entityNodeRef)){
			nodeService.setProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
		}
		
	}
	

}
