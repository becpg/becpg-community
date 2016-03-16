/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
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
	private static final String MESSAGE_NOTAUTHORIZED_ING = "message.formulate.notauhorized.ing";

	/** The logger. */
	private static final Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

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
		logger.debug("\t==== Calculate ingredient list of " + formulatedProduct.getName());

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		// no compo, nor ingList on formulated product => no formulation
		if (!formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
				|| (!alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST)
						&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST))) {
			// logger.debug("no compo => no formulation");
			return true;
		}

		if (formulatedProduct.getIngList() != null) {
			for (IngListDataItem il : formulatedProduct.getIngList()) {
				if ((il.getIsManual() == null) || !il.getIsManual()) {
					// reset
					il.setQtyPerc(null);
					il.setMini(null);
					il.setMaxi(null);
					il.setIsGMO(false);
					il.setIsProcessingAid(true);
					il.setIsSupport(true);
					il.setIsIonized(false);
					il.getGeoOrigin().clear();
					il.getGeoTransfo().clear();
					il.getBioOrigin().clear();
				}
			}
		} else {
			formulatedProduct.setIngList(new LinkedList<IngListDataItem>());
		}

		if (formulatedProduct.getProductSpecifications() == null) {
			formulatedProduct.setProductSpecifications(new LinkedList<ProductSpecificationData>());
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

		List<CompoListDataItem> compoList = formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<>();
		Map<String, Double> totalQtyIngMap = new HashMap<>();
		Map<String, Double> totalQtyVolMap = new HashMap<>();

		List<IngListDataItem> retainNodes = new ArrayList<>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if ((i.getIsManual() != null) && i.getIsManual()) {
				retainNodes.add(i);
			}
		}

		Double totalQtyUsed = 0d, totalVolumeUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
					ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
					visitILOfPart(formulatedProduct, compoItem, componentProductData, retainNodes, totalQtyIngMap, totalQtyVolMap, reqCtrlMap);

					QName type = nodeService.getType(compoItem.getProduct());
					if ((type != null) && (type.isMatch(PLMModel.TYPE_RAWMATERIAL) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| type.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) && (compoItem.getDeclType() != DeclarationType.Omit)) {
						Double qty = FormulationHelper.getQtyInKg(compoItem);
						if (qty != null) {
							totalQtyUsed += (qty * FormulationHelper.getYield(compoItem)) / 100;
						}

						Double vol = compoItem.getVolume();
						if (vol != null) {
							totalVolumeUsed += vol / 100;
						}
					}
				}

			}
		}

		formulatedProduct.getIngList().retainAll(retainNodes);

		if (totalQtyUsed != 0) {
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
				// qtyPerc
				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getName());

				if (totalQtyIng != null) {
					if (ingListDataItem.getParent() != null) {
						Double parentTotalQtyIng = totalQtyIngMap.get(ingListDataItem.getParent().getName());
						if (parentTotalQtyIng != null) {
							ingListDataItem.setQtyPerc((totalQtyIng / parentTotalQtyIng) * 100);
						} else {
							ingListDataItem.setQtyPerc(null);
						}
					} else {
						ingListDataItem.setQtyPerc(totalQtyIng / totalQtyUsed);
					}
				} else {
					ingListDataItem.setQtyPerc(null);
				}

				// qtyVolumePerc
				if (totalQtyVolMap.get(ingListDataItem.getName()) != null) {
					ingListDataItem.setVolumeQtyPerc(totalQtyVolMap.get(ingListDataItem.getName()) / totalVolumeUsed);
				}

				// add detailable aspect
				if (!ingListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}

		}

		// check formulated product
		checkILOfFormulatedProduct(formulatedProduct.getIngList(), getMergedForbiddenIngList(formulatedProduct), reqCtrlMap);

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
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem, ProductData componentProductData,
			List<IngListDataItem> retainNodes, Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException {

		// check product respect specification
		checkILOfPart(compoListDataItem.getProduct(), compoListDataItem.getDeclType(), componentProductData,
				getMergedForbiddenIngList(formulatedProduct), reqCtrlMap);

		if (componentProductData.getIngList() == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("CompoItem: " + compoListDataItem.getProduct() + " - doesn't have ing ");
			}

			return;
		}

		// calculate ingList of formulated product
		calculateILOfPart(compoListDataItem, CompositeHelper.getHierarchicalCompoList(componentProductData.getIngList()),
				formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, totalQtyVolMap, null);
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
	private void calculateILOfPart(CompoListDataItem compoListDataItem, Composite<IngListDataItem> compositeIngList, List<IngListDataItem> ingList,
			List<IngListDataItem> retainNodes, Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap,
			IngListDataItem parentIngListDataItem) throws FormulateException {

		// OMIT is not taken in account
		if (compoListDataItem.getDeclType() == DeclarationType.Omit) {
			return;
		}

		for (Composite<IngListDataItem> component : compositeIngList.getChildren()) {

			// Look for ing
			IngListDataItem ingListDataItem = component.getData();
			NodeRef ingNodeRef = ingListDataItem.getIng();
			IngListDataItem newIngListDataItem = findIngListDataItem(ingList, ingListDataItem);

			if (newIngListDataItem == null) {

				newIngListDataItem = new IngListDataItem();
				newIngListDataItem.setName(GUID.generate());
				newIngListDataItem.setIng(ingNodeRef);
				newIngListDataItem.setParent(parentIngListDataItem);
				newIngListDataItem.setDepthLevel(parentIngListDataItem == null ? 1 : parentIngListDataItem.getDepthLevel() + 1);
				newIngListDataItem.setIsProcessingAid(true);
				newIngListDataItem.setIsSupport(true);
				ingList.add(newIngListDataItem);
			}

			if (!retainNodes.contains(newIngListDataItem)) {
				retainNodes.add(newIngListDataItem);
			}

			Double totalQtyIng = totalQtyIngMap.get(newIngListDataItem.getName());
			if (totalQtyIng == null) {
				totalQtyIng = 0d;
				totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
			}

			// Calculate qty
			Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();

			if ((qty != null) && (qtyIng != null)) {
				qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				Double valueToAdd = qty * qtyIng;

				Double volumeQty = totalQtyVolMap.get(newIngListDataItem.getName());
				if (volumeQty == null) {
					volumeQty = 0d;
				}

				// Semi finished
				if ((ingListDataItem.getVolumeQtyPerc() != null) && (compoListDataItem.getVolume() != null)) {

					totalQtyVolMap.put(newIngListDataItem.getName(),
							volumeQty + ((ingListDataItem.getVolumeQtyPerc() * compoListDataItem.getVolume()) / 100));
				}

				totalQtyIng += valueToAdd;
				totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
			}

			// Calculate geo origins
			for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
				if (!newIngListDataItem.getGeoOrigin().contains(geoOrigin)) {
					newIngListDataItem.getGeoOrigin().add(geoOrigin);
				}
			}

			// Calculate geo transfo
			for (NodeRef geoTransfo : ingListDataItem.getGeoTransfo()) {
				if (!newIngListDataItem.getGeoTransfo().contains(geoTransfo)) {
					newIngListDataItem.getGeoTransfo().add(geoTransfo);
				}
			}

			// Calculate bio origins
			for (NodeRef bioOrigin : ingListDataItem.getBioOrigin()) {
				if (!newIngListDataItem.getBioOrigin().contains(bioOrigin)) {
					newIngListDataItem.getBioOrigin().add(bioOrigin);
				}
			}

			// Processing Aid
			if ((ingListDataItem.getIsProcessingAid() == null) || !ingListDataItem.getIsProcessingAid()) {
				newIngListDataItem.setIsProcessingAid(false);
			}

			// Support
			if ((ingListDataItem.getIsSupport() == null) || !ingListDataItem.getIsSupport()) {
				newIngListDataItem.setIsSupport(false);
			}

			// GMO
			if (ingListDataItem.getIsGMO() && !newIngListDataItem.getIsGMO()) {
				newIngListDataItem.setIsGMO(true);
			}

			// Ionized
			if (ingListDataItem.getIsIonized() && !newIngListDataItem.getIsIonized()) {
				newIngListDataItem.setIsIonized(true);
			}

			// recursive
			if (!component.isLeaf()) {
				calculateILOfPart(compoListDataItem, component, ingList, retainNodes, totalQtyIngMap, totalQtyVolMap, newIngListDataItem);
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
	private void checkILOfPart(NodeRef productNodeRef, DeclarationType declType, ProductData componentProductData,
			List<ForbiddenIngListDataItem> forbiddenIngredientsList, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(productNodeRef))) {

			// datalist ingList is null or empty
			if ((componentProductData.getIngList() == null) || componentProductData.getIngList().isEmpty()) {

				if ((declType == null) || !declType.equals(DeclarationType.DoNotDetails)) {
					// req not respected
					String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);
					addReqCtrl(reqCtrlMap, RequirementType.Tolerated, message, productNodeRef);
				}
			} else {

				forbiddenIngredientsList.forEach(fil -> {
					componentProductData.getIngList().forEach(ingListDataItem->  {

						if (!RequirementType.Authorized.equals(fil.getReqType())) {

							if (checkRuleMatchIng(ingListDataItem, fil)) {
								// Look for raw material
								if ((componentProductData.getCompoListView().getCompoList() != null)
										&& !componentProductData.getCompoListView().getCompoList().isEmpty()) {
									for (CompoListDataItem c : componentProductData.getCompoListView().getCompoList()) {
										checkILOfPart(c.getProduct(), declType, (ProductData) alfrescoRepository.findOne(c.getProduct()),
												forbiddenIngredientsList, reqCtrlMap);
									}
								} else {
									logger.debug("Adding not respected for :" + fil.getReqMessage());
									ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
									if (reqCtrl == null) {
										reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), null, new ArrayList<NodeRef>(),
												RequirementDataType.Specification);
										reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
									} else {
										reqCtrl.setReqDataType(RequirementDataType.Specification);
									}

									if (!reqCtrl.getSources().contains(productNodeRef)) {
										reqCtrl.getSources().add(productNodeRef);
									}
								}
							}
						}

					});
				});

			}
		}
	}

	public List<ForbiddenIngListDataItem> getMergedForbiddenIngList(ProductData product) {

		if ((product.getProductSpecifications() == null) || product.getProductSpecifications().isEmpty()) {

			if (product instanceof ProductSpecificationData) {
				return ((ProductSpecificationData) product).getForbiddenIngList();
			} else {
				return new ArrayList<ForbiddenIngListDataItem>();
			}
		} else {

			List<ForbiddenIngListDataItem> unmergedForbiddenIngList = new ArrayList<>();

			for (ProductSpecificationData specification : product.getProductSpecifications()) {
				unmergedForbiddenIngList.addAll(getMergedForbiddenIngList(specification));
			}

			return mergeForbiddenIngList(unmergedForbiddenIngList);
		}
	}

	public List<ForbiddenIngListDataItem> mergeForbiddenIngList(List<ForbiddenIngListDataItem> unmergedList) {
		List<ForbiddenIngListDataItem> mergedList = new ArrayList<>();
		Map<String, ForbiddenIngListDataItem> mergingForbiddenIngMap = new HashMap<>();
		// merge ing, bio/geo orig props from common fbd ings
		for (ForbiddenIngListDataItem forbiddenIng : unmergedList) {
			if (forbiddenIng.getReqMessage() != null) {

				if (mergingForbiddenIngMap.containsKey(forbiddenIng.getReqMessage())
						&& (mergingForbiddenIngMap.get(forbiddenIng.getReqMessage()) != null)) {
					// merge values

					ForbiddenIngListDataItem mappedForbiddenIng = mergingForbiddenIngMap.get(forbiddenIng.getReqMessage());
					
					mappedForbiddenIng.getIngs().addAll(forbiddenIng.getIngs());
					mappedForbiddenIng.getBioOrigins().addAll(forbiddenIng.getBioOrigins());
					mappedForbiddenIng.getGeoOrigins().addAll(forbiddenIng.getGeoOrigins());
					mappedForbiddenIng.getRequiredGeoOrigins().addAll(forbiddenIng.getRequiredGeoOrigins());
					mappedForbiddenIng.getGeoTransfo().addAll(forbiddenIng.getGeoTransfo());

					// returns false if string is null or is not valid
					// if one of these is ionized or gmo, they all are
					if (BooleanUtils.toBoolean(forbiddenIng.getIsGMO())) {
						if (!BooleanUtils.toBoolean(mappedForbiddenIng.getIsGMO())) {
							mappedForbiddenIng.setIsGMO(forbiddenIng.getIsGMO());
						}
					}

					if (BooleanUtils.toBoolean(forbiddenIng.getIsIonized())) {
						if (!BooleanUtils.toBoolean(mappedForbiddenIng.getIsIonized())) {
							mappedForbiddenIng.setIsIonized(forbiddenIng.getIsIonized());
						}
					}

				} else {
					mergingForbiddenIngMap.put(forbiddenIng.getReqMessage(), forbiddenIng);
				}
			}
		}
		mergedList.addAll(mergingForbiddenIngMap.values());
		return mergedList;
	}

	private boolean checkRuleMatchIng(IngListDataItem ingListDataItem, ForbiddenIngListDataItem fil) {

		// GMO
		if ((fil.getIsGMO() != null) && !fil.getIsGMO().isEmpty() && !fil.getIsGMO().equals(ingListDataItem.getIsGMO().toString())) {
			return false; // check next rule
		}

		// Ionized
		if ((fil.getIsIonized() != null) && !fil.getIsIonized().isEmpty() && !fil.getIsIonized().equals(ingListDataItem.getIsIonized().toString())) {
			return false; // check next rule
		}

		// Ings
		if (!fil.getIngs().isEmpty()) {
			if (!fil.getIngs().contains(ingListDataItem.getIng())) {
				return false; // check next rule
			} else if (fil.getQtyPercMaxi() != null) {
				return false; // check next rule (we will
				// check
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
				return false; // check next rule
			}
		}

		// Required GeoOrigins
		if (!fil.getRequiredGeoOrigins().isEmpty()) {
			boolean hasGeoOrigin = true;
			for (NodeRef n : ingListDataItem.getGeoOrigin()) {
				if (!fil.getRequiredGeoOrigins().contains(n)) {
					hasGeoOrigin = false;
				}
			}

			if (hasGeoOrigin) {
				return false; // check next rule
			}
		}

		// GeoTransfo
		if (!fil.getGeoTransfo().isEmpty()) {
			boolean hasGeoTransfo = false;
			for (NodeRef n : ingListDataItem.getGeoTransfo()) {
				if (fil.getGeoTransfo().contains(n)) {
					hasGeoTransfo = true;
				}
			}

			if (!hasGeoTransfo) {
				return false; // check next rule
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
				return false; // check next rule
			}

		}

		return true;
	}

	/**
	 * check the ingredients of the part according to the specification
	 *
	 */
	private void checkILOfFormulatedProduct(Collection<IngListDataItem> ingList,
			List<ForbiddenIngListDataItem> productSpecicationsForbiddenIngredientsList, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		boolean checkAutorized = false;
		for (ForbiddenIngListDataItem fil : productSpecicationsForbiddenIngredientsList) {

			for (IngListDataItem ingListDataItem : ingList) {
				if (!RequirementType.Authorized.equals(fil.getReqType())) {
					// Ings
					if (!fil.getIngs().isEmpty()) {
						if (fil.getIngs().contains(ingListDataItem.getIng())) {
							if ((fil.getQtyPercMaxi() != null) && (fil.getQtyPercMaxi() <= ingListDataItem.getQtyPerc())) {

								// req not respected
								ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(fil.getNodeRef());
								if (reqCtrl == null) {
									reqCtrl = new ReqCtrlListDataItem(null, fil.getReqType(), fil.getReqMessage(), ingListDataItem.getIng(),
											new ArrayList<NodeRef>(), RequirementDataType.Specification);
									reqCtrlMap.put(fil.getNodeRef(), reqCtrl);
								} else {
									reqCtrl.setReqDataType(RequirementDataType.Specification);
								}
							}
						}
					}

				} else {
					checkAutorized = true;
				}

			}
		}

		// Check autorized

		if (checkAutorized) {

			for (IngListDataItem ingListDataItem : ingList) {
				boolean autorized = false;

				for (ForbiddenIngListDataItem fil : productSpecicationsForbiddenIngredientsList) {
					if (RequirementType.Authorized.equals(fil.getReqType())) {
						if (checkRuleMatchIng(ingListDataItem, fil)) {
							autorized = true;
							if ((fil.getReqMessage() != null) && (fil.getReqMessage() != "")) {
								addReqCtrl(reqCtrlMap, RequirementType.Authorized, fil.getReqMessage(), ingListDataItem.getIng());
							}
							break;
						}
					}
				}

				if (!autorized) {

					String message = I18NUtil.getMessage(MESSAGE_NOTAUTHORIZED_ING);
					addReqCtrl(reqCtrlMap, RequirementType.Forbidden, message, ingListDataItem.getIng());
				}

			}

		}

	}

	// TODO refactor this method
	@Deprecated
	private IngListDataItem findIngListDataItem(List<IngListDataItem> ingLists, IngListDataItem ingList) {

		if ((ingList != null) && (ingList.getIng() != null)) {
			for (IngListDataItem i : ingLists) {
				if (ingList.getIng().equals(i.getIng())) {
					// check parent
					IngListDataItem parentIngListDataItem = ingList.getParent();
					IngListDataItem p = i.getParent();
					int j = 0;
					boolean isFound = true;
					while ((parentIngListDataItem != null) || (p != null)) {
						if (j > 256) {
							logger.warn("Cycle detected...");
							isFound = false;
							break;
						}
						if (((parentIngListDataItem != null) && (p == null)) || ((parentIngListDataItem == null) && (p != null))) {
							isFound = false;
							break;
						} else if ((parentIngListDataItem != null) && (p != null)
								&& (((parentIngListDataItem.getIng() != null) && !parentIngListDataItem.getIng().equals(p.getIng()))
										|| ((p.getIng() != null) && !p.getIng().equals(parentIngListDataItem.getIng())))) {
							isFound = false;
							break;
						}

						parentIngListDataItem = parentIngListDataItem.getParent();
						p = p.getParent();
						j++;
					}
					if (isFound) {
						return i;
					}
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

		Collections.sort(ingList, (i1, i2) -> {

			// increase
			if (((i1.getParent() == null) && (i2.getParent() == null)) || ((i1.getParent() != null) && i1.getParent().equals(i2.getParent()))) {
				if (i2.getQtyPerc() != null) {
					return i2.getQtyPerc().compareTo(i1.getQtyPerc());
				} else {
					return Objects.equals(i2.getQtyPerc(), i1.getQtyPerc()) ? 0 : -1;
				}
			} else {
				IngListDataItem root1 = findRoot(i1);
				IngListDataItem root2 = findRoot(i2);
				if (root1.equals(root2)) {
					return Integer.compare(i1.getDepthLevel() != null ? i1.getDepthLevel() : -1,
							i2.getDepthLevel() != null ? i2.getDepthLevel() : -1);
				} else {
					return Double.compare(root2.getQtyPerc() != null ? root2.getQtyPerc() : -1d,
							root1.getQtyPerc() != null ? root1.getQtyPerc() : -1d);
				}
			}
		});

		int i = 1;
		for (IngListDataItem il : ingList) {
			il.setSort(i);
			i++;
		}
	}

	private IngListDataItem findRoot(IngListDataItem ingList) {
		while (ingList.getParent() != null) {
			ingList = ingList.getParent();
		}
		return ingList;
	}

	private void addReqCtrl(Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, RequirementType requirementType, String message, NodeRef sourceNodeRef) {
		ReqCtrlListDataItem reqCtrl = null;
		for (ReqCtrlListDataItem r : reqCtrlMap.values()) {
			if (message.equals(r.getReqMessage())) {
				reqCtrl = r;
				break;
			}
		}

		if (reqCtrl == null) {
			reqCtrl = new ReqCtrlListDataItem(null, requirementType, message, null, new ArrayList<NodeRef>(), RequirementDataType.Ingredient);
			reqCtrlMap.put(null, reqCtrl);
		}

		if (!reqCtrl.getSources().contains(sourceNodeRef)) {
			reqCtrl.getSources().add(sourceNodeRef);
		}
	}
}
