package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.ECOReportService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ChangeUnitDataItem;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.ecm.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.filters.EffectiveFilters;

/**
 * Engineering change order service implementation
 * @author quere
 *
 */
public class ECOServiceImpl implements ECOService {
		
	private static final String VERSION_DESCRIPTION = "Applied by ECO %s";
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);
	
	private WUsedListService wUsedListService;
	private NodeService nodeService;
	private CheckOutCheckInService checkOutCheckInService;
	private ProductService productService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private DictionaryService dictionaryService;
	private RepoService repoService;
	private ECOReportService ecoReportService;
	
	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}


	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}


	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	public void setEcoReportService(ECOReportService ecoReportService) {
		this.ecoReportService = ecoReportService;
	}

	@Override
	public void calculateWUsedList(NodeRef ecoNodeRef) {
		
		logger.debug("calculateWUsedList");
		
		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		
		//clear WUsedList
		ecoData.getWUsedList().clear();
		
		if(ecoData.getReplacementList() != null){
		
			for(ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()){
				
				// replacement, 1st level
				ChangeUnitDataItem changeUnitDataItem = new ChangeUnitDataItem(null, 
												replacementListDataItem.getRevision(), 
												null, 
												null, 
												Boolean.FALSE, 
												replacementListDataItem.getSourceItem(), 
												replacementListDataItem.getTargetItem(),
												null);
				
				if(ecoData.getChangeUnitList()==null){
					ecoData.setChangeUnitList(new LinkedList<ChangeUnitDataItem>());
				}
				
				ecoData.getChangeUnitList().add(changeUnitDataItem);
				ecoData.getWUsedList().add(new WUsedListDataItem(null, 1, null, true, null, replacementListDataItem.getSourceItem()));
				
				List<QName> associationQNames = wUsedListService.evaluateWUsedAssociations(replacementListDataItem.getSourceItem());				
				logger.debug("WUsed lists: " + associationQNames + " - of nodeRef: " + replacementListDataItem.getSourceItem());
				
				for(QName associationQName : associationQNames){
									
					MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(replacementListDataItem.getSourceItem(), associationQName, RepoConsts.MAX_DEPTH_LEVEL);
					
					logger.debug("WUsed of list: " + associationQName + "size: " + wUsedData.getTree().size());
					
					QName datalistQName = wUsedListService.evaluateListFromAssociation(associationQName);
					calculateWUsedList(ecoData, replacementListDataItem.getRevision(), wUsedData, datalistQName, 2);
				}
			}					
		}	

		// change state
		ecoData.setEcoState(ECOState.Simulated);
		
		alfrescoRepository.save( ecoData);
	}

	@Override
	public void apply(NodeRef ecoNodeRef) {
		
		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);
		
		// execute
		resetTreatedWUseds(ecoData);
		impactWUseds(ecoData, false);
		
		// change state
		ecoData.setEcoState(ECOState.Applied);
		
		alfrescoRepository.save( ecoData);
		
		// generate report
		ecoReportService.generateReport(ecoData);
	}

	@Override
	public void createSimulationComposants(NodeRef ecoNodeRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		
		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);				
		
		// execute
		resetTreatedWUseds(ecoData);
		impactWUseds(ecoData, true);	
		
		// change state
		ecoData.setEcoState(ECOState.Simulated);
		
		alfrescoRepository.save(ecoData);
		
		// generate report
		ecoReportService.generateReport(ecoData);		
	}

	private void calculateWUsedList(ChangeOrderData ecoData, RevisionType revision, MultiLevelListData wUsedData, QName dataListQName, int level){
		
		for(Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()){
			
			NodeRef sourceItem = kv.getValue().getEntityNodeRef();
			
			ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItem);
			
			if(changeUnitDataItem == null){
				
				changeUnitDataItem = new ChangeUnitDataItem(null, revision, null, null, Boolean.FALSE, sourceItem, null, null);
				ecoData.getChangeUnitList().add(changeUnitDataItem);
			}
			else{
				// test revision
				RevisionType dbRevision = changeUnitDataItem.getRevision();
				
				logger.debug("dbRevision: " + dbRevision + " - revision: " + revision);
				
				if(revision.equals(RevisionType.Major)){
					
					if(!dbRevision.equals(RevisionType.Major)){
					
						changeUnitDataItem.setRevision(RevisionType.Major);
					}					
				}
				else if(revision.equals(RevisionType.Minor)){
					
					if(dbRevision.equals(RevisionType.Minor)){
						
						changeUnitDataItem.setRevision(RevisionType.Minor);
					}
				}
			}
			
			logger.debug("calculateWUsed, linkNodeRef: " + kv.getKey() + " -level: " + level);			
			ecoData.getWUsedList().add(new WUsedListDataItem(null, level, dataListQName, true, kv.getKey(), sourceItem));
			
			// recursive
			calculateWUsedList(ecoData, revision, kv.getValue(), dataListQName, level+1);
		}
	}		
	
	private void createSimulationEntities(ChangeOrderData ecoData){		
		
		// create simulation output folder
		NodeRef parentNodeRef = nodeService.getPrimaryParent(ecoData.getNodeRef()).getParentRef();
		NodeRef tempFolder = repoService.createFolderByPath(parentNodeRef, 
							RepoConsts.PATH_ECO_TEMPORARY, 
							TranslateHelper.getTranslatedPath(RepoConsts.PATH_ECO_TEMPORARY));
		
		
		for(int z_idx=0 ; z_idx<ecoData.getWUsedList().size() ; z_idx++){
			
			WUsedListDataItem wul = ecoData.getWUsedList().get(z_idx);
			
			if(z_idx + 1 < ecoData.getWUsedList().size()){
			
				// has WUsed ?
				WUsedListDataItem nextWUL = ecoData.getWUsedList().get(z_idx+1);					
				if(wul.getDepthLevel() > 1 && nextWUL.getDepthLevel() > wul.getDepthLevel()){
					
					ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(wul.getSourceItem());
					if(changeUnitDataItem.getSimulationItem() == null){
						
						// create simulation output
						NodeRef productToFormulateNodeRef;
						if(changeUnitDataItem.getTargetItem() == null){
							productToFormulateNodeRef = changeUnitDataItem.getSourceItem();
						}
						else{
							productToFormulateNodeRef = changeUnitDataItem.getTargetItem();
						}
						
						ProductData productData = (ProductData) alfrescoRepository.findOne(productToFormulateNodeRef);
						
						productData.setNodeRef(null);
						productData.setParentNodeRef(tempFolder);
						
						NodeRef simulationNodeRef = alfrescoRepository.save( productData).getNodeRef();
						
						// add simulation aspect
						nodeService.addAspect(simulationNodeRef, ECMModel.ASPECT_SIMULATION_ENTITY, null);
						nodeService.createAssociation(simulationNodeRef, productToFormulateNodeRef, ECMModel.ASSOC_SIMULATION_SOURCE_ITEM);

						logger.debug("simulationNodeRef product: " + productToFormulateNodeRef + " created: " + simulationNodeRef);
						// store simulation item
						changeUnitDataItem.setSimulationItem(simulationNodeRef);
					}
				}
			}			
		}		
	}
	
	/**
	 * Reset treated WUsed
	 * @param ecoNodeRef
	 */
	private void resetTreatedWUseds(ChangeOrderData ecoData){
				
		for(ChangeUnitDataItem cul : ecoData.getChangeUnitList()){					
			
			if(cul.getTreated()){
				cul.setTreated(Boolean.FALSE);
				cul.setReqDetails("");
				cul.setReqType(null);
			}
		}		
	}
	
	/**
	 * Impact the WUsed items
	 * @param ecoNodeRef
	 * @param isSimulation
	 */
	private void impactWUseds(ChangeOrderData ecoData, boolean isSimulation){				
		
		if(isSimulation){
			
			// clear simulationList
			ecoData.getSimulationList().clear();
			
			// create simulation output
			createSimulationEntities(ecoData);
		}
		
		// clear changeUnitList
		for(ChangeUnitDataItem cul : ecoData.getChangeUnitList()){
			cul.setTreated(Boolean.FALSE);
			cul.setReqType(null);
			cul.setReqDetails(null);
		}
		
		logger.debug("impactWUseds, WUsed impacted size: " + ecoData.getWUsedList().size());
		
		for(int z_idx=0 ; z_idx<ecoData.getWUsedList().size() ; z_idx++){
					
			WUsedListDataItem wul = ecoData.getWUsedList().get(z_idx);
			
			// impact WUsed ?
			if(wul.getIsWUsedImpacted()){
			
				// is CU treated ? yes => go to next wul
				ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(wul.getSourceItem());
				
				if(changeUnitDataItem != null){
				
					Boolean isCUTreated = changeUnitDataItem.getTreated();
					
					if(isCUTreated != null && isCUTreated.equals(Boolean.TRUE)){
						continue;
					}
					else{
											
						// 1st level : nothing to do...
						if(wul.getDepthLevel() == 1){					
							changeUnitDataItem.setTreated(Boolean.TRUE);
						}
						else{
							impactWUsed(ecoData, wul.getSourceItem(), isSimulation);
						}
					}	
				}	
			}
			else{
				// look for next sibling node, and delete unused changeUnit
				//TODO : gérer le cas où recoche l'impact d'un cas d'emploi, il faut recréer le changeUnit
				if(z_idx<ecoData.getWUsedList().size()){
				
					int index = z_idx+1;
					while(index<ecoData.getWUsedList().size() && ecoData.getWUsedList().get(index).getDepthLevel() > wul.getDepthLevel()){
						index++;
						//deleteChangeUnit(ecoData, ecoData.getWUsedList().get(index).getSourceItem());
					}
					logger.debug("Next sibling node of : " + z_idx + " is : " + index);
					z_idx = index;
				}				
			}
		}				
	}
	
//	private void deleteChangeUnit(ChangeOrderData ecoData, NodeRef sourceItemNodeRef){
//		
//		ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItemNodeRef);
//		
//		if(changeUnitDataItem != null){
//		
//			// delete simulation item
//			if(changeUnitDataItem.getSimulationItem() != null){
//				
//				NodeRef parentNodeRef = nodeService.getPrimaryParent(changeUnitDataItem.getSimulationItem()).getParentRef();
//				QName parentQName = nodeService.getType(parentNodeRef);
//				
//				if(parentQName.equals(BeCPGModel.TYPE_ENTITY_FOLDER)){
//												
//					nodeService.deleteNode(parentNodeRef);
//				}
//			}
//			
//			nodeService.deleteNode(changeUnitDataItem.getNodeRef());
//			
//			ecoData.getChangeUnitMap().remove(changeUnitDataItem.getSourceItem());
//		}		
//	}
//	
	
	/**
	 * Impact the WUsed item
	 * @param changeUnitNodeRef
	 * @return
	 */
	private void impactWUsed(ChangeOrderData ecoData, NodeRef sourceItemNodeRef, boolean isSimulation){

		ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItemNodeRef);		
		// calculate dataList to load
		List<QName> dataListQNames = new ArrayList<QName>();	
		
		/*
		 *  calculate ReplacementLink (replace leafs)
		 */
		QName className = nodeService.getType(sourceItemNodeRef);
		if(dictionaryService.isSubClass(className, BeCPGModel.TYPE_PRODUCT)){
											
			Map<NodeRef,NodeRef> replacementLinks = new HashMap<NodeRef, NodeRef>();
			List<AssociationRef> assocRefs = nodeService.getSourceAssocs(sourceItemNodeRef, ECMModel.ASSOC_WUL_SOURCE_ITEM);
			
			for(AssociationRef assocRef : assocRefs){
				
				NodeRef linkNodeRef = assocRef.getSourceRef();
				
				for(int z_idx=0 ; z_idx<ecoData.getWUsedList().size() ; z_idx++){
					
					WUsedListDataItem wulDataItem = ecoData.getWUsedList().get(z_idx);					
					
					if(linkNodeRef.equals(wulDataItem.getNodeRef())){
							
						// look for previous level
						int index = z_idx;
						while(ecoData.getWUsedList().get(index).getDepthLevel() >= wulDataItem.getDepthLevel()){
							index--;
						}
						
						logger.debug("current wul: " + z_idx + " previous level: " + index);
						
						WUsedListDataItem wulDataItem2 = ecoData.getWUsedList().get(index);
						ChangeUnitDataItem changeUnitDataItem2 = ecoData.getChangeUnitMap().get(wulDataItem2.getSourceItem());
						Boolean isWUsedImpacted = changeUnitDataItem2.getTreated();
						
						// 1st level or treated
						if(wulDataItem2.getDepthLevel() == 1 || 
									(isWUsedImpacted != null && isWUsedImpacted == Boolean.TRUE)){
													
							NodeRef replacementNodeRef = null;
							
							if(changeUnitDataItem2.getTargetItem() == null){
								replacementNodeRef = changeUnitDataItem2.getSourceItem();
							}
							else{
								replacementNodeRef = changeUnitDataItem2.getTargetItem();
							}
							
							if(isSimulation){
								
								// is there any simulation created ?								
								if(changeUnitDataItem2.getSimulationItem() != null){
									replacementNodeRef = changeUnitDataItem2.getSimulationItem();
								}								
							}
							
							replacementLinks.put(wulDataItem.getLink(), replacementNodeRef);
						}
						else{
							// WUsed not impacted => exit (it will be treated by another branch
							logger.debug("WUsed not impacted => exit (it will be treated by another branch");
							return;
						}
						
						// look for dataList to load
						QName qName = wulDataItem.getImpactedDataList();
						if(!dataListQNames.contains(qName)){
							dataListQNames.add(qName);
						}																						
					}
				}			
			}
			
			NodeRef simulationNodeRef = null;
			NodeRef productToFormulateNodeRef = null;
			
			if(changeUnitDataItem.getTargetItem() == null){
				productToFormulateNodeRef = changeUnitDataItem.getSourceItem();
			}
			else{
				productToFormulateNodeRef = changeUnitDataItem.getTargetItem();
			}
			
			if(isSimulation){
				
				// is there any simulation created ?
				if(changeUnitDataItem.getSimulationItem() != null){
					simulationNodeRef = changeUnitDataItem.getSimulationItem();
				}
				
				logger.debug("simulation node is" + simulationNodeRef);
			}
			else{
				
				/*
				 *  manage revision
				 */
				if(!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)){
					
					VersionType versionType = changeUnitDataItem.getRevision().equals(RevisionType.Major) ? VersionType.MAJOR : VersionType.MINOR;
					
					// checkout
					NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(sourceItemNodeRef);
					
					// checkin
					Map<String, Serializable> properties = new HashMap<String, Serializable>();
					properties.put(VersionModel.PROP_VERSION_TYPE, versionType);
					properties.put(Version.PROP_DESCRIPTION, String.format(VERSION_DESCRIPTION, ecoData.getCode()));	
					
					productToFormulateNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);									
					changeUnitDataItem.setTargetItem(productToFormulateNodeRef);
				}							
			}
									
						
			ProductData productToFormulateData = (ProductData) alfrescoRepository.findOne(productToFormulateNodeRef);
						
			logger.debug("do replacement for node: " + nodeService.getProperty(sourceItemNodeRef, ContentModel.PROP_NAME));
			
			for(QName dataListQName : dataListQNames){
				
				//TODO not generic
				if(dataListQName.equals(BeCPGModel.TYPE_COMPOLIST)){
					
					for(CompoListDataItem c : productToFormulateData.getCompoList(EffectiveFilters.FUTUR)){
						
						if(replacementLinks.containsKey(c.getNodeRef())){

							logger.debug("replace node " + c.getNodeRef() + " by node " + replacementLinks.get(c.getNodeRef()));							
							c.setProduct(replacementLinks.get(c.getNodeRef()));
						}
					}
				}
				else if(dataListQName.equals(BeCPGModel.TYPE_PACKAGINGLIST)){
					
					for(PackagingListDataItem p : productToFormulateData.getPackagingList(EffectiveFilters.FUTUR)){
						
						if(replacementLinks.containsKey(p.getNodeRef())){
							
							p.setProduct(replacementLinks.get(p.getNodeRef()));
						}
					}
				}
			}
			
			
			
			
			try {
				productToFormulateData = productService.formulate(productToFormulateData);
			} catch (FormulateException e) {
				
				logger.error("Failed to formulate product. NodeRef: " + productToFormulateNodeRef, e);
			}
			
			// isTreated and save in DB
			changeUnitDataItem.setTreated(Boolean.TRUE);			
			
			// update simulation List
			ProductData sourceData = (ProductData) alfrescoRepository.findOne(sourceItemNodeRef);
			updateCalculatedCharactValues(ecoData, sourceData, productToFormulateData);
			
			// check req
			checkRequirements(ecoData, sourceData, productToFormulateData);
			
			// save in DB
			if(isSimulation){
				//save in DB if we created a simulation node
				if(simulationNodeRef != null){		
					
					productToFormulateData.setNodeRef(simulationNodeRef);					
					alfrescoRepository.save( productToFormulateData);
				}
			}	
			else{
				productToFormulateData.setNodeRef(productToFormulateNodeRef);
				alfrescoRepository.save( productToFormulateData);
			}
		}
	}	
	
	
	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData, ProductData targetData){
		
		for(NodeRef charactNodeRef : ecoData.getCalculatedCharacts()){
			
			Double sourceValue = getCharactValue(charactNodeRef, sourceData);
			Double targetValue = getCharactValue(charactNodeRef, targetData);
			
			logger.debug("calculated charact: " + charactNodeRef + " - sourceValue: " + sourceValue + " - targetValue: " + targetValue);
			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, targetValue));
		}
		
		logger.debug("simList size: " + ecoData.getSimulationList().size());
	}
	
	private void checkRequirements(ChangeOrderData ecoData, ProductData sourceData, ProductData targetData){
		
		RequirementType reqType = null;
		String reqDetails = null;
		
		if(targetData.getCompoListView()!=null && targetData.getCompoListView().getReqCtrlList() != null){
			for(ReqCtrlListDataItem rcl : targetData.getCompoListView().getReqCtrlList()){
				
				RequirementType newReqType = rcl.getReqType();
				
				if(reqType == null){
					reqType = newReqType;
				}
				else{
					
					 if(RequirementType.Tolerated.equals(newReqType) && reqType.equals(RequirementType.Info)){
						reqType = newReqType;
					}
					else if(RequirementType.Forbidden.equals(newReqType) && !reqType.equals(RequirementType.Forbidden)){
						reqType = newReqType;
					}
				}
				
				if(reqDetails == null){
					reqDetails = rcl.getReqMessage();
				}
				else{
					reqDetails += RepoConsts.LABEL_SEPARATOR;
					reqDetails += rcl.getReqMessage();
				}
			}
		}		
		
		ChangeUnitDataItem cuDataItem = ecoData.getChangeUnitMap().get(sourceData.getNodeRef());
		
		if(cuDataItem != null){
			cuDataItem.setReqType(reqType);
			cuDataItem.setReqDetails(reqDetails);
		}
	}
	
	private Double getCharactValue(NodeRef charactNodeRef, ProductData productData){
		
		Double value = null;
		QName charactType = nodeService.getType(charactNodeRef);
		
		//TODO not generic
		if(charactType.equals(BeCPGModel.TYPE_COST)){
			
			if(productData.getCostList() != null){
			
				for(CostListDataItem c : productData.getCostList()){
					if(charactNodeRef.equals(c.getCost())){
						value = c.getValue();
					}
				}
			}			
		}
		else if(charactType.equals(BeCPGModel.TYPE_NUT)){
			
			if(productData.getNutList() != null){
			
				for(NutListDataItem n : productData.getNutList()){
					if(charactNodeRef.equals(n.getNut())){
						value = n.getValue();
					}
				}
			}			
		}
		else if(charactType.equals(BeCPGModel.TYPE_ING)){
			
			if(productData.getIngList() != null){
				
				for(IngListDataItem i : productData.getIngList()){
					if(charactNodeRef.equals(i.getIng())){
						value = i.getQtyPerc();
					}
				}	
			}			
		}
		
		return value;
	}	
}
