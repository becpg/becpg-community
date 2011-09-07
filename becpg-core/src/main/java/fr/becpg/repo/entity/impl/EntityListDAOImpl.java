package fr.becpg.repo.entity.impl;

import java.io.Serializable;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * 
 * @author querephi
 *
 */
public class EntityListDAOImpl implements EntityListDAO{	
	
	private static Log logger = LogFactory.getLog(EntityListDAOImpl.class);
	
	private NodeService nodeService;
	
	private DictionaryService dictionaryService;
	
	private FileFolderService fileFolderService;
	
	private NamespaceService namespaceService;
	
	private CopyService copyService;
	
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
		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName, DataListModel.TYPE_DATALIST, properties).getChildRef();
	}
	
	@Override
	public Set<NodeRef> getExistingListsNodeRef(NodeRef listContainerNodeRef) {
		
		Set<NodeRef> existingLists = new HashSet<NodeRef>();
		
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
	public Set<QName> getExistingListsQName(NodeRef listContainerNodeRef) {
		
		Set<QName> existingLists = new HashSet<QName>();
		
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
	public NodeRef getLink(NodeRef listContainerNodeRef, QName propertyQName, NodeRef nodeRef) {
		// TODO Refactor the code to use this method
		
		if(listContainerNodeRef != null && propertyQName != null && nodeRef != null){
			
			List<FileInfo> fileInfos = fileFolderService.listFiles(listContainerNodeRef);
    		
    		for(FileInfo fileInfo : fileInfos){
    			
    			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(fileInfo.getNodeRef(), propertyQName);
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
    	
    	NodeRef containerDataLists = nodeService.getChildByName(targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
		
		if(containerDataLists == null){
		    /*-- create an empty container --*/
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
			properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
			containerDataLists = nodeService.createNode(targetNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, BeCPGModel.ASSOC_ENTITYLISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
		}
        
        if(sourceNodeRef != null){
        	
        	/*-- copy source datalists--*/
        	logger.debug("/*-- copy source datalists--*/");
        	NodeRef sourceListsNodeRef = nodeService.getChildByName(sourceNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
        		        	
        	if(sourceListsNodeRef != null){
        			        		    	        			
				List<FileInfo> sourceDataLists = fileFolderService.listFolders(sourceListsNodeRef);	        				        			
				for(FileInfo sourceDataList : sourceDataLists){
					
					// TODO : a supprimer car il y a maintenant une datalistPolicy (ATTENTION à la démo car ca va tout casser ! car les templates ont toujours des GUID dans le name)
					String dataListType = (String)nodeService.getProperty(sourceDataList.getNodeRef(), DataListModel.PROP_DATALISTITEMTYPE);
					String dataListName = dataListType.split(RepoConsts.MODEL_PREFIX_SEPARATOR)[1];
					logger.debug("check missing list: " + dataListName + " - containerDL: " + containerDataLists);	        				
					
					NodeRef existingListNodeRef = nodeService.getChildByName(containerDataLists, ContentModel.ASSOC_CONTAINS, dataListName);
					boolean copy = true;
					if(existingListNodeRef != null){
						if(override){
							logger.debug("delete existing list");
							nodeService.deleteNode(existingListNodeRef);
						}
						else{
							copy = false;
						}
					}
					
					if(copy){	        						        										
						logger.debug("copy list");
						NodeRef newDLNodeRef = copyService.copy (sourceDataList.getNodeRef(), containerDataLists, ContentModel.ASSOC_CONTAINS, DataListModel.TYPE_DATALIST, true);						
						nodeService.setProperty(newDLNodeRef, ContentModel.PROP_NAME, dataListName);
					}
				}
        	}
        }    	
	}

}
