/*
 * 
 */
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGSearchService;

// TODO: Auto-generated Javadoc
/**
 * The Class RepoServiceImpl.
 *
 * @author querephi
 */
@Service
public class RepoServiceImpl implements RepoService {
	
	/** The Constant XPATH. */
	private static final String XPATH = "./%s:%s";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(RepoServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService = null;	
	
	/** The search service. */
	private BeCPGSearchService beCPGSearchService;
	
	/** The fileFolderService service. */
	private FileFolderService fileFolderService;
	
	
		
	/**
	 * 
	 * @param fileFolderService
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}		
	
	


	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.helper.RepoService#createFolderByPaths(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
	 */
	@Override
	public NodeRef createFolderByPaths(NodeRef parentNodeRef, List<String> paths) {			    
		
    	for(String folderName : paths){
    		
    		if(folderName.equals("") == false){    				    		    		
    			
    			parentNodeRef = createFolderByPath(parentNodeRef, folderName, folderName);    		
    		}
    	}
		
		return parentNodeRef;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.helper.RepoService#createFolderByPath(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
	 */
	@Override
	public NodeRef createFolderByPath(NodeRef parentNodeRef, String path, String name) {
		
		NodeRef folderNodeRef = getFolderByPath(parentNodeRef, path);
		
		if(folderNodeRef == null){
			logger.debug("Create folder : " + name);
			
			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    	properties.put(ContentModel.PROP_NAME, name);	    		    	
	    	
	    	folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
	    											QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(path)), 
	    											ContentModel.TYPE_FOLDER, properties).getChildRef();	    		 
		}		

		return folderNodeRef;
	}

	@Override
	public NodeRef getFolderByPath(NodeRef parentNodeRef, String path) {
		
		String xPath = path.contains(RepoConsts.MODEL_PREFIX_SEPARATOR) ? 
						path : String.format(XPATH, NamespaceService.CONTENT_MODEL_PREFIX, ISO9075.encode(path));

		logger.debug("get folder by path: " + xPath);
		
		List<NodeRef> nodes = beCPGSearchService.searchByPath(parentNodeRef,xPath);
		
		if(!nodes.isEmpty()){
			return nodes.get(0);			
		}
		
		return null;
	}

	@Override
	public void moveNode(NodeRef nodeRefToMove, NodeRef destionationNodeRef) {

		logger.debug("start moveNode");
		
		// check the product is not already classified !
		NodeRef parentOfNodeRefToMove = nodeService.getPrimaryParent(nodeRefToMove).getParentRef();
		if (destionationNodeRef.equals(parentOfNodeRefToMove)) {
			// nothing to do...
			logger.debug("product already classified, nothing to do...");
			return;
		}

		// Check there is not a node with the same name, then rename node
		String name = getAvailableName(destionationNodeRef, (String) nodeService.getProperty(nodeRefToMove, ContentModel.PROP_NAME));		

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Move node '%s' in folder '%s'", name, destionationNodeRef));
		}

		try {
			fileFolderService.move(nodeRefToMove, destionationNodeRef, name);
		} catch (Exception e) {
			logger.error("classifyProduct : Failed to move product", e);
		}
	}

	@Override
	public String getAvailableName(NodeRef folderNodeRef, String name) {
		
		List<FileInfo> fileInfos = fileFolderService.list(folderNodeRef);
		if (fileInfos.size() > 0) {
			int count = 0;
			NodeRef nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, name);

			while (nodeRef!=null) {
				count++;				
				String nameWithCounter = String.format("%s (%d)", name, count);
				nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, nameWithCounter);
			}
			
			if (count > 0) {
				name = String.format("%s (%d)", name, count);
			}
		}
		
		return name;
	}
}
