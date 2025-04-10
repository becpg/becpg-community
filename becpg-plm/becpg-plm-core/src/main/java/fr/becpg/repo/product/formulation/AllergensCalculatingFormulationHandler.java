/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.requirement.AllergenRequirementScanner;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
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

	private static final Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	protected NodeService nodeService;

	protected NodeService mlNodeService;

	private AllergenRequirementScanner allergenRequirementScanner;

	/**
	 * <p>Setter for the field <code>allergenRequirementScanner</code>.</p>
	 *
	 * @param allergenRequirementScanner a {@link fr.becpg.repo.product.requirement.AllergenRequirementScanner} object.
	 */
	public void setAllergenRequirementScanner(AllergenRequirementScanner allergenRequirementScanner) {
		this.allergenRequirementScanner = allergenRequirementScanner;
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
				formulatedProduct.setAllergenList(new LinkedList<>());
			}

			List<AllergenListDataItem> retainNodes = new ArrayList<>();
			Map<String, ReqCtrlListDataItem> errors = new HashMap<>();
			Map<String, ReqCtrlListDataItem> rclCtrlMap = new HashMap<>();

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
										ReqCtrlListDataItem error = rclCtrlMap.get(message);

										List<NodeRef> sourceNodeRefs = new ArrayList<>();
										if (error == null) {
											error = ReqCtrlListDataItem.tolerated().ofDataType(RequirementDataType.Allergen)
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
							String code = (String) nodeService.getProperty(allergenNodeRef, PLMModel.PROP_ALLERGEN_CODE);
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
					Double regulatoryThreshold = getRegulatoryThreshold(formulatedProduct, allergenListDataItem.getAllergen());
					if (regulatoryThreshold != null && allergenListDataItem.getQtyPerc() != null) {
						if (regulatoryThreshold > allergenListDataItem.getQtyPerc()) {
							allergenListDataItem.setVoluntary(false);
						} else if (regulatoryThreshold <= allergenListDataItem.getQtyPerc()) {
							allergenListDataItem.setVoluntary(true);
						}
					}

					if (!Boolean.TRUE.equals(allergenListDataItem.getVoluntary())) {
						Double inVolRegulatoryThreshold = getInVolRegulatoryThreshold(allergenListDataItem.getAllergen());
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

			sort(formulatedProduct.getAllergenList());
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

	private Double getRegulatoryThreshold(ProductData formulatedProduct, NodeRef allergen) {
		Double ret = null;

		for (Map.Entry<ProductSpecificationData, List<AllergenListDataItem>> entry : allergenRequirementScanner
				.extractRequirements(formulatedProduct.getProductSpecifications()).entrySet()) {
			List<AllergenListDataItem> requirements = entry.getValue();

			AllergenListDataItem temp = requirements.stream().filter(al -> al.getAllergen().equals(allergen)).findFirst().orElse(null);
			if ((temp != null) && (temp.getQtyPerc() != null)) {
				ret = temp.getQtyPerc();
			}

			if (ret != null) {
				break;
			}

		}

		return ret != null ? ret : (Double) nodeService.getProperty(allergen, PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD);
	}

	private Double getInVolRegulatoryThreshold(NodeRef allergen) {
		return (Double) nodeService.getProperty(allergen, PLMModel.PROP_ALLERGEN_INVOL_REGULATORY_THRESHOLD);
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
	 * @param part
	 *            the part
	 * @param qtyUsed
	 * @param isRawMaterial
	 * @param totalQtyPercMap
	 * @param allergenMap
	 *            the allergen map
	 */
	private List<ReqCtrlListDataItem> visitPart(VariantDataItem variantDataItem, ProductData partProduct, ProductData formulatedProduct,
			List<AllergenListDataItem> retainNodes, Double qtyUsed, Double netQty, Map<String, ReqCtrlListDataItem> errors) {

		List<ReqCtrlListDataItem> ret = new ArrayList<>();

		for (AllergenListDataItem allergenListDataItem : partProduct.getAllergenList()) {

			// Look for allergen
			NodeRef allergenNodeRef = allergenListDataItem.getAllergen();
			if (allergenNodeRef != null) {

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

						String message = I18NUtil.getMessage(MESSAGE_NULL_PERC, extractName(allergenNodeRef));
						ReqCtrlListDataItem error = errors.get(message);

						if ((allergenListDataItem.getQtyPerc() != null) && (qtyUsed != null)
								&& ((newAllergenListDataItem.getQtyPerc() != null) || (error == null))) {

							Double value = allergenListDataItem.getQtyPerc() * qtyUsed;
							if ((netQty != null) && (netQty != 0d)) {
								value = value / netQty;
							}

							if (logger.isDebugEnabled()) {
								logger.debug("Add " + extractName(allergenNodeRef) + "[" + partProduct.getName() + "] - "
										+ allergenListDataItem.getQtyPerc() + "% * " + qtyUsed + " / " + netQty + "(=" + value + " ) kg to "
										+ newAllergenListDataItem.getQtyPerc());
							}

							newAllergenListDataItem.addQtyPerc(variantDataItem, value);

						} else {

							Double regulatoryThreshold = (Double) nodeService.getProperty(allergenListDataItem.getAllergen(),
									PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD);

							if (error != null) {
								if ((allergenListDataItem.getQtyPerc() == null) || (qtyUsed == null)) {
									if (!error.getSources().contains(partProduct.getNodeRef())) {
										error.getSources().add(partProduct.getNodeRef());
									}
								}
							} else if (regulatoryThreshold != null) {
								List<NodeRef> sourceNodeRefs = new ArrayList<>();
								sourceNodeRefs.add(partProduct.getNodeRef());

								error = ReqCtrlListDataItem.forbidden().ofDataType(RequirementDataType.Allergen)
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

	private void addEmptyError(Map<String, ReqCtrlListDataItem> errors, List<ReqCtrlListDataItem> ret, ProductData partProduct) {
		String message = I18NUtil.getMessage(MESSAGE_EMPTY_ALLERGEN);
		ReqCtrlListDataItem error = errors.get(message);

		List<NodeRef> sourceNodeRefs = new ArrayList<>();
		if (error == null) {
			error = ReqCtrlListDataItem.forbidden().ofDataType(RequirementDataType.Allergen)
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

			String type1 = (String) nodeService.getProperty(a1.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
			String type2 = (String) nodeService.getProperty(a2.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);

			String allergenName1 = extractName(a1.getAllergen());
			String allergenName2 = extractName(a2.getAllergen());

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

		}).forEach(al -> al.setSort(sort.getAndIncrement()));

	}

	private String extractName(NodeRef charactRef) {
		return (String) nodeService.getProperty(charactRef, BeCPGModel.PROP_CHARACT_NAME);
	}

}
