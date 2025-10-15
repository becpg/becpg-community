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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.GUID;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.formulation.labeling.EvaporatedDataItem;
import fr.becpg.repo.product.helper.IngListHelper;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;
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

			Map<NodeRef, RequirementListDataItem> reqCtrlMap = new HashMap<>();

			if (accept(formulatedProduct)) {

				if (formulatedProduct.getIngList() != null) {
					for (IngListDataItem il : formulatedProduct.getIngList()) {
						if (!Boolean.TRUE.equals(il.getIsManual())) {
							// reset
							il.setQtyPerc(null);
							il.setQtyPerc1(null);
							il.setQtyPerc2(null);
							il.setQtyPerc3(null);
							il.setQtyPerc4(null);
							il.setQtyPercWithYield(null);
							il.setQtyPercWithSecondaryYield(null);
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
					formulatedProduct.setIngList(new ArrayList<>());
				}

				// IngList
				calculateIL(formulatedProduct, reqCtrlMap);
			}

			if (!reqCtrlMap.isEmpty()) {
				if (formulatedProduct.getReqCtrlList() == null) {
					formulatedProduct.setReqCtrlList(new ArrayList<>());
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
	private void calculateIL(ProductData formulatedProduct, Map<NodeRef, RequirementListDataItem> reqCtrlMap) {

		List<CompoListDataItem> compoList = formulatedProduct
				.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()));

		Map<String, IngListDataItem> totalQtyIngMap = new HashMap<>();

		List<IngListDataItem> retainNodes = new ArrayList<>();

		// manuel
		for (IngListDataItem i : formulatedProduct.getIngList()) {
			if (Boolean.TRUE.equals(i.getIsManual())) {
				retainNodes.add(i);
			}
		}

		Set<NodeRef> visited = new HashSet<>();

		boolean shouldSort = (compoList != null) && (compoList.size() > 1);

		double totalQtyUsedWithYield = 0d;
		double totalVolumeUsed = 0d;
		if (compoList != null) {
			for (CompoListDataItem compoItem : compoList) {

				if ((compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
					ProductData componentProductData = (ProductData) alfrescoRepository.findOne(compoItem.getProduct());

					if ((!DeclarationType.Omit.equals(compoItem.getDeclType()))
							&& (!(componentProductData instanceof LocalSemiFinishedProductData))) {

						visitILOfPart(formulatedProduct, compoItem, componentProductData, retainNodes, totalQtyIngMap, reqCtrlMap, visited);

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

			Set<EvaporatedDataItem> evaporatedDataItems = new HashSet<>();

			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

				IngListDataItem totalQtyIng = totalQtyIngMap.get(ingListDataItem.getName());

				Double totalQty = totalQtyIng != null ? totalQtyIng.getQtyPerc() : null;
				Double totalQty1 = totalQtyIng != null ? totalQtyIng.getQtyPerc1() : null;
				Double totalQty2 = totalQtyIng != null ? totalQtyIng.getQtyPerc2() : null;
				Double totalQty3 = totalQtyIng != null ? totalQtyIng.getQtyPerc3() : null;
				Double totalQty4 = totalQtyIng != null ? totalQtyIng.getQtyPerc4() : null;
				Double totalQty5 = totalQtyIng != null ? totalQtyIng.getQtyPerc5() : null;
				Double totalQtyMini = totalQtyIng != null ? totalQtyIng.getMini() : null;
				Double totalQtyMaxi = totalQtyIng != null ? totalQtyIng.getMaxi() : null;
				Double totalQtyIngWithYield = totalQtyIng != null ? totalQtyIng.getQtyPercWithYield() : null;
				Double totalVol = totalQtyIng != null ? totalQtyIng.getVolumeQtyPerc() : null;

				if (totalQty != null) {
					ingListDataItem.setQtyPerc(totalQty / totalQtyUsedWithYield);
				} else {
					ingListDataItem.setQtyPerc(null);
				}
				if (totalQty1 != null) {
					ingListDataItem.setQtyPerc1(totalQty1 / totalQtyUsedWithYield);
				} else {
					ingListDataItem.setQtyPerc1(null);
				}
				if (totalQty2 != null) {
					ingListDataItem.setQtyPerc2(totalQty2 / totalQtyUsedWithYield);
				} else {
					ingListDataItem.setQtyPerc2(null);
				}
				if (totalQty3 != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setQtyPerc3(totalQty3);
					} else {
						ingListDataItem.setQtyPerc3(totalQty3 / totalQtyUsedWithYield);
					}
				}
				if (totalQty4 != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setQtyPerc4(totalQty4);
					} else {
						ingListDataItem.setQtyPerc4(totalQty4 / totalQtyUsedWithYield);
					}
				}

				if (totalQty5 != null) {
					ingListDataItem.setQtyPerc5(totalQty5 / totalQtyUsedWithYield);
				} else {
					ingListDataItem.setQtyPerc5(null);
				}

				if (totalVol != null) {
					ingListDataItem.setVolumeQtyPerc(totalVol / totalVolumeUsed);
				} else {
					ingListDataItem.setVolumeQtyPerc(null);
				}

				if (totalQtyMini != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setMini(totalQtyMini);
					} else {
						ingListDataItem.setMini(totalQtyMini / totalQtyUsedWithYield);
					}
				}
				if (totalQtyMaxi != null) {
					if (formulatedProduct.isGeneric()) {
						ingListDataItem.setMaxi(totalQtyMaxi);
					} else {
						ingListDataItem.setMaxi(totalQtyMaxi / totalQtyUsedWithYield);
					}
				}

				if ((totalQtyIngWithYield != null) && !formulatedProduct.isGeneric()) {
					double primaryYieldFactor = formulatedProduct.getYield() != null ? formulatedProduct.getYield() / 100d : 1d;
					Double qtyPercWithYield = totalQtyIngWithYield / totalQtyUsedWithYield;

					if (hasEvaporationData(ingListDataItem)) {
						Double evaporateRate = getEvaporateRate(ingListDataItem);
						evaporatedDataItems.add(new EvaporatedDataItem(ingListDataItem.getIng(), evaporateRate, null, null));
					} else {
						qtyPercWithYield /= primaryYieldFactor;
					}
					ingListDataItem.setQtyPercWithYield(qtyPercWithYield);
				} else {
					ingListDataItem.setQtyPercWithYield(null);
				}

				if (!formulatedProduct.isGeneric() && (formulatedProduct.getSecondaryYield() != null)
						&& (formulatedProduct.getSecondaryYield() != 0d)) {
					Double qtyPercWithSecondaryYield = ingListDataItem.getQtyPercWithYield() != null ? ingListDataItem.getQtyPercWithYield()
							: ingListDataItem.getQtyPerc();

					if (qtyPercWithSecondaryYield != null) {
						double secondaryYieldFactor = formulatedProduct.getSecondaryYield() / 100d;
						if (hasEvaporationData(ingListDataItem)) {
							Double evaporateRate = getEvaporateRate(ingListDataItem);
							evaporatedDataItems.add(new EvaporatedDataItem(ingListDataItem.getIng(), evaporateRate, null, null));
						} else {
							qtyPercWithSecondaryYield /= secondaryYieldFactor;
						}
					}
					ingListDataItem.setQtyPercWithSecondaryYield(qtyPercWithSecondaryYield);
				} else {
					ingListDataItem.setQtyPercWithSecondaryYield(null);
				}

				// add detailable aspect
				if (!ingListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					ingListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}

			if (!formulatedProduct.isGeneric()) {

				applyEvaporation(formulatedProduct, evaporatedDataItems);
				applySecondaryEvaporation(formulatedProduct, evaporatedDataItems);
			}

			// Normalize percentages to account for omitted ingredients
			normalizePercentagesForOmittedIngredients(formulatedProduct);
		}

		// sort collection
		if (shouldSort) {
			sortIL(formulatedProduct.getIngList());
		}
	}

	private boolean hasEvaporationData(IngListDataItem ingListDataItem) {
		return EvaporatingFormulationHelper.hasEvaporationData(ingListDataItem.getIng(), nodeService);
	}

	private Double getEvaporateRate(IngListDataItem ingListDataItem) {
		return EvaporatingFormulationHelper.getEvaporateRate(ingListDataItem.getIng(), nodeService);
	}

	private void applyEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems) {
		applyEvaporation(formulatedProduct, evaporatedDataItems, formulatedProduct.getYield(), IngListDataItem::getQtyPercWithYield,
				IngListDataItem::setQtyPercWithYield);
	}

	private void applySecondaryEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems) {
		applyEvaporation(formulatedProduct, evaporatedDataItems, formulatedProduct.getSecondaryYield(), IngListDataItem::getQtyPercWithSecondaryYield,
				IngListDataItem::setQtyPercWithSecondaryYield);
	}

	private void applyEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems, Double currYield,
			Function<IngListDataItem, Double> getQtyPercWithYield, BiConsumer<IngListDataItem, Double> setQtyPercWithYield) {
		if ((currYield != null) && (currYield != 0d)) {
			double yieldFactor = currYield / 100d;
			Double evaporatingQty = 100d - currYield;

			if (!evaporatedDataItems.isEmpty() && (evaporatingQty > 0d)) {

				// Use EvaporatingFormulationHelper for evaporation processing
				Function<NodeRef, IngListDataItem> matchItem = nodeRef -> formulatedProduct.getIngList().stream()
						.filter(i -> (i != null) && (i.getIng() != null) && i.getIng().equals(nodeRef))
						.findFirst().orElse(null);
				
				Function<IngListDataItem, String> getItemName = IngListDataItem::getName;
				
				EvaporatingFormulationHelper.applyEvaporation(evaporatingQty, evaporatedDataItems,
						getQtyPercWithYield, setQtyPercWithYield, matchItem, getItemName, null);

				// Adjust quantities by the yield factor (only if yieldFactor is not zero to avoid division by zero)
				if (Math.abs(yieldFactor) > 0.000001) {
					for (EvaporatedDataItem evaporatedDataItem : evaporatedDataItems) {
						if ((evaporatedDataItem == null) || (evaporatedDataItem.getProductNodeRef() == null)) {
							continue;
						}

						IngListDataItem ingListDataItem = matchItem.apply(evaporatedDataItem.getProductNodeRef());

						if (ingListDataItem != null) {
							Double currentQty = getQtyPercWithYield.apply(ingListDataItem);
							if (currentQty != null) {
								double adjustedQty = currentQty / yieldFactor;
								setQtyPercWithYield.accept(ingListDataItem, adjustedQty);
							}
						}
					}
				} else if (logger.isWarnEnabled()) {
					logger.warn("Cannot adjust quantities: yieldFactor is zero");
				}
			}
		}
	}


	/**
	 * Normalizes ingredient percentages after some ingredients have been omitted.
	 * When ingredients are omitted, the percentages of the remaining ingredients are
	 * rescaled so that their total sum is 100%.
	 *
	 * @param formulatedProduct the product being formulated
	 */
	private void normalizePercentagesForOmittedIngredients(ProductData formulatedProduct) {
		if ((formulatedProduct.getIngList() == null) || formulatedProduct.getIngList().isEmpty()) {
			return;
		}

		// Calculate the sum of percentages for non-omitted ingredients at level 1
		double sumOfNonOmittedPerc = 0d;
		double sumOfNonOmittedVolumePerc = 0d;

		for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
			if ((ingListDataItem.getDepthLevel() == null) || (ingListDataItem.getDepthLevel() == 1)) {
				if (!DeclarationType.Omit.equals(ingListDataItem.getDeclType())) {
					if (ingListDataItem.getQtyPerc() != null) {
						sumOfNonOmittedPerc += ingListDataItem.getQtyPerc();
					}
					if (ingListDataItem.getVolumeQtyPerc() != null) {
						sumOfNonOmittedVolumePerc += ingListDataItem.getVolumeQtyPerc();
					}
				}
			}
		}

		// If the sum is not 100, normalize the percentages
		if ((Math.abs(sumOfNonOmittedPerc) > 0.00001) && (Math.abs(sumOfNonOmittedPerc - 100.0) > 0.00001)) {
			double normalizationFactor = 100.0 / sumOfNonOmittedPerc;
			if (logger.isDebugEnabled()) {
				logger.debug("Normalizing percentages for omitted ingredients. Sum of non-omitted: " + sumOfNonOmittedPerc
						+ ", Normalization factor: " + normalizationFactor);
			}

			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
				if (!DeclarationType.Omit.equals(ingListDataItem.getDeclType())) {
					// Normalize all relevant percentage fields
					if (ingListDataItem.getQtyPerc() != null) {
						ingListDataItem.setQtyPerc(ingListDataItem.getQtyPerc() * normalizationFactor);
					}
					if (ingListDataItem.getQtyPerc1() != null) {
						ingListDataItem.setQtyPerc1(ingListDataItem.getQtyPerc1() * normalizationFactor);
					}
					if (ingListDataItem.getQtyPerc2() != null) {
						ingListDataItem.setQtyPerc2(ingListDataItem.getQtyPerc2() * normalizationFactor);
					}
					if (!formulatedProduct.isGeneric() && ingListDataItem.getQtyPerc3() != null) {
						ingListDataItem.setQtyPerc3(ingListDataItem.getQtyPerc3() * normalizationFactor);
					}
					if (!formulatedProduct.isGeneric() && ingListDataItem.getQtyPerc4() != null) {
						ingListDataItem.setQtyPerc4(ingListDataItem.getQtyPerc4() * normalizationFactor);
					}
					if (ingListDataItem.getQtyPerc5() != null) {
						ingListDataItem.setQtyPerc5(ingListDataItem.getQtyPerc5() * normalizationFactor);
					}
					if (ingListDataItem.getMini() != null) {
						ingListDataItem.setMini(ingListDataItem.getMini() * normalizationFactor);
					}
					if (ingListDataItem.getMaxi() != null) {
						ingListDataItem.setMaxi(ingListDataItem.getMaxi() * normalizationFactor);
					}
				}
			}
		}

		// Separately normalize volume percentages
		if ((Math.abs(sumOfNonOmittedVolumePerc) > 0.00001) && (Math.abs(sumOfNonOmittedVolumePerc - 100.0) > 0.00001)) {
			double volumeNormalizationFactor = 100.0 / sumOfNonOmittedVolumePerc;
			if (logger.isDebugEnabled()) {
				logger.debug("Normalizing volume percentages. Sum of non-omitted: " + sumOfNonOmittedVolumePerc
						+ ", Normalization factor: " + volumeNormalizationFactor);
			}
			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
				if (!DeclarationType.Omit.equals(ingListDataItem.getDeclType()) && ingListDataItem.getVolumeQtyPerc() != null) {
					ingListDataItem.setVolumeQtyPerc(ingListDataItem.getVolumeQtyPerc() * volumeNormalizationFactor);
				}
			}
		}
	}

	private void addReqCtrl(Map<NodeRef, RequirementListDataItem> reqCtrlMap, NodeRef reqNodeRef, RequirementType requirementType, MLText message,
			NodeRef sourceNodeRef, RequirementDataType requirementDataType) {

		RequirementListDataItem reqCtrl = reqCtrlMap.get(reqNodeRef);
		if (reqCtrl == null) {
			reqCtrl = RequirementListDataItem.build().ofType(requirementType).withMessage(message).ofDataType(requirementDataType);

			reqCtrlMap.put(reqNodeRef, reqCtrl);
		} else {
			reqCtrl.setReqDataType(requirementDataType);
		}

		reqCtrl.addSource(sourceNodeRef);

	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param formulatedProduct product being formulated
	 * @param compoListDataItem the component list item in the formulation
	 * @param componentProductData the component product whose ingredients are added
	 * @param retainNodes list of ingredient items to retain
	 * @param totalQtyIngMap map accumulating total quantities per ingredient key
	 * @param reqCtrlMap map of requirement controls by node
	 * @param visited set tracking already visited nodeRefs to avoid cycles
	 */
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem, ProductData componentProductData,
			List<IngListDataItem> retainNodes, Map<String, IngListDataItem> totalQtyIngMap, Map<NodeRef, RequirementListDataItem> reqCtrlMap,
			Set<NodeRef> visited) {

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
					addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "missing-inglist"), RequirementType.Tolerated,
							MLTextHelper.getI18NMessage(MESSAGE_MISSING_INGLIST), componentProductData.getNodeRef(), RequirementDataType.Ingredient);
				}

				return;

			} else if ((compoListDataItem.getDeclType() == null) || (!DeclarationType.DoNotDetails.equals(compoListDataItem.getDeclType())
					&& !DeclarationType.Omit.equals(compoListDataItem.getDeclType()))) {
				double total = 0d;
				for (IngListDataItem ingListDataItem : componentProductData.getIngList()) {
					if ((ingListDataItem.getQtyPerc() != null) && !DeclarationType.Omit.equals(ingListDataItem.getDeclType())
							&& ((ingListDataItem.getDepthLevel() == null) || (ingListDataItem.getDepthLevel() == 1))) {
						total += ingListDataItem.getQtyPerc();
					}

				}

				// Due to double precision
				if (Math.abs(total - 100d) > 0.00001) {
					addReqCtrl(reqCtrlMap, new NodeRef(RepoConsts.SPACES_STORE, "incorrect-inglist-total"), RequirementType.Tolerated,
							MLTextHelper.getI18NMessage(MESSAGE_INCORRECT_INGLIST_TOTAL), componentProductData.getNodeRef(),
							RequirementDataType.Ingredient);
				}

			}
		}

		// calculate ingList of formulated product
		calculateILOfPart(formulatedProduct, componentProductData, compoListDataItem,
				CompositeHelper.getHierarchicalCompoList(
						IngListHelper.extractParentList(componentProductData.getIngList(), associationService, alfrescoRepository)),
				formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, null, formulatedProduct.isGeneric());
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param formulatedProduct
	 * @param componentProductData
	 * @param compoListDataItem
	 * @param compositeIngList
	 * @param ingList
	 * @param retainNodes
	 * @param totalQtyIngMap
	 * @param parentIngListDataItem
	 * @param isGeneric
	 */
	private void calculateILOfPart(ProductData formulatedProduct, ProductData componentProductData, CompoListDataItem compoListDataItem,
			Composite<IngListDataItem> compositeIngList, List<IngListDataItem> ingList, List<IngListDataItem> retainNodes,
			Map<String, IngListDataItem> totalQtyIngMap, IngListDataItem parentIngListDataItem, boolean isGeneric) {

		// OMIT is not taken in account
		if (compoListDataItem.getDeclType() == DeclarationType.Omit) {
			return;
		}

		for (Composite<IngListDataItem> component : compositeIngList.getChildren()) {

			IngListDataItem ingListDataItem = component.getData();

			if (!DeclarationType.Omit.equals(ingListDataItem.getDeclType())) {

				IngListDataItem newIngListDataItem = findOrCreateIngListDataItem(ingList, ingListDataItem, parentIngListDataItem);

				if (!retainNodes.contains(newIngListDataItem)) {
					retainNodes.add(newIngListDataItem);
					newIngListDataItem.getClaims().addAll(ingListDataItem.getClaims());
				} else {
					newIngListDataItem.getClaims().retainAll(ingListDataItem.getClaims());
				}

				IngListDataItem totalIng = totalQtyIngMap.computeIfAbsent(newIngListDataItem.getName(), k -> new IngListDataItem());

				Double totalQtyIngWithYield = totalIng.getQtyPercWithYield();

				Double volumeQty = totalIng.getVolumeQtyPerc();

				// Calculate qty
				Double qty = FormulationHelper.getQtyInKg(compoListDataItem);
				Double qtyIngWithYield = ingListDataItem.getQtyPercWithYield();
				if ((qtyIngWithYield == null) || componentProductData.isGeneric()) {
					qtyIngWithYield = ingListDataItem.getQtyPerc();
				}

				if (qty != null) {

					Double yieldFactor = FormulationHelper.getYield(compoListDataItem) / 100d;
					updateQty(qty, ingListDataItem.getQtyPerc(), totalIng::getQtyPerc, totalIng::setQtyPerc, yieldFactor);
					updateQty(qty, ingListDataItem.getQtyPerc1(), totalIng::getQtyPerc1, totalIng::setQtyPerc1, yieldFactor);
					updateQty(qty, ingListDataItem.getQtyPerc2(), totalIng::getQtyPerc2, totalIng::setQtyPerc2, yieldFactor);
					updateQty(qty, ingListDataItem.getQtyPerc5(), totalIng::getQtyPerc5, totalIng::setQtyPerc5, yieldFactor);
					updateMinMaxQty(qty, ingListDataItem.getQtyPerc3(), totalIng::getQtyPerc3, totalIng::setQtyPerc3, isGeneric, true);
					updateMinMaxQty(qty, ingListDataItem.getQtyPerc4(), totalIng::getQtyPerc4, totalIng::setQtyPerc4, isGeneric, true);

					if ((qtyIngWithYield != null)) {

						double valueToAdd = qty * qtyIngWithYield;

						if (totalQtyIngWithYield == null) {
							totalQtyIngWithYield = 0d;
						}

						if ((FormulationHelper.getYield(compoListDataItem) != null) && (nodeService.hasAspect(ingListDataItem.getIng(),
								PLMModel.ASPECT_WATER)
								|| ((nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE) != null)
										&& ((Double) nodeService.getProperty(ingListDataItem.getIng(), PLMModel.PROP_EVAPORATED_RATE) == 100d)))) {

							valueToAdd = qty * ((qtyIngWithYield) - (100d - FormulationHelper.getYield(compoListDataItem)));
						}

						totalQtyIngWithYield += valueToAdd;
						totalIng.setQtyPercWithYield(totalQtyIngWithYield);

					}

					updateMinMaxQty(qty, ingListDataItem.getMini(), totalIng::getMini, totalIng::setMini, isGeneric, false);
					updateMinMaxQty(qty, ingListDataItem.getMaxi(), totalIng::getMaxi, totalIng::setMaxi, isGeneric, true);

					if ((ingListDataItem.getVolumeQtyPerc() != null) && (compoListDataItem.getVolume() != null)) {

						if (volumeQty == null) {
							volumeQty = 0d;
						}
						totalIng.setVolumeQtyPerc(volumeQty + ((ingListDataItem.getVolumeQtyPerc() * compoListDataItem.getVolume()) / 100));
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
							newIngListDataItem, isGeneric);
				}
			}
		}
	}

	private void updateQty(Double qty, Double qtyIng, Supplier<Double> getTotalQty, Consumer<Double> setTotalQty, Double factor) {
		if (qtyIng != null) {
			Double totalQty = getTotalQty.get();
			if (totalQty == null) {
				totalQty = 0d;
			}
			totalQty += qty * qtyIng * factor;
			setTotalQty.accept(totalQty);
		}
	}

	private void updateMinMaxQty(Double qty, Double qtyIng, Supplier<Double> getTotalQty, Consumer<Double> setTotalQty, boolean isGeneric,
			boolean isMax) {
		if (qtyIng != null) {
			Double totalQty = getTotalQty.get();
			if (isGeneric) {
				if ((totalQty == null) || (isMax ? qtyIng > totalQty : qtyIng < totalQty)) {
					totalQty = qtyIng;
				}
			} else {
				if (totalQty == null) {
					totalQty = 0d;
				}
				totalQty += qty * qtyIng;
			}
			setTotalQty.accept(totalQty);
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
						if ((parentIngListDataItem != null) && (p != null)) {
							parentIngListDataItem = parentIngListDataItem.getParent();
							p = p.getParent();
						}
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

	private IngListDataItem findOrCreateIngListDataItem(List<IngListDataItem> ingList, IngListDataItem ingListDataItem,
			IngListDataItem parentIngListDataItem) {
		IngListDataItem newIngListDataItem = findIngListDataItem(ingList, ingListDataItem);
		if (newIngListDataItem == null) {
			newIngListDataItem = new IngListDataItem();
			newIngListDataItem.setName(GUID.generate());
			newIngListDataItem.setIng(ingListDataItem.getIng());
			newIngListDataItem.setParent(parentIngListDataItem);
			newIngListDataItem.setDepthLevel(parentIngListDataItem == null ? 1 : parentIngListDataItem.getDepthLevel() + 1);
			newIngListDataItem.setIsProcessingAid(true);
			newIngListDataItem.setIsSupport(true);
			ingList.add(newIngListDataItem);
		}
		newIngListDataItem.setSort(ingListDataItem.getSort());
		return newIngListDataItem;
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
			MutableInt index = new MutableInt(1);

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
