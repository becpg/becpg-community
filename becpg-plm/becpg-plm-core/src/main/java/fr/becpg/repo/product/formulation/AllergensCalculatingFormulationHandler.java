/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.allergen.AllergenItem;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.requirement.AllergenRequirementScanner;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * The Class AllergensCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AllergensCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	/** Constant <code>MESSAGE_FORBIDDEN_ALLERGEN="message.formulate.allergen.forbidden"</code> */
	public static final String MESSAGE_FORBIDDEN_ALLERGEN = "message.formulate.allergen.forbidden";

	/** Constant <code>MESSAGE_NOT_VALIDATED_ALLERGEN="message.formulate.allergen.notValidated"</code> */
	public static final String MESSAGE_NOT_VALIDATED_ALLERGEN = "message.formulate.allergen.notValidated";

	/** Constant <code>MESSAGE_NULL_PERC="message.formulate.allergen.error.nullQt"{trunked}</code> */
	public static final String MESSAGE_NULL_PERC = "message.formulate.allergen.error.nullQtyPerc";

	/** Constant <code>MESSAGE_EMPTY_ALLERGEN="message.formulate.allergen.error.empty"</code> */
	public static final String MESSAGE_EMPTY_ALLERGEN = "message.formulate.allergen.error.empty";

	public static final String CONF_ALLERGEN_SORT_BY_PARENT = "beCPG.formulation.allergenList.sortByParent";

	private static final Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	protected NodeService nodeService;

	protected NodeService mlNodeService;

	private AllergenRequirementScanner allergenRequirementScanner;

	private SystemConfigurationService systemConfigurationService;

	/**
	 * <p>Setter for the field <code>allergenRequirementScanner</code>.</p>
	 *
	 * @param allergenRequirementScanner a {@link fr.becpg.repo.product.requirement.AllergenRequirementScanner} object.
	 */
	public void setAllergenRequirementScanner(AllergenRequirementScanner allergenRequirementScanner) {
		this.allergenRequirementScanner = allergenRequirementScanner;
	}

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object.
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Start AllergensCalculatingVisitor");

			if (formulatedProduct.getAllergenList() == null) {
				formulatedProduct.setAllergenList(new ArrayList<>());
			}

			List<AllergenListDataItem> retainNodes = new ArrayList<>();
			Map<String, RequirementListDataItem> errors = new HashMap<>();
			Map<String, RequirementListDataItem> rclCtrlMap = new HashMap<>();

			// compoList
			Double netQty = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			if (formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				for (CompoListDataItem compoItem : formulatedProduct.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					if (!DeclarationType.Omit.equals(compoItem.getDeclType())) {

						NodeRef part = compoItem.getProduct();

						ProductData partProduct = (ProductData) alfrescoRepository.findOne(part);

						Double qtyUsed = FormulationHelper.getQtyInKg(compoItem);

						if ((qtyUsed != null) && (qtyUsed > 0)) {
							if (!(partProduct instanceof LocalSemiFinishedProductData)) {
								visitPart(compoItem, partProduct, formulatedProduct, retainNodes, qtyUsed, netQty, errors).forEach(error -> {
									if (!rclCtrlMap.containsKey(error.getKey())) {
										rclCtrlMap.put(error.getKey(), error);
									}
								});

								if ((partProduct.isRawMaterial()) && !SystemState.Valid.equals(partProduct.getState())) {

									if ((partProduct.getAllergenList() == null) || partProduct.getAllergenList().isEmpty()
											|| (partProduct.getAllergenList().get(0).getParentNodeRef() == null)
											|| !SystemState.Valid.toString().equals(nodeService.getProperty(
													partProduct.getAllergenList().get(0).getParentNodeRef(), BeCPGModel.PROP_ENTITYLIST_STATE))) {

										String message = I18NUtil.getMessage(MESSAGE_NOT_VALIDATED_ALLERGEN);
										RequirementListDataItem error = rclCtrlMap.get(message);

										List<NodeRef> sourceNodeRefs = new ArrayList<>();
										if (error == null) {
											error = RequirementListDataItem.tolerated().ofDataType(RequirementDataType.Allergen)
													.withMessage(MLTextHelper.getI18NMessage(MESSAGE_NOT_VALIDATED_ALLERGEN))
													.withSources(sourceNodeRefs);
										} else {
											sourceNodeRefs = error.getSources();
										}

										if (!sourceNodeRefs.contains(partProduct.getNodeRef())) {
											sourceNodeRefs.add(partProduct.getNodeRef());
										}

										rclCtrlMap.put(message, error);
									}

								}

							}
						}

					}
				}

			}

			// process
			if (formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				formulatedProduct.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).forEach(processItem -> {
					NodeRef resource = processItem.getResource();
					if ((resource != null)) {
						visitPart(processItem, (ProductData) alfrescoRepository.findOne(resource), formulatedProduct, retainNodes, null, null,
								errors);
					}
				});
			}

			if (!formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					&& !formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				retainNodes.addAll(formulatedProduct.getAllergenList());

				if (!(formulatedProduct instanceof ResourceProductData)) {
					for (AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()) {
						allergenListDataItem.setInVoluntary(false);
						allergenListDataItem.getInVoluntarySources().clear();
					}
				}

			} else {
				for (AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()) {
					if (Boolean.TRUE.equals(allergenListDataItem.getIsManual())) {
						retainNodes.add(allergenListDataItem);
					}
				}
			}

			formulatedProduct.getAllergenList().retainAll(retainNodes);
			formulatedProduct.getReqCtrlList().addAll(rclCtrlMap.values());

		}

		if (formulatedProduct.getIngList() != null)

		{
			Set<NodeRef> visitedAllergens = new HashSet<>();
			for (IngListDataItem ing : formulatedProduct.getIngList()) {
				if (ing.getIng() != null) {

					IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());

					for (NodeRef allergenNodeRef : ingItem.getAllergenList()) {
						AllergenListDataItem allergen = findAllergen(formulatedProduct, allergenNodeRef);

						AllergenItem allergenItem = (AllergenItem) alfrescoRepository.findOne(allergenNodeRef);
						
						if (allergen == null) {
							allergen = new AllergenListDataItem();
							allergen.setAllergen(allergenNodeRef);
							formulatedProduct.getAllergenList().add(allergen);
						}

						if (!allergen.getVoluntarySources().contains(ing.getIng())) {
							allergen.getVoluntarySources().add(ing.getIng());
						}
						allergen.setVoluntary(true);

						if (!ingItem.getAllergensQtyMap().isEmpty()
								&& !formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
							String code = allergenItem.getAllergenCode();
							Double allergenRate = null;
							if (ingItem.getAllergensQtyMap().containsKey(code)) {
								allergenRate = ingItem.getAllergensQtyMap().get(code);
							} else if (ingItem.getAllergensQtyMap().containsKey("ALL")) {
								allergenRate = ingItem.getAllergensQtyMap().get("ALL");
							}

							if (allergenRate != null) {
								if (!visitedAllergens.contains(allergenNodeRef)) {
									allergen.setQtyPerc(0d);
									visitedAllergens.add(allergenNodeRef);
								}
								allergen.setQtyPerc(
										allergen.getQtyPerc() + ((ing.getQtyPerc() == null ? 0d : ing.getQtyPerc()) * allergenRate / 100));
							}
						}
					}
				}
			}
		}

		// sort
		if (formulatedProduct.getAllergenList() != null) {

			formulatedProduct.getAllergenList().forEach(allergenListDataItem -> {
				if (!Boolean.TRUE.equals(allergenListDataItem.getIsManual())) {
					AllergenItem allergen = (AllergenItem) alfrescoRepository.findOne(allergenListDataItem.getAllergen());
					Double regulatoryThreshold = getRegulatoryThreshold(formulatedProduct, allergen);
					if (regulatoryThreshold != null && allergenListDataItem.getQtyPerc() != null) {
						if (regulatoryThreshold > allergenListDataItem.getQtyPerc()) {
							allergenListDataItem.setVoluntary(false);
						} else if (regulatoryThreshold <= allergenListDataItem.getQtyPerc()) {
							allergenListDataItem.setVoluntary(true);
						}
					}

					if (!Boolean.TRUE.equals(allergenListDataItem.getVoluntary())) {
						Double inVolRegulatoryThreshold = getInVolRegulatoryThreshold(allergen);
						if (inVolRegulatoryThreshold != null && allergenListDataItem.getQtyPerc() != null) {
							if (inVolRegulatoryThreshold > allergenListDataItem.getQtyPerc()) {
								allergenListDataItem.setInVoluntary(false);
							} else if (inVolRegulatoryThreshold <= allergenListDataItem.getQtyPerc()) {
								allergenListDataItem.setInVoluntary(true);
							}
						}
					}
				}
			});

			if (isHierarchicalSortEnabled()) {
				sortByParent(formulatedProduct.getAllergenList());
			} else {
				sort(formulatedProduct.getAllergenList());
			}
		}

		return true;

	}

	private AllergenListDataItem findAllergen(ProductData formulatedProduct, NodeRef allergenNodeRef) {

		for (AllergenListDataItem allergen : formulatedProduct.getAllergenList()) {
			if (allergenNodeRef.equals(allergen.getAllergen())) {
				return allergen;
			}
		}

		return null;
	}

	private Double getRegulatoryThreshold(ProductData formulatedProduct, AllergenItem allergen) {
		Double ret = null;

		for (Map.Entry<ProductSpecificationData, List<AllergenListDataItem>> entry : allergenRequirementScanner
				.extractRequirements(formulatedProduct.getProductSpecifications()).entrySet()) {
			List<AllergenListDataItem> requirements = entry.getValue();

			AllergenListDataItem temp = requirements.stream().filter(al -> al.getAllergen().equals(allergen.getNodeRef())).findFirst().orElse(null);
			if ((temp != null) && (temp.getQtyPerc() != null)) {
				ret = temp.getQtyPerc();
			}

			if (ret != null) {
				break;
			}

		}

		return ret != null ? ret : allergen.getAllergenRegulatoryThreshold();
	}

	private Double getInVolRegulatoryThreshold(AllergenItem allergen) {
		return allergen.getAllergenInVoluntaryRegulatoryThreshold();
	}

	private boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
			return false;
		}

		// no compo, nor allergenList on formulated product => no formulation
		if ((!formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
				&& !formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)))
				|| !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_ALLERGENLIST)) {
			return false;
		}
		return true;
	}

	/**
	 * Visit part.
	 *
	 * @param variantDataItem the variant context for the visit
	 * @param partProduct the part product being visited
	 * @param formulatedProduct the target formulated product being updated
	 * @param retainNodes the list used to retain processed allergen items
	 * @param qtyUsed the quantity of the part used
	 * @param netQty the net quantity context
	 * @param errors a map collecting requirement errors keyed by id
	 */
	private List<RequirementListDataItem> visitPart(VariantDataItem variantDataItem, ProductData partProduct, ProductData formulatedProduct,
			List<AllergenListDataItem> retainNodes, Double qtyUsed, Double netQty, Map<String, RequirementListDataItem> errors) {

		List<RequirementListDataItem> ret = new ArrayList<>();

		for (AllergenListDataItem allergenListDataItem : partProduct.getAllergenList()) {

			// Look for allergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			if (allergenNodeRef != null) {

				AllergenItem allergen = (AllergenItem) alfrescoRepository.findOne(allergenNodeRef);
				
				AllergenListDataItem newAllergenListDataItem = formulatedProduct.getAllergenList().stream()
						.filter(a -> allergenNodeRef.equals(a.getAllergen())).findFirst().orElse(null);

				if (newAllergenListDataItem == null) {
					newAllergenListDataItem = new AllergenListDataItem();
					newAllergenListDataItem.setAllergen(allergenNodeRef);
					formulatedProduct.getAllergenList().add(newAllergenListDataItem);
				}

				if (!retainNodes.contains(newAllergenListDataItem)) {
					// Reset existing variants
					if (!Boolean.TRUE.equals(newAllergenListDataItem.getIsManual())) {

						newAllergenListDataItem.setVariants(null);

						if (!(partProduct instanceof ResourceProductData)) {
							newAllergenListDataItem.setQtyPerc(null);
							newAllergenListDataItem.setVoluntary(false);
							newAllergenListDataItem.getVoluntarySources().clear();
						}

						newAllergenListDataItem.setInVoluntary(false);
						newAllergenListDataItem.getInVoluntarySources().clear();

						newAllergenListDataItem.setOnLine(false);
						newAllergenListDataItem.setOnSite(false);

						// add detailable aspect
						if (!newAllergenListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
							newAllergenListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
						}

					}
					retainNodes.add(newAllergenListDataItem);
				}

				if (!Boolean.TRUE.equals(newAllergenListDataItem.getIsManual())) {

					// Define voluntary presence
					if (Boolean.TRUE.equals(allergenListDataItem.getVoluntary())) {
						newAllergenListDataItem.setVoluntary(true);
						if (!newAllergenListDataItem.getVoluntarySources().contains(partProduct.getNodeRef())
								&& !(partProduct instanceof SemiFinishedProductData)) {
							newAllergenListDataItem.getVoluntarySources().add(partProduct.getNodeRef());
						}
					} else if (allergenListDataItem.getVoluntary() == null) {

						if (!Boolean.TRUE.equals(newAllergenListDataItem.getVoluntary())) {
							newAllergenListDataItem.setVoluntary(null);
						}
						addEmptyError(errors, ret, partProduct);
					}

					for (NodeRef p : allergenListDataItem.getVoluntarySources()) {
						if (!newAllergenListDataItem.getVoluntarySources().contains(p)) {
							if (!(partProduct.isRawMaterial()) || (alfrescoRepository.findOne(p) instanceof IngItem)) {
								newAllergenListDataItem.getVoluntarySources().add(p);
							}
						}
					}

					// Define involuntary
					if (Boolean.TRUE.equals(allergenListDataItem.getInVoluntary())) {

						if (!(Boolean.TRUE.equals(newAllergenListDataItem.getIsCleaned()) && (partProduct instanceof ResourceProductData))) {
							newAllergenListDataItem.setInVoluntary(true);

						}

						if (!newAllergenListDataItem.getInVoluntarySources().contains(partProduct.getNodeRef())
								&& !(partProduct instanceof SemiFinishedProductData)) {
							newAllergenListDataItem.getInVoluntarySources().add(partProduct.getNodeRef());
						}

					} else if (allergenListDataItem.getInVoluntary() == null) {

						if (!Boolean.TRUE.equals(newAllergenListDataItem.getInVoluntary())) {
							newAllergenListDataItem.setInVoluntary(null);
						}
						addEmptyError(errors, ret, partProduct);
					}

					for (NodeRef p : allergenListDataItem.getInVoluntarySources()) {
						if (!newAllergenListDataItem.getInVoluntarySources().contains(p)) {
							if (!(partProduct.isRawMaterial()) || (alfrescoRepository.findOne(p) instanceof IngItem)) {
								newAllergenListDataItem.getInVoluntarySources().add(p);
							}
						}
					}

					if (!(partProduct.isRawMaterial()) || (formulatedProduct.isGeneric())) {

						if (Boolean.TRUE.equals(allergenListDataItem.getOnSite())) {
							newAllergenListDataItem.setOnSite(true);
						} else if ((allergenListDataItem.getOnSite() == null) && !Boolean.TRUE.equals(newAllergenListDataItem.getOnSite())) {
							newAllergenListDataItem.setOnSite(null);
						}

						if (Boolean.TRUE.equals(allergenListDataItem.getOnLine())) {
							newAllergenListDataItem.setOnLine(true);
						} else if ((allergenListDataItem.getOnLine() == null) && !Boolean.TRUE.equals(newAllergenListDataItem.getOnLine())) {
							newAllergenListDataItem.setOnLine(null);
						}
					}

					// Add qty
					if (formulatedProduct.isGeneric()) {
						// Generic raw material
						if (allergenListDataItem.getQtyPerc() != null) {
							if ((newAllergenListDataItem.getQtyPerc() == null)
									|| (newAllergenListDataItem.getQtyPerc() < allergenListDataItem.getQtyPerc())) {
								newAllergenListDataItem.setQtyPerc(allergenListDataItem.getQtyPerc());
							}
						}
					} else {

						String message = I18NUtil.getMessage(MESSAGE_NULL_PERC, extractName(allergen));
						RequirementListDataItem error = errors.get(message);

						if ((allergenListDataItem.getQtyPerc() != null) && (qtyUsed != null)
								&& ((newAllergenListDataItem.getQtyPerc() != null) || (error == null))) {

							Double value = allergenListDataItem.getQtyPerc() * qtyUsed;
							if ((netQty != null) && (netQty != 0d)) {
								value = value / netQty;
							}

							if (logger.isDebugEnabled()) {
								logger.debug("Add " + extractName(allergen) + "[" + partProduct.getName() + "] - "
										+ allergenListDataItem.getQtyPerc() + "% * " + qtyUsed + " / " + netQty + "(=" + value + " ) kg to "
										+ newAllergenListDataItem.getQtyPerc());
							}

							newAllergenListDataItem.addQtyPerc(variantDataItem, value);

						} else {
							
							Double regulatoryThreshold = allergen.getAllergenRegulatoryThreshold();

							if (error != null) {
								if ((allergenListDataItem.getQtyPerc() == null) || (qtyUsed == null)) {
									if (!error.getSources().contains(partProduct.getNodeRef())) {
										error.getSources().add(partProduct.getNodeRef());
									}
								}
							} else if (regulatoryThreshold != null) {
								List<NodeRef> sourceNodeRefs = new ArrayList<>();
								sourceNodeRefs.add(partProduct.getNodeRef());

								error = RequirementListDataItem.forbidden().ofDataType(RequirementDataType.Allergen)
										.withMessage(MLTextHelper.getI18NMessage(MESSAGE_NULL_PERC,
												mlNodeService.getProperty(allergenNodeRef, BeCPGModel.PROP_CHARACT_NAME)))
										.withSources(sourceNodeRefs).withCharact(allergenNodeRef);

								errors.put(message, error);

								if (logger.isDebugEnabled()) {
									logger.debug("Adding allergen error " + error.toString());
								}

								ret.add(error);
							}

							if (regulatoryThreshold != null && newAllergenListDataItem.getQtyPerc() == null) {
								newAllergenListDataItem.setQtyPerc(0d);
							}
						}

					}

					// Add variants if it adds an allergen
					if ((variantDataItem.getVariants() != null) && (Boolean.TRUE.equals(allergenListDataItem.getVoluntary())
							|| Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()))) {
						if (newAllergenListDataItem.getVariants() == null) {
							newAllergenListDataItem.setVariants(new ArrayList<>());
						}

						for (NodeRef variant : variantDataItem.getVariants()) {
							if (!newAllergenListDataItem.getVariants().contains(variant)) {
								newAllergenListDataItem.getVariants().add(variant);
							}
						}
					}

				}
			}
		}

		return ret;
	}

	private void addEmptyError(Map<String, RequirementListDataItem> errors, List<RequirementListDataItem> ret, ProductData partProduct) {
		String message = I18NUtil.getMessage(MESSAGE_EMPTY_ALLERGEN);
		RequirementListDataItem error = errors.get(message);

		List<NodeRef> sourceNodeRefs = new ArrayList<>();
		if (error == null) {
			error = RequirementListDataItem.forbidden().ofDataType(RequirementDataType.Allergen)
					.withMessage(MLTextHelper.getI18NMessage(MESSAGE_EMPTY_ALLERGEN)).withSources(sourceNodeRefs);
			ret.add(error);
		} else {
			sourceNodeRefs = error.getSources();
		}

		if (!sourceNodeRefs.contains(partProduct.getNodeRef())) {
			sourceNodeRefs.add(partProduct.getNodeRef());
		}

		errors.put(message, error);

	}

	/**
	 * Sort allergens by type and name.
	 *
	 * @param allergenList a {@link java.util.List} object.
	 */
	protected void sort(List<AllergenListDataItem> allergenList) {

		final int BEFORE = -1;
		final int AFTER = 1;

		MutableInt sort = new MutableInt(1);
		allergenList.stream().sorted((a1, a2) -> {
			AllergenItem allergen1 = (AllergenItem) alfrescoRepository.findOne(a1.getAllergen());
			AllergenItem allergen2 = (AllergenItem) alfrescoRepository.findOne(a2.getAllergen());
			String type1 = allergen1.getAllergenType();
			String type2 = allergen2.getAllergenType();

			String allergenName1 = extractName(allergen1);
			String allergenName2 = extractName(allergen2);

			if (type1 == null && type2 == null) {
				return allergenName1.compareTo(allergenName2);
			} else if (type1 == null) {
				return BEFORE;
			} else if (type2 == null) {
				return AFTER;
			}

			if (type1.equals(type2)) {
				return allergenName1.compareTo(allergenName2);
			} else if (AllergenType.Major.toString().equals(type1) && AllergenType.Minor.toString().equals(type2)) {
				return BEFORE;
			} else if (AllergenType.Minor.toString().equals(type1) && AllergenType.Major.toString().equals(type2)) {
				return AFTER;
			}

			return type1.compareTo(type2);

		}).forEach(al -> {
			al.setSort(sort.getAndIncrement());
			al.setDepthLevel(Integer.valueOf(1));
		});

	}

	/**
	 * Sort allergens by parent/child hierarchy using allergen subsets while preserving
	 * alphabetical order inside each level.
	 *
	 * @param allergenList a {@link java.util.List} object.
	 */
	protected void sortByParent(List<AllergenListDataItem> allergenList) {
		Map<NodeRef, AllergenItem> allergenByNodeRef = new HashMap<>();
		Map<NodeRef, AllergenListDataItem> itemByNodeRef = new HashMap<>();

		for (AllergenListDataItem allergenListDataItem : allergenList) {
			if (allergenListDataItem.getAllergen() != null) {
				itemByNodeRef.put(allergenListDataItem.getAllergen(), allergenListDataItem);
				allergenByNodeRef.put(allergenListDataItem.getAllergen(),
						(AllergenItem) alfrescoRepository.findOne(allergenListDataItem.getAllergen()));
			}
		}

		Map<NodeRef, List<NodeRef>> childrenByParent = new HashMap<>();
		Set<NodeRef> knownChildren = new HashSet<>();

		for (Map.Entry<NodeRef, AllergenItem> entry : allergenByNodeRef.entrySet()) {
			NodeRef parentNodeRef = entry.getKey();
			AllergenItem parentAllergen = entry.getValue();
			if (parentAllergen != null && parentAllergen.getAllergenSubset() != null) {
				for (NodeRef childNodeRef : parentAllergen.getAllergenSubset()) {
					if (itemByNodeRef.containsKey(childNodeRef)) {
						childrenByParent.computeIfAbsent(parentNodeRef, key -> new ArrayList<>()).add(childNodeRef);
						knownChildren.add(childNodeRef);
					}
				}
			}
		}

		List<NodeRef> roots = new ArrayList<>();
		for (NodeRef allergenNodeRef : itemByNodeRef.keySet()) {
			if (!knownChildren.contains(allergenNodeRef)) {
				roots.add(allergenNodeRef);
			}
		}

		roots.sort((allergenNodeRef1, allergenNodeRef2) -> compareAllergens(allergenByNodeRef.get(allergenNodeRef1), allergenByNodeRef.get(allergenNodeRef2)));

		List<AllergenListDataItem> orderedItems = new ArrayList<>();
		Set<NodeRef> visited = new HashSet<>();

		for (NodeRef rootNodeRef : roots) {
			appendNode(rootNodeRef, Integer.valueOf(1), orderedItems, itemByNodeRef, allergenByNodeRef, childrenByParent, visited,
					new HashSet<>());
		}

		List<NodeRef> remainingNodes = new ArrayList<>();
		for (NodeRef allergenNodeRef : itemByNodeRef.keySet()) {
			if (!visited.contains(allergenNodeRef)) {
				remainingNodes.add(allergenNodeRef);
			}
		}

		remainingNodes.sort((allergenNodeRef1, allergenNodeRef2) -> compareAllergens(allergenByNodeRef.get(allergenNodeRef1), allergenByNodeRef.get(allergenNodeRef2)));
		for (NodeRef remainingNode : remainingNodes) {
			appendNode(remainingNode, Integer.valueOf(1), orderedItems, itemByNodeRef, allergenByNodeRef, childrenByParent, visited,
					new HashSet<>());
		}

		allergenList.clear();
		allergenList.addAll(orderedItems);

		MutableInt sort = new MutableInt(1);
		for (AllergenListDataItem allergenListDataItem : allergenList) {
			allergenListDataItem.setSort(sort.getAndIncrement());
		}
	}

	/**
	 * Returns whether allergen hierarchical sorting is enabled in system
	 * configuration.
	 *
	 * @return true when hierarchical sorting is enabled
	 */
	protected boolean isHierarchicalSortEnabled() {
		if (systemConfigurationService == null) {
			return false;
		}
		return Boolean.parseBoolean(systemConfigurationService.confValue(CONF_ALLERGEN_SORT_BY_PARENT));
	}

	/**
	 * Appends a parent node and its children recursively while preserving level and
	 * alphabetical order.
	 *
	 * @param allergenNodeRef the allergen node to append
	 * @param depthLevel current hierarchy level
	 * @param orderedItems flattened result list
	 * @param itemByNodeRef allergen item map
	 * @param allergenByNodeRef allergen metadata map
	 * @param childrenByParent parent to children map
	 * @param visited global visited set
	 * @param recursionStack recursion stack for cycle protection
	 */
	private void appendNode(NodeRef allergenNodeRef, Integer depthLevel, List<AllergenListDataItem> orderedItems,
			Map<NodeRef, AllergenListDataItem> itemByNodeRef, Map<NodeRef, AllergenItem> allergenByNodeRef,
			Map<NodeRef, List<NodeRef>> childrenByParent, Set<NodeRef> visited, Set<NodeRef> recursionStack) {

		if ((allergenNodeRef == null) || visited.contains(allergenNodeRef) || recursionStack.contains(allergenNodeRef)) {
			return;
		}

		AllergenListDataItem allergenListDataItem = itemByNodeRef.get(allergenNodeRef);
		if (allergenListDataItem == null) {
			return;
		}

		recursionStack.add(allergenNodeRef);
		allergenListDataItem.setDepthLevel(depthLevel);
		orderedItems.add(allergenListDataItem);
		visited.add(allergenNodeRef);

		List<NodeRef> children = childrenByParent.get(allergenNodeRef);
		if (children != null) {
			children.sort((childNodeRef1, childNodeRef2) -> compareAllergens(allergenByNodeRef.get(childNodeRef1), allergenByNodeRef.get(childNodeRef2)));
			for (NodeRef childNodeRef : children) {
				appendNode(childNodeRef, Integer.valueOf(depthLevel.intValue() + 1), orderedItems, itemByNodeRef, allergenByNodeRef,
						childrenByParent, visited, recursionStack);
			}
		}

		recursionStack.remove(allergenNodeRef);
	}

	/**
	 * Compare two allergens using major/minor priority and alphabetical fallback.
	 *
	 * @param allergen1 first allergen
	 * @param allergen2 second allergen
	 * @return comparison result
	 */
	private int compareAllergens(AllergenItem allergen1, AllergenItem allergen2) {
		final int BEFORE = -1;
		final int AFTER = 1;

		if (allergen1 == null && allergen2 == null) {
			return 0;
		} else if (allergen1 == null) {
			return BEFORE;
		} else if (allergen2 == null) {
			return AFTER;
		}

		String type1 = allergen1.getAllergenType();
		String type2 = allergen2.getAllergenType();

		String allergenName1 = extractName(allergen1);
		String allergenName2 = extractName(allergen2);

		if (type1 == null && type2 == null) {
			return allergenName1.compareTo(allergenName2);
		} else if (type1 == null) {
			return BEFORE;
		} else if (type2 == null) {
			return AFTER;
		}

		if (type1.equals(type2)) {
			return allergenName1.compareTo(allergenName2);
		} else if (AllergenType.Major.toString().equals(type1) && AllergenType.Minor.toString().equals(type2)) {
			return BEFORE;
		} else if (AllergenType.Minor.toString().equals(type1) && AllergenType.Major.toString().equals(type2)) {
			return AFTER;
		}

		return type1.compareTo(type2);
	}

	private String extractName(AllergenItem allergen) {
		return allergen.getCharactName();
	}

}
