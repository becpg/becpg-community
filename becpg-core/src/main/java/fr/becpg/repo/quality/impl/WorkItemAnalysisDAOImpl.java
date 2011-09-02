package fr.becpg.repo.quality.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.BecpgDataListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.quality.data.WorkItemAnalysisData;
import fr.becpg.repo.quality.data.dataList.ControlListDataItem;

public class WorkItemAnalysisDAOImpl implements BeCPGDao<WorkItemAnalysisData> {

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private BecpgDataListDAO dataListDAO;
	private AssociationService associationService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setDataListDAO(BecpgDataListDAO dataListDAO) {
		this.dataListDAO = dataListDAO;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public NodeRef create(NodeRef parentNodeRef, WorkItemAnalysisData wiaData) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		
		properties.put(ContentModel.PROP_NAME, wiaData.getName());
		
		NodeRef cpNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, wiaData.getName()), 
								QualityModel.TYPE_WORK_ITEM_ANALYSIS, properties).getChildRef();
				
		// control list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(cpNodeRef);			
		}
		createControlList(listContainerNodeRef, wiaData.getControlList());
		
		return cpNodeRef;
	}

	@Override
	public void update(NodeRef wiaNodeRef, WorkItemAnalysisData wiaData) {
		
		nodeService.setProperty(wiaNodeRef, ContentModel.PROP_NAME, wiaData.getName());
		
		// control list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(wiaNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(wiaNodeRef);			
		}
		createControlList(listContainerNodeRef, wiaData.getControlList());
	}

	@Override
	public WorkItemAnalysisData find(NodeRef wiaNodeRef) {
		
		WorkItemAnalysisData wiaData = new WorkItemAnalysisData();
		wiaData.setName((String)nodeService.getProperty(wiaNodeRef, ContentModel.PROP_NAME));
		wiaData.setNodeRef(wiaNodeRef);
		
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(wiaNodeRef);
		if(listContainerNodeRef != null){
			wiaData.setControlList(loadControlList(listContainerNodeRef));
		}
		return wiaData;
	}

	@Override
	public void delete(NodeRef wiaNodeRef) {
		
		nodeService.deleteNode(wiaNodeRef);
		
	}
	
	private List<ControlListDataItem> loadControlList(NodeRef listContainerNodeRef){
		
		List<ControlListDataItem> controlDefList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef controlDefListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
    		
    		if(controlDefListNodeRef != null)
    		{
    			controlDefList = new ArrayList<ControlListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(controlDefListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    	
		    		List<AssociationRef> methodAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_CL_METHOD);
		    		NodeRef methodNodeRef = methodAssocRefs.isEmpty() ? null : (methodAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> charactsAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_CL_CHARACTS);
		    		List<NodeRef> charactsNodeRef = new ArrayList<NodeRef>(charactsAssocRefs.size());
		    		for(AssociationRef assocRef : charactsAssocRefs)
		    			charactsNodeRef.add(assocRef.getTargetRef());
		    		
		    		ControlListDataItem controlDefListDataItem = new ControlListDataItem(nodeRef, 
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_TYPE),
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_MINI),
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_MAXI),
		    							(Boolean)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_REQUIRED),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_SAMPLE_ID),
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_VALUE),
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_TARGET),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_UNIT),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_CL_STATE),
		    							methodNodeRef,
		    							charactsNodeRef);
		    		
		    		controlDefList.add(controlDefListDataItem);
		    	}
    		}    		
    	}
    	
    	return controlDefList;
	}
	
	private void createControlList(NodeRef listContainerNodeRef, List<ControlListDataItem> controlList){
		
		if(listContainerNodeRef != null)
		{  
			NodeRef controlListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
			
			if(controlList == null){
				//delete existing list
				if(controlListNodeRef != null)
					nodeService.deleteNode(controlListNodeRef);
			}
			else{    			
	    		//control list, create if needed	    		
	    		if(controlListNodeRef == null)
	    		{		    						
		    		controlListNodeRef = dataListDAO.createList(listContainerNodeRef, QualityModel.TYPE_CONTROL_LIST);
	    		}
			
	    		List<FileInfo> files = fileFolderService.listFiles(controlListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> controlListToTreat = new ArrayList<NodeRef>();
	    		for(ControlListDataItem controlListDataItem : controlList){
	    			controlListToTreat.add(controlListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){	    				    		
		    		
	    			if(!controlListToTreat.contains(file.getNodeRef())){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		for(ControlListDataItem controlListDataItem : controlList)
	    		{    			
	    			NodeRef controlNodeRef = controlListDataItem.getNodeRef();	  
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	    			
	    			properties.put(QualityModel.PROP_CL_TYPE, controlListDataItem.getType());
	    			properties.put(QualityModel.PROP_CL_MINI, controlListDataItem.getMini());
	    			properties.put(QualityModel.PROP_CL_MAXI, controlListDataItem.getMaxi());
	    			properties.put(QualityModel.PROP_CL_REQUIRED, controlListDataItem.getRequired());
	    			properties.put(QualityModel.PROP_CL_SAMPLE_ID, controlListDataItem.getSampleId());
	    			properties.put(QualityModel.PROP_CL_VALUE, controlListDataItem.getValue());
	    			properties.put(QualityModel.PROP_CL_TARGET, controlListDataItem.getTarget());
	    			properties.put(QualityModel.PROP_CL_UNIT, controlListDataItem.getUnit());
	    			properties.put(QualityModel.PROP_CL_STATE, controlListDataItem.getState());
	    			
		    		if(filesToUpdate.containsKey(controlNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(controlNodeRef), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(controlListNodeRef, 
		    									ContentModel.ASSOC_CONTAINS, 
		    									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
		    									QualityModel.TYPE_CONTROL_LIST, properties);
		    			controlNodeRef = childAssocRef.getChildRef();
		    		}		
		    		
		    		// method
		    		associationService.update(controlNodeRef, QualityModel.ASSOC_CL_METHOD, controlListDataItem.getMethod());
		    				    		
		    		// characts
		    		associationService.update(controlNodeRef, QualityModel.ASSOC_CL_CHARACTS, controlListDataItem.getCharacts());		    		
	    		}
			}
		}
	}

}
