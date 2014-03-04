/*
 * 
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class IngsCalculatingVisitor.
 * 
 * @author querephi
 */
public class IngsCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The Constant NO_GRP. */
	public static final String NO_GRP = "-";
	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";

	private NodeRef tmpDiluentNodeRef = new NodeRef("ings","tempDiluent","0");

	/** The logger. */
	private static Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

	private NodeService nodeService;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Calculate ingredient list");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		if (formulatedProduct.getIngList() != null) {
			for (IngListDataItem il : formulatedProduct.getIngList()) {
				if (il.getIsManual() == null || !il.getIsManual()) {
					// reset
					il.setQtyPerc(null);
					il.setIsGMO(false);
					il.setIsIonized(false);
					il.getGeoOrigin().clear();
					il.getBioOrigin().clear();
				}
			}
		}

		// Load product specification

		// IngList
		calculateIL(formulatedProduct);

		return true;
	}

	/**
	 * Calculate the ingredient list of a product.
	 * 
	 * @param productData
	 *            the product data
	 * @return the list
	 * @throws FormulateException
	 */
	private void calculateIL(ProductData formulatedProduct) throws FormulateException {

		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT);

		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<NodeRef, ReqCtrlListDataItem>();
		Map<NodeRef, Double> totalQtyIngMap = new HashMap<NodeRef, Double>();
		Map<NodeRef, Double> totalQtyVolMap = new HashMap<NodeRef, Double>();
		NodeRef diluantIngNodeRef = null;

		List<IngListDataItem> retainNodes = new ArrayList<IngListDataItem>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if (i.getIsManual() != null && i.getIsManual()) {
				retainNodes.add(i);
			}
		}

		Double totalQtyUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<IngListDataItem> componentIngList = (List) alfrescoRepository.loadDataList(compoItem.getProduct(), PLMModel.TYPE_INGLIST,
						PLMModel.TYPE_INGLIST);

				visitILOfPart(formulatedProduct, compoItem, componentIngList, retainNodes,
						totalQtyIngMap, totalQtyVolMap, reqCtrlMap);

				QName type = nodeService.getType(compoItem.getProduct());
				if (type != null
						&& (type.isMatch(PLMModel.TYPE_RAWMATERIAL) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || type
								.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) && compoItem.getDeclType() != DeclarationType.Omit) {
					Double qty = FormulationHelper.getQtyInKg(compoItem);
					if (qty != null) {
						totalQtyUsed += qty * FormulationHelper.getYield(compoItem) / 100;
					}
				}

				if (nodeService.hasAspect(compoItem.getProduct(), PLMModel.ASPECT_DILUENT) && !componentIngList.isEmpty()) {
					diluantIngNodeRef = componentIngList.get(0).getIng();
				}

			}
		}

		formulatedProduct.getIngList().retainAll(retainNodes);

		if (totalQtyUsed != 0) {
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
				// qtyPerc
				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getIng());
				
				if(diluantIngNodeRef!=null && diluantIngNodeRef.equals(ingListDataItem.getIng()) && totalQtyVolMap.get(tmpDiluentNodeRef)!=null){
					totalQtyIng +=totalQtyVolMap.get(tmpDiluentNodeRef);
				}
				
				ingListDataItem.setQtyPerc(totalQtyIng / totalQtyUsed);

				// qtyVolumePerc
				ingListDataItem.setVolumeQtyPerc(totalQtyVolMap.get(ingListDataItem.getIng()));

				// add detailable aspect
				if (!ingListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}

		}

		// check formulated product
		checkILOfFormulatedProduct(formulatedProduct.getIngList(), formulatedProduct.getProductSpecifications(), reqCtrlMap);

		// sort collection
		sortIL(formulatedProduct.getIngList());

		formulatedProduct.getCompoListView().getReqCtrlList().addAll(reqCtrlMap.values());
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 * @param totalQtyVolMap
	 * @throws FormulateException
	 */
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem,
			 List<IngListDataItem> componentIngList, List<IngListDataItem> retainNodes,
			Map<NodeRef, Double> totalQtyIngMap, Map<NodeRef, Double> totalQtyVolMap, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap)
			throws FormulateException {

		// check product respect specification
		checkILOfPart(compoListDataItem.getProduct(), componentIngList,  formulatedProduct.getProductSpecifications(), reqCtrlMap);

		if (componentIngList == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("CompoItem: " + compoListDataItem.getProduct() + " - doesn't have ing ");
			}

			return;
		}

		// calculate ingList of formulated product
		List<IngListDataItem> retainedComponentIngList = new ArrayList<>();
		calculateIngListOfPart(CompositeHelper.getHierarchicalCompoList(componentIngList), 1d, retainedComponentIngList);
		calculateILOfPart(formulatedProduct, compoListDataItem, retainedComponentIngList, formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, totalQtyVolMap);
	}

	// Keep ingList that don't have children
	private void calculateIngListOfPart(Composite<IngListDataItem> compositeIngList, Double parentQty, List<IngListDataItem> retainedComponentIngList) {

		for (Composite<IngListDataItem> component : compositeIngList.getChildren()) {
			if (component.isLeaf()) {
				retainedComponentIngList.add(component.getData());
			} else {
				Double qty = null;
				if (parentQty != null && component.getData().getQtyPerc() != null) {
					qty = parentQty * component.getData().getQtyPerc() / 100;
				}
				calculateIngListOfPart(component, qty, retainedComponentIngList);
			}
		}
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 * @param totalQtyVolMap
	 * @throws FormulateException
	 */
	private void calculateILOfPart(ProductData formulatedProduct,CompoListDataItem compoListDataItem, List<IngListDataItem> componentIngList, List<IngListDataItem> ingList,
			List<IngListDataItem> retainNodes, Map<NodeRef, Double> totalQtyIngMap, Map<NodeRef, Double> totalQtyVolMap) throws FormulateException {

		// OMIT is not taken in account
		if (compoListDataItem.getDeclType() == DeclarationType.Omit) {
			return;
		}

		for (IngListDataItem ingListDataItem : componentIngList) {

			// Look for ing
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = findIngListDataItem(ingList, ingNodeRef);

			if (newIngListDataItem == null) {

				newIngListDataItem = new IngListDataItem();
				newIngListDataItem.setIng(ingNodeRef);
				ingList.add(newIngListDataItem);
			}

			if (!retainNodes.contains(newIngListDataItem)) {
				retainNodes.add(newIngListDataItem);
			}

			Double totalQtyIng = totalQtyIngMap.get(ingNodeRef);
			if (totalQtyIng == null) {
				totalQtyIng = 0d;
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
			}

			// Calculate qty
			Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();

			if (qty != null && qtyIng != null) {
				qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				Double valueToAdd = qty * qtyIng;
				
				Double volumePerc = totalQtyVolMap.get(ingNodeRef);
				if (volumePerc == null) {
					volumePerc = 0d;
				}

				if (nodeService.hasAspect(compoListDataItem.getProduct(), PLMModel.ASPECT_RECONSTITUTABLE)) {
					Double reconstitionRate = (Double) nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_RECONSTITUTION_RATE);
					if (reconstitionRate != null) {

						// Raw material 
						totalQtyVolMap.put(ingNodeRef, volumePerc + compoListDataItem.getVolume() * reconstitionRate);

						Double qtyDiluent = totalQtyVolMap.get(tmpDiluentNodeRef);
						if (qtyDiluent == null) {
							qtyDiluent = 0d;
						}
						
						qtyDiluent = qtyDiluent - (
								(compoListDataItem.getVolume() * reconstitionRate) - compoListDataItem.getVolume())*FormulationHelper.getQtyInKg(compoListDataItem);
						
						// Decrease diluent
						totalQtyVolMap.put(tmpDiluentNodeRef, qtyDiluent);
						
						valueToAdd = valueToAdd + ((compoListDataItem.getVolume() * reconstitionRate) - compoListDataItem.getVolume())*FormulationHelper.getQtyInKg(compoListDataItem);
					}
				} else {
					// Semi finished
					if (ingListDataItem.getVolumeQtyPerc() != null) {
	
						Double netVolume = FormulationHelper.getNetVolume(formulatedProduct.getNodeRef(), nodeService);
						Double calculatedVolume = formulatedProduct.getYieldVolume()/(100*netVolume);
						
						
						totalQtyVolMap.put(ingNodeRef, volumePerc + ingListDataItem.getVolumeQtyPerc() * compoListDataItem.getVolume()/ calculatedVolume );
					}
				}

				totalQtyIng += valueToAdd;
				totalQtyIngMap.put(ingNodeRef, totalQtyIng);
			}

			// Calculate geo origins
			for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
				if (!newIngListDataItem.getGeoOrigin().contains(geoOrigin)) {
					newIngListDataItem.getGeoOrigin().add(geoOrigin);
				}
			}

			// Calculate bio origins
			for (NodeRef bioOrigin : ingListDataItem.getBioOrigin()) {
				if (!newIngListDataItem.getBioOrigin().contains(bioOrigin)) {
					newIngListDataItem.getBioOrigin().add(bioOrigin);
				}
			}

			// GMO
			if (ingListDataItem.getIsGMO() && !newIngListDataItem.getIsGMO()) {
				newIngListDataItem.setIsGMO(true);
			}

			// Ionized
			if (ingListDataItem.getIsIonized() && !newIngListDataItem.getIsIonized()) {
				newIngListDataItem.setIsIonized(true);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("productData: " + compoListDataItem.getProduct() + " - ing: "
						+ nodeService.getProperty(ingNodeRef, ContentModel.PROP_NAME) + " qtyPerc: " + totalQtyIng);
			}
		}
	}

	/**
	 * check the ingredients of the part according to the specification
	 * 
	 * @param compoListDataItem
	 *            the compo list data item
	 * @param ingMap
	 *            the ing map
	 * @param totalQtyIngMap
	 *            the total qty ing map
	 */
	private void checkILOfPart(NodeRef productNodeRef, List<IngListDataItem> ingList, List<ProductSpecificationData> productSpecicationDataList,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(productNodeRef))) {

			// datalist ingList is null or empty
			if ((!alfrescoRepository.hasDataList(productNodeRef, PLMModel.TYPE_INGLIST) || ingList.isEmpty())) {

				// req not respected
				String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);

				ReqCtrlListDataItem reqCtrl = null;
				for (ReqCtrlListDataItem r : reqCtrlMap.values()) {
					if (message.equals(r.getReqMessage())) {
						reqCtrl = r;
						break;
					}
				}

				if (reqCtrl == null) {
					reqCtrl = new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>());
					reqCtrlMap.put(null, reqCtrl);
				}

				if (!reqCtrl.getSources().contains(productNodeRef)) {
					reqCtrl.getSources().add(productNodeRef);
				}
			} else {
				for (ProductSpecificationData productSpecificationData : productSpecicationDataList) {

					for (IngListDataItem ingListDataItem : ingList) {
						if (logger.isDebugEnabled()) {
							logger.debug("For " + productNodeRef + " testing ing :"
									+ nodeService.getProperty(ingListDataItem.getCharactNodeRef(), ContentModel.PROP_NAME));
						}
						for (ForbiddenIngListDataItem fil : productSpecificationData.getForbiddenIngList()) {

							// GMO
							if (fil.getIsGMO() != null && !fil.getIsGMO().isEmpty() && !fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())) {
								continue; // check next rule
							}

							// Ionized
							if (fil.getIsIonized() != null && !fil.getIsIonized().isEmpty()
									&& !fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())) {
								continue; // check next rule
							}

							// Ings
							if (!fil.getIngs().isEmpty()) {
								if (!fil.getIngs().contains(ingListDataItem.getIng())) {
									continue; // check next rule
								} else if (fil.getQtyPercMaxi() != null) {
									continue; // check next rule (we will check
												// in
												// checkILOfFormulatedProduct)
								}
							}

							// GeoOrigins
							if (!fil.getGeoOrigins().isEmpty()) {
								boolean hasGeoOrigin = false;
								for (NodeRef n : ingListDataItem.getGeoOrigin()) {
									if (fil.getGeoOrigins().contains(n)) {
										hasGeoOrigin = true;
									}
								}

								if (!hasGeoOrigin) {
									continue; // check next rule
								}
							}

							// BioOrigins
							if (!fil.getBioOrigins().isEmpty()) {
								boolean hasBioOrigin = false;
								for (NodeRef n : ingListDataItem.getBioOrigin()) {
									if (fil.getBioOrigins().contains(n)) {
										hasBioOrigin = true;
									}
								}

								if (!hasBioOrigin) {
									continue; // check next rule
								}
							}

							logger.debug("Adding not respected for :" + fil.getReqMessage());
							// req not respected
							ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
							if (reqCtrl == null) {
								reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
								reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
							}

							if (!reqCtrl.getSources().contains(productNodeRef)) {
								reqCtrl.getSources().add(productNodeRef);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * check the ingredients of the part according to the specification
	 * 
	 */
	private void checkILOfFormulatedProduct(Collection<IngListDataItem> ingList, List<ProductSpecificationData> productSpecicationDataList,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		for (ProductSpecificationData productSpecification : productSpecicationDataList) {

			for (IngListDataItem ingListDataItem : ingList) {

				for (ForbiddenIngListDataItem fil : productSpecification.getForbiddenIngList()) {

					// Ings
					if (!fil.getIngs().isEmpty()) {
						if (fil.getIngs().contains(ingListDataItem.getIng())) {
							if (fil.getQtyPercMaxi() != null && fil.getQtyPercMaxi() <= ingListDataItem.getQtyPerc()) {

								// req not respected
								ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
								if (reqCtrl == null) {
									reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), new ArrayList<NodeRef>());
									reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
								}
							}
						}
					}
				}
			}
		}
	}

	private IngListDataItem findIngListDataItem(List<IngListDataItem> ingList, NodeRef ingNodeRef) {
		if (ingNodeRef != null) {
			for (IngListDataItem i : ingList) {
				if (ingNodeRef.equals(i.getIng())) {
					return i;
				}
			}
		}
		return null;
	}

	/**
	 * Sort ingList by qty perc in descending order.
	 * 
	 * @param costList
	 *            the cost list
	 * @return the list
	 */
	private void sortIL(List<IngListDataItem> ingList) {

		Collections.sort(ingList, new Comparator<IngListDataItem>() {

			@Override
			public int compare(IngListDataItem i1, IngListDataItem i2) {

				// increase
				return i2.getQtyPerc().compareTo(i1.getQtyPerc());
			}

		});

		int i = 1;
		for (IngListDataItem il : ingList) {
			il.setSort(i);
			i++;
		}
	}
}
