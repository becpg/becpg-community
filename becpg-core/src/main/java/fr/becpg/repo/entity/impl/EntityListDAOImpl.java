package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * 
 * @author querephi
 *
 */
public class EntityListDAOImpl implements EntityListDAO{	
	
	private static final String QUERY_PARENT = " +PARENT:\"%s\" +TYPE:\"%s\" +@bcpg\\:isManualListItem:true";
	
	private static Log logger = LogFactory.getLog(EntityListDAOImpl.class);
	
	private NodeService nodeService;
	
	private DictionaryService dictionaryService;
	
	private FileFolderService fileFolderService;
	
	private NamespaceService namespaceService;
	
	private CopyService copyService;
	
	private BeCPGSearchService beCPGSearchService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}
	
	@Override
	public NodeRef getListContainer(NodeRef nodeRef) {
				
		return nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
	}

	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName listQName) {
		
		if(listQName== null){
			return null;
		}
			
		NodeRef listNodeRef = null;		
		if(listContainerNodeRef != null){
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName.getLocalName());		
		}
		return listNodeRef;	
	}

	@Override
	public NodeRef createListContainer(NodeRef nodeRef) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		return nodeService.createNode(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, 
				BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
	}
	
	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName listQName) {
		
		ClassDefinition classDef = dictionaryService.getClass(listQName);
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, listQName.getLocalName());
		properties.put(ContentModel.PROP_TITLE, classDef.getTitle());
		properties.put(ContentModel.PROP_DESCRIPTION, classDef.getDescription());
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, listQName.toPrefixString(namespaceService));
		return nodeService.createNode(listContainerNodeRef, 
									ContentModel.ASSOC_CONTAINS, listQName, 
									DataListModel.TYPE_DATALIST, 
									properties).getChildRef();
	}
	
	@Override
	public List<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {
		
		List<NodeRef> existingLists = new ArrayList<NodeRef>();
		
		if(listContainerNodeRef != null){
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);
		
			for(FileInfo node : nodes){
				
				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String)nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				
				if(dataListType != null && !dataListType.isEmpty()){
					
					QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
					
					if(dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)){
						
						existingLists.add(listNodeRef);
					}
				}
				
			}								
		}
		return existingLists;
	}

	@Override
	public List<QName> getExistingListsQName(NodeRef listContainerNodeRef) {
		
		List<QName> existingLists = new ArrayList<QName>();
		
		if(listContainerNodeRef != null){
			List<FileInfo> nodes = fileFolderService.listFolders(listContainerNodeRef);
			
			for(FileInfo node : nodes){
				
				NodeRef listNodeRef = node.getNodeRef();
				String dataListType = (String)nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
				
				if(dataListType != null && !dataListType.isEmpty()){
					
					existingLists.add(QName.createQName(dataListType, namespaceService));					
				}
			}								
		}
		return existingLists;				
	}

	@Override
	public NodeRef getLink(NodeRef listContainerNodeRef, QName assocQName, NodeRef nodeRef) {
		
		if(listContainerNodeRef != null && assocQName != null && nodeRef != null){
			
			List<FileInfo> fileInfos = fileFolderService.listFiles(listContainerNodeRef);
    		
    		for(FileInfo fileInfo : fileInfos){
    			
    			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(fileInfo.getNodeRef(), assocQName);
    			if(assocRefs.size() > 0 && nodeRef.equals(assocRefs.get(0).getTargetRef())){
    				return fileInfo.getNodeRef();
    			}
	    		
	    	}
		}
		return null;
	}
	
    @Override
	public void copyDataLists(NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean override) {
		
    	//do not initialize entity version
    	if(nodeService.hasAspect(targetNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
    		return;
    	}
    	
        if(sourceNodeRef != null){
        	
        	/*-- copy source datalists--*/
        	logger.debug("/*-- copy source datalists--*/");        	
        	NodeRef sourceListContainerNodeRef = getListContainer(sourceNodeRef);
        	NodeRef targetListContainerNodeRef = getListContainer(targetNodeRef);
        		        	
        	if(sourceListContainerNodeRef != null){
        			        		    	        			
				List<NodeRef> sourceListsNodeRef = getExistingListsNodeRef(sourceListContainerNodeRef);
				for(NodeRef sourceListNodeRef : sourceListsNodeRef){
					
					// create container if needed
					if(targetListContainerNodeRef == null){
						
						targetListContainerNodeRef = createListContainer(targetNodeRef);						
					}
					
					String dataListType = (String)nodeService.getProperty(sourceListNodeRef, DataListModel.PROP_DATALISTITEMTYPE);
					QName listQName = QName.createQName(dataListType, namespaceService);						        				
					
					NodeRef existingListNodeRef = getList(targetListContainerNodeRef, listQName);
					boolean copy = true;
					if(existingListNodeRef != null){
						if(override){
							nodeService.deleteNode(existingListNodeRef);
						}
						else{
							copy = false;
						}
					}
					
					if(copy){	        						        										
						NodeRef newDLNodeRef = copyService.copy (sourceListNodeRef, 
																targetListContainerNodeRef, 
																ContentModel.ASSOC_CONTAINS, 
																DataListModel.TYPE_DATALIST, 
																true);						
						nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, listQName.getLocalName());
					}
				}
        	}
        }    	
	}
    
	/**
	 * Get the manual links
	 * @param listNodeRef
	 * @return
	 */
    @Override
	public List<NodeRef> getManualLinks(NodeRef listNodeRef, QName listQName){
		
		return beCPGSearchService.unProtLuceneSearch(String.format(QUERY_PARENT, listNodeRef, listQName));
	}
}
