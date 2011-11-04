package fr.becpg.repo.entity.wused;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.wused.data.WUsedData;

public class WUsedListServiceImpl implements WUsedListService {

	private static Log logger = LogFactory.getLog(WUsedListServiceImpl.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public WUsedData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel) {
		
		Map<NodeRef, WUsedData> wUsedLoadedList = new HashMap<NodeRef, WUsedData>();
		return getWUsedEntity(entityNodeRef, associationName, 1, maxDepthLevel, wUsedLoadedList);
	}

	private WUsedData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int depthLevel, int maxDepthLevel, Map<NodeRef, WUsedData> wUsedLoadedList){
		
		Map<NodeRef, WUsedData> rootList = new HashMap<NodeRef, WUsedData>();
		List<AssociationRef> associationRefs = nodeService.getSourceAssocs(entityNodeRef, associationName);

		logger.debug("associationRefs size" + associationRefs.size());
		
		for(AssociationRef associationRef : associationRefs){
						
			NodeRef nodeRef = associationRef.getSourceRef();
			
			//we display nodes that are in workspace
			if(nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)){
				NodeRef compoListNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

				if(compoListNodeRef != null){
					NodeRef dataListsNodeRef = nodeService.getPrimaryParent(compoListNodeRef).getParentRef();
					
					if(dataListsNodeRef != null){
						NodeRef rootNodeRef = nodeService.getPrimaryParent(dataListsNodeRef).getParentRef();
						logger.debug("rootNodeRef: " + rootNodeRef);
						
						//we don't display history version
						if(!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
							
							WUsedData wUsedData = null;
							
							// next level
							if(depthLevel < maxDepthLevel){
																
								if(wUsedLoadedList.containsKey(rootNodeRef)){
									wUsedData = wUsedLoadedList.get(rootNodeRef);
								}
								else{
									wUsedData = getWUsedEntity(rootNodeRef, associationName, depthLevel+1, maxDepthLevel, wUsedLoadedList);
								}																
							}	
							else{
								wUsedData = new WUsedData(rootNodeRef, new HashMap<NodeRef, WUsedData>());
							}
							
							rootList.put(nodeRef, wUsedData);
						}
					}
				}
			}
		}
		
		return new WUsedData(entityNodeRef, rootList);
	}
	
	@Override
	public List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef){
		List<QName> wUsedAssociations = new ArrayList<QName>();
		
		QName nodeType = nodeService.getType(targetAssocNodeRef);
		
		if(nodeType.isMatch(BeCPGModel.TYPE_RAWMATERIAL) ||
				nodeType.isMatch(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT) ||
				nodeType.isMatch(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT) ||
				nodeType.isMatch(BeCPGModel.TYPE_FINISHEDPRODUCT) ||
				nodeType.isMatch(BeCPGModel.TYPE_CONDSALESUNIT)){
			
			wUsedAssociations.add(BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		}
		else if(nodeType.isMatch(BeCPGModel.TYPE_PACKAGINGMATERIAL) ||
				nodeType.isMatch(BeCPGModel.TYPE_PACKAGINGKIT)){
			wUsedAssociations.add(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT);
		}
		
		return wUsedAssociations;
	}

	@Override
	public QName evaluateListFromAssociation(QName associationName) {
		
		QName listQName = null;
		
		if(associationName.equals(BeCPGModel.ASSOC_COMPOLIST_PRODUCT)){
			listQName = BeCPGModel.TYPE_COMPOLIST;
		}
		else if(associationName.equals(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT)){
			listQName = BeCPGModel.TYPE_PACKAGINGLIST;
		}
		
		return listQName;
	}

}
