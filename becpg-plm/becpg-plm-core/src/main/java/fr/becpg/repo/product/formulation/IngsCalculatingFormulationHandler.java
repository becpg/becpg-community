/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
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


	/** The logger. */
	private static final Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

	private NodeService nodeService;

	private boolean ingsCalculatingWithYield = false;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setIngsCalculatingWithYield(boolean ingsCalculatingWithYield) {
		this.ingsCalculatingWithYield = ingsCalculatingWithYield;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("\t==== Calculate ingredient list of " + formulatedProduct.getName());

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<>();

		if (accept(formulatedProduct)) {

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

			// Load product specification

			// IngList
			calculateIL(formulatedProduct, reqCtrlMap);
		}

	
		if(!reqCtrlMap.isEmpty()) {
			if (formulatedProduct.getReqCtrlList() == null) {
				formulatedProduct.setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
			}
			
			formulatedProduct.getReqCtrlList().addAll(reqCtrlMap.values());
		}

		return true;
	}

	private boolean accept(ProductData formulatedProduct) {
		return formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
				&& (alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST)
						|| alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST));
	}

	/**
	 * Calculate the ingredient list of a product.
	 *
	 * @param reqCtrlMap
	 *
	 * @param productData
	 *            the product data
	 * @return the list
	 * @throws FormulateException
	 */
	private void calculateIL(ProductData formulatedProduct, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) throws FormulateException {

		List<CompoListDataItem> compoList = formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

		Map<String, Double> totalQtyIngMap = new HashMap<>();
		Map<String, Double> totalQtyVolMap = new HashMap<>();

		List<IngListDataItem> retainNodes = new ArrayList<>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if ((i.getIsManual() != null) && i.getIsManual()) {
				retainNodes.add(i);
			}
		}

		Set<NodeRef> visited = new HashSet<>();

		Double totalQtyUsed = 0d, totalVolumeUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
					ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());
					visitILOfPart(formulatedProduct, compoItem, componentProductData, retainNodes, totalQtyIngMap, totalQtyVolMap, reqCtrlMap,
							visited);

					QName type = nodeService.getType(compoItem.getProduct());
					if ((type != null) && (type.isMatch(PLMModel.TYPE_RAWMATERIAL) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)
							|| type.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)) && (compoItem.getDeclType() != DeclarationType.Omit)) {
						Double qty = FormulationHelper.getQtyInKg(compoItem);
						if (qty != null) {
							totalQtyUsed += (applyYield(qty, formulatedProduct.getYield()) * FormulationHelper.getYield(compoItem)) / 100;
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

					if (ingsCalculatingWithYield && (formulatedProduct.getYield() != null) && (formulatedProduct.getRecipeQtyUsed() != null)
							&& nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_WATER)) {

						Double waterLost = (100 - (formulatedProduct.getYield())) * formulatedProduct.getRecipeQtyUsed();
						ingListDataItem.setQtyPerc((totalQtyIng - (waterLost)) / totalQtyUsed);

					} else {
						ingListDataItem.setQtyPerc(totalQtyIng / totalQtyUsed);
					}
				} else {
					ingListDataItem.setQtyPerc(null);
				}

				Double totalQtyMini = totalQtyIngMap.get(ingListDataItem.getName() + "-mini");
				if (totalQtyMini != null) {

					ingListDataItem.setMini(totalQtyMini / totalQtyUsed);
				}

				Double totalQtyMaxi = totalQtyIngMap.get(ingListDataItem.getName() + "-maxi");
				if (totalQtyMaxi != null) {

					ingListDataItem.setMaxi(totalQtyMaxi / totalQtyUsed);
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

		// sort collection
		sortIL(formulatedProduct.getIngList());

	}

	private Double applyYield(Double qty, Double yield) {
		if (ingsCalculatingWithYield && (qty != null) && (yield != null)) {
			return (qty * yield) / 100d;
		}
		return qty;
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
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, Set<NodeRef> visited) throws FormulateException {

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

			Double totalQtyMaxi = totalQtyIngMap.get(newIngListDataItem.getName() + "-maxi");
			if (totalQtyMaxi == null) {
				totalQtyMaxi = 0d;
				totalQtyIngMap.put(newIngListDataItem.getName() + "-maxi", totalQtyMaxi);
			}

			Double totalQtyMini = totalQtyIngMap.get(newIngListDataItem.getName() + "-mini");
			if (totalQtyMini == null) {
				totalQtyMini = 0d;
				totalQtyIngMap.put(newIngListDataItem.getName() + "-mini", totalQtyMini);
			}

			// Calculate qty
			Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();
			Double mini = ingListDataItem.getMini();
			Double maxi = ingListDataItem.getMaxi();

			if (qty != null) {
				qty *= FormulationHelper.getYield(compoListDataItem) / 100;

				if ((qtyIng != null)) {
					Double valueToAdd = qty * qtyIng;
					totalQtyIng += valueToAdd;
					totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
				}

				if ((maxi != null)) {
					Double valueToAdd = qty * maxi;
					totalQtyMaxi += valueToAdd;
					totalQtyIngMap.put(newIngListDataItem.getName() + "-maxi", totalQtyMaxi);
				}

				if ((mini != null)) {
					Double valueToAdd = qty * mini;
					totalQtyMini += valueToAdd;
					totalQtyIngMap.put(newIngListDataItem.getName() + "-mini", totalQtyMini);
				}

				Double volumeQty = totalQtyVolMap.get(newIngListDataItem.getName());
				if (volumeQty == null) {
					volumeQty = 0d;
				}

				// Semi finished
				if ((ingListDataItem.getVolumeQtyPerc() != null) && (compoListDataItem.getVolume() != null)) {
					totalQtyVolMap.put(newIngListDataItem.getName(),
							volumeQty + ((ingListDataItem.getVolumeQtyPerc() * compoListDataItem.getVolume()) / 100));
				}

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
			if (((i1.getParent() == null) && (i2.getParent() == null))
					|| ((i1.getParent() != null) && (i2.getParent() != null) && i1.getParent().equals(i2.getParent()))) {
				return Double.compare(i2.getQtyPerc() != null ? i2.getQtyPerc() : -1d, i1.getQtyPerc() != null ? i1.getQtyPerc() : -1d);
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

	
}
