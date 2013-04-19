package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.repo.RepoConsts;
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
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.product.helper.CharactHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.L2CacheSupport.Action;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Engineering change order service implementation
 * 
 * @author quere
 * 
 */
@Service
public class ECOServiceImpl implements ECOService {

	private static final String VERSION_DESCRIPTION = "Applied by ECO %s";
	private static Log logger = LogFactory.getLog(ECOServiceImpl.class);

	private WUsedListService wUsedListService;

	private NodeService nodeService;
	private CheckOutCheckInService checkOutCheckInService;

	private ProductService productService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private DictionaryService dictionaryService;

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


	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		logger.debug("Run simulation");
		doRun(ecoNodeRef, ECOState.Simulated);
	}

	@Override
	public void apply(NodeRef ecoNodeRef) {
		logger.debug("Run apply");
		doRun(ecoNodeRef, ECOState.Applied);
	}

	private void doRun(NodeRef ecoNodeRef, final ECOState state) {
		final ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		L2CacheSupport.doInCacheContext(new Action() {

			@Override
			public void run() {
				StopWatch watch = new StopWatch();
				if(logger.isDebugEnabled()){
					watch.start();
				}
				
				// execute
				impactWUseds(ecoData, ECOState.Simulated.equals(state));

				
				// change state
				ecoData.setEcoState(state);
				
				
				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.warn("Impact Where Used [" + state.toString() + "] executed in  " + watch.getTotalTimeSeconds() + " seconds");
				}
			}

		}, ECOState.Simulated.equals(state));

	
		alfrescoRepository.save(ecoData);

	}

	@Override
	public void calculateWUsedList(NodeRef ecoNodeRef) {

		logger.debug("calculateWUsedList");

		ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		// clear WUsedList
		ecoData.getWUsedList().clear();

		if (ecoData.getReplacementList() != null) {

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

				// replacement, 1st level
				ChangeUnitDataItem changeUnitDataItem = new ChangeUnitDataItem(null, replacementListDataItem.getRevision(), null, null, Boolean.FALSE,
						replacementListDataItem.getSourceItem(), replacementListDataItem.getTargetItem());

				ecoData.getChangeUnitList().add(changeUnitDataItem);
				
				WUsedListDataItem parent = new WUsedListDataItem(null, null, null, true, null, replacementListDataItem.getSourceItem());
				
				ecoData.getWUsedList().add(parent);

				List<QName> associationQNames = wUsedListService.evaluateWUsedAssociations(replacementListDataItem.getSourceItem());
			
				for (QName associationQName : associationQNames) {

					MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(replacementListDataItem.getSourceItem(), associationQName, RepoConsts.MAX_DEPTH_LEVEL);

					QName datalistQName = wUsedListService.evaluateListFromAssociation(associationQName);
					calculateWUsedList(ecoData, replacementListDataItem.getRevision(), wUsedData, datalistQName, parent);
				}
			}
		}
		
		// change state
		ecoData.setEcoState(ECOState.Simulated);

		alfrescoRepository.save(ecoData);
		
		if(logger.isDebugEnabled()){
			logger.debug("Calculated Wused :");
			for(WUsedListDataItem item : ecoData.getWUsedList()){
				  logger.debug("- "+ item.getDepthLevel()+" "+nodeService.getProperty(item.getSourceItem(),ContentModel.PROP_NAME) );
			}
			
			
		}
	}

	private void calculateWUsedList(ChangeOrderData ecoData, RevisionType revision, MultiLevelListData wUsedData, QName dataListQName, WUsedListDataItem parent) {

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			NodeRef sourceItem = kv.getValue().getEntityNodeRef();

			ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItem);

			if (changeUnitDataItem == null) {

				changeUnitDataItem = new ChangeUnitDataItem(null, revision, null, null, Boolean.FALSE, sourceItem, null);
				ecoData.getChangeUnitList().add(changeUnitDataItem);
			} else {
				// test revision
				RevisionType dbRevision = changeUnitDataItem.getRevision();

				logger.debug("dbRevision: " + dbRevision + " - revision: " + revision);

				if (revision.equals(RevisionType.Major)) {

					if (!dbRevision.equals(RevisionType.Major)) {

						changeUnitDataItem.setRevision(RevisionType.Major);
					}
				} else if (revision.equals(RevisionType.Minor)) {

					if (dbRevision.equals(RevisionType.Minor)) {

						changeUnitDataItem.setRevision(RevisionType.Minor);
					}
				}
			}

			
			WUsedListDataItem  wUsedListDataItem = new WUsedListDataItem(null, parent, dataListQName, true, kv.getKey(), sourceItem);
			
			ecoData.getWUsedList().add(wUsedListDataItem);

			// recursive
			calculateWUsedList(ecoData, revision, kv.getValue(), dataListQName, wUsedListDataItem);
		}
	}

	/**
	 * Reset treated WUsed
	 * 
	 * @param ecoNodeRef
	 */
	private void resetTreatedWUseds(ChangeOrderData ecoData) {

		for (ChangeUnitDataItem cul : ecoData.getChangeUnitList()) {

			if (cul.getTreated()) {
				cul.setTreated(Boolean.FALSE);
				cul.setReqDetails(null);
				cul.setReqType(null);
			}
		}
	}

	/**
	 * Impact the WUsed items
	 * 
	 * @param ecoNodeRef
	 * @param isSimulation
	 */
	private void impactWUseds(ChangeOrderData ecoData, boolean isSimulation) {

		if (isSimulation) {
			// clear simulationList
			ecoData.getSimulationList().clear();
		}

		// clear changeUnitList
		resetTreatedWUseds(ecoData);

		logger.debug("impactWUseds, WUsed impacted size: " + ecoData.getWUsedList().size());

		for (int z_idx = 0; z_idx < ecoData.getWUsedList().size(); z_idx++) {

			WUsedListDataItem wul = ecoData.getWUsedList().get(z_idx);

			// impact WUsed ?
			if (wul.getIsWUsedImpacted()) {

				// is CU treated ? yes => go to next wul
				ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(wul.getSourceItem());

				if (changeUnitDataItem != null) {

					Boolean isCUTreated = changeUnitDataItem.getTreated();

					if (isCUTreated != null && isCUTreated.equals(Boolean.TRUE)) {
						continue;
					} else {

						// 1st level : nothing to do...
						if (wul.getParent() == null) {
							changeUnitDataItem.setTreated(Boolean.TRUE);
						} else {
							impactWUsed(ecoData, wul.getSourceItem(), isSimulation);
						}
					}
				}
			} else {
				// look for next sibling node, and delete unused changeUnit
				// TODO : gérer le cas où recoche l'impact d'un cas d'emploi, il
				// faut recréer le changeUnit
				// TODO use parent instead
				if (z_idx < ecoData.getWUsedList().size()) {

					int index = z_idx + 1;
					while (index < ecoData.getWUsedList().size() && ecoData.getWUsedList().get(index).getDepthLevel() > wul.getDepthLevel()) {
						index++;
						// deleteChangeUnit(ecoData,
						// ecoData.getWUsedList().get(index).getSourceItem());
					}
					logger.debug("Next sibling node of : " + z_idx + " is : " + index);
					z_idx = index;
				}
			}
		}
	}

	/**
	 * Impact the WUsed item
	 * 
	 * @param changeUnitNodeRef
	 * @return
	 */
	private void impactWUsed(ChangeOrderData ecoData, NodeRef sourceItemNodeRef, boolean isSimulation) {

		ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItemNodeRef);
		// calculate dataList to load
		List<QName> dataListQNames = new ArrayList<QName>();

		/*
		 * calculate ReplacementLink (replace leafs)
		 */
		QName className = nodeService.getType(sourceItemNodeRef);
		if (dictionaryService.isSubClass(className, BeCPGModel.TYPE_PRODUCT)) {

			Map<NodeRef, NodeRef> replacementLinks = new HashMap<NodeRef, NodeRef>();
			List<AssociationRef> assocRefs = nodeService.getSourceAssocs(sourceItemNodeRef, ECMModel.ASSOC_WUL_SOURCE_ITEM);

			for (AssociationRef assocRef : assocRefs) {

				NodeRef linkNodeRef = assocRef.getSourceRef();

				for (int z_idx = 0; z_idx < ecoData.getWUsedList().size(); z_idx++) {

					WUsedListDataItem wulDataItem = ecoData.getWUsedList().get(z_idx);

					if (linkNodeRef.equals(wulDataItem.getNodeRef())) {

						// look for previous level
						int index = z_idx;
						while (ecoData.getWUsedList().get(index).getDepthLevel() >= wulDataItem.getDepthLevel()) {
							index--;
						}

						logger.debug("current wul: " + z_idx + " previous level: " + index);

						WUsedListDataItem wulDataItem2 = ecoData.getWUsedList().get(index);
						ChangeUnitDataItem changeUnitDataItem2 = ecoData.getChangeUnitMap().get(wulDataItem2.getSourceItem());
						Boolean isWUsedImpacted = changeUnitDataItem2.getTreated();

						// 1st level or treated
						if (wulDataItem2.getDepthLevel() == 1 || (isWUsedImpacted != null && isWUsedImpacted == Boolean.TRUE)) {

							NodeRef replacementNodeRef = null;

							if (changeUnitDataItem2.getTargetItem() == null) {
								replacementNodeRef = changeUnitDataItem2.getSourceItem();
							} else {
								replacementNodeRef = changeUnitDataItem2.getTargetItem();
							}

							replacementLinks.put(wulDataItem.getLink(), replacementNodeRef);
						} else {
							// WUsed not impacted => exit (it will be treated by
							// another branch
							logger.debug("WUsed not impacted => exit (it will be treated by another branch");
							return;
						}

						// look for dataList to load
						QName qName = wulDataItem.getImpactedDataList();
						if (!dataListQNames.contains(qName)) {
							dataListQNames.add(qName);
						}
					}
				}
			}

			// NodeRef simulationNodeRef = null;
			NodeRef productToFormulateNodeRef = null;

			if (changeUnitDataItem.getTargetItem() == null) {
				productToFormulateNodeRef = changeUnitDataItem.getSourceItem();
			} else {
				productToFormulateNodeRef = changeUnitDataItem.getTargetItem();
			}

			// Create a new revision if apply else use product
			if (!isSimulation) {

				/*
				 * manage revision
				 */
				if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {

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

			if (logger.isDebugEnabled()) {
				logger.debug("do replacement for node: " + nodeService.getProperty(sourceItemNodeRef, ContentModel.PROP_NAME));
			}

			for (QName dataListQName : dataListQNames) {

				// TODO not generic
				// We can create an helper that parse annotation ie
				// Helper.getDataList(RepositoryEntity, Qname)
				if (dataListQName.equals(BeCPGModel.TYPE_COMPOLIST)) {

					for (CompoListDataItem c : productToFormulateData.getCompoList()) {

						if (replacementLinks.containsKey(c.getNodeRef())) {

							logger.debug("replace node " + c.getNodeRef() + " by node " + replacementLinks.get(c.getNodeRef()));
							c.setProduct(replacementLinks.get(c.getNodeRef()));
						}
					}
				} else if (dataListQName.equals(BeCPGModel.TYPE_PACKAGINGLIST)) {

					for (PackagingListDataItem p : productToFormulateData.getPackagingList()) {

						if (replacementLinks.containsKey(p.getNodeRef())) {

							p.setProduct(replacementLinks.get(p.getNodeRef()));
						}
					}
				}
			}

			// Before formulate we create simulation List
			createCalculatedCharactValues(ecoData, productToFormulateData);

			try {
				
				StopWatch watch = new StopWatch();
				if(logger.isDebugEnabled()){
					watch.start();
				}
				
				productToFormulateData = productService.formulate(productToFormulateData);
				

				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.warn("Formulate product "+productToFormulateData.getName()+" in "+watch.getTotalTimeSeconds()+"s");
				}
				
			} catch (FormulateException e) {

				logger.error("Failed to formulate product. NodeRef: " + productToFormulateNodeRef, e);
			}

			// isTreated and save in DB
			changeUnitDataItem.setTreated(Boolean.TRUE);

			// update simulation List

			updateCalculatedCharactValues(ecoData, productToFormulateData);

			// check req
			checkRequirements(ecoData, sourceItemNodeRef, productToFormulateData);

			alfrescoRepository.save(productToFormulateData);
		}
	}

	private void createCalculatedCharactValues(ChangeOrderData ecoData, ProductData sourceData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double sourceValue = CharactHelper.getCharactValue(charactNodeRef, charactType, sourceData);

			ecoData.getSimulationList().add(new SimulationListDataItem(null, sourceData.getNodeRef(), charactNodeRef, sourceValue, null));
		}

	}

	private void updateCalculatedCharactValues(ChangeOrderData ecoData, ProductData targetData) {

		for (NodeRef charactNodeRef : ecoData.getCalculatedCharacts()) {
			QName charactType = nodeService.getType(charactNodeRef);
			Double targetValue = CharactHelper.getCharactValue(charactNodeRef, charactType, targetData);
			for (SimulationListDataItem simulationListDataItem : ecoData.getSimulationList()) {
				if (simulationListDataItem.getCharact().equals(charactNodeRef) && simulationListDataItem.getSourceItem().equals(targetData.getNodeRef())) {
					simulationListDataItem.setTargetValue(targetValue);

					logger.debug("calculated charact: " + targetData.getNodeRef() + " - " + charactNodeRef + " - sourceValue: " + simulationListDataItem.getSourceValue()
							+ " - targetValue: " + targetValue);
				}

			}
		}

		logger.debug("simList size: " + ecoData.getSimulationList().size());
	}

	private void checkRequirements(ChangeOrderData ecoData, NodeRef sourceItemNodeRef, ProductData targetData) {

		RequirementType reqType = null;
		String reqDetails = null;

		if (targetData.getCompoListView() != null && targetData.getCompoListView().getReqCtrlList() != null) {
			for (ReqCtrlListDataItem rcl : targetData.getCompoListView().getReqCtrlList()) {

				RequirementType newReqType = rcl.getReqType();

				if (reqType == null) {
					reqType = newReqType;
				} else {

					if (RequirementType.Tolerated.equals(newReqType) && reqType.equals(RequirementType.Info)) {
						reqType = newReqType;
					} else if (RequirementType.Forbidden.equals(newReqType) && !reqType.equals(RequirementType.Forbidden)) {
						reqType = newReqType;
					}
				}

				if (reqDetails == null) {
					reqDetails = rcl.getReqMessage();
				} else {
					reqDetails += RepoConsts.LABEL_SEPARATOR;
					reqDetails += rcl.getReqMessage();
				}
			}
		}

		ChangeUnitDataItem cuDataItem = ecoData.getChangeUnitMap().get(sourceItemNodeRef);

		if (cuDataItem != null) {
			cuDataItem.setReqType(reqType);
			cuDataItem.setReqDetails(reqDetails);
		}
	}

}
