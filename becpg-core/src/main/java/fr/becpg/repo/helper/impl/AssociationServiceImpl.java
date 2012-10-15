package fr.becpg.repo.helper.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.repo.helper.AssociationService;

@Service
public class AssociationServiceImpl implements AssociationService {
	
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
    			if(assocNodeRefs == null){
    				nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
    			}
    			else if(!assocNodeRefs.contains(assocRef.getTargetRef()))
    				nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), qName);
    			else
    				assocNodeRefs.remove(assocRef.getTargetRef());//already in db
    		}    		
		}
		
		//add nodes that are not in db
		if(assocNodeRefs != null){
			for(NodeRef n : assocNodeRefs){
				if(nodeService.exists(n)){
					nodeService.createAssociation(nodeRef, n, qName);
				}
			}
		}		
	}
	
	// TODO : refactor : utiliser cette méthode dans la création des datalists ! productdao, etc...
	@Override
	public void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef) {
		
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, qName);
		
		boolean createAssoc = true;
		if(!assocRefs.isEmpty() && assocRefs.get(0).getTargetRef() != null){
			if(assocRefs.get(0).getTargetRef().equals(assocNodeRef)){
				createAssoc = false;
			}
			else{
				nodeService.removeAssociation(nodeRef, assocRefs.get(0).getTargetRef(), qName);
			}
		}
		
		if(createAssoc && assocNodeRef != null){
			nodeService.createAssociation(nodeRef, assocNodeRef, qName);
		}
	}

}
