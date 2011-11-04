package fr.becpg.repo.eco.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.NodeSearcher;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECOModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.eco.ECOService;
import fr.becpg.repo.eco.data.ChangeOrderData;
import fr.becpg.repo.eco.data.RevisionType;
import fr.becpg.repo.eco.data.dataList.ChangeUnitData;
import fr.becpg.repo.eco.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.eco.data.dataList.SimulationListDataItem;
import fr.becpg.repo.eco.data.dataList.WUsedListDataItem;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityCheckOutCheckInService;
import fr.becpg.repo.entity.version.EntityCheckOutCheckInServiceImpl;
import fr.becpg.repo.entity.wused.WUsedListService;
import fr.becpg.repo.entity.wused.data.WUsedData;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.formulation.FormulateException;

/**
 * Engineering change order service implementation
 * @author quere
 *
 */
public class ECOServiceImpl implements ECOService {
		
	private static final String VERSION_DESCRIPTION = "Applied by ECO %s";
	private static final String VERSION_INITIAL = "1.0";
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);
	
	private BeCPGDao<ChangeOrderData>changeOrderDAO;
	private WUsedListService wUsedListService;
	private BeCPGDao<ChangeUnitData>changeUnitDAO;
	private NodeService nodeService;
	private EntityCheckOutCheckInService entityCheckOutCheckInService;
	private ProductService productService;
	private ProductDAO productDAO;
	private EntityListDAO entityListDAO;
	private DictionaryService dictionaryService;
	private ProductDictionaryService productDictionaryService;
	private RepoService repoService;
	private AssociationService associationService;
	
	public void setChangeOrderDAO(BeCPGDao<ChangeOrderData> changeOrderDAO) {
		this.changeOrderDAO = changeOrderDAO;
	}

	public void setwUsedListService(WUsedListService wUsedListService) {
		this.wUsedListService = wUsedListService;
	}

	public void setChangeUnitDAO(BeCPGDao<ChangeUnitData> changeUnitDAO) {
		this.changeUnitDAO = changeUnitDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityCheckOutCheckInService(EntityCheckOutCheckInService entityCheckOutCheckInService) {
		this.entityCheckOutCheckInService = entityCheckOutCheckInService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setProductDictionaryService(ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public void calculateWUsedList(NodeRef ecoNodeRef) {
		
		logger.debug("calculateWUsedList");
		
		ChangeOrderData ecoData = changeOrderDAO.find(ecoNodeRef);
		
		if(ecoData.getReplacementList() != null){
		
			for(ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()){
				
				// replacement, 1st level
				ChangeUnitData changeUnitData = new ChangeUnitData(null, 
												replacementListDataItem.getRevision(), 
												null, 
												null, 
												Boolean.FALSE, 
												replacementListDataItem.getSourceItem(), 
												replacementListDataItem.getTargetItem());
				NodeRef changeUnitNodeRef = changeUnitDAO.create(ecoNodeRef, changeUnitData);				
				ecoData.getWUsedList().add(new WUsedListDataItem(null, 1, null, true, null, changeUnitNodeRef));
				
				List<QName> associationQNames = wUsedListService.evaluateWUsedAssociations(replacementListDataItem.getSourceItem());				
				logger.debug("WUsed lists: " + associationQNames + " - of nodeRef: " + replacementListDataItem.getSourceItem());
				
				for(QName associationQName : associationQNames){
									
					WUsedData wUsedData = wUsedListService.getWUsedEntity(replacementListDataItem.getSourceItem(), associationQName, RepoConsts.MAX_DEPTH_LEVEL);
					
					logger.debug("WUsed of list: " + associationQName + "size: " + wUsedData.getRootList().size());
					
					QName datalistQName = wUsedListService.evaluateListFromAssociation(associationQName);
					calculateWUsedList(ecoData, replacementListDataItem.getRevision(), wUsedData, datalistQName, 2);
				}
			}
			
			changeOrderDAO.update(ecoNodeRef, ecoData);
		}		
	}

	@Override
	public void apply(NodeRef ecoNodeRef) {
		
		impactWUseds(ecoNodeRef, false);		
	}

	@Override
	public void createSimulationComposants(NodeRef ecoNodeRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NodeRef generateSimulationReport(NodeRef ecoNodeRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		
		impactWUseds(ecoNodeRef, true);		
	}

	private void calculateWUsedList(ChangeOrderData ecoData, RevisionType revision, WUsedData wUsedData, QName dataListQName, int level){
		
		for(Map.Entry<NodeRef, WUsedData> kv : wUsedData.getRootList().entrySet()){
			
			NodeRef sourceItem = kv.getValue().getEntityNodeRef();
			String key = ChangeOrderData.getChangeUnitKey(sourceItem, null);
			NodeRef changeUnitNodeRef = ecoData.getChangeUnitsMap().get(key);
			
			if(changeUnitNodeRef == null){
				
				ChangeUnitData changeUnitData = new ChangeUnitData(null, revision, null, null, Boolean.FALSE, sourceItem, null);
				changeUnitNodeRef = changeUnitDAO.create(ecoData.getNodeRef(), changeUnitData);
				ecoData.getChangeUnitsMap().put(key, changeUnitNodeRef);
			}
			else{
				// test revision
				RevisionType dbRevision = RevisionType.valueOf((String)nodeService.getProperty(changeUnitNodeRef, ECOModel.PROP_CU_REVISION));
				if(revision.equals(RevisionType.Major)){
					
					if(!dbRevision.equals(RevisionType.Major)){
					
						nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_REVISION, RevisionType.Major);
					}					
				}
				else if(revision.equals(RevisionType.Minor)){
					
					if(dbRevision.equals(RevisionType.Minor)){
						
						nodeService.setProperty(changeUnitNodeRef, ECOModel.PROP_CU_REVISION, RevisionType.Minor);
					}
				}
			}
			
			logger.debug("calculateWUsed, linkNodeRef: " + kv.getKey() + " -level: " + level);
			ecoData.getWUsedList().add(new WUsedListDataItem(null, level, dataListQName, true, kv.getKey(), changeUnitNodeRef));
			
			// recursive
			calculateWUsedList(ecoData, revision, kv.getValue(), dataListQName, level+1);
		}
	}	
		
	private void createSimulationEntities(ChangeOrderData ecoData){
		
		List<NodeRef> sourceItems = new ArrayList<NodeRef>();		
		Collection<QName> dataListQNames = productDictionaryService.getDataLists();
		
		// create simulation output folder
		NodeRef parentNodeRef = nodeService.getPrimaryParent(ecoData.getNodeRef()).getParentRef();
		NodeRef simulationOutputFolder = repoService.createFolderByPath(parentNodeRef, 
							RepoConsts.PATH_SIMULATION_OUTPUT, 
							TranslateHelper.getTranslatedPath(RepoConsts.PATH_SIMULATION_OUTPUT));
		
		
		for(int z_idx=0 ; z_idx<ecoData.getWUsedList().size() ; z_idx++){
			
			WUsedListDataItem wul = ecoData.getWUsedList().get(z_idx);
			
			if(z_idx + 1 < ecoData.getWUsedList().size()){
			
				// has WUsed ?
				WUsedListDataItem nextWUL = ecoData.getWUsedList().get(z_idx+1);					
				if(wul.getDepthLevel() > 1 && nextWUL.getDepthLevel() > wul.getDepthLevel()){
					
					ChangeUnitData changeUnitData = changeUnitDAO.find(wul.getChangeUnit());
					if(changeUnitData.getTargetItem() == null){
						
						// create simulation output
						ProductData productData = productDAO.find(changeUnitData.getSourceItem(), dataListQNames);
						NodeRef targetNodeRef = productDAO.create(simulationOutputFolder, productData, dataListQNames);
						
						// add simulationEntityAspect
						nodeService.addAspect(targetNodeRef, ECOModel.ASPECT_SIMULATION_ENTITY, null);
						associationService.update(targetNodeRef, ECOModel.ASSOC_SIMULATION_SOURCE_ITEM, changeUnitData.getSourceItem());
						
						// store targetNodeRef
						changeUnitData.setTargetItem(targetNodeRef);
						changeUnitDAO.update(wul.getChangeUnit(), changeUnitData);
					}
				}
			}			
		}
	}
	
	/**
	 * Impact the WUsed items
	 * @param ecoNodeRef
	 * @param isSimulation
	 */
	private void impactWUseds(NodeRef ecoNodeRef, boolean isSimulation){
		
		ChangeOrderData ecoData = changeOrderDAO.find(ecoNodeRef);
		
		// create simulation output
		if(isSimulation){
			createSimulationEntities(ecoData);
		}
		
		logger.debug("impactWUseds, WUsed impacted size: " + ecoData.getWUsedList().size());
		
		for(WUsedListDataItem wul : ecoData.getWUsedList()){
					
			// is CU treated ? yes => go to next wul
			Boolean isCUTreated = (Boolean)nodeService.getProperty(wul.getChangeUnit(), ECOModel.PROP_CU_TREATED);
			
			if(isCUTreated != null && isCUTreated.equals(Boolean.TRUE)){
				continue;
			}
			else{
									
				// 1st level : nothing to do...
				if(wul.getDepthLevel() == 1){					
					nodeService.setProperty(wul.getChangeUnit(), ECOModel.PROP_CU_TREATED, Boolean.TRUE);
				}
				else{
					impactWUsed(ecoData, wul.getChangeUnit(), isSimulation);
				}
			}			
		}
		
		changeOrderDAO.update(ecoNodeRef, ecoData);
	}
	
	
	/**
	 * Impact the WUsed item
	 * @param changeUnitNodeRef
	 * @return
	 */
	private void impactWUsed(ChangeOrderData ecoData, NodeRef changeUnitNodeRef, boolean isSimulation){

		ChangeUnitData changeUnitData = changeUnitDAO.find(changeUnitNodeRef);
		NodeRef sourceNodeRef = changeUnitData.getSourceItem();
		NodeRef targetNodeRef = null;
		// calculate dataList to load
		List<QName> dataListQNames = new ArrayList<QName>();	
		
		/*
		 *  calculate ReplacementLink (replace leafs)
		 */
		QName className = nodeService.getType(sourceNodeRef);
		if(dictionaryService.isSubClass(className, BeCPGModel.TYPE_PRODUCT)){
											
			Map<NodeRef,NodeRef> replacementLinks = new HashMap<NodeRef, NodeRef>();
			List<AssociationRef> assocRefs = nodeService.getSourceAssocs(changeUnitNodeRef, ECOModel.ASSOC_WUL_CHANGE_UNIT);
			
			for(AssociationRef assocRef : assocRefs){
				
				NodeRef linkNodeRef = assocRef.getSourceRef();
				
				for(int z_idx=0 ; z_idx<ecoData.getWUsedList().size() ; z_idx++){
					
					WUsedListDataItem wulDataItem = ecoData.getWUsedList().get(z_idx);
					
					logger.debug("wul level: " + wulDataItem.getDepthLevel() + " - dataList: " + wulDataItem.getImpactedDataList() + " wulDataItem.getLink(): " + wulDataItem.getLink());
					
					if(linkNodeRef.equals(wulDataItem.getNodeRef())){
							
						// look for previous level
						int index = z_idx;
						while(ecoData.getWUsedList().get(index).getDepthLevel() == wulDataItem.getDepthLevel()){
							index--;
						}
						
						logger.debug("current wul: " + z_idx + " previous level: " + index);
						
						WUsedListDataItem wulDataItem2 = ecoData.getWUsedList().get(index);
						NodeRef changeUnitNodeRef2 = wulDataItem2.getChangeUnit();
						Boolean isWUsedImpacted = (Boolean)nodeService.getProperty(changeUnitNodeRef2, ECOModel.PROP_CU_TREATED);
						if(isWUsedImpacted != null && isWUsedImpacted == Boolean.TRUE){
							
							List<AssociationRef> assocRefs2 = nodeService.getTargetAssocs(wulDataItem2.getChangeUnit(), ECOModel.ASSOC_CU_TARGET_ITEM);
							NodeRef targetNodeRef2 = assocRefs2.size() > 0 ? assocRefs2.get(0).getTargetRef() : null;
							
							replacementLinks.put(wulDataItem.getLink(), targetNodeRef2);
						}
						else{
							// WUsed not impacted => exit (it will be treated by another branch)
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
			
			/*
			 *  get targetNodeRef	
			 */
			if(changeUnitData.getTargetItem() != null){
				targetNodeRef = changeUnitData.getTargetItem();
			}
			else if(changeUnitData.getRevision().equals(RevisionType.NoRevision)){
				targetNodeRef = sourceNodeRef;
			}
			else{
				
				if(isSimulation){
					// simulation, but not simulation entity has been create so we work on the sourceNodeRef
					targetNodeRef = sourceNodeRef;
				}
				else{
					
					/*
					 *  do revision
					 */
					String versionLabel = (String)nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_VERSION_LABEL);
					versionLabel = versionLabel == null ? VERSION_INITIAL : versionLabel;
					boolean majorVersion = changeUnitData.getRevision().equals(RevisionType.Major) ? true : false;
					
					//Calculate new version
					VersionNumber versionNumber = entityCheckOutCheckInService.getVersionNumber(versionLabel, majorVersion);
					
					// checkout
					NodeRef workingCopyNodeRef = entityCheckOutCheckInService.checkout(sourceNodeRef);
					
					// checkin
					Map<String, Serializable> properties = new HashMap<String, Serializable>();
					properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), versionNumber.toString());
					properties.put(Version.PROP_DESCRIPTION, String.format(VERSION_DESCRIPTION, ecoData.getCode()));	
					
					targetNodeRef = entityCheckOutCheckInService.checkin(workingCopyNodeRef, properties);					
					
					changeUnitData.setTargetItem(targetNodeRef);
				}
							
			}	
			
			NodeRef listContainerNodeRef = entityListDAO.getListContainer(sourceNodeRef);
			List<QName> existingLists = entityListDAO.getExistingListsQName(listContainerNodeRef);
			
			ProductData sourceData = productDAO.find(sourceNodeRef, existingLists);
			ProductData targetData = null;
			if(sourceNodeRef.equals(targetNodeRef)){		
				//TODO: do a deep copy in memory instead of load it from DB
				//targetData = new ProductData(sourceData);
				targetData = productDAO.find(targetNodeRef, existingLists);
			}
			else{
				targetData = productDAO.find(targetNodeRef, existingLists);
			}
			
			for(QName dataListQName : dataListQNames){
				
				//TODO not generic
				if(dataListQName.equals(BeCPGModel.TYPE_COMPOLIST)){
					
					for(CompoListDataItem c : targetData.getCompoList()){
						
						if(replacementLinks.containsKey(c.getNodeRef())){
							logger.debug("Replace CompoList item, linkNodeRef: " + c.getNodeRef() + " - target: " + replacementLinks.get(c.getNodeRef()));
							c.setProduct(replacementLinks.get(c.getNodeRef()));
						}
					}
				}
				else if(dataListQName.equals(BeCPGModel.TYPE_PACKAGINGLIST)){
					
					for(PackagingListDataItem p : targetData.getPackagingList()){
						
						if(replacementLinks.containsKey(p.getNodeRef())){
							logger.debug("Replace pkgingList item, linkNodeRef: " + p.getNodeRef() + " - target: " + replacementLinks.get(p.getNodeRef()));
							p.setProduct(replacementLinks.get(p.getNodeRef()));
						}
					}
				}
			}
			
			try {
				logger.debug("formulate nodeRef: " + targetNodeRef);
				targetData = productService.formulate(targetData);
			} catch (FormulateException e) {
				
				logger.error("Failed to formulate product. NodeRef: " + targetNodeRef, e);
			}
			
			// update simulation List
			updateCalculatedCharactValues(ecoData, sourceData, targetData);
			
			// save in DB
			if(!isSimulation){
				productDAO.update(targetNodeRef, targetData, productDictionaryService.getDataLists());
			}					
		}		
	}	
	
	
	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData, ProductData targetData){
		
		for(NodeRef charactNodeRef : ecoData.getCalculatedCharacts()){
			
			Float sourceValue = getCharactValue(charactNodeRef, sourceData);
			Float targetValue = getCharactValue(charactNodeRef, targetData);
			
			logger.debug("calculated charact: " + charactNodeRef + " - sourceValue: " + sourceValue + " - targetValue: " + targetValue);
			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, targetValue));
		}
		
		logger.debug("simList size: " + ecoData.getSimulationList().size());
	}
	
	private Float getCharactValue(NodeRef charactNodeRef, ProductData productData){
		
		Float value = null;
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
