package fr.becpg.repo.quality.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;

public class QualityControlDAOImpl implements BeCPGDao<QualityControlData> {

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
	public NodeRef create(NodeRef parentNodeRef, QualityControlData qcData) {		
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		
		properties.put(ContentModel.PROP_NAME, qcData.getName());
		properties.put(QualityModel.PROP_QC_SAMPLES_COUNTER, qcData.getSamplesCounter());
		properties.put(QualityModel.PROP_QC_STATE, qcData.getState());
		properties.put(QualityModel.PROP_QC_BATCH_START, qcData.getBatchStart());
		properties.put(QualityModel.PROP_QC_BATCH_DURATION, qcData.getBatchDuration());					
		properties.put(QualityModel.PROP_QC_BATCH_ID, qcData.getBatchId());
		properties.put(QualityModel.PROP_QC_ORDER_ID, qcData.getOrderId());
		
		NodeRef qcNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, 
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qcData.getName()), 
								QualityModel.TYPE_QUALITY_CONTROL, properties).getChildRef();
		
		associationService.update(qcNodeRef, QualityModel.ASSOC_QC_PRODUCT, qcData.getProduct());
		associationService.update(qcNodeRef, QualityModel.ASSOC_QC_CONTROL_PLANS, qcData.getControlPlans());
		
		// sampling list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(qcNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(qcNodeRef);			
		}
		createSamplingList(listContainerNodeRef, qcData.getSamplingList());
		
		return qcNodeRef;
	}

	@Override
	public void update(NodeRef qcNodeRef, QualityControlData qcData) {
		
		nodeService.setProperty(qcNodeRef, ContentModel.PROP_NAME, qcData.getName());
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_SAMPLES_COUNTER, qcData.getSamplesCounter());
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_STATE, qcData.getState());
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_START, qcData.getBatchStart());
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_DURATION, qcData.getBatchDuration());					
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_ID, qcData.getBatchId());
		nodeService.setProperty(qcNodeRef, QualityModel.PROP_QC_ORDER_ID, qcData.getOrderId());
		
		associationService.update(qcNodeRef, QualityModel.ASSOC_QC_PRODUCT, qcData.getProduct());
		associationService.update(qcNodeRef, QualityModel.ASSOC_QC_CONTROL_PLANS, qcData.getControlPlans());
		
		// sampling list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(qcNodeRef);
		if(listContainerNodeRef == null){
			listContainerNodeRef = dataListDAO.createListContainer(qcNodeRef);			
		}
		createSamplingList(listContainerNodeRef, qcData.getSamplingList());
	}

	@Override
	public QualityControlData find(NodeRef qcNodeRef) {
		
		QualityControlData qcData = new QualityControlData();
		
		qcData.setNodeRef(qcNodeRef);
		qcData.setName((String)nodeService.getProperty(qcNodeRef, ContentModel.PROP_NAME));
		qcData.setSamplesCounter((Integer)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_SAMPLES_COUNTER));
		qcData.setState((String)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_STATE));
		qcData.setBatchStart((Date)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_START));
		qcData.setBatchDuration((Integer)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_DURATION));
		qcData.setBatchId((String)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_BATCH_ID));
		qcData.setOrderId((String)nodeService.getProperty(qcNodeRef, QualityModel.PROP_QC_ORDER_ID));
		
		// control plans
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(qcNodeRef, QualityModel.ASSOC_QC_CONTROL_PLANS);
		for(AssociationRef assocRef : assocRefs){
			qcData.getControlPlans().add(assocRef.getTargetRef());
		}
		
		// product
		assocRefs = nodeService.getTargetAssocs(qcNodeRef, QualityModel.ASSOC_QC_PRODUCT);
		if(!assocRefs.isEmpty()){
			qcData.setProduct(assocRefs.get(0).getTargetRef());
		}		
		
		// sampling list
		NodeRef listContainerNodeRef = dataListDAO.getListContainer(qcNodeRef);
		if(listContainerNodeRef != null){
			qcData.setSamplingList(loadSamplingList(listContainerNodeRef));
		}
		
		return qcData;
	}

	@Override
	public void delete(NodeRef qcNodeRef) {
		
		nodeService.deleteNode(qcNodeRef);
		
	}
	
	private List<SamplingListDataItem> loadSamplingList(NodeRef listContainerNodeRef){
	
		List<SamplingListDataItem> samplingList = null;
    	
    	if(listContainerNodeRef != null)
    	{    		
    		NodeRef samplingListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_SAMPLING_LIST);
    		
    		if(samplingListNodeRef != null)
    		{
    			samplingList = new ArrayList<SamplingListDataItem>();
				List<FileInfo> nodes = fileFolderService.listFiles(samplingListNodeRef);
	    		
	    		for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
		    	{	    			
	    			FileInfo node = nodes.get(z_idx);
	    			NodeRef nodeRef = node.getNodeRef();	    					    		
		    	
		    		List<AssociationRef> controlPointAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_SL_CONTROL_POINT);
		    		NodeRef controlPointNodeRef = controlPointAssocRefs.isEmpty() ? null : (controlPointAssocRefs.get(0)).getTargetRef();
		    		
		    		List<AssociationRef> controlStepAssocRefs = nodeService.getTargetAssocs(nodeRef, QualityModel.ASSOC_SL_CONTROL_STEP);
		    		NodeRef controlStepNodeRef = controlStepAssocRefs.isEmpty() ? null : (controlStepAssocRefs.get(0)).getTargetRef();
		    		
		    		SamplingListDataItem samplingListDataItem = new SamplingListDataItem(nodeRef, 
		    							(Date)nodeService.getProperty(nodeRef, QualityModel.PROP_SL_DATETIME),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_SL_SAMPLE_ID),
		    							(String)nodeService.getProperty(nodeRef, QualityModel.PROP_SL_SAMPLE_STATE),
		    							controlPointNodeRef, 
		    							controlStepNodeRef);
		    		
		    		samplingList.add(samplingListDataItem);
		    	}
    		}    		
    	}
    	
    	return samplingList;
	}
	
	private void createSamplingList(NodeRef listContainerNodeRef, List<SamplingListDataItem> samplingList){
		
		if(listContainerNodeRef != null)
		{  
			NodeRef samplingListNodeRef = dataListDAO.getList(listContainerNodeRef, QualityModel.TYPE_SAMPLING_LIST);
			
			if(samplingList == null){
				//delete existing list
				if(samplingListNodeRef != null)
					nodeService.deleteNode(samplingListNodeRef);
			}
			else{    			
	    		//sampling list, create if needed	    		
	    		if(samplingListNodeRef == null)
	    		{		    						
		    		samplingListNodeRef = dataListDAO.createList(listContainerNodeRef, QualityModel.TYPE_SAMPLING_LIST);
	    		}
			
	    		List<FileInfo> files = fileFolderService.listFiles(samplingListNodeRef);
	    		
	    		//create temp list
	    		List<NodeRef> samplingListToTreat = new ArrayList<NodeRef>();
	    		for(SamplingListDataItem samplingListDataItem : samplingList){
	    			samplingListToTreat.add(samplingListDataItem.getNodeRef());
	    		}
	    		
	    		//remove deleted nodes
	    		Map<NodeRef, NodeRef> filesToUpdate = new HashMap<NodeRef, NodeRef>();
	    		for(FileInfo file : files){	    				    		
		    		
	    			if(!samplingListToTreat.contains(file.getNodeRef())){
	    				//delete
	    				nodeService.deleteNode(file.getNodeRef());
	    			}
	    			else{
	    				filesToUpdate.put(file.getNodeRef(), file.getNodeRef());
	    			}
	    		}
	    		
	    		//update or create nodes	    		    			    		
	    		for(SamplingListDataItem samplingListDataItem : samplingList)
	    		{    			
	    			NodeRef samplingNodeRef = samplingListDataItem.getNodeRef();	  
	    			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		    		properties.put(QualityModel.PROP_SL_DATETIME, samplingListDataItem.getDateTime());
		    		properties.put(QualityModel.PROP_SL_SAMPLE_ID, samplingListDataItem.getSampleId());
		    		properties.put(QualityModel.PROP_SL_SAMPLE_STATE, samplingListDataItem.getSampleState());
		    		
		    		if(filesToUpdate.containsKey(samplingNodeRef)){
		    			//update
		    			nodeService.setProperties(filesToUpdate.get(samplingNodeRef), properties);		    			
		    		}
		    		else{
		    			//create
		    			ChildAssociationRef childAssocRef = nodeService.createNode(samplingListNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()), QualityModel.TYPE_SAMPLING_LIST, properties);
		    			samplingNodeRef = childAssocRef.getChildRef();
		    		}		
		    		
		    		// control point
		    		associationService.update(samplingNodeRef, QualityModel.ASSOC_SL_CONTROL_POINT, samplingListDataItem.getControlPoint());
		    				    		
		    		// control step
		    		associationService.update(samplingNodeRef, QualityModel.ASSOC_SL_CONTROL_STEP, samplingListDataItem.getControlStep());		    		
	    		}
			}
		}
	}

}
