/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.AllergenType;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.requirement.AllergenRequirementScanner;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * The Class AllergensCalculatingVisitor.
 *
 * @author querephi
 */
public class AllergensCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	public static final String MESSAGE_FORBIDDEN_ALLERGEN = "message.formulate.allergen.forbidden";

	public static final String MESSAGE_NOT_VALIDATED_ALLERGEN = "message.formulate.allergen.notValidated";

	public static final String MESSAGE_NULL_PERC = "message.formulate.allergen.error.nullQtyPerc";
	
	private static final Log logger = LogFactory.getLog(AllergensCalculatingFormulationHandler.class);

	protected AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	protected NodeService nodeService;
	
	protected NodeService mlNodeService;
	
	
	private AllergenRequirementScanner allergenRequirementScanner;

	public void setAllergenRequirementScanner(AllergenRequirementScanner allergenRequirementScanner) {
		this.allergenRequirementScanner = allergenRequirementScanner;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (accept(formulatedProduct)) {
			logger.debug("Start AllergensCalculatingVisitor");

			if (formulatedProduct.getAllergenList() == null) {
				formulatedProduct.setAllergenList(new LinkedList<AllergenListDataItem>());
			}

			List<AllergenListDataItem> retainNodes = new ArrayList<>();
			Map<String, ReqCtrlListDataItem> errors = new HashMap<>();
			Map<String, ReqCtrlListDataItem> rclCtrlMap = new HashMap<>();

			// compoList
			Double netQty = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
			if (formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				for (CompoListDataItem compoItem : formulatedProduct.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

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

							if ((partProduct instanceof RawMaterialData) && !SystemState.Valid.equals(partProduct.getState())) {

								if ((partProduct.getAllergenList() == null) || partProduct.getAllergenList().isEmpty()
										|| (partProduct.getAllergenList().get(0).getParentNodeRef() == null)
										|| !SystemState.Valid.toString().equals(nodeService.getProperty(
												partProduct.getAllergenList().get(0).getParentNodeRef(), BeCPGModel.PROP_ENTITYLIST_STATE))) {

									String message = I18NUtil.getMessage(MESSAGE_NOT_VALIDATED_ALLERGEN);
									ReqCtrlListDataItem error = rclCtrlMap.get(message);

									List<NodeRef> sourceNodeRefs = new ArrayList<>();
									if (error == null) {
										error = new ReqCtrlListDataItem(null, RequirementType.Tolerated, MLTextHelper.getI18NMessage(MESSAGE_NOT_VALIDATED_ALLERGEN), null, sourceNodeRefs,
												RequirementDataType.Allergen);
									} else {
										sourceNodeRefs = error.getSources();
									}

									sourceNodeRefs.add(partProduct.getNodeRef());

									rclCtrlMap.put(message, error);
								}

							}

						}
					}
				}

				formulatedProduct.getAllergenList().forEach(allergenListDataItem -> {
					if (!allergenListDataItem.getIsManual() && (allergenListDataItem.getVoluntary() || allergenListDataItem.getInVoluntary())) {
						Double regulatoryThreshold = getRegulatoryThreshold(formulatedProduct, allergenListDataItem.getAllergen());
						if ((regulatoryThreshold != null) && (allergenListDataItem.getQtyPerc() != null)
								&& (regulatoryThreshold > allergenListDataItem.getQtyPerc())) {
							allergenListDataItem.setVoluntary(false);
							allergenListDataItem.setInVoluntary(false);
						}
					}
				});

			} else if (!(formulatedProduct instanceof ResourceProductData)) {
				retainNodes.addAll(formulatedProduct.getAllergenList());
				for (AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()) {
					allergenListDataItem.setInVoluntary(false);
					allergenListDataItem.getInVoluntarySources().clear();
				}
			}
			// process
			if (formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				formulatedProduct.getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).forEach(processItem -> {
					NodeRef resource = processItem.getResource();
					if ((resource != null)) {
						visitPart(processItem, (ProductData) alfrescoRepository.findOne(resource), formulatedProduct, retainNodes, null, null, errors);
					}
				});
			}

			formulatedProduct.getAllergenList().retainAll(retainNodes);
			formulatedProduct.getReqCtrlList().addAll(rclCtrlMap.values());
			// sort
			sort(formulatedProduct.getAllergenList());

		}

		return true;

	}

	private Double getRegulatoryThreshold(ProductData formulatedProduct, NodeRef allergen) {
		Double ret = null;
		
		for (Map.Entry<ProductSpecificationData, List<AllergenListDataItem>> entry : allergenRequirementScanner.extractRequirements(formulatedProduct.getProductSpecifications()).entrySet()) {
			List<AllergenListDataItem> requirements = entry.getValue();

		
			AllergenListDataItem temp = requirements.stream()
					.filter(al -> al.getAllergen().equals(allergen)).findFirst().orElse(null);
			if ((temp != null) && (temp.getQtyPerc() != null)) {
				ret = temp.getQtyPerc();
			}
			
			if(ret!=null) {
				break;
			}
		
		}

		return ret != null ? ret : (Double) nodeService.getProperty(allergen, PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD);
	}

	private boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) 
				|| formulatedProduct instanceof ProductSpecificationData) {
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
					if (!newAllergenListDataItem.getIsManual()) {
						newAllergenListDataItem.setVariants(null);

						newAllergenListDataItem.setQtyPerc(null);
						newAllergenListDataItem.setVoluntary(false);
						newAllergenListDataItem.getVoluntarySources().clear();

						newAllergenListDataItem.setInVoluntary(false);
						newAllergenListDataItem.getInVoluntarySources().clear();

						// add detailable aspect
						if (!newAllergenListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
							newAllergenListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
						}

					}
					retainNodes.add(newAllergenListDataItem);
				}

				if (!newAllergenListDataItem.getIsManual()) {

					// Define voluntary presence
					if (allergenListDataItem.getVoluntary()) {
						newAllergenListDataItem.setVoluntary(true);
						if (!newAllergenListDataItem.getVoluntarySources().contains(partProduct.getNodeRef())
								&& !(partProduct instanceof SemiFinishedProductData)) {
							newAllergenListDataItem.getVoluntarySources().add(partProduct.getNodeRef());
						}
					}

					for (NodeRef p : allergenListDataItem.getVoluntarySources()) {
						if (!newAllergenListDataItem.getVoluntarySources().contains(p)) {
							newAllergenListDataItem.getVoluntarySources().add(p);
						}
					}

					// Define involuntary
					if (allergenListDataItem.getInVoluntary()) {
						newAllergenListDataItem.setInVoluntary(true);
						if (!newAllergenListDataItem.getInVoluntarySources().contains(partProduct.getNodeRef())
								&& !(partProduct instanceof SemiFinishedProductData)) {
							newAllergenListDataItem.getInVoluntarySources().add(partProduct.getNodeRef());
						}
					}

					for (NodeRef p : allergenListDataItem.getInVoluntarySources()) {
						if (!newAllergenListDataItem.getInVoluntarySources().contains(p)) {
							newAllergenListDataItem.getInVoluntarySources().add(p);
						}
					}

					// Add qty
					if (formulatedProduct instanceof RawMaterialData) {
						// Generic raw material
						if (allergenListDataItem.getQtyPerc() != null) {
							if ((newAllergenListDataItem.getQtyPerc() == null)
									|| (newAllergenListDataItem.getQtyPerc() < allergenListDataItem.getQtyPerc())) {
								newAllergenListDataItem.setQtyPerc(allergenListDataItem.getQtyPerc());
							}
						}
					} else {
						String message = I18NUtil.getMessage("message.formulate.allergen.error.nullQtyPerc", extractName(allergenNodeRef));
						ReqCtrlListDataItem error = errors.get(message);

						if ((allergenListDataItem.getQtyPerc() != null) && (qtyUsed != null)
								&& ((newAllergenListDataItem.getQtyPerc() != null) || (error == null))) {
							if (newAllergenListDataItem.getQtyPerc() == null) {
								newAllergenListDataItem.setQtyPerc(0d);
							}

							Double value = allergenListDataItem.getQtyPerc() * qtyUsed;
							if ((netQty != null) && (netQty != 0d)) {
								value = value / netQty;
							}

							if (logger.isDebugEnabled()) {
								logger.debug("Add " + extractName(allergenNodeRef) + "[" + partProduct.getName() + "] - "
										+ allergenListDataItem.getQtyPerc() + "% * " + qtyUsed + " / " + netQty + "(=" + value + " ) kg to "
										+ newAllergenListDataItem.getQtyPerc());
							}

							value += newAllergenListDataItem.getQtyPerc();

							if (value > 100d) {
								value = 100d;
							}

							newAllergenListDataItem.setQtyPerc(value);

						} else {

							Double regulatoryThreshold = (Double) nodeService.getProperty(allergenListDataItem.getAllergen(),
									PLMModel.PROP_ALLERGEN_REGULATORY_THRESHOLD);

							if (error != null) {
								if ((allergenListDataItem.getQtyPerc() == null) || (qtyUsed == null)) {
									if (!error.getSources().contains(partProduct.getNodeRef())) {
										error.getSources().add(partProduct.getNodeRef());
									}
								}
							} else {
								List<NodeRef> sourceNodeRefs = new ArrayList<>();
								sourceNodeRefs.add(partProduct.getNodeRef());

								error = new ReqCtrlListDataItem(null, RequirementType.Forbidden, MLTextHelper.getI18NMessage(MESSAGE_NULL_PERC,mlNodeService.getProperty(allergenNodeRef, BeCPGModel.PROP_CHARACT_NAME)), allergenNodeRef, sourceNodeRefs,
										RequirementDataType.Allergen);
								errors.put(message, error);

								if (regulatoryThreshold != null) {
									if (logger.isDebugEnabled()) {
										logger.debug("Adding allergen error " + error.toString());
									}

									ret.add(error);
								}
							}
							if (regulatoryThreshold == null) {
								// Reset
								newAllergenListDataItem.setQtyPerc(null);
							} else if (newAllergenListDataItem.getQtyPerc() == null) {
								newAllergenListDataItem.setQtyPerc(0d);
							}
						}

					}

					// Add variants if it adds an allergen
					if ((variantDataItem.getVariants() != null) && (allergenListDataItem.getVoluntary() || allergenListDataItem.getInVoluntary())) {
						if (newAllergenListDataItem.getVariants() == null) {
							newAllergenListDataItem.setVariants(new ArrayList<NodeRef>());
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

	/**
	 * Sort allergens by type and name.
	 *
	 * @param costList
	 *            the cost list
	 * @return the list
	 */
	protected void sort(List<AllergenListDataItem> allergenList) {

		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		AtomicInteger atomicInteger = new AtomicInteger(1);
		allergenList.stream().sorted((a1, a2) -> {

			int comp = AFTER;
			String type1 = (String) nodeService.getProperty(a1.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);
			String type2 = (String) nodeService.getProperty(a2.getAllergen(), PLMModel.PROP_ALLERGEN_TYPE);

			if ((type1 != null) && (type2 != null)) {
				comp = type1.compareTo(type2);

				if (EQUAL == comp) {

					String allergenName1 = extractName(a1.getAllergen());
					String allergenName2 = extractName(a2.getAllergen());

					comp = allergenName1.compareTo(allergenName2);
				} else {

					if (AllergenType.Major.toString().equals(type1)) {
						comp = BEFORE;
					} else {
						comp = AFTER;
					}
				}
			}

			return comp;

		}).forEach(al -> al.setSort(atomicInteger.getAndIncrement()));

	}

	private String extractName(NodeRef charactRef) {
		return (String) nodeService.getProperty(charactRef, BeCPGModel.PROP_CHARACT_NAME);
	}

}
