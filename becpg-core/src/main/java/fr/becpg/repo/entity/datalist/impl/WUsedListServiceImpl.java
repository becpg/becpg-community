package fr.becpg.repo.entity.datalist.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

@Service
public class WUsedListServiceImpl implements WUsedListService {

	private static Log logger = LogFactory.getLog(WUsedListServiceImpl.class);

	private NodeService nodeService;

	private EntityListDAO entityListDAO;

	private PermissionService permissionService;
	
	private DictionaryService dictionaryService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public MultiLevelListData getWUsedEntity(NodeRef entityNodeRef, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(Arrays.asList(entityNodeRef), associationName, 0, maxDepthLevel);
	}

	@Override
	public MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, QName associationName, int maxDepthLevel) {
		return getWUsedEntity(entityNodeRefs, associationName, 0, maxDepthLevel);
	}

	private MultiLevelListData getWUsedEntity(List<NodeRef> entityNodeRefs, QName associationName, int depthLevel, int maxDepthLevel) {

		MultiLevelListData ret = new MultiLevelListData(entityNodeRefs, depthLevel);

		List<AssociationRef> associationRefs = new ArrayList<>();
		boolean first = true;
		for (NodeRef entityNodeRef : entityNodeRefs) {
			if (first) {
				associationRefs = nodeService.getSourceAssocs(entityNodeRef, associationName);
				if(logger.isDebugEnabled()){
					logger.debug("associationRefs size" + associationRefs.size()+ "  for entityNodeRef "+entityNodeRef+" and assocs"+associationName);
				}
				first = false;
			} else {
				//Test for join
				for (Iterator<AssociationRef> iterator = associationRefs.iterator(); iterator.hasNext();) {
					AssociationRef associationRef = (AssociationRef) iterator.next();
					boolean delete = true;
					for (AssociationRef associationRef2 : nodeService.getSourceAssocs(entityNodeRef, associationName)) {
						//Test that assoc is also include in product
						if (getEntity(associationRef.getSourceRef()).equals(getEntity(associationRef2.getSourceRef()))) {
							// TODO Test same parent
				
							delete = false;
							break;
						}
					}
					if(delete){
						iterator.remove();
					}
				}
			}
		}

		for (AssociationRef associationRef : associationRefs) {

			NodeRef nodeRef = associationRef.getSourceRef();

			// we display nodes that are in workspace
			if (nodeRef != null && nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE) && permissionService.hasReadPermission(nodeRef) == AccessStatus.ALLOWED) {
				NodeRef rootNodeRef = getEntity(nodeRef);

				// we don't display history version and simulation entities
				if (!nodeService.hasAspect(rootNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION) && permissionService.hasReadPermission(rootNodeRef) == AccessStatus.ALLOWED) {

					MultiLevelListData multiLevelListData = null;
					// next level
					if (maxDepthLevel < 0 || depthLevel + 1 < maxDepthLevel) {
						multiLevelListData = getWUsedEntity(Arrays.asList(rootNodeRef), associationName, depthLevel + 1, maxDepthLevel);
					} else {
						multiLevelListData = new MultiLevelListData(rootNodeRef, depthLevel + 1);
					}
					ret.getTree().put(nodeRef, multiLevelListData);
				}
			}
		}
		return ret;
	}

	private NodeRef getEntity(NodeRef sourceRef) {
		//TODO use cached version if perfs slow aka attribute extractor subClass
		if(dictionaryService.isSubClass(nodeService.getType(sourceRef), BeCPGModel.TYPE_ENTITY_V2)){
			return sourceRef;
		} 
		return entityListDAO.getEntity(sourceRef);
	}

	@Override
	@Deprecated
	public List<QName> evaluateWUsedAssociations(NodeRef targetAssocNodeRef) {
		List<QName> wUsedAssociations = new ArrayList<QName>();

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(BeCPGModel.TYPE_RAWMATERIAL) || nodeType.isMatch(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT) || nodeType.isMatch(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(BeCPGModel.TYPE_FINISHEDPRODUCT)) {

			wUsedAssociations.add(BeCPGModel.ASSOC_COMPOLIST_PRODUCT);
		} else if (nodeType.isMatch(BeCPGModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(BeCPGModel.TYPE_PACKAGINGKIT)) {
			wUsedAssociations.add(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT); }
//		} else if (nodeType.isMatch(BeCPGModel.TYPE_RESOURCEPRODUCT)) {
//			wUsedAssociations.add(MPMModel.ASSOC_PL_RESOURCE);
//		}

		return wUsedAssociations;
	}

	@Override
	@Deprecated
	public QName evaluateListFromAssociation(QName associationName) {

		QName listQName = null;

		if (associationName.equals(BeCPGModel.ASSOC_COMPOLIST_PRODUCT)) {
			listQName = BeCPGModel.TYPE_COMPOLIST;
		} else if (associationName.equals(BeCPGModel.ASSOC_PACKAGINGLIST_PRODUCT)) {
			listQName = BeCPGModel.TYPE_PACKAGINGLIST; }
//		} else if (associationName.equals(MPMModel.ASSOC_PL_RESOURCE)) {
//			listQName = MPMModel.TYPE_PROCESSLIST;
//		}

		return listQName;
	}

}
