package fr.becpg.repo.eco.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ECOModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.eco.data.RevisionType;
import fr.becpg.repo.eco.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.helper.AssociationService;

//public class ChangeUnitDAOImpl implements BeCPGDao<ChangeUnitDataItem> {
//
//	private final static String VALUE_TREATED = "Treated";
//	private final static String NAME_PATTERN_SEPARATOR = " - ";
//	
//	private static Log logger = LogFactory.getLog(ChangeUnitDAOImpl.class);
//	
//	private NodeService nodeService;
//	private AssociationService associationService;
//	
//	public void setNodeService(NodeService nodeService) {
//		this.nodeService = nodeService;
//	}
//	
//	public void setAssociationService(AssociationService associationService) {
//		this.associationService = associationService;
//	}
//	
//	@Override
//	public NodeRef create(NodeRef parentNodeRef, ChangeUnitDataItem changeUnitData) {
//		
//		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//		
//		String name = getName(changeUnitData);
//		
//		properties.put(ContentModel.PROP_NAME, name);
//		properties.put(ECOModel.PROP_CU_REVISION, changeUnitData.getRevision());		
//		properties.put(ECOModel.PROP_CU_REQ_DETAILS, changeUnitData.getReqDetails());
//		properties.put(ECOModel.PROP_CU_REQ_RESPECTED, changeUnitData.getReqRespected());
//		properties.put(ECOModel.PROP_CU_TREATED, changeUnitData.getTreated());
//		
//		NodeRef changeUnitNodeRef = nodeService.createNode(parentNodeRef, ECOModel.ASSOC_CHANGE_UNITS, 
//								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
//								ECOModel.TYPE_CHANGE_UNIT, properties).getChildRef();
//		
//		associationService.update(changeUnitNodeRef, ECOModel.ASSOC_CU_SOURCE_ITEM, changeUnitData.getSourceItem());
//		associationService.update(changeUnitNodeRef, ECOModel.ASSOC_CU_TARGET_ITEM, changeUnitData.getTargetItem());
//		
//		return changeUnitNodeRef;
//	}
//
//	@Override
//	public void update(NodeRef changeUnitNodeRef, ChangeUnitDataItem changeUnitData) {
//		
//		nodeService.setProperty(changeUnitNodeRef, ContentModel.PROP_NAME, getName(changeUnitData));
//		nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_REVISION, changeUnitData.getRevision());		
//		nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_REQ_DETAILS, changeUnitData.getReqDetails());
//		nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_REQ_RESPECTED, changeUnitData.getReqRespected());
//		nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_TREATED, changeUnitData.getTreated());		
//		
//		associationService.update(changeUnitNodeRef, ECOModel.ASSOC_CU_SOURCE_ITEM, changeUnitData.getSourceItem());
//		associationService.update(changeUnitNodeRef, ECOModel.ASSOC_CU_TARGET_ITEM, changeUnitData.getTargetItem());
//		
//	}
//
//	@Override
//	public ChangeUnitDataItem find(NodeRef changeUnitNodeRef) {
//		
//		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(changeUnitNodeRef, ECOModel.ASSOC_CU_SOURCE_ITEM);
//		NodeRef sourceItem = assocRefs.size() == 1 ? assocRefs.get(0).getTargetRef() : null;
//		
//		assocRefs = nodeService.getTargetAssocs(changeUnitNodeRef, ECOModel.ASSOC_CU_TARGET_ITEM);
//		NodeRef targetItem = assocRefs.size() == 1 ? assocRefs.get(0).getTargetRef() : null;
//		
//		RevisionType revision = null;
//		String strRevision = (String)nodeService.getProperty(changeUnitNodeRef, ECOModel.PROP_CU_REVISION);
//		if(strRevision != null){
//			revision = RevisionType.valueOf(strRevision);
//		}
//		
//		ChangeUnitDataItem changeUnitData = new ChangeUnitDataItem(changeUnitNodeRef, 
//									revision, 
//									(Boolean)nodeService.getProperty(changeUnitNodeRef, ECOModel.PROP_CU_REQ_RESPECTED), 
//									(String)nodeService.getProperty(changeUnitNodeRef, ECOModel.PROP_CU_REQ_DETAILS), 
//									(Boolean)nodeService.getProperty(changeUnitNodeRef, ECOModel.PROP_CU_TREATED),
//									sourceItem, 
//									targetItem);
//		
//		return changeUnitData;
//	}
//
//	@Override
//	public void delete(NodeRef changeUnitNodeRef) {
//		
//		nodeService.deleteNode(changeUnitNodeRef);
//		
//	}
//	
//	private String getName(ChangeUnitDataItem changeUnitData){
//	
//		String name = changeUnitData.getRevision() + NAME_PATTERN_SEPARATOR + (String)nodeService.getProperty(changeUnitData.getSourceItem(), ContentModel.PROP_NAME);
//		
//		if(changeUnitData.getTargetItem() != null){
//			name += NAME_PATTERN_SEPARATOR + (String)nodeService.getProperty(changeUnitData.getTargetItem(), ContentModel.PROP_NAME);
//		}
//		
//		if(changeUnitData.getTreated()){
//			name += NAME_PATTERN_SEPARATOR + VALUE_TREATED;
//		}
//		
//		return name;
//	}
//
//}
