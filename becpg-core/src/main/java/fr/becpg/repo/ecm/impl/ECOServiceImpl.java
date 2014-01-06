package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
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
import fr.becpg.repo.product.data.productList.CompositionDataItem;
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
	private TransactionService transactionService;

	private ProductService productService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

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

	@Override
	public void doSimulation(NodeRef ecoNodeRef) {
		logger.debug("Run simulation");
		doRun(ecoNodeRef, ECOState.Simulated, false);
	}

	@Override
	public void apply(NodeRef ecoNodeRef, boolean requireNewTx) {
		logger.debug("Run apply");
		doRun(ecoNodeRef, ECOState.Applied,requireNewTx);
	}

	private void doRun(NodeRef ecoNodeRef, final ECOState state,final boolean requireNewTx) {

		final ChangeOrderData ecoData = (ChangeOrderData) alfrescoRepository.findOne(ecoNodeRef);

		L2CacheSupport.doInCacheContext(new Action() {

			@Override
			public void run() {
				StopWatch watch = new StopWatch();
				if (logger.isDebugEnabled()) {
					watch.start();
				}

				// Clear changeUnitList
				resetTreatedWUseds(ecoData,true);

				// Reset simulation item
				if (ECOState.Simulated.equals(state)) {
					ecoData.getSimulationList().clear();
				}

				// Visit Wused
				visitChildrens(CompositeHelper.getHierarchicalCompoList(ecoData.getWUsedList()), ecoData, ECOState.Simulated.equals(state),requireNewTx);
	
				// Change eco state
				ecoData.setEcoState(state);
				
				
				if(ECOState.Simulated.equals(state)){
					resetTreatedWUseds(ecoData,false);
				}

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
		ecoData.getChangeUnitList().clear();

		if (ecoData.getReplacementList() != null) {

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {

				if (replacementListDataItem.getSourceItems() != null && replacementListDataItem.getSourceItems().size() > 0) {

					List<NodeRef> sourceList = new ArrayList<>(replacementListDataItem.getSourceItems());

					//
					// TODO ici stocker le lien vers le replacementListDataItem
					// dans parent pour usage après
					//
					WUsedListDataItem parent = new WUsedListDataItem(null, null, null, true, null, sourceList);

					ecoData.getWUsedList().add(parent);

					// Keep only the first assocs
					List<QName> associationQNames = wUsedListService.evaluateWUsedAssociations(sourceList.get(0));

					for (QName associationQName : associationQNames) {

						MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(sourceList, associationQName, RepoConsts.MAX_DEPTH_LEVEL);

						QName datalistQName = wUsedListService.evaluateListFromAssociation(associationQName);
						calculateWUsedList(ecoData, replacementListDataItem.getRevision(), wUsedData, datalistQName, parent);
					}
				}
			}
		}

		// change state
		ecoData.setEcoState(ECOState.WUsedCalculated);

		alfrescoRepository.save(ecoData);

		if (logger.isDebugEnabled()) {
			logger.debug("Calculated Wused :");
			for (WUsedListDataItem item : ecoData.getWUsedList()) {
				String nodeNames = "";
				for (NodeRef nodeRef : item.getSourceItems()) {
					nodeNames += " " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				}

				if (item.getSourceItems().size() > 0) {
					logger.debug("- " + item.getDepthLevel() + " " + nodeNames);
				}
			}

		}
	}

	private void calculateWUsedList(ChangeOrderData ecoData, RevisionType revision, MultiLevelListData wUsedData, QName dataListQName, WUsedListDataItem parent) {

		for (Map.Entry<NodeRef, MultiLevelListData> kv : wUsedData.getTree().entrySet()) {

			List<NodeRef> sourceItems = kv.getValue().getEntityNodeRefs();

			ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(sourceItems.get(0));

			if (changeUnitDataItem == null) {

				changeUnitDataItem = new ChangeUnitDataItem(revision, null, null, Boolean.FALSE, sourceItems.get(0), null);
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
			// TODO
			// Ici les liens doivent être multiple cas 2 vers 1
			// Les liens doivent être mis à jour ou supprimer lors de la
			// création d'une version
			// sinon impossible de sauvegarder l'ECM

			WUsedListDataItem wUsedListDataItem = new WUsedListDataItem(null, parent, dataListQName, true, /*
																											 * kv
																											 * .
																											 * getKey
																											 * (
																											 * )
																											 */null, sourceItems);

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
	private void resetTreatedWUseds(ChangeOrderData ecoData, boolean full) {

		for (ChangeUnitDataItem cul : ecoData.getChangeUnitList()) {

			if (cul.getTreated()) {
				cul.setTreated(Boolean.FALSE);
				if(full){
					cul.setReqDetails(null);
					cul.setReqType(null);
				}
			}
		}
	}

	private void visitChildrens(Composite<WUsedListDataItem> composite, final ChangeOrderData ecoData,final boolean isSimulation, boolean requireNewTx) {
		for (final Composite<WUsedListDataItem> component : composite.getChildren()) {

			// Not First level
			if (component.getData() != null && component.getData().getDepthLevel() > 1) {

				final ChangeUnitDataItem changeUnitDataItem = ecoData.getChangeUnitMap().get(component.getData().getSourceItems().get(0));

				if (logger.isDebugEnabled()) {
					logger.debug("Get ChangeUnit for " + nodeService.getProperty(component.getData().getSourceItems().get(0), ContentModel.PROP_NAME));
					if (changeUnitDataItem != null) {
						logger.debug("ChangeUnit :" + changeUnitDataItem.toString());
					} else {
						logger.error("ChangeUnit is null");
					}
				}

				// We break if product treated
				if (changeUnitDataItem != null && !changeUnitDataItem.getTreated()) {
					
					// We test if all referring nodes are treated before
					// apply
					// to branch
					if (component.getData().getDepthLevel() > 2 && shouldSkipCurrentBranch(ecoData, changeUnitDataItem)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Skip current branch at " + nodeService.getProperty(changeUnitDataItem.getSourceItem(), ContentModel.PROP_NAME));
						}
						break;
					}

					transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {

						@Override
						public Void execute() throws Throwable {

						
							NodeRef productNodeRef = getProductToImpact(ecoData, changeUnitDataItem, isSimulation);

							if (productNodeRef != null) {

								ProductData productToFormulateData = (ProductData) alfrescoRepository.findOne(productNodeRef);

								// Before formulate we create simulation List
								createCalculatedCharactValues(ecoData, productToFormulateData);

								// Level 2
								if (component.getData().getDepthLevel() == 2) {
									applyReplacementList(ecoData, productToFormulateData);
								} else {
									// Level 3 OR more
									applyImpactedProductsData(ecoData, changeUnitDataItem, productToFormulateData);
								}

								formulate(productToFormulateData);

								// update simulation List
								updateCalculatedCharactValues(ecoData, productToFormulateData);

								// check req
								checkRequirements(changeUnitDataItem, productToFormulateData);

								alfrescoRepository.save(productToFormulateData);

							} else {
								logger.warn("Product to impact is empty");
							}
							
							changeUnitDataItem.setTreated(Boolean.TRUE);
							// isTreated and save in DB
							if(!isSimulation){
								alfrescoRepository.save(changeUnitDataItem);
							}
							

							return null;
						}

					}, isSimulation, requireNewTx);

				}

			}

			if (!component.isLeaf()) {
				visitChildrens((Composite<WUsedListDataItem>) component, ecoData, isSimulation, requireNewTx);
			}

		}

	}

	private boolean shouldSkipCurrentBranch(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem) {

		// TODO On doit également stopper la propagation si
		// !wulDataItem.getIsWUsedImpacted()

		boolean skip = false;
		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if (wulDataItem.getParent() != null && wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem())) {
				if (ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)) == null
						|| !ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0)).getTreated()) {
					skip = true;
					break;
				}
			}

		}
		return skip;
	}

	private void applyImpactedProductsData(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem, ProductData product) {

		// TODO On doit également stopper la propagation si
		// !wulDataItem.getIsWUsedImpacted()

		for (WUsedListDataItem wulDataItem : ecoData.getWUsedList()) {
			if (wulDataItem.getParent() != null && wulDataItem.getSourceItems().contains(changeUnitDataItem.getSourceItem())) {
				ChangeUnitDataItem toApply = ecoData.getChangeUnitMap().get(wulDataItem.getParent().getSourceItems().get(0));
				if (toApply.getTargetItem() != null) {
					applyToList(product.getCompoList(), toApply.getSourceItem(), toApply.getTargetItem());
					applyToList(product.getPackagingList(), toApply.getSourceItem(), toApply.getTargetItem());

				}

			}

		}

	}

	private <T extends CompositionDataItem> void applyToList(List<T> items, NodeRef sourceNodeRef, NodeRef targetNodeRef) {
		for (T item : items) {
			if (item.getProduct() != null && item.getProduct().equals(sourceNodeRef)) {
				item.setProduct(targetNodeRef);
				break;
			}
		}

	}

	private void applyReplacementList(ChangeOrderData ecoData, ProductData product) {

		if (ecoData.getReplacementList() != null) {

			applyToList(ecoData, product.getCompoList());
			applyToList(ecoData, product.getPackagingList());

		}

	}

	@Deprecated
	// On doit passer par wused pour faire ça en utilisants les liens multiples
	// Et le premier lien vers la replacementList
	//
	private <T extends CompositionDataItem> void applyToList(ChangeOrderData ecoData, List<T> items) {
		Map<NodeRef, CompositionDataItem> temp = new HashMap<>();
		Set<NodeRef> toDelete = new HashSet<>();

		for (Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
			T compoListDataItem = (T) iterator.next();

			for (ReplacementListDataItem replacementListDataItem : ecoData.getReplacementList()) {
				if (replacementListDataItem.getSourceItems() != null && !replacementListDataItem.getSourceItems().isEmpty()) {

					// TODO Can be retrieve by link from wusedList

					// Look if compo match with
					if (replacementListDataItem.getSourceItems().get(0).equals(compoListDataItem.getProduct())) {

						boolean apply = true;
						if (replacementListDataItem.getSourceItems().size() > 1) {

							for (int i = 1; i < replacementListDataItem.getSourceItems().size(); i++) {
								apply = false;
								for (Iterator<T> iterator2 = items.iterator(); iterator2.hasNext();) {
									CompositionDataItem compoListDataItem2 = (CompoListDataItem) iterator2.next();
									if (replacementListDataItem.getSourceItems().get(i).equals(compoListDataItem2.getProduct())) {
										apply = true;
										// DELETE
										toDelete.add(compoListDataItem2.getNodeRef());
									}
								}

								if (!apply) {
									break;
								}
							}

						}

						if (apply) {
							// ADD
							if (temp.containsKey(compoListDataItem.getProduct())) {
								T newCompoListDataItem = (T) compoListDataItem.createCopy();
								newCompoListDataItem.setNodeRef(null);
								newCompoListDataItem.setProduct(replacementListDataItem.getTargetItem());
								if (compoListDataItem.getQty() != null && replacementListDataItem.getQtyPerc() != null) {
									newCompoListDataItem.setQty(replacementListDataItem.getQtyPerc() / 100 * compoListDataItem.getQty());
								}
								items.add(newCompoListDataItem);
							} // UPDATE
							else {
								if (replacementListDataItem.getTargetItem() == null) {
									iterator.remove();
								} else {
									compoListDataItem.setProduct(replacementListDataItem.getTargetItem());
									if (compoListDataItem.getQty() != null && replacementListDataItem.getQtyPerc() != null) {
										compoListDataItem.setQty(replacementListDataItem.getQtyPerc() / 100 * compoListDataItem.getQty());
									}
									temp.put(compoListDataItem.getProduct(), compoListDataItem);
								}
							}
						}
					}

				}

			}
		}

		items.removeAll(toDelete);

	}

	private NodeRef getProductToImpact(ChangeOrderData ecoData, ChangeUnitDataItem changeUnitDataItem, boolean isSimulation) {

		NodeRef productToImpact = changeUnitDataItem.getSourceItem();

		if (productToImpact != null) {

			// Create a new revision if apply else use product
			if (!isSimulation) {

				/*
				 * manage revision
				 */
				if (!changeUnitDataItem.getRevision().equals(RevisionType.NoRevision)) {

					if(changeUnitDataItem.getTargetItem()!=null){
						return changeUnitDataItem.getTargetItem();
					}
					
					VersionType versionType = changeUnitDataItem.getRevision().equals(RevisionType.Major) ? VersionType.MAJOR : VersionType.MINOR;

					// checkout
					NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(productToImpact);

					// checkin
					Map<String, Serializable> properties = new HashMap<String, Serializable>();
					properties.put(VersionModel.PROP_VERSION_TYPE, versionType);
					properties.put(Version.PROP_DESCRIPTION, String.format(VERSION_DESCRIPTION, ecoData.getCode()));

					NodeRef targetNodeRef = checkOutCheckInService.checkin(workingCopyNodeRef, properties);

					changeUnitDataItem.setTargetItem(targetNodeRef);

					return targetNodeRef;
				}
			}
			// TODO mettre à jour les wUsedLink
		}

		return productToImpact;
	}

	private void formulate(ProductData productToFormulateData) {

		StopWatch watch = new StopWatch();
		if (logger.isDebugEnabled()) {
			watch.start();
		}
		try {

			productService.formulate(productToFormulateData);

		} catch (FormulateException e) {
			logger.error("Failed to formulate product. NodeRef: " + productToFormulateData.getNodeRef(), e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.warn("Formulate product " + productToFormulateData.getName() + " in " + watch.getTotalTimeSeconds() + "s");
			}
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
					if (logger.isDebugEnabled()) {
						logger.debug("calculated charact: " + targetData.getNodeRef() + " - " + charactNodeRef + " - sourceValue: " + simulationListDataItem.getSourceValue()
								+ " - targetValue: " + targetValue);
					}
				}

			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("simList size: " + ecoData.getSimulationList().size());
		}
	}

	private void checkRequirements(ChangeUnitDataItem changeUnitDataItem, ProductData targetData) {

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

		changeUnitDataItem.setReqType(reqType);
		changeUnitDataItem.setReqDetails(reqDetails);
	}

}
