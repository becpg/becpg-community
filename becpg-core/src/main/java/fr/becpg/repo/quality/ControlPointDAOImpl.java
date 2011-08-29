package fr.becpg.repo.quality;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;

public class ControlPointDAOImpl implements ControlPointDAO {

	private static Log logger = LogFactory.getLog(ControlPointDAOImpl.class);
	
	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private DataListsDAO dataListDAO;
	private AssociationService associationService;
		
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setDataListDAO(DataListsDAO dataListDAO) {
		this.dataListDAO = dataListDAO;
	}
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	@Override
	public NodeRef create(NodeRef parentNodeRef, ControlPointData cpData) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();		
		properties.put(ContentModel.PROP_NAME, cpData.getName());
		
		NodeRef cpNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, cpData.getName()), 
								QualityModel.TYPE_CONTROL_POINT, properties).getChildRef();
				
		// control def list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(cpNodeRef);			
		}
		createControlDefList(listContainerNodeRef, cpData.getControlDefList());
		
		return cpNodeRef;		
	}

	@Override
	public void update(NodeRef cpNodeRef, ControlPointData cpData) {
		
		nodeService.setProperty(cpData.getNodeRef(), ContentModel.PROP_NAME, cpData.getName());
		
		// control def list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(cpNodeRef);			
		}
		createControlDefList(listContainerNodeRef, cpData.getControlDefList());
		
	}

	@Override
	public ControlPointData find(NodeRef cpNodeRef) {
		
		ControlPointData cpData = new ControlPointData();
		cpData.setNodeRef(cpNodeRef);
		
		
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef != null){
			cpData.setControlDefList(loadControlDefList(listContainerNodeRef));
		}
		
		return cpData;
	}

	@Override
	public void delete(NodeRef cpNodeRef) {
		
		nodeService.deleteNode(cpNodeRef);		
	}
	

	private List<ControlDefListDataItem> loadControlDefList(NodeRef listContainerNodeRef){
		
		List<ControlDefListDataItem> controlDefList = null;
    	
		logger.debug("loadControlDefList");
		
    	if(listContainerNodeRef != null)
    	{    		
    		logger.debug("loadControlDefList, list container exists");
    		NodeRef controlDefListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROLDEF_LIST);
    		
    		if(controlDefListNodeRef != null)
    		{
    			controlDefList = new ArrayList<ControlDefListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(controlDefListNodeRef);
	    		
				logger.debug("loadControlDefList, list exists, size: " + nodes.size());
				
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    	
		    		List<AssociationRef> methodAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_CDL_METHOD);
		    		NodeRef methodNodeRef = methodAssocRefs.isEmpty() ? null : (methodAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> charactsAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_CDL_CHARACTS);
		    		List<NodeRef> charactsNodeRef = new ArrayList<NodeRef>(charactsAssocRefs.size());
		    		for(AssociationRef assocRef : charactsAssocRefs){
		    			charactsNodeRef.add(assocRef.getTargetRef());
		    		}
		    		
		    		ControlDefListDataItem controlDefListDataItem = new ControlDefListDataItem(nodeRef, 
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_CDL_TYPE), 
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CDL_MINI), 
		    							(Float)nodeService.getProperty(nodeRef, QualityModel.PROP_CDL_MAXI), 
		    							(Boolean)nodeService.getProperty(nodeRef, QualityModel.PROP_CDL_REQUIRED), 
		    							methodNodeRef, 
		    							charactsNodeRef);
		    		
		    		controlDefList.add(controlDefListDataItem);
		    	}
    		}    		
    	}
    	
    	return controlDefList;
	}
	
	
	private void createControlDefList(NodeRef listContainerNodeRef, List<ControlDefListDataItem> controlDefList){
		
		if(listContainerNodeRef != null)
		{  
			NodeRef controlDefListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_CONTROLDEF_LIST);
			
			if(controlDefList == null){
				//delete existing list
				if(controlDefListNodeRef != null)
					nodeService.deleteNode(controlDefListNodeRef);
			}
			else{    			
	    		//controlDef list, create if needed	    		
	    		if(controlDefListNodeRef == null)
	    		{		    						
		    		controlDefListNodeRef = dataListDAO.createList(listContainerNodeRef, QualityModel.TYPE_CONTROLDEF_LIST);
	    		}
			
	    		List<FileInfo> files = fileFolderService.listFiles(controlDefListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> controlDefListToTreat = new ArrayList<NodeRef>();
	    		for(ControlDefListDataItem controlDefListDataItem : controlDefList){
	    			controlDefListToTreat.add(controlDefListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){	    				    		
		    		
	    			if(!controlDefListToTreat.contains(file.getNodeRef())){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		for(ControlDefListDataItem controlDefListDataItem : controlDefList)
	    		{    			
	    			NodeRef controlDefNodeRef = controlDefListDataItem.getNodeRef();	  
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(QualityModel.PROP_CDL_TYPE, controlDefListDataItem.getType());
		    		properties.put(QualityModel.PROP_CDL_MINI, controlDefListDataItem.getMini());
		    		properties.put(QualityModel.PROP_CDL_MAXI, controlDefListDataItem.getMaxi());
		    		properties.put(QualityModel.PROP_CDL_REQUIRED, controlDefListDataItem.getRequired());
		    				    		
		    		if(filesToUpdate.containsKey(controlDefNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(controlDefNodeRef), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(controlDefListNodeRef, 
				    									ContentModel.ASSOC_CONTAINS, 
				    									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
				    									QualityModel.TYPE_CONTROLDEF_LIST, properties);
		    			controlDefNodeRef = childAssocRef.getChildRef();
		    		}		
		    		
		    		// control method
		    		associationService.update(controlDefNodeRef, QualityModel.ASSOC_CDL_METHOD, controlDefListDataItem.getMethod());
		    				    		
		    		// control step
		    		associationService.update(controlDefNodeRef, QualityModel.ASSOC_CDL_CHARACTS, controlDefListDataItem.getCharacts());		    		
	    		}
			}
		}
	}

}
