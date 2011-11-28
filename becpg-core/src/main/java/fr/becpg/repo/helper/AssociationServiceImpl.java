package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AssociationServiceImpl implements AssociationService {

	private static Log logger = LogFactory.getLog(AssociationServiceImpl.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	// TODO : refactor : utiliser cette méthode dans la création des datalists ! productdao, etc...
	@Override
	public void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs) {
		
		List<AssociationRef> dbAssocNodeRefs = nodeService.getTargetAssocs(nodeRef, qName);
		
		if(dbAssocNodeRefs != null){
			//remove from db
    		for(AssociationRef assocRef : dbAssocNodeRefs){
    			if(!assocNodeRefs.contains(assocRef.getTargetRef()))
    				nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
    			else
    				assocNodeRefs.remove(assocRef.getTargetRef());//already in db
    		}    		
		}
		
		//add nodes that are not in db
		if(assocNodeRefs != null){
			for(NodeRef n : assocNodeRefs){
				nodeService.createAssociation(nodeRef, n, qName);
			}
		}		
	}
	
	// TODO : refactor : utiliser cette méthode dans la création des datalists ! productdao, etc...
	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {
		
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, qName);
		
		boolean createAssoc = true;
		if(!assocRefs.isEmpty()){
			if(assocRefs.get(0).equals(assocNodeRef)){
				createAssoc = false;
			}
			else{
				nodeService.removeAssociation(nodeRef, assocNodeRef, qName);
			}
		}
		
		if(createAssoc && assocNodeRef != null){
			logger.debug("###createAssoc: " + qName + "nodeRef: " + nodeRef);
			nodeService.createAssociation(nodeRef, assocNodeRef, qName);
		}
	}

}
