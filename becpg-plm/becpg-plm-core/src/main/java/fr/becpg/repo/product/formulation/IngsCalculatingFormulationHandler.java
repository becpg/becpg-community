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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.helper.IngListHelper;
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

	private static final String MINI_SUFFIX = "-mini";
	private static final String MAXI_SUFFIX = "-maxi";
	private static final String YIELD_SUFFIX = "-yield";

	/** The Constant NO_GRP. */
	public static final String NO_GRP = "-";

	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";
	private static final String MESSAGE_INCORRECT_INGLIST_TOTAL = "message.formulate.incorrect.ingList.total";

	private static final Log logger = LogFactory.getLog(IngsCalculatingFormulationHandler.class);

	private NodeService nodeService;

	private AssociationService associationService;

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
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
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

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (!(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData))) {

			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap = new HashMap<>();

			if (accept(formulatedProduct)) {

				if (formulatedProduct.getIngList() != null) {
					for (IngListDataItem il : formulatedProduct.getIngList()) {
						if (!Boolean.TRUE.equals(il.getIsManual())) {
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
							il.getClaims().clear();
							il.setDeclType(null);
						}
					}
				} else {
					formulatedProduct.setIngList(new LinkedList<>());
				}

				// IngList
				calculateIL(formulatedProduct, reqCtrlMap);
			}

			if (!reqCtrlMap.isEmpty()) {
				if (formulatedProduct.getReqCtrlList() == null) {
					formulatedProduct.setReqCtrlList(new LinkedList<>());
				}

				formulatedProduct.getReqCtrlList().addAll(reqCtrlMap.values());
			}
		}

		return true;
	}

	private boolean accept(ProductData formulatedProduct) {
		return !Boolean.TRUE.equals(formulatedProduct.getIsIngListManual())
				&& formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))
				&& (alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST)
						|| alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST));
	}

	/**
	 * Calculate the ingredient list of a product.
	 */
	private void calculateIL(ProductData formulatedProduct, Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap) {

		List<CompoListDataItem> compoList = formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

		Map<String, Double> totalQtyIngMap = new HashMap<>();
		Map<String, Double> totalQtyVolMap = new HashMap<>();

		List<IngListDataItem> retainNodes = new ArrayList<>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if (Boolean.TRUE.equals(i.getIsManual())) {
				retainNodes.add(i);
			}
		}

		Set<NodeRef> visited = new HashSet<>();

		boolean shouldSort = compoList != null && compoList.size() > 1;

		Double totalQtyUsedWithYield = 0d;
		Double totalVolumeUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
					ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());

					if ((!DeclarationType.Omit.equals(compoItem.getDeclType()))
							&& (!(componentProductData instanceof LocalSemiFinishedProductData))) {

						visitILOfPart(formulatedProduct, compoItem, componentProductData, retainNodes, totalQtyIngMap, totalQtyVolMap, reqCtrlMap,
								visited);

						Double qty = FormulationHelper.getQtyInKg(compoItem);
						if (qty != null) {
							totalQtyUsedWithYield += (qty * FormulationHelper.getYield(compoItem)) / 100d;
						}

						Double vol = compoItem.getVolume();
						if (vol != null) {
							totalVolumeUsed += vol / 100d;
						}

					}

				}

			}
		}

		formulatedProduct.getIngList().retainAll(retainNodes);

		if (totalQtyUsedWithYield != 0d) {
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

				Double totalQtyIng = totalQtyIngMap.get(ingListDataItem.getName());
				if (totalQtyIng != null) {
					ingListDataItem.setQtyPerc(totalQtyIng / totalQtyUsedWithYield);
				} else {
					ingListDataItem.setQtyPerc(null);
				}

				Double totalQtyIngWithYield = totalQtyIngMap.get(ingListDataItem.getName() + YIELD_SUFFIX);
				if ((totalQtyIngWithYield != null) && !formulatedProduct.isGeneric()) {
					Double x = (formulatedProduct.getYield() != null ? formulatedProduct.getYield() / 100d : 1d);

					Double qtyPercWithYield = (totalQtyIngWithYield) / (totalQtyUsedWithYield);

					if ((formulatedProduct.getYield() != null) && (nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_WATER)
							|| (nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_EVAPORABLE)
									&& (Double )nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE) == 100d))) {

//						Double evaporateRate = (Double) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE);
//
//						if (evaporateRate == null) {
//							evaporateRate = 100d;
//						}

						qtyPercWithYield = qtyPercWithYield / x + ((100d - 100d / x));// * (evaporateRate/100d));
					} else {
						qtyPercWithYield = qtyPercWithYield / x;
					}
					ingListDataItem.setQtyPercWithYield(qtyPercWithYield);
				} else {
					ingListDataItem.setQtyPercWithYield(null);
				}

				if (!formulatedProduct.isGeneric() && (formulatedProduct.getSecondaryYield() != null)
						&& (formulatedProduct.getSecondaryYield() != 0d)) {

					Double qtyPercWithSecondaryYield = ingListDataItem.getQtyPercWithYield() != null ? ingListDataItem.getQtyPercWithYield()
							: ingListDataItem.getQtyPerc();
					Double x = (formulatedProduct.getSecondaryYield() / 100d);
					if (nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_WATER) ||  (nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_EVAPORABLE)
							&& (Double)nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE)== 100d) ) {

//						Double evaporateRate = (Double) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE);
//
//						if (evaporateRate == null) {
//							evaporateRate = 100d;
//						}
//						
						qtyPercWithSecondaryYield = qtyPercWithSecondaryYield / x + ((100d - 100d / x)); /* * (evaporateRate/100d)) */

					} else {

						if (qtyPercWithSecondaryYield != null) {
							qtyPercWithSecondaryYield = qtyPercWithSecondaryYield / x;
						}
					}

					ingListDataItem.setQtyPercWithSecondaryYield(qtyPercWithSecondaryYield);
				} else {
					ingListDataItem.setQtyPercWithSecondaryYield(null);
				}

				Double totalQtyMini = totalQtyIngMap.get(ingListDataItem.getName() + MINI_SUFFIX);
				if (totalQtyMini != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setMini(totalQtyMini);
					} else {
						ingListDataItem.setMini(totalQtyMini / totalQtyUsedWithYield);
					}
				}

				Double totalQtyMaxi = totalQtyIngMap.get(ingListDataItem.getName() + MAXI_SUFFIX);
				if (totalQtyMaxi != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setMaxi(totalQtyMaxi);
					} else {
						ingListDataItem.setMaxi(totalQtyMaxi / totalQtyUsedWithYield);
					}
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
		if (shouldSort) {
			sortIL(formulatedProduct.getIngList());
		}
	}

	private void addReqCtrl(Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, NodeRef reqNodeRef, RequirementType requirementType, MLText message,
			NodeRef sourceNodeRef, RequirementDataType requirementDataType) {

		ReqCtrlListDataItem reqCtrl = reqCtrlMap.get(reqNodeRef);
		if (reqCtrl == null) {
			reqCtrl = ReqCtrlListDataItem.build().ofType(requirementType).withMessage(message).ofDataType(requirementDataType);

			reqCtrlMap.put(reqNodeRef, reqCtrl);
		} else {
			reqCtrl.setReqDataType(requirementDataType);
		}

		reqCtrl.addSource(sourceNodeRef);

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
	 * @param isRawMaterial
	 * @throws FormulateException
	 */
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem, ProductData componentProductData,
			List<IngListDataItem> retainNodes, Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap,
			Map<NodeRef, ReqCtrlListDataItem> reqCtrlMap, Set<NodeRef> visited) {

		if (!visited.contains(componentProductData.getNodeRef())) {

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
					double total = 0d;
					for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
						if ((ingListDataItem.getQtyPerc() != null)
								&& ((ingListDataItem.getDepthLevel() == null) || (ingListDataItem.getDepthLevel() == 1))) {
							total += ingListDataItem.getQtyPerc();
						}

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
		calculateILOfPart(formulatedProduct, componentProductData, compoListDataItem,
				CompositeHelper.getHierarchicalCompoList(
						IngListHelper.extractParentList(componentProductData.getIngList(), associationService, alfrescoRepository)),
				formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, totalQtyVolMap, null, formulatedProduct.isGeneric());
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
	 * @param isRawMaterial
	 * @throws FormulateException
	 */
	private void calculateILOfPart(ProductData formulatedProduct, ProductData componentProductData, CompoListDataItem compoListDataItem,
			Composite<IngListDataItem> compositeIngList, List<IngListDataItem> ingList, List<IngListDataItem> retainNodes,
			Map<String, Double> totalQtyIngMap, Map<String, Double> totalQtyVolMap, IngListDataItem parentIngListDataItem, boolean isGeneric) {

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

			//Keep Sort
			newIngListDataItem.setSort(ingListDataItem.getSort());

			if (!retainNodes.contains(newIngListDataItem)) {
				retainNodes.add(newIngListDataItem);
				newIngListDataItem.getClaims().addAll(ingListDataItem.getClaims());
			} else {
				newIngListDataItem.getClaims().retainAll(ingListDataItem.getClaims());
			}

			Double totalQtyIng = totalQtyIngMap.get(newIngListDataItem.getName());
			Double totalQtyIngWithYield = totalQtyIngMap.get(newIngListDataItem.getName() + YIELD_SUFFIX);
			Double totalQtyMaxi = totalQtyIngMap.get(newIngListDataItem.getName() + MAXI_SUFFIX);
			Double totalQtyMini = totalQtyIngMap.get(newIngListDataItem.getName() + MINI_SUFFIX);

			// Calculate qty
			Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
			Double qtyIng = ingListDataItem.getQtyPerc();
			Double qtyIngWithYield = ingListDataItem.getQtyPercWithYield();
			if ((qtyIngWithYield == null) || componentProductData.isGeneric()) {
				qtyIngWithYield = qtyIng;
			}

			Double mini = ingListDataItem.getMini();
			Double maxi = ingListDataItem.getMaxi();

			if (qty != null) {

				if ((qtyIng != null)) {
					double valueToAdd = qty * qtyIng * ((FormulationHelper.getYield(compoListDataItem) / 100d));
					if (totalQtyIng == null) {
						totalQtyIng = 0d;
					}
					totalQtyIng += valueToAdd;
					totalQtyIngMap.put(newIngListDataItem.getName(), totalQtyIng);
				}

				if ((qtyIngWithYield != null)) {

					double valueToAdd = qty * qtyIngWithYield;

					if (totalQtyIngWithYield == null) {
						totalQtyIngWithYield = 0d;
					}

					if ((FormulationHelper.getYield(compoListDataItem) != null)
							&& (nodeService.hasAspect(ingListDataItem.getIng(), PLMModel.ASPECT_WATER) || (nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE) != null
									&& (Double) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE) == 100d))) {
						
//						Double evaporateRate = (Double) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE);
//
//						if (evaporateRate == null) {
//							evaporateRate = 100d;
//						}
						
						valueToAdd = qty * ((qtyIngWithYield) - ((100d  - FormulationHelper.getYield(compoListDataItem))));//* (evaporateRate/100d)));
					}

					totalQtyIngWithYield += valueToAdd;
					totalQtyIngMap.put(newIngListDataItem.getName() + YIELD_SUFFIX, totalQtyIngWithYield);

				}

				if ((maxi != null)) {
					if (isGeneric) {

						if ((totalQtyMaxi == null) || (maxi > totalQtyMaxi)) {
							totalQtyMaxi = maxi;
						}

					} else {
						double valueToAdd = qty * maxi;
						if (totalQtyMaxi == null) {
							totalQtyMaxi = 0d;
						}
						totalQtyMaxi += valueToAdd;
					}

					totalQtyIngMap.put(newIngListDataItem.getName() + MAXI_SUFFIX, totalQtyMaxi);

				}

				if ((mini != null)) {
					if (isGeneric) {
						if ((totalQtyMini == null) || (mini < totalQtyMini)) {
							totalQtyMini = mini;
						}
					} else {
						double valueToAdd = qty * mini;
						if (totalQtyMini == null) {
							totalQtyMini = 0d;
						}
						totalQtyMini += valueToAdd;
					}

					totalQtyIngMap.put(newIngListDataItem.getName() + MINI_SUFFIX, totalQtyMini);
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
			if ((ingListDataItem.getIsProcessingAid() == null) || !Boolean.TRUE.equals(ingListDataItem.getIsProcessingAid())) {
				newIngListDataItem.setIsProcessingAid(false);
			}

			// Support
			if ((ingListDataItem.getIsSupport() == null) || !Boolean.TRUE.equals(ingListDataItem.getIsSupport())) {
				newIngListDataItem.setIsSupport(false);
			}

			// GMO
			if (Boolean.TRUE.equals(ingListDataItem.getIsGMO()) && !Boolean.TRUE.equals(newIngListDataItem.getIsGMO())) {
				newIngListDataItem.setIsGMO(true);
			}

			// Ionized
			if (Boolean.TRUE.equals(ingListDataItem.getIsIonized()) && !Boolean.TRUE.equals(newIngListDataItem.getIsIonized())) {
				newIngListDataItem.setIsIonized(true);
			}

			// Decltype
			newIngListDataItem.setDeclType(ingListDataItem.getDeclType());

			// recursive
			if (!component.isLeaf()) {
				calculateILOfPart(formulatedProduct, componentProductData, compoListDataItem, component, ingList, retainNodes, totalQtyIngMap,
						totalQtyVolMap, newIngListDataItem, isGeneric);
			}
		}
	}

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
						if ((j > 256) || (((parentIngListDataItem != null) && (p == null)) || ((parentIngListDataItem == null) && (p != null)))
								|| ((parentIngListDataItem != null) && (p != null)
										&& (((parentIngListDataItem.getIng() != null) && !parentIngListDataItem.getIng().equals(p.getIng()))
												|| ((p.getIng() != null) && !p.getIng().equals(parentIngListDataItem.getIng()))))) {
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
	 * Sort ingList by qty perc in descending order group by parent
	 *
	 */
	private void sortIL(List<IngListDataItem> ingList) {
		if (!ingList.isEmpty()) {
			final IngListDataItem nullPlaceholder = new IngListDataItem();
			Map<IngListDataItem, List<IngListDataItem>> byParent = ingList.stream()
					.collect(Collectors.groupingBy(item -> item.getParent() == null ? nullPlaceholder : item.getParent()));

			List<IngListDataItem> sortedList = new ArrayList<>();
			AtomicInteger index = new AtomicInteger(1);

			sorted(byParent.getOrDefault(nullPlaceholder, Collections.emptyList())).forEach(root -> processItem(root, byParent, sortedList));

			sortedList.forEach(item -> item.setSort(index.getAndIncrement()));
			ingList.sort(Comparator.comparing(IngListDataItem::getSort, Comparator.nullsLast(Comparator.naturalOrder())));
		}
	}

	private void processItem(IngListDataItem item, Map<IngListDataItem, List<IngListDataItem>> byParent, List<IngListDataItem> sortedList) {
		sortedList.add(item);
		sorted(byParent.getOrDefault(item, Collections.emptyList())).forEach(child -> processItem(child, byParent, sortedList));
	}

	private List<IngListDataItem> sorted(List<IngListDataItem> items) {
		return items.stream().sorted(Comparator.comparing(IngListDataItem::getQtyPerc, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Comparator.comparing(this::getLegalName))).toList();
	}

	private String getLegalName(IngListDataItem ingListDataItem) {

		if (ingListDataItem.getIng() != null) {
			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
			return ingItem.getLegalName(Locale.getDefault());
		}
		return ingListDataItem.getName();
	}

}
