/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 */
public class NutsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<NutListDataItem> {

	public static final String UNIT_PER100G = "/100g";

	public static final String UNIT_PER100ML = "/100mL";

	public static final String NUT_FORMULATED = I18NUtil.getMessage("message.formulate.nut.formulated");

	public static final String MESSAGE_MAXIMAL_DAILY_VALUE = "message.formulate.nut.maximalDailyValue";

	public static final String MESSAGE_NUT_NOT_IN_RANGE = "message.formulate.nut.notInRangeValue";

	private static final Log logger = LogFactory.getLog(NutsCalculatingFormulationHandler.class);

	private boolean propagateModeEnable = false;

	public void setPropagateModeEnable(boolean propagateModeEnable) {
		this.propagateModeEnable = propagateModeEnable;
	}

	@Override
	protected Class<NutListDataItem> getInstanceClass() {
		return NutListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (accept(formulatedProduct)) {
			logger.debug("Nuts calculating visitor");

			if (formulatedProduct.getNutList() == null) {
				formulatedProduct.setNutList(new LinkedList<NutListDataItem>());
			}

			boolean hasCompo = formulatedProduct.hasCompoListEl(new VariantFilters<>());

			if (hasCompo) {

				cleanSimpleList(formulatedProduct.getNutList());

				if (!propagateModeEnable) {

					synchronizeTemplate(formulatedProduct, formulatedProduct.getNutList());

					visitChildren(formulatedProduct, formulatedProduct.getNutList());

				} else {

					List<NutListDataItem> retainNodes = new ArrayList<>();
					Map<NodeRef, Double> totalQtiesValue = new HashMap<>();

					boolean isGenericRawMaterial = formulatedProduct instanceof RawMaterialData;

					// compoList
					Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

					for (CompoListDataItem compoItem : formulatedProduct.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

						if (compoItem.getDeclType() != DeclarationType.Omit) {

							NodeRef part = compoItem.getProduct();
							Double weight = FormulationHelper.getQtyInKg(compoItem);
							Double vol = FormulationHelper.getNetVolume(compoItem, nodeService);

							ProductData partProduct = (ProductData) alfrescoRepository.findOne(part);

							Double qtyUsed = FormulationHelper.isProductUnitLiter(partProduct.getUnit()) ? vol : weight;

							if (qtyUsed != null) {
								if (!(partProduct instanceof LocalSemiFinishedProductData)) {
									visitPart(partProduct, formulatedProduct.getNutList(), retainNodes, qtyUsed, netQty, isGenericRawMaterial,
											totalQtiesValue);
								}
							}
						}
					}

					formulatedProduct.getNutList().retainAll(retainNodes);

					for (Map.Entry<NodeRef, List<NodeRef>> mandatoryCharact : getMandatoryCharacts(formulatedProduct, null).entrySet()) {
						if ((mandatoryCharact.getValue() != null) && !mandatoryCharact.getValue().isEmpty()) {
							String message = I18NUtil.getMessage(MESSAGE_UNDEFINED_CHARACT,
									nodeService.getProperty(mandatoryCharact.getKey(), BeCPGModel.PROP_CHARACT_NAME));

							formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message,
									mandatoryCharact.getKey(), mandatoryCharact.getValue(), RequirementDataType.Nutrient));
						}
					}

					if (isGenericRawMaterial) {
						formulateGenericRawMaterial(formulatedProduct.getNutList(), totalQtiesValue, netQty);
					}

				}
			}

			if (formulatedProduct.getNutList() != null) {

				computeFormulatedList(formulatedProduct, formulatedProduct.getNutList(), PLMModel.PROP_NUT_FORMULA,
						"message.formulate.nutList.error");

				formulatedProduct.getNutList().forEach(n -> {
					if (n.getNut() != null) {

						n.setGroup((String) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTGROUP));
						n.setUnit(calculateUnit(formulatedProduct.getUnit(), (String) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTUNIT)));

						if (n.getLossPerc() != null) {
							if (n.getValue() != null) {
								n.setValue((n.getValue() * (100 - n.getLossPerc())) / 100);
							}
							if (n.getMini() != null) {
								n.setMini((n.getMini() * (100 - n.getLossPerc())) / 100);
							}
							if (n.getMaxi() != null) {
								n.setMaxi((n.getMaxi() * (100 - n.getLossPerc())) / 100);
							}
						}

						if ((formulatedProduct.getServingSize() != null) && (n.getValue() != null)) {
							Double valuePerserving = (n.getValue() * formulatedProduct.getServingSize()) / 100;
							n.setValuePerServing(valuePerserving);
							Double gda = (Double) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTGDA);
							if ((gda != null) && (gda != 0d)) {
								n.setGdaPerc((100 * n.getValuePerServing()) / gda);
							}
							Double ul = (Double) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTUL);
							if (ul != null) {
								if (valuePerserving > ul) {
									String message = I18NUtil.getMessage(MESSAGE_MAXIMAL_DAILY_VALUE,
											nodeService.getProperty(n.getNut(), BeCPGModel.PROP_CHARACT_NAME));

									formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message,
											n.getNut(), new ArrayList<NodeRef>(), RequirementDataType.Specification));
								}
							}
						} else {
							n.setValuePerServing(null);
							n.setGdaPerc(null);
						}

						if (isCharactFormulated(n) && hasCompo) {
							if (n.getManualValue() == null) {
								n.setMethod(NUT_FORMULATED);
							}
						}

						if (transientFormulation) {
							n.setTransient(true);
						}
					}
				});

				checkRequirementsOfFormulatedProduct(formulatedProduct);

			}
		} else if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) && (formulatedProduct.getNutList() != null)) {
			cleanSimpleList(formulatedProduct.getNutList());

		}
		return true;
	}

	private List<ReqCtrlListDataItem> visitPart(ProductData partProduct, List<NutListDataItem> nutList, List<NutListDataItem> retainNodes,
			Double qtyUsed, Double netQty, Boolean isGenericRawMaterial, Map<NodeRef, Double> totalQtiesValue) {

		List<ReqCtrlListDataItem> ret = new ArrayList<>();

		for (NutListDataItem nutListDataItem : partProduct.getNutList()) {
			NodeRef nutNodeRef = nutListDataItem.getNut();
			if (nutNodeRef != null) {

				NutListDataItem newNutListDataItem = nutList.stream().filter(n -> nutNodeRef.equals(n.getNut())).findFirst().orElse(null);

				if (newNutListDataItem == null) {
					newNutListDataItem = new NutListDataItem();
					newNutListDataItem.setNut(nutNodeRef);
					nutList.add(newNutListDataItem);
				}

				if (!retainNodes.contains(newNutListDataItem)) {
					retainNodes.add(newNutListDataItem);
				}

				if ((nutListDataItem != null) && (qtyUsed != null)) {

					calculate(newNutListDataItem, nutListDataItem, qtyUsed, netQty, isGenericRawMaterial);

					if ((totalQtiesValue != null) && (nutListDataItem.getValue() != null)) {
						Double currentQty = totalQtiesValue.get(newNutListDataItem.getCharactNodeRef());
						if (currentQty == null) {
							currentQty = 0d;
						}
						totalQtiesValue.put(newNutListDataItem.getCharactNodeRef(), currentQty + qtyUsed);
					}
				}

			}
		}

		return ret;
	}

	@Override
	protected boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| ((formulatedProduct.getNutList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_NUTLIST))) {
			return false;
		}
		return true;
	}

	/**
	 * Calculate the nutListUnit
	 *
	 * @param productUnit
	 * @param nutUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String nutUnit) {
		if ((nutUnit == null) || nutUnit.contains("/")) {
			return nutUnit;
		}
		return nutUnit += calculateSuffixUnit(productUnit);
	}

	/**
	 * Calculate the suffix of nutListUnit
	 *
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (ProductUnit.L.equals(productUnit) || ProductUnit.mL.equals(productUnit) || ProductUnit.cL.equals(productUnit)) {
			return UNIT_PER100ML;
		} else {
			return UNIT_PER100G;
		}
	}

	@Override
	protected List<NutListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getNutList();
	}

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();
		for (Map.Entry<NodeRef, List<NodeRef>> kv : getMandatoryCharactsFromList(formulatedProduct.getNutList()).entrySet()) {
			String formula = (String) nodeService.getProperty(kv.getKey(), PLMModel.PROP_NUT_FORMULA);
			if ((formula == null) || formula.isEmpty()) {
				mandatoryCharacts.put(kv.getKey(), kv.getValue());
			}
		}
		return mandatoryCharacts;
	}

	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Nutrient;
	}

	@Override
	protected String getSpecErrorMessageKey() {
		return MESSAGE_NUT_NOT_IN_RANGE;
	}

}
