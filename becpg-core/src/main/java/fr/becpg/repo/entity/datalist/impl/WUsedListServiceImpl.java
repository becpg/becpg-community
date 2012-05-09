package fr.becpg.repo.entity.datalist.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public class WUsedListServiceImpl implements WUsedListService {

	private static Log logger = LogFactory.getLog(WUsedListServiceImpl.class);
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel) {
		
		return getWUsedEntity(entityNodeRef, associationName, 0, maxDepthLevel);
	}

	private MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int depthLevel, int maxDepthLevel){
		
		MultiLevelListData ret = new MultiLevelListData(entityNodeRef, depthLevel);
		
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
						
						//we don't display history version and simulation entities
						if(!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION) && !nodeService.hasAspect(rootNodeRef, ECMModel.ASPECT_SIMULATION_ENTITY)){
							
							MultiLevelListData multiLevelListData = null;						
							// next level
							if(maxDepthLevel < 0 || depthLevel+1 < maxDepthLevel ){
								multiLevelListData = getWUsedEntity(rootNodeRef, associationName, depthLevel+1, maxDepthLevel);																
							}	
							else{
								multiLevelListData = new MultiLevelListData(rootNodeRef, depthLevel+1);
							}
							ret.getTree().put(nodeRef, multiLevelListData);
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	@Override
	public List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef){
		List<QName> wUsedAssociations = new ArrayList<QName>();
		
		QName nodeType = nodeService.getType(targetAssocNodeRef);
		
		if(nodeType.isMatch(BeCPGModel.TYPE_RAWMATERIAL) ||
				nodeType.isMatch(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT) ||
				nodeType.isMatch(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT) ||
				nodeType.isMatch(BeCPGModel.TYPE_FINISHEDPRODUCT)){
			
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
