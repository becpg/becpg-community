package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.TranslateHelper;

public class EntityTplServiceImpl implements EntityTplService {

	private static final String QUERY_ENTITY_TEMPLATE = " +TYPE:\"bcpg:entity\" +@bcpg\\:entityTplClassName:\"%s\" +@bcpg\\:entityTplEnabled:true";
	private static final String QUERY_ENTITY_FOLDER_TEMPLATE = " +TYPE:\"bcpg:entityFolder\" +@bcpg\\:entityTplClassName:\"%s\" +@bcpg\\:entityTplEnabled:true";
	
	private static Log logger = LogFactory.getLog(EntityServiceImpl.class);
	
	private NodeService nodeService;
	
	private EntityListDAO entityListDAO;
	
	private SearchService searchService;
	
	private DictionaryService dictionaryService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Create the entity folderTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	@Override
	public NodeRef createFolderTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<String> subFolders){
		
		TypeDefinition typeDef = dictionaryService.getType(entityType);
		String entityTplName = typeDef.getTitle();
		
		// ProductFolder
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, entityTplName);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_CLASS_NAME, entityType);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_ENABLED, enabled);
					
		NodeRef entityTplFolderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityTplName);
		if(entityTplFolderNodeRef == null){				
			entityTplFolderNodeRef = nodeService.createNode(parentNodeRef, 
															ContentModel.ASSOC_CONTAINS, 
															QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
																	entityType.getLocalName()), 
															BeCPGModel.TYPE_ENTITY_FOLDER, properties).getChildRef();			
			
			// subFolders
			if(subFolders != null){				
				for(String subFolder : subFolders){
					
					properties.clear();
					properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(subFolder));
					NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityTplFolderNodeRef, ContentModel.ASSOC_CONTAINS, (String)properties.get(ContentModel.PROP_NAME));
					if(documentsFolderNodeRef == null){			
						nodeService.createNode(entityTplFolderNodeRef, 
												ContentModel.ASSOC_CONTAINS, 
												QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(subFolder)), 
												ContentModel.TYPE_FOLDER, 
												properties).getChildRef();
					}								
				}
			}			
		}			
		
		return entityTplFolderNodeRef;
	}

	/**
	 * Create the entityTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	@Override
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists){
		
		TypeDefinition typeDef = dictionaryService.getType(entityType);
		String entityTplName = typeDef.getTitle();
		
		// entityTpl
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, entityTplName);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_CLASS_NAME, entityType);
		properties.put(BeCPGModel.PROP_ENTITY_TPL_ENABLED, enabled);
		
		NodeRef entityTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, entityTplName);
		if(entityTplNodeRef == null){
			entityTplNodeRef = nodeService.createNode(parentNodeRef, 
														ContentModel.ASSOC_CONTAINS, 
														QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
																			entityType.getLocalName()), 
														BeCPGModel.TYPE_ENTITY, properties).getChildRef();
		}
		
		
		//  entityLists			
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityTplNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = entityListDAO.createListContainer(entityTplNodeRef);
		}
		
		if(entityLists != null){
			for(QName entityList : entityLists){
				
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, entityList);
				if(listNodeRef == null){
					entityListDAO.createList(listContainerNodeRef, entityList);
				}
			}
		}			
		
		return entityTplNodeRef;
	}
	
	/**
	 * Get the entity folderTpl
	 */
	@Override
	public NodeRef getFolderTpl(QName nodeType) {

		return getTpl(true, nodeType);
	}

	/**
	 * Get the entityTpl
	 */
	@Override
	public NodeRef getEntityTpl(QName nodeType) {
		
		return getTpl(false, nodeType);
	}
	
	/**
	 * Look for the template
	 * @param isContainer
	 * @param nodeType
	 * @return
	 */
	private NodeRef getTpl(boolean isContainer, QName nodeType){
		
		NodeRef tplNodeRef = null;
		
		if(nodeType == null){
			return null;
		}		
    	
    	String query;
    	
    	if(isContainer){    		
    		query = String.format(QUERY_ENTITY_FOLDER_TEMPLATE, nodeType);
    	}
    	else{
    		query = String.format(QUERY_ENTITY_TEMPLATE, nodeType);
    	}
		
							
		logger.debug(query);
		
		SearchParameters sp = new SearchParameters();
        sp.addStore(RepoConsts.SPACES_STORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);	        
        sp.setLimitBy(LimitBy.FINAL_SIZE);
        sp.setLimit(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
        sp.setMaxItems(RepoConsts.MAX_RESULTS_SINGLE_VALUE);
        
        ResultSet resultSet =null;
        
        try{
	        resultSet = searchService.query(sp);
			
	        logger.debug("resultSet.length() : " + resultSet.length());
	        if(resultSet.length() > 0)
	        	tplNodeRef = resultSet.getNodeRefs().get(0);
        }
        finally{
        	if(resultSet != null)
        		resultSet.close();
        }
        
        return tplNodeRef;
	}
}
