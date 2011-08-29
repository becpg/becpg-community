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
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;

public class ControlPlanDAOImpl implements ControlPlanDAO {

	private static Log logger = LogFactory.getLog(ControlPlanDAOImpl.class);
	
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
	public NodeRef create(NodeRef parentNodeRef, ControlPlanData cpData) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		
		properties.put(ContentModel.PROP_NAME, cpData.getName());
		
		NodeRef cpNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, cpData.getName()), 
								QualityModel.TYPE_CONTROL_PLAN, properties).getChildRef();
				
		// sampling def list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(cpNodeRef);			
		}
		createSamplingDefList(listContainerNodeRef, cpData.getSamplingDefList());
		
		return cpNodeRef;
	}

	@Override
	public void update(NodeRef cpNodeRef, ControlPlanData cpData) {
		
		nodeService.setProperty(cpNodeRef, ContentModel.PROP_NAME, cpData.getName());
		
		// sampling list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(cpNodeRef);			
		}
		createSamplingDefList(listContainerNodeRef, cpData.getSamplingDefList());		
	}
	
	@Override
	public ControlPlanData find(NodeRef cpNodeRef) {
		
		ControlPlanData cpData = new ControlPlanData();
		cpData.setName((String)nodeService.getProperty(cpNodeRef, ContentModel.PROP_NAME));
		cpData.setNodeRef(cpNodeRef);
		
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(cpNodeRef);
		if(listContainerNodeRef != null){
			cpData.setSamplingDefList(loadSamplingDefList(listContainerNodeRef));
		}
		return cpData;
	}

	@Override
	public void delete(NodeRef cpNodeRef) {
		
		nodeService.deleteNode(cpNodeRef);
		
	}
	
	private List<SamplingDefListDataItem> loadSamplingDefList(NodeRef listContainerNodeRef){
		
		List<SamplingDefListDataItem> samplingDefList = null;
		
		logger.debug("loadSamplingDefList");
    	
    	if(listContainerNodeRef != null)
    	{    		
    		logger.debug("loadSamplingDefList, container exists");
    		
    		NodeRef samplingDefListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_SAMPLINGDEF_LIST);
    		
    		if(samplingDefListNodeRef != null)
    		{    			
    			samplingDefList = new ArrayList<SamplingDefListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(samplingDefListNodeRef);
	    	
				logger.debug("loadSamplingDefList, list exists, size: " + nodes.size());
				
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    	
		    		List<AssociationRef> controlPointAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_SDL_CONTROL_POINT);
		    		NodeRef controlPointNodeRef = controlPointAssocRefs.isEmpty() ? null : (controlPointAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> controlStepAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_SDL_CONTROL_STEP);
		    		NodeRef controlStepNodeRef = controlStepAssocRefs.isEmpty() ? null : (controlStepAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> controlingGroupAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_SDL_CONTROLING_GROUP);
		    		NodeRef controlingGroupNodeRef = controlingGroupAssocRefs.isEmpty() ? null : (controlingGroupAssocRefs.get(0)).getTargetRef();
		    				    		
		    		SamplingDefListDataItem samplingDefListDataItem = new SamplingDefListDataItem(nodeRef, 
		    							(Integer)nodeService.getProperty(nodeRef, QualityModel.PROP_SDL_QTY),
		    							(Integer)nodeService.getProperty(nodeRef, QualityModel.PROP_SDL_FREQ),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_SDL_FREQUNIT),
		    							controlPointNodeRef, 
		    							controlStepNodeRef,
		    							controlingGroupNodeRef);
		    		
		    		samplingDefList.add(samplingDefListDataItem);
		    	}
    		}    		
    	}
    	
    	return samplingDefList;
	}
	
	private void createSamplingDefList(NodeRef listContainerNodeRef, List<SamplingDefListDataItem> samplingDefList){
		
		if(listContainerNodeRef != null)
		{  
			NodeRef samplingDefListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_SAMPLINGDEF_LIST);
			
			if(samplingDefList == null){
				//delete existing list
				if(samplingDefListNodeRef != null)
					nodeService.deleteNode(samplingDefListNodeRef);
			}
			else{    			
	    		//samplingDef list, create if needed	    		
	    		if(samplingDefListNodeRef == null)
	    		{		    						
		    		samplingDefListNodeRef = dataListDAO.createList(listContainerNodeRef, QualityModel.TYPE_SAMPLINGDEF_LIST);
	    		}
			
	    		List<FileInfo> files = fileFolderService.listFiles(samplingDefListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> samplingDefListToTreat = new ArrayList<NodeRef>();
	    		for(SamplingDefListDataItem samplingDefListDataItem : samplingDefList){
	    			samplingDefListToTreat.add(samplingDefListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){	    				    		
		    		
	    			if(!samplingDefListToTreat.contains(file.getNodeRef())){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		for(SamplingDefListDataItem samplingDefListDataItem : samplingDefList)
	    		{    			
	    			NodeRef samplingDefNodeRef = samplingDefListDataItem.getNodeRef();	  
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(QualityModel.PROP_SDL_QTY, samplingDefListDataItem.getQty());
		    		properties.put(QualityModel.PROP_SDL_FREQ, samplingDefListDataItem.getFreq());
		    		properties.put(QualityModel.PROP_SDL_FREQUNIT, samplingDefListDataItem.getFreqUnit());
		    				    		
		    		if(filesToUpdate.containsKey(samplingDefNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(samplingDefNodeRef), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(samplingDefListNodeRef, ContentModel.ASSOC_CONTAINS, 
									    					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), 
									    					QualityModel.TYPE_SAMPLINGDEF_LIST, properties);
		    			samplingDefNodeRef = childAssocRef.getChildRef();
		    		}		
		    		
		    		// control point
		    		associationService.update(samplingDefNodeRef, QualityModel.ASSOC_SDL_CONTROL_POINT, samplingDefListDataItem.getControlPoint());
		    				    		
		    		// control step
		    		associationService.update(samplingDefNodeRef, QualityModel.ASSOC_SDL_CONTROL_STEP, samplingDefListDataItem.getControlStep());
		    		
		    		// controling group
		    		associationService.update(samplingDefNodeRef, QualityModel.ASSOC_SDL_CONTROLING_GROUP, samplingDefListDataItem.getControlingGroup());
	    		}
			}
		}
	}

}
