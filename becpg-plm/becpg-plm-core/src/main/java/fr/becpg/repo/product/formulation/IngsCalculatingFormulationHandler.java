/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
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
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class IngsCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class IngsCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The Constant NO_GRP. */
	public static final String NO_GRP = "-";

	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";
	private static final String MESSAGE_INCORRECT_INGLIST_TOTAL = "message.formulate.incorrect.ingList.total";

	private static final Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

	private NodeService nodeService;

	private boolean ingsCalculatingWithYield = false;

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>alfrescoRepository</code>.
	 * </p>
	 *
	 * @param alfrescoRepository
	 *            a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>
	 * Setter for the field <code>ingsCalculatingWithYield</code>.
	 * </p>
	 *
	 * @param ingsCalculatingWithYield
	 *            a boolean.
	 */
	public void setIngsCalculatingWithYield(boolean ingsCalculatingWithYield) {
		this.ingsCalculatingWithYield = ingsCalculatingWithYield;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
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

		if (!reqCtrlMap.isEmpty()) {
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

							if (!ingsCalculatingWithYield) {
								qty *= FormulationHelper.getYield(compoItem) / 100d;
							}

							totalQtyUsed += applyYield(qty,
									formulatedProduct.getYield() != null
											? (formulatedProduct.getYield() * FormulationHelper.getYield(compoItem) / 100d)
											: null);
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

	private void addReqCtrl(Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, NodeRef reqNodeRef, RequirementType requirementType, MLText message,
			NodeRef sourceNodeRef, RequirementDataType requirementDataType) {

		ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(reqNodeRef);
		if (reqCtrl == null) {
			reqCtrl = new ReqCtrlListDataItem(null, requirementType, message, null, new ArrayList<NodeRef>(), requirementDataType);
			reqCtrlMap.put(reqNodeRef, reqCtrl);
		} else {
			reqCtrl.setReqDataType(requirementDataType);
		}

		if (!reqCtrl.getSources().contains(sourceNodeRef)) {
			reqCtrl.getSources().add(sourceNodeRef);
		}

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

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeService.getType(componentProductData.getNodeRef()))
				&& !visited.contains(componentProductData.getNodeRef())) {

			visited.add(componentProductData.getNodeRef());

			// datalist ingList is null or empty
			if ((componentProductData.getIngList() == null) || componentProductData.getIngList().isEmpty()) {

				if ((compoListDataItem.getDeclType() == null) || (!compoListDataItem.getDeclType().equals(DeclarationType.DoNotDetails)
						&& !compoListDataItem.getDeclType().equals(DeclarationType.Omit))) {
					if (logger.isDebugEnabled()) {
						logger.debug("CompoItem: " + compoListDataItem.getProduct() + " - doesn't have ing ");
					}

					// req not respected
					String message = I18NUtil.getMessage(MESSAGE_MISSING_INGLIST);
					addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "missing-inglist"), RequirementType.Tolerated, new MLText(message),
							componentProductData.getNodeRef(), RequirementDataType.Ingredient);
				}

				return;

			} else {

				if ((compoListDataItem.getDeclType() == null) || (!compoListDataItem.getDeclType().equals(DeclarationType.DoNotDetails)
						&& !compoListDataItem.getDeclType().equals(DeclarationType.Omit))) {
					Double total = 0d;
					for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
						if ((ingListDataItem.getQtyPerc() != null)
								&& ((ingListDataItem.getDepthLevel() == null) || (ingListDataItem.getDepthLevel() == 1))) {
							total += ingListDataItem.getQtyPerc();
						}

					}

					if (ingsCalculatingWithYield && (componentProductData.getYield() != null)) {
						total = (componentProductData.getYield() * total) / 100d;
					}
					// Due to double precision
					if (Math.abs(total - 100d) > 0.00001) {
						String message = I18NUtil.getMessage(MESSAGE_INCORRECT_INGLIST_TOTAL);
						addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "incorrect-inglist-total"), RequirementType.Tolerated,
								new MLText(message), componentProductData.getNodeRef(), RequirementDataType.Ingredient);
					}

				}
			}
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
				if (!ingsCalculatingWithYield) {
					qty *= FormulationHelper.getYield(compoListDataItem) / 100;
				}

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
	 */
	private void sortIL(List<IngListDataItem> ingList) {

		if (!ingList.isEmpty()) {
			final IngListDataItem nullPlaceholder = new IngListDataItem();
			Map<IngListDataItem, List<IngListDataItem>> byParent = ingList.stream()
					.collect(Collectors.groupingBy(obj -> (obj.getParent() == null ? nullPlaceholder : obj.getParent()), Collectors.toList()));

			Stack<IngListDataItem> processor = new Stack<>();

			int i = 1;

			byParent.getOrDefault(nullPlaceholder, Collections.emptyList()).stream().sorted(Comparator
					.comparing(IngListDataItem::getQtyPerc, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(IngListDataItem::getName))
					.collect(Collectors.toList()).forEach(processor::add);

			while (!processor.isEmpty()) {
				i++;
				IngListDataItem il = processor.pop();
				byParent.getOrDefault(il, Collections.emptyList()).stream()
						.sorted(Comparator.comparingDouble(IngListDataItem::getQtyPerc).thenComparing(IngListDataItem::getName))
						.collect(Collectors.toList()).forEach(processor::add);

				il.setSort(i);

			}

			ingList.sort(Comparator.comparingInt(IngListDataItem::getSort));
		}

	}

}
