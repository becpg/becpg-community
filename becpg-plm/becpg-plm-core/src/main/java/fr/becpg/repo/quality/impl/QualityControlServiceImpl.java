/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.DataListModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.quality.QualityControlService;
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.QualityControlState;
import fr.becpg.repo.quality.data.WorkItemAnalysisData;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.ControlListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class QualityControlServiceImpl implements QualityControlService {

	private static Log logger = LogFactory.getLog(QualityControlServiceImpl.class);
	
	private final static String BATCH_SEPARATOR = "/";
	private static final long HOUR = 3600*1000; // in milli-seconds.
	
	private NodeService nodeService;	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private EntityListDAO entityListDAO;
	private BehaviourFilter policyBehaviourFilter;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	public void createSamplingList(NodeRef qcNodeRef, NodeRef controlPlanNodeRef) {
		
		logger.debug("createSamplingList");		
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);								
		Map<NodeRef, WorkItemAnalysisData> wiaMap = new HashMap<NodeRef, WorkItemAnalysisData>();
		createSamples(qualityControlData, controlPlanNodeRef, wiaMap);
		alfrescoRepository.save(qualityControlData);
	}
	
	private void createSamples(QualityControlData qualityControlData, NodeRef controlPlanNodeRef, Map<NodeRef, WorkItemAnalysisData> wiaMap){
		
		logger.debug("createSamples");		
		List<SamplingListDataItem> samplingList = new ArrayList<SamplingListDataItem>();		
		int samplesCounter = 0;
		String batchId = qualityControlData.getBatchId();
		Date batchStart = qualityControlData.getBatchStart();
		Integer batchDuration = qualityControlData.getBatchDuration();
		
		logger.debug("load control plan");
		ControlPlanData controlPlanData = (ControlPlanData) alfrescoRepository.findOne(controlPlanNodeRef);
		
		logger.debug("control plan loaded, controlPlanData.getSamplingDefList(): " + controlPlanData.getSamplingDefList());
		
		if(controlPlanData.getSamplingDefList() != null){
		
			for(SamplingDefListDataItem sdl : controlPlanData.getSamplingDefList()){
				
				logger.debug("create sample");
				
				//TODO : take in account freq and english language for lot !!!
				String freq = sdl.getFreqUnit();
				int samplesToTake = 1;
				int freqInHour = 0;
				Date sampleDateTime = batchStart;
				
				// per batch
				if(freq.equals("/batch")){
					
				}
				else if(freq.equals("/hour")){
					
					freqInHour = 1;
					samplesToTake = batchDuration / freqInHour + 1;					
				}
				else if(freq.equals("/4hours")){
					
					freqInHour = 4;
					samplesToTake = batchDuration / freqInHour + 1;					
				}
				else if(freq.equals("/8hours")){
					
					freqInHour = 8;
					samplesToTake = batchDuration / freqInHour + 1;					
				}
				
				// create samples to take 
				for(int z_idx=0 ; z_idx<samplesToTake ; z_idx++){
					
					// several samples must be taken
					for(int z_idx2=0 ; z_idx2<sdl.getQty() ; z_idx2++){
					
						samplesCounter++;
						String sampleId = batchId + BATCH_SEPARATOR + samplesCounter;
						
						samplingList.add(new SamplingListDataItem(
										sampleDateTime, 
										sampleId, 
										null, 
										sdl.getControlPoint(), 
										sdl.getControlStep(),
										sdl.getSamplingGroup(),
										sdl.getControlingGroup(),
										sdl.getFixingGroup()));
					}			
					
					// calculate next time
					sampleDateTime = new Date(sampleDateTime.getTime() + freqInHour * HOUR);
				}				
			}
			
			qualityControlData.setSamplesCounter(samplesCounter);
			qualityControlData.setSamplingList(samplingList);			
		}		
	}
	
	@Override
	public void createControlList(NodeRef sampleListNodeRef) {
		
		NodeRef qcNodeRef = entityListDAO.getEntity(sampleListNodeRef);
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);
		
		ProductData productData = null;
		ProductData productMicrobioCriteriaData = null;
		
		if(qualityControlData.getProduct() != null){
		
			logger.debug("createSamplingList - load product");

			productData = (ProductData) alfrescoRepository.findOne(qualityControlData.getProduct());
			
			// load microbio
			List<AssociationRef> controlPointAssocRefs = nodeService.getTargetAssocs(qualityControlData.getProduct(), PLMModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
			
			if(!controlPointAssocRefs.isEmpty()){
				NodeRef productMicrobioCriteriaNodeRef = (controlPointAssocRefs.get(0)).getTargetRef();
				
				productMicrobioCriteriaData = (ProductData) alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);				
				productData.setMicrobioList(productMicrobioCriteriaData.getMicrobioList());
			}			
		}
		
		SamplingListDataItem sl = (SamplingListDataItem) alfrescoRepository.findOne(sampleListNodeRef);
		List<ControlListDataItem> controlList = calculateControlList(qualityControlData, sl, productData);
		createControlList(qualityControlData.getNodeRef(), sl.getControlingGroup(), controlList);		
	}

	/**
	 * get control point and create control list
	 * @param qualityControlData
	 * @param sampleListNodeRef
	 * @param productData
	 * @return
	 */
	private List<ControlListDataItem> calculateControlList(QualityControlData qualityControlData, SamplingListDataItem sl, ProductData productData){
		
		List<ControlListDataItem> controlList = new LinkedList<>();
		
		ControlPointData controlPointData = (ControlPointData) alfrescoRepository.findOne(sl.getControlPoint());
		
		logger.debug("calculateControlList, controlPointData.getControlDefList(): " + controlPointData.getControlDefList().size());				
		for(ControlDefListDataItem cdl : controlPointData.getControlDefList()){
			
			for(NodeRef n : cdl.getCharacts()){
			
				Double target = null;
				Double mini = null;
				Double maxi = null;
				String unit = null;
				
				if(productData != null){
					// TODO : générique			
					if(cdl.getType().equals("bcpg_microbioList")){
					
						if(productData.getMicrobioList() != null){							
						
							for(MicrobioListDataItem mbl : productData.getMicrobioList()){
								if(n.equals(mbl.getMicrobio())){
									
									target = mbl.getValue();
									unit = mbl.getUnit();
									maxi = mbl.getMaxi();
									break;
								}
							}
						}						
					}
					else if(cdl.getType().equals("bcpg_nutList")){
						
						if(productData.getNutList() != null){
							
							for(NutListDataItem nl : productData.getNutList()){
								if(n.equals(nl.getNut())){
									
									target = nl.getValue();
									unit = nl.getUnit();
									mini = nl.getMini();
									maxi = nl.getMaxi();
									break;
								}
							}
						}					
					}
				}
				
				List<NodeRef> subList = new ArrayList<NodeRef>();
				subList.add(n);
				
				controlList.add(new ControlListDataItem(null, cdl.getType(), mini, maxi, cdl.getRequired(), 
													sl.getSampleId(), null, target, unit, null, cdl.getMethod(), subList));					
				}		
			}			
		
		return controlList;
	}
	
	private void createControlList(NodeRef qualityControlNodeRef, NodeRef authorityNodeRef, List<ControlListDataItem> controlList){
				
		String name = "Analyses";		
		if(authorityNodeRef != null){
			name += " - " + nodeService.getProperty(authorityNodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
		}
		
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(qualityControlNodeRef);
		NodeRef controlListNodeRef = entityListDAO.getList(listContainerNodeRef, name);
		if(controlListNodeRef == null){
			try{
				policyBehaviourFilter.disableBehaviour(DataListModel.TYPE_DATALIST);
				controlListNodeRef = entityListDAO.createList(listContainerNodeRef, name, QualityModel.TYPE_CONTROL_LIST);
			}
			finally{
				policyBehaviourFilter.enableBehaviour(DataListModel.TYPE_DATALIST);
			}			
		}
		
		logger.debug("createControlList " + controlList.size());
		for(ControlListDataItem cl : controlList){
			alfrescoRepository.create(controlListNodeRef, cl);
		}
	}

	@Override
	public void updateControlListState(NodeRef controlListNodeRef) {
		Double mini = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_MINI);
		Double maxi = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_MAXI);
		Double value = (Double) nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_VALUE);
		
		if(value != null && (mini != null || maxi != null)){
			
			boolean isCompliant = true;
			if(mini != null && mini.compareTo(value) > 0){
				isCompliant = false;
			}
			if(maxi != null && maxi.compareTo(value) < 0){
				isCompliant = false;
			}
			logger.debug("updateControlListState isCompliant: " + isCompliant + " mini " + mini + " maxi " + maxi + " value " + value);
			if(isCompliant){
				nodeService.setProperty(controlListNodeRef, QualityModel.PROP_CL_STATE, QualityControlState.Compliant.toString());				
			}	
			else{
				nodeService.setProperty(controlListNodeRef, QualityModel.PROP_CL_STATE, QualityControlState.NonCompliant.toString());
			}						
		}
		
		updateSampleState(controlListNodeRef);		
	}
		
	private void updateSampleState(NodeRef controlListNodeRef){
	
		String sampleId = (String)nodeService.getProperty(controlListNodeRef, QualityModel.PROP_CL_SAMPLE_ID);
		if(sampleId != null){
			NodeRef parentNodeRef = nodeService.getPrimaryParent(controlListNodeRef).getParentRef();
			if(isSampleControled(parentNodeRef, sampleId)){
				NodeRef entityNodeRef = entityListDAO.getEntity(controlListNodeRef);
				QualityControlData qualityControlData = (QualityControlData)alfrescoRepository.findOne(entityNodeRef);
				QualityControlState qcState = QualityControlState.Compliant;
				
				for(SamplingListDataItem sl : qualityControlData.getSamplingList()){
					if(sampleId.equals(sl.getSampleId())){
						sl.setSampleState(calculateSampleState(parentNodeRef, sampleId));
					}
					
					if(sl.getSampleState() == null || sl.getSampleState().equals("")){
						qcState = sl.getSampleState();
						break;
					}
					else if(sl.getSampleState().equals(QualityControlState.NonCompliant)){						
						qcState = sl.getSampleState();
					}					
				}				
				
				logger.debug("QC state : " + qcState);
				qualityControlData.setState(qcState);
				alfrescoRepository.save(qualityControlData);
			}
		}
		else{
			logger.warn("SampleId is null");
		} 
	}
	
	private boolean isSampleControled(NodeRef parentNodeRef, String sampleId){
		
		boolean isSampleControled = true;
				
		List<NodeRef> clNodeRefs = BeCPGQueryBuilder
		.createQuery()
		.ofType(QualityModel.TYPE_CONTROL_LIST)
		.parent(parentNodeRef)		
		.andPropEquals(QualityModel.PROP_CL_SAMPLE_ID, sampleId)
		// unsupported in DB
		//.andPropEquals(QualityModel.PROP_CL_REQUIRED, "true")
		.isNull(QualityModel.PROP_CL_STATE)
		.inDB()
		.list();
		
		for(NodeRef clNodeRef : clNodeRefs){
			Boolean isRequired = (Boolean)nodeService.getProperty(clNodeRef, QualityModel.PROP_CL_REQUIRED);
			if(isRequired != null && isRequired.booleanValue()){
				isSampleControled = false;
				break;
			}
		}
		
		logger.debug("Is sample controled : " + isSampleControled);
		return isSampleControled;
	}
	
	private QualityControlState calculateSampleState(NodeRef parentNodeRef, String sampleId){
		
		QualityControlState sampleState = QualityControlState.Compliant;
		
		List<NodeRef> clNodeRefs = BeCPGQueryBuilder
		.createQuery()
		.ofType(QualityModel.TYPE_CONTROL_LIST)
		.parent(parentNodeRef)
		.andPropEquals(QualityModel.PROP_CL_SAMPLE_ID, sampleId)	
		// unsupported in DB
		//.andPropEquals(QualityModel.PROP_CL_REQUIRED, "true")
		.andPropEquals(QualityModel.PROP_CL_STATE, QualityControlState.NonCompliant.toString())
		.inDB()
		.list();
		
		for(NodeRef clNodeRef : clNodeRefs){
			Boolean isRequired = (Boolean)nodeService.getProperty(clNodeRef, QualityModel.PROP_CL_REQUIRED);
			if(isRequired != null && isRequired.booleanValue()){
				sampleState = QualityControlState.NonCompliant;
				break;
			}
		}
		
		logger.debug("Sample state : " + sampleState);
		return sampleState;
	}
}
