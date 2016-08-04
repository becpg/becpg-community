/*
 * 
 */
package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class RepoServiceImpl.
 *
 * @author querephi
 */
@Service("repoService")
public class RepoServiceImpl implements RepoService {
	
	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(RepoServiceImpl.class);

	@Autowired
	private final NodeService nodeService = null;
	
	@Autowired
	private FileFolderService fileFolderService;
	
	@Autowired
	private Repository repository;

	
	@Autowired
	private NamespaceService namespaceService;


	/* (non-Javadoc)
	 * @see fr.becpg.repo.helper.RepoService#createFolderByPaths(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
	 */
	@Override
	public NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths) {			    
		
    	for(String folderName : paths){
    		if(folderName!=null && !folderName.isEmpty()){    				    		    		
    			parentNodeRef = getOrCreateFolderByPath(parentNodeRef, folderName, folderName);    		
    		}
    	}
		
		return parentNodeRef;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.helper.RepoService#createFolderByPath(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
	 */
	@Override
	public NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name) {
		
		NodeRef folderNodeRef = getFolderByPath(parentNodeRef, path);
		
		if(folderNodeRef == null){
			logger.debug("Create folder : " + name + "  ");

			Map<QName, Serializable> properties = new HashMap<>();
	    	properties.put(ContentModel.PROP_NAME, PropertiesHelper.cleanFolderName(name));	    		    	
	    	folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
	    											QName.resolveToQName(namespaceService, path), 
	    											ContentModel.TYPE_FOLDER, properties).getChildRef();	    		 
		}		

		return folderNodeRef;
	}

	@Override
	public NodeRef getFolderByPath(NodeRef parentNodeRef, String path) {
		
		NodeRef ret = BeCPGQueryBuilder.createQuery().selectNodeByPath(parentNodeRef,BeCPGQueryBuilder.encodePath(path));
		if(ret==null && !path.contains(RepoConsts.MODEL_PREFIX_SEPARATOR)){
			//try by name...
			return nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, path);
		}
		
		return ret;
	}
	

	@Override
	public NodeRef getFolderByPath(String path) {
		return getFolderByPath(repository.getCompanyHome(), path);
	}

	@Override
	public void moveNode(NodeRef nodeRefToMove, NodeRef destionationNodeRef) {

		logger.debug("start moveNode");
		
		// check the nodeRefToMove is not already moved !
		NodeRef parentOfNodeRefToMove = nodeService.getPrimaryParent(nodeRefToMove).getParentRef();
		if (destionationNodeRef.equals(parentOfNodeRefToMove)) {
			// nothing to do...
			logger.debug("nodeRefToMove is not already moved, nothing to do...");
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
			if (e instanceof ConcurrencyFailureException) {
				throw (ConcurrencyFailureException) e;
			}
			logger.error("Failed to move node", e);
		}
	}

	@Override
	public String getAvailableName(NodeRef folderNodeRef, String name) {
		
		List<FileInfo> fileInfos = fileFolderService.list(folderNodeRef);
		if (!fileInfos.isEmpty()) {
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
