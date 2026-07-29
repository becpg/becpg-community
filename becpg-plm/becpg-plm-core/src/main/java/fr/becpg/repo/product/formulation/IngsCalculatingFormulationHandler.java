/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
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

/**
 * The Class IngsCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class IngsCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** The Constant NO_GRP. */
	public static final String NO_GRP = "-";

	/** Constant <code>MESSAGE_MISSING_INGLIST="message.formulate.missing.ingList"</code> */
	private static final String MESSAGE_MISSING_INGLIST = "message.formulate.missing.ingList";
	/** Constant <code>MESSAGE_INCORRECT_INGLIST_TOTAL="message.formulate.incorrect.ingList.tot"{trunked}</code> */
	private static final String MESSAGE_INCORRECT_INGLIST_TOTAL = "message.formulate.incorrect.ingList.total";

	/** Constant <code>logger</code> */
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
							// Reconstitution is re-derived from the variants on each formulation (generic products)
							il.setReconstitutionRate(null);
							il.setReconstitutionPriority(null);
							il.setDiluentRef(null);
							il.setTargetReconstitutionRef(null);
							if (il.getAspects() != null) {
								il.getAspects().remove(PLMModel.ASPECT_RECONSTITUTABLE);
							}
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

	/**
	 * <p>accept.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean accept(ProductData formulatedProduct) {
		return !Boolean.TRUE.equals(formulatedProduct.getIsIngListManual())
				&& formulatedProduct.hasCompoListEl(FormulationFilters.EFFECTIVE_VARIANT_COMPO)
				&& (alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLIST)
						|| alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_INGLABELINGLIST));
	}

	/**
	 * Calculate the ingredient list of a product.
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param reqCtrlMap a {@link java.util.Map} object
	 */
	private void calculateIL(ProductData formulatedProduct, Map<NodeRef, RequirementListDataItem> reqCtrlMap) {

		List<CompoListDataItem> compoList = formulatedProduct.getCompoList(FormulationFilters.EFFECTIVE_VARIANT_COMPO);

		Map<String, IngListDataItem> totalQtyIngMap = new HashMap<>();
		Map<String, IngListDataItem> totalQtyOmittedIngMap = new HashMap<>();

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

						if (!shouldOmit(componentProductData)) {

							visitILOfPart(formulatedProduct, compoItem, componentProductData, retainNodes, totalQtyIngMap, totalQtyOmittedIngMap,
									reqCtrlMap, visited);

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
		}

		formulatedProduct.getIngList().retainAll(retainNodes);

		if (totalQtyUsedWithYield != 0d) {

			Set<EvaporatedDataItem> evaporatedDataItems = new HashSet<>();

			Set<IngListDataItem> supersededEvaporation = collectSupersededEvaporation(formulatedProduct.getIngList());

			for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

				IngListDataItem totalQtyIng = totalQtyIngMap.get(ingListDataItem.getName());
				IngListDataItem totalQtyOmittedIng = totalQtyOmittedIngMap.get(ingListDataItem.getName());

				// Use omitted ingredient totals if ingredient is omitted, otherwise use non-omitted totals
				IngListDataItem totalToUse = DeclarationType.Omit.equals(ingListDataItem.getDeclType()) ? totalQtyOmittedIng : totalQtyIng;

				Double totalQty = totalToUse != null ? totalToUse.getQtyPerc() : null;
				Double totalQty1 = totalToUse != null ? totalToUse.getQtyPerc1() : null;
				Double totalQty2 = totalToUse != null ? totalToUse.getQtyPerc2() : null;
				Double totalQty3 = totalToUse != null ? totalToUse.getQtyPerc3() : null;
				Double totalQty4 = totalToUse != null ? totalToUse.getQtyPerc4() : null;
				Double totalQty5 = totalToUse != null ? totalToUse.getQtyPerc5() : null;
				Double totalQtyMini = totalToUse != null ? totalToUse.getMini() : null;
				Double totalQtyMaxi = totalToUse != null ? totalToUse.getMaxi() : null;
				Double totalQtyIngWithYield = totalToUse != null ? totalToUse.getQtyPercWithYield() : null;
				Double totalVol = totalToUse != null ? totalToUse.getVolumeQtyPerc() : null;

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

					if (hasEvaporationData(ingListDataItem) && !supersededEvaporation.contains(ingListDataItem)) {
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
						if (hasEvaporationData(ingListDataItem) && !supersededEvaporation.contains(ingListDataItem)) {
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

			boolean fixCompositeYield = !formulatedProduct.isGeneric() && !evaporatedDataItems.isEmpty()
					&& formulatedProduct.getIngList().stream().anyMatch(item -> item.getParent() != null);

			if (fixCompositeYield) {
				clampNegativeQtyPercWithYield(formulatedProduct.getIngList());
			}

			aggregateParentQtyPercWithYield(formulatedProduct.getIngList());

			if (fixCompositeYield) {
				normalizeQtyPercWithYield(formulatedProduct.getIngList());
			}

		}

		// sort collection
		if (shouldSort) {
			sortIL(formulatedProduct.getIngList());
		}
	}

	/**
	 * Collects the composite ingredients whose own evaporation rate is superseded by one of their
	 * sub-ingredients.
	 * <p>
	 * The same ingredient may legitimately be used both as a simple component and as a composite one,
	 * so an evaporation rate is often set on a parent and on one of its sub-ingredients at the same
	 * time. Both would then compete for the evaporation budget while the parent quantity already
	 * includes the child one, counting the same water twice. When a sub-ingredient declares an
	 * evaporation rate, it describes the evaporation precisely, so the parent rate is ignored (see
	 * #34702).
	 *
	 * @param ingList the formulated product ingredient list
	 * @return the items whose evaporation rate must not be applied
	 */
	private Set<IngListDataItem> collectSupersededEvaporation(List<IngListDataItem> ingList) {

		Set<IngListDataItem> superseded = Collections.newSetFromMap(new IdentityHashMap<>());

		for (IngListDataItem item : ingList) {
			if ((item.getParent() == null) || (item.getIng() == null) || !hasEvaporationData(item)) {
				continue;
			}
			IngListDataItem parent = item.getParent();
			int depth = 0;
			while ((parent != null) && (depth < 256)) {
				if ((parent.getIng() != null) && hasEvaporationData(parent)) {
					superseded.add(parent);
				}
				parent = parent.getParent();
				depth++;
			}
		}

		if (logger.isDebugEnabled() && !superseded.isEmpty()) {
			for (IngListDataItem item : superseded) {
				logger.debug("Ignoring evaporation rate of composite ingredient detailed by an evaporating sub-ingredient: " + item.getName());
			}
		}

		return superseded;
	}

	/**
	 * Recomputes each parent (composite) ingredient "with yield" percentages as the sum of its
	 * direct children percentages, when the children fully declare their parent.
	 * <p>
	 * A composite ingredient is, by definition, the sum of its sub-ingredients. When an evaporation
	 * rate is present on a sub-ingredient, the leaf percentages already reflect the lost water,
	 * whereas the parent percentage was re-concentrated independently by the product yield, producing
	 * top-level sums above 100 %. Aggregating the children back into the parent restores the invariant
	 * for both the primary and secondary yield percentages. The computation runs bottom-up so
	 * multi-level hierarchies are resolved consistently. Items without children are left untouched.
	 * <p>
	 * The aggregation only applies when the sub-ingredients cover the whole parent, i.e. their
	 * {@code qtyPerc} sum matches the parent one (see {@link #isFullyDeclaredBySubIngredients(IngListDataItem, List)}).
	 * A partially declared composite keeps its own computed percentages: summing incomplete children
	 * would under-evaluate it, and summing children without percentages at all would empty the column
	 * (see #34702).
	 *
	 * @param ingList the formulated product ingredient list
	 */
	private void aggregateParentQtyPercWithYield(List<IngListDataItem> ingList) {

		Map<IngListDataItem, List<IngListDataItem>> childrenByParent = new IdentityHashMap<>();
		for (IngListDataItem item : ingList) {
			if (item.getParent() != null) {
				childrenByParent.computeIfAbsent(item.getParent(), parent -> new ArrayList<>()).add(item);
			}
		}

		if (childrenByParent.isEmpty()) {
			return;
		}

		List<IngListDataItem> parents = new ArrayList<>(childrenByParent.keySet());
		parents.sort(Comparator.comparingInt(this::getIngDepth).reversed());

		for (IngListDataItem parent : parents) {
			List<IngListDataItem> children = childrenByParent.get(parent);
			if (!isFullyDeclaredBySubIngredients(parent, children)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Partially declared composite ingredient, keeping its own with-yield percentages: " + parent.getName());
				}
				continue;
			}
			aggregateChildren(parent, children, IngListDataItem::getQtyPercWithYield, IngListDataItem::setQtyPercWithYield);
			aggregateChildren(parent, children, IngListDataItem::getQtyPercWithSecondaryYield, IngListDataItem::setQtyPercWithSecondaryYield);
		}
	}

	/**
	 * Tells whether the sub-ingredients of a composite ingredient cover its whole quantity.
	 * <p>
	 * Only a fully declared composite is equal to the sum of its sub-ingredients. Sub-ingredients are
	 * frequently declared partially — a few known components of a raw material, or components without
	 * any percentage — in which case the parent percentages must be kept as computed. A parent without
	 * percentage, or declared at 0 % (label-only wrapper whose children carry the real percentages),
	 * can never be covered by its children.
	 *
	 * @param parent the composite ingredient
	 * @param children its direct sub-ingredients
	 * @return true when the children percentages sum up to the parent percentage
	 */
	private boolean isFullyDeclaredBySubIngredients(IngListDataItem parent, List<IngListDataItem> children) {

		Double parentQtyPerc = parent.getQtyPerc();
		if ((parentQtyPerc == null) || (parentQtyPerc <= 0d)) {
			return false;
		}

		double childrenQtyPerc = 0d;
		for (IngListDataItem child : children) {
			if (child.getQtyPerc() == null) {
				return false;
			}
			childrenQtyPerc += child.getQtyPerc();
		}

		return Math.abs(childrenQtyPerc - parentQtyPerc) <= Math.max(0.001d, parentQtyPerc / 100d);
	}

	/**
	 * Sets a parent "with yield" percentage to the sum of its children, leaving it untouched when at
	 * least one child has no value for that column.
	 *
	 * @param parent the composite ingredient
	 * @param children its direct sub-ingredients
	 * @param getter the accessor of the column to aggregate
	 * @param setter the mutator of the column to aggregate
	 */
	private void aggregateChildren(IngListDataItem parent, List<IngListDataItem> children, Function<IngListDataItem, Double> getter,
			BiConsumer<IngListDataItem, Double> setter) {

		double sum = 0d;
		for (IngListDataItem child : children) {
			Double value = getter.apply(child);
			if (value == null) {
				return;
			}
			sum += value;
		}

		setter.accept(parent, sum);
	}

	/**
	 * Clamps any negative "with yield" percentage to zero.
	 * <p>
	 * When the product yield implies more evaporation than the evaporating ingredients can absorb,
	 * the leftover-evaporation fallback subtracts the unapplied amount from the first fully
	 * evaporating ingredient, which can drive its percentage below zero. A mass fraction can never
	 * be negative, so it is floored at zero; the resulting deviation from 100 % is then corrected by
	 * {@link #normalizeQtyPercWithYield(List)}.
	 *
	 * @param ingList the formulated product ingredient list
	 */
	private void clampNegativeQtyPercWithYield(List<IngListDataItem> ingList) {
		for (IngListDataItem item : ingList) {
			if ((item.getQtyPercWithYield() != null) && (item.getQtyPercWithYield() < 0d)) {
				item.setQtyPercWithYield(0d);
			}
			if ((item.getQtyPercWithSecondaryYield() != null) && (item.getQtyPercWithSecondaryYield() < 0d)) {
				item.setQtyPercWithSecondaryYield(0d);
			}
		}
	}

	/**
	 * Restores the "sum of top-level percentages = 100 %" invariant on the "with yield" columns.
	 * <p>
	 * The top-level "with yield" percentages are mass fractions of the finished product and must
	 * therefore sum to 100 %, exactly like the plain {@code qtyPerc} column. When a sub-ingredient
	 * carries an evaporation rate but the evaporating ingredients cannot absorb the whole product
	 * yield loss (net quantity implying more evaporation than the rates account for), part of the
	 * evaporation budget stays unapplied and the items remain over-concentrated, producing a sum
	 * above 100 % (see #34702). Rescaling every "with yield" value by {@code 100 / topLevelSum} is
	 * equivalent to concentrating the non-evaporating ingredients by the effective yield (the
	 * evaporation actually applied) instead of the raw net/gross yield, and preserves the
	 * parent = sum-of-children invariant since parents and children are scaled by the same factor.
	 * <p>
	 * The correction is only applied when the list is a complete 100 % list (its {@code qtyPerc}
	 * top-level sum is ~100 %), so partial lists (omitted ingredients, etc.) are left untouched.
	 *
	 * @param ingList the formulated product ingredient list
	 */
	private void normalizeQtyPercWithYield(List<IngListDataItem> ingList) {

		double topLevelQtyPerc = 0d;
		for (IngListDataItem item : ingList) {
			if ((item.getParent() == null) && (item.getQtyPerc() != null)) {
				topLevelQtyPerc += item.getQtyPerc();
			}
		}

		if (Math.abs(topLevelQtyPerc - 100d) > 1d) {
			return;
		}

		rescaleToHundred(ingList, IngListDataItem::getQtyPercWithYield, IngListDataItem::setQtyPercWithYield);
		rescaleToHundred(ingList, IngListDataItem::getQtyPercWithSecondaryYield, IngListDataItem::setQtyPercWithSecondaryYield);
	}

	/**
	 * Rescales a "with yield" percentage column so that the top-level values sum to 100 %.
	 *
	 * @param ingList the ingredient list
	 * @param getter the accessor of the column to rescale
	 * @param setter the mutator of the column to rescale
	 */
	private void rescaleToHundred(List<IngListDataItem> ingList, Function<IngListDataItem, Double> getter,
			BiConsumer<IngListDataItem, Double> setter) {

		double topLevelSum = 0d;
		for (IngListDataItem item : ingList) {
			if (item.getParent() == null) {
				Double value = getter.apply(item);
				if (value != null) {
					topLevelSum += value;
				}
			}
		}

		if (topLevelSum <= 0.000001d) {
			return;
		}

		double factor = 100d / topLevelSum;
		if (Math.abs(factor - 1d) < 0.0001d) {
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Normalizing ingList with-yield column: top-level sum=" + topLevelSum + " factor=" + factor);
		}

		for (IngListDataItem item : ingList) {
			Double value = getter.apply(item);
			if (value != null) {
				setter.accept(item, value * factor);
			}
		}
	}

	/**
	 * Returns the depth of an ingredient in the ingredient hierarchy, walking up the parent chain
	 * when the stored depth level is not available.
	 *
	 * @param ingListDataItem the ingredient item
	 * @return the depth (1 for a top-level ingredient)
	 */
	private int getIngDepth(IngListDataItem ingListDataItem) {
		if (ingListDataItem.getDepthLevel() != null) {
			return ingListDataItem.getDepthLevel();
		}
		int depth = 1;
		IngListDataItem parent = ingListDataItem.getParent();
		while ((parent != null) && (depth < 256)) {
			depth++;
			parent = parent.getParent();
		}
		return depth;
	}

	/**
	 * <p>shouldOmit.</p>
	 *
	 * @param componentProductData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean shouldOmit(ProductData componentProductData) {
		boolean shouldOmit = false;
		if (componentProductData.getIngList() != null && !componentProductData.getIngList().isEmpty()) {
			shouldOmit = true;
			for (IngListDataItem ingListItem : componentProductData.getIngList()) {
				DeclarationType ingDeclarationType = ingListItem.getDeclType();

				if (!DeclarationType.Omit.equals(ingDeclarationType)) {
					shouldOmit = false;
					break;
				}
			}
		}
		return shouldOmit;
	}

	/**
	 * <p>hasEvaporationData.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a boolean
	 */
	private boolean hasEvaporationData(IngListDataItem ingListDataItem) {
		return EvaporatingFormulationHelper.hasEvaporationData(ingListDataItem.getIng(), nodeService);
	}

	/**
	 * <p>getEvaporateRate.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double getEvaporateRate(IngListDataItem ingListDataItem) {
		return EvaporatingFormulationHelper.getEvaporateRate(ingListDataItem.getIng(), nodeService);
	}

	/**
	 * <p>applyEvaporation.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param evaporatedDataItems a {@link java.util.Set} object
	 */
	private void applyEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems) {
		applyEvaporation(formulatedProduct, evaporatedDataItems, formulatedProduct.getYield(), IngListDataItem::getQtyPercWithYield,
				IngListDataItem::setQtyPercWithYield);
	}

	/**
	 * <p>applySecondaryEvaporation.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param evaporatedDataItems a {@link java.util.Set} object
	 */
	private void applySecondaryEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems) {
		applyEvaporation(formulatedProduct, evaporatedDataItems, formulatedProduct.getSecondaryYield(), IngListDataItem::getQtyPercWithSecondaryYield,
				IngListDataItem::setQtyPercWithSecondaryYield);
	}

	/**
	 * <p>applyEvaporation.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param evaporatedDataItems a {@link java.util.Set} object
	 * @param currYield a {@link java.lang.Double} object
	 * @param getQtyPercWithYield a {@link java.util.function.Function} object
	 * @param setQtyPercWithYield a {@link java.util.function.BiConsumer} object
	 */
	private void applyEvaporation(ProductData formulatedProduct, Set<EvaporatedDataItem> evaporatedDataItems, Double currYield,
			Function<IngListDataItem, Double> getQtyPercWithYield, BiConsumer<IngListDataItem, Double> setQtyPercWithYield) {
		if ((currYield != null) && (currYield != 0d)) {
			double yieldFactor = currYield / 100d;
			Double evaporatingQty = 100d - currYield;

			if (!evaporatedDataItems.isEmpty() && (evaporatingQty > 0d)) {

				// Use EvaporatingFormulationHelper for evaporation processing
				Function<NodeRef, IngListDataItem> matchItem = nodeRef -> formulatedProduct.getIngList().stream()
						.filter(i -> (i != null) && (i.getIng() != null) && i.getIng().equals(nodeRef)).findFirst().orElse(null);

				Function<IngListDataItem, String> getItemName = IngListDataItem::getName;

				EvaporatingFormulationHelper.applyEvaporation(evaporatingQty, evaporatedDataItems, getQtyPercWithYield, setQtyPercWithYield,
						matchItem, getItemName, null);

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
	 * <p>addReqCtrl.</p>
	 *
	 * @param reqCtrlMap a {@link java.util.Map} object
	 * @param reqNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param requirementType a {@link fr.becpg.repo.regulatory.RequirementType} object
	 * @param message a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param requirementDataType a {@link fr.becpg.repo.regulatory.RequirementDataType} object
	 */
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
	 * @param totalQtyOmittedIngMap a {@link java.util.Map} object
	 */
	private void visitILOfPart(ProductData formulatedProduct, CompoListDataItem compoListDataItem, ProductData componentProductData,
			List<IngListDataItem> retainNodes, Map<String, IngListDataItem> totalQtyIngMap, Map<String, IngListDataItem> totalQtyOmittedIngMap,
			Map<NodeRef, RequirementListDataItem> reqCtrlMap, Set<NodeRef> visited) {

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
					&& !DeclarationType.Omit.equals(compoListDataItem.getDeclType())) && !shouldOmit(componentProductData)) {

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
		List<IngListDataItem> componentIngList = componentProductData.getIngList();
		if ((componentIngList != null) && !componentProductData.hasCompoListEl()) {
			componentIngList = IngListHelper.scaleRelativeDepthChildren(componentIngList);
		}
		calculateILOfPart(formulatedProduct, componentProductData, compoListDataItem,
				CompositeHelper.getHierarchicalCompoList(
						IngListHelper.extractParentList(componentIngList, associationService, alfrescoRepository)),
				formulatedProduct.getIngList(), retainNodes, totalQtyIngMap, totalQtyOmittedIngMap, null, formulatedProduct.isGeneric());
	}

	/**
	 * Add the ingredients of the part in the ingredient list.
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param componentProductData a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param compositeIngList a {@link fr.becpg.repo.data.hierarchicalList.Composite} object
	 * @param ingList a {@link java.util.List} object
	 * @param retainNodes a {@link java.util.List} object
	 * @param totalQtyIngMap a {@link java.util.Map} object
	 * @param parentIngListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param isGeneric a boolean
	 * @param totalQtyOmittedIngMap a {@link java.util.Map} object
	 */
	private void calculateILOfPart(ProductData formulatedProduct, ProductData componentProductData, CompoListDataItem compoListDataItem,
			Composite<IngListDataItem> compositeIngList, List<IngListDataItem> ingList, List<IngListDataItem> retainNodes,
			Map<String, IngListDataItem> totalQtyIngMap, Map<String, IngListDataItem> totalQtyOmittedIngMap, IngListDataItem parentIngListDataItem,
			boolean isGeneric) {

		// OMIT is not taken in account
		if (compoListDataItem.getDeclType() == DeclarationType.Omit || shouldOmit(componentProductData)) {
			return;
		}

		for (Composite<IngListDataItem> component : compositeIngList.getChildren()) {

			IngListDataItem ingListDataItem = component.getData();

			IngListDataItem newIngListDataItem = findOrCreateIngListDataItem(ingList, ingListDataItem, parentIngListDataItem);

			boolean isOmit = DeclarationType.Omit.equals(ingListDataItem.getDeclType());

			if (!retainNodes.contains(newIngListDataItem)) {
				retainNodes.add(newIngListDataItem);
				if (isOmit) {
					newIngListDataItem.setDeclType(DeclarationType.Omit);
				}
				newIngListDataItem.getClaims().addAll(ingListDataItem.getClaims());
			} else {
				newIngListDataItem.getClaims().retainAll(ingListDataItem.getClaims());
			}

			if (!isOmit) {
				if (DeclarationType.Omit.equals(newIngListDataItem.getDeclType())) {
					newIngListDataItem.setDeclType(DeclarationType.Detail);
					IngListDataItem omittedIng = totalQtyOmittedIngMap.remove(newIngListDataItem.getName());
					if (omittedIng != null) {
						IngListDataItem target = totalQtyIngMap.computeIfAbsent(newIngListDataItem.getName(), k -> new IngListDataItem());
						mergeIngListDataItem(target, omittedIng);
					}
				} else {
					newIngListDataItem.setDeclType(ingListDataItem.getDeclType());
				}
			}

			IngListDataItem totalIng = DeclarationType.Omit.equals(newIngListDataItem.getDeclType())
					? totalQtyOmittedIngMap.computeIfAbsent(newIngListDataItem.getName(), k -> new IngListDataItem())
					: totalQtyIngMap.computeIfAbsent(newIngListDataItem.getName(), k -> new IngListDataItem());

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

			// For generic products, propagate the reconstitution defined on the variants, keeping the highest rate
			if (isGeneric) {
				propagateReconstitution(newIngListDataItem, ingListDataItem);
			}

			// recursive
			if (!component.isLeaf()) {
				calculateILOfPart(formulatedProduct, componentProductData, compoListDataItem, component, ingList, retainNodes, totalQtyIngMap,
						totalQtyOmittedIngMap, newIngListDataItem, isGeneric);
			}

		}
	}

	/**
	 * <p>updateQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @param qtyIng a {@link java.lang.Double} object
	 * @param getTotalQty a {@link java.util.function.Supplier} object
	 * @param setTotalQty a {@link java.util.function.Consumer} object
	 * @param factor a {@link java.lang.Double} object
	 */
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

	/**
	 * <p>updateMinMaxQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @param qtyIng a {@link java.lang.Double} object
	 * @param getTotalQty a {@link java.util.function.Supplier} object
	 * @param setTotalQty a {@link java.util.function.Consumer} object
	 * @param isGeneric a boolean
	 * @param isMax a boolean
	 */
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

	/**
	 * <p>findIngListDataItem.</p>
	 *
	 * @param ingLists a {@link java.util.List} object
	 * @param ingList a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
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

	/**
	 * <p>findOrCreateIngListDataItem.</p>
	 *
	 * @param ingList a {@link java.util.List} object
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param parentIngListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
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
	 * @param ingList a {@link java.util.List} object
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

	/**
	 * <p>processItem.</p>
	 *
	 * @param item a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param byParent a {@link java.util.Map} object
	 * @param sortedList a {@link java.util.List} object
	 */
	private void processItem(IngListDataItem item, Map<IngListDataItem, List<IngListDataItem>> byParent, List<IngListDataItem> sortedList) {
		sortedList.add(item);
		sorted(byParent.getOrDefault(item, Collections.emptyList())).forEach(child -> processItem(child, byParent, sortedList));
	}

	/**
	 * <p>sorted.</p>
	 *
	 * @param items a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	private List<IngListDataItem> sorted(List<IngListDataItem> items) {
		return items.stream().sorted(Comparator.comparing(IngListDataItem::getQtyPerc, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Comparator.comparing(this::getLegalName))).toList();
	}

	/**
	 * <p>getLegalName.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	private String getLegalName(IngListDataItem ingListDataItem) {

		if (ingListDataItem.getIng() != null) {
			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
			return ingItem.getLegalName(Locale.getDefault());
		}
		return ingListDataItem.getName();
	}

	/**
	 * Propagates the reconstitution definition of a variant ingredient onto the aggregated generic
	 * ingredient. When several variants define a reconstitution for the same ingredient, the highest
	 * rate is retained along with its diluent, target and priority.
	 *
	 * @param target the aggregated generic ingredient
	 * @param source the variant ingredient
	 */
	private void propagateReconstitution(IngListDataItem target, IngListDataItem source) {
		Double sourceRate = source.getReconstitutionRate();
		if ((sourceRate == null) || (source.getAspects() == null) || !source.getAspects().contains(PLMModel.ASPECT_RECONSTITUTABLE)) {
			return;
		}

		Double targetRate = target.getReconstitutionRate();
		if ((targetRate == null) || (sourceRate > targetRate)) {
			target.setReconstitutionRate(sourceRate);
			target.setReconstitutionPriority(source.getReconstitutionPriority());
			target.setDiluentRef(source.getDiluentRef());
			target.setTargetReconstitutionRef(source.getTargetReconstitutionRef());
			if (!target.getAspects().contains(PLMModel.ASPECT_RECONSTITUTABLE)) {
				target.getAspects().add(PLMModel.ASPECT_RECONSTITUTABLE);
			}
		}
	}

	/**
	 * <p>mergeIngListDataItem.</p>
	 *
	 * @param target a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param source a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 */
	private void mergeIngListDataItem(IngListDataItem target, IngListDataItem source) {
		target.setQtyPerc(sum(target.getQtyPerc(), source.getQtyPerc()));
		target.setQtyPerc1(sum(target.getQtyPerc1(), source.getQtyPerc1()));
		target.setQtyPerc2(sum(target.getQtyPerc2(), source.getQtyPerc2()));
		target.setQtyPerc3(sum(target.getQtyPerc3(), source.getQtyPerc3()));
		target.setQtyPerc4(sum(target.getQtyPerc4(), source.getQtyPerc4()));
		target.setQtyPerc5(sum(target.getQtyPerc5(), source.getQtyPerc5()));
		target.setMini(sum(target.getMini(), source.getMini()));
		target.setMaxi(sum(target.getMaxi(), source.getMaxi()));
		target.setQtyPercWithYield(sum(target.getQtyPercWithYield(), source.getQtyPercWithYield()));
		target.setVolumeQtyPerc(sum(target.getVolumeQtyPerc(), source.getVolumeQtyPerc()));
	}

	/**
	 * <p>sum.</p>
	 *
	 * @param d1 a {@link java.lang.Double} object
	 * @param d2 a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double sum(Double d1, Double d2) {
		if ((d1 == null) && (d2 == null)) {
			return null;
		}
		if (d1 == null) {
			return d2;
		}
		if (d2 == null) {
			return d1;
		}
		return d1 + d2;
	}

}
