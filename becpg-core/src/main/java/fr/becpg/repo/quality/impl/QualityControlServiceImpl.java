package fr.becpg.repo.quality.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.MicrobioListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.quality.QualityControlService;
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.WorkItemAnalysisData;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.ControlListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class QualityControlServiceImpl implements QualityControlService {

	private static Log logger = LogFactory.getLog(QualityControlServiceImpl.class);
	
	private final static String BATCH_SEPARATOR = "/";
	private static final long HOUR = 3600*1000; // in milli-seconds.
	
	private NodeService nodeService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

		
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}



	@Override
	public void createSamplingList(NodeRef qcNodeRef, NodeRef controlPlanNodeRef) {
		
		logger.debug("createSamplingList");
		
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(qcNodeRef);
		
		// TODO gérer les controles qui ne sont pas fait sur un produit (ex: vérif résidu désinfectant cuve,etc...)
		// TODO optimiser le chargement des datalists...		
		ProductData productData = null;
		ProductData productMicrobioCriteriaData = null;
		
		if(qualityControlData.getProduct() != null){
		
			logger.debug("createSamplingList - load product");

			productData = (ProductData) alfrescoRepository.findOne(qualityControlData.getProduct());
			
			// load microbio
			List<AssociationRef> controlPointAssocRefs = nodeService.getTargetAssocs(qualityControlData.getProduct(), BeCPGModel.ASSOC_PRODUCT_MICROBIO_CRITERIA);
			
			if(!controlPointAssocRefs.isEmpty()){
				NodeRef productMicrobioCriteriaNodeRef = (controlPointAssocRefs.get(0)).getTargetRef();
				
				productMicrobioCriteriaData = (ProductData) alfrescoRepository.findOne(productMicrobioCriteriaNodeRef);				
				productData.setMicrobioList(productMicrobioCriteriaData.getMicrobioList());
			}			
		}		
				
		createSamples(qualityControlData, controlPlanNodeRef, productData);
		
		logger.debug("save qualityControl, name: " + qualityControlData.getName());
		alfrescoRepository.create(qcNodeRef, qualityControlData);
		
	}
	
	private void createSamples(QualityControlData qualityControlData, NodeRef controlPlanNodeRef, ProductData productData){
		
		logger.debug("createSamples");
		
		List<SamplingListDataItem> samplingList = new ArrayList<SamplingListDataItem>();
		Map<NodeRef, WorkItemAnalysisData> wiaMap = new HashMap<NodeRef, WorkItemAnalysisData>();
		
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
				if(freq.equals("/lot")){
					
				}
				else if(freq.equals("/heure")){
					
					freqInHour = 1;
					samplesToTake = batchDuration / freqInHour + 1;					
				}
				else if(freq.equals("/4heures")){
					
					freqInHour = 4;
					samplesToTake = batchDuration / freqInHour + 1;					
				}
				else if(freq.equals("/8heures")){
					
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
										sdl.getControlStep()));													
											
						// create work items
						WorkItemAnalysisData wiaData = wiaMap.get(sdl.getControlingGroup());
						
						if(wiaData == null){
							wiaData = new WorkItemAnalysisData();
							wiaMap.put(sdl.getControlingGroup(), wiaData);
						}
						
						prepareWorkItemAnalysis(sdl, sampleId, wiaData, productData);
					}			
					
					// calculate next time
					sampleDateTime = new Date(sampleDateTime.getTime() + freqInHour * HOUR);
				}				
			}
			
			qualityControlData.setSamplesCounter(samplesCounter);
			qualityControlData.setSamplingList(samplingList);
			
			// create work items
			createWorkItemAnalysis(qualityControlData.getNodeRef(), wiaMap);
		}		
	}

	private void prepareWorkItemAnalysis(SamplingDefListDataItem sdl, String sampleId, WorkItemAnalysisData wiaData, ProductData productData){
		
		// get control point and create control list
		ControlPointData controlPointData = (ControlPointData) alfrescoRepository.findOne(sdl.getControlPoint());
		
		logger.debug("prepareWorkItemAnalysis, controlPointData.getControlDefList(): " + controlPointData.getControlDefList());
		
		if(controlPointData.getControlDefList() != null){
			
			for(ControlDefListDataItem cdl : controlPointData.getControlDefList()){
				
				for(NodeRef n : cdl.getCharacts()){
				
					Double target = null;
					Double mini = null;
					Double maxi = null;
					String unit = null;
					
					// TODO : générique			
					if(cdl.getType().equals("Microbiologie")){
					
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
					else if(cdl.getType().equals("Nutritionnelle")){
						
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
					
					List<NodeRef> subList = new ArrayList<NodeRef>();
					subList.add(n);
					
					wiaData.getControlList().add(new ControlListDataItem(null, cdl.getType(), mini, maxi, cdl.getRequired(), 
														sampleId, null, target, unit, null, cdl.getMethod(), subList));
					
				}		
			}
		}		
	}
	
	private void createWorkItemAnalysis(NodeRef qualityControlNodeRef, Map<NodeRef, WorkItemAnalysisData> wiaMap){
		
		for(NodeRef authorityNodeRef : wiaMap.keySet()){
			
			WorkItemAnalysisData wiaData = wiaMap.get(authorityNodeRef);
			String name = "Analyses";
			
			if(authorityNodeRef != null){
				name += " - " + nodeService.getProperty(authorityNodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
			}
			
			wiaData.setName(name);
			
			// TODO manage update ? or create another work item Analyses - RD - 1 puis 2, etc...			
			alfrescoRepository.create(qualityControlNodeRef, wiaData);
		}
	}
}
