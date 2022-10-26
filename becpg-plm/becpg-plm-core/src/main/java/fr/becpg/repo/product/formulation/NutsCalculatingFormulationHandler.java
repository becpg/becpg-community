/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.repository.model.VariantAwareDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;
import fr.becpg.repo.variant.model.VariantData;

/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class NutsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<NutListDataItem> {

	/** Constant <code>NUT_FORMULATED="I18NUtil.getMessage(message.formulate.n"{trunked}</code> */
	public static final String NUT_FORMULATED = I18NUtil.getMessage("message.formulate.nut.formulated");

	/** Constant <code>MESSAGE_MAXIMAL_DAILY_VALUE="message.formulate.nut.maximalDailyValue"</code> */
	public static final String MESSAGE_MAXIMAL_DAILY_VALUE = "message.formulate.nut.maximalDailyValue";

	private static final Log logger = LogFactory.getLog(NutsCalculatingFormulationHandler.class);

	private boolean propagateModeEnable = false;

	/**
	 * <p>Setter for the field <code>propagateModeEnable</code>.</p>
	 *
	 * @param propagateModeEnable a boolean.
	 */
	public void setPropagateModeEnable(boolean propagateModeEnable) {
		this.propagateModeEnable = propagateModeEnable;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<NutListDataItem> getInstanceClass() {
		return NutListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Nuts calculating visitor");

			if (formulatedProduct.getNutList() == null) {
				formulatedProduct.setNutList(new LinkedList<>());
			}

			cleanSimpleList(formulatedProduct.getNutList(), formulatedProduct.hasCompoListEl(new VariantFilters<>())
					|| formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL));

			synchronizeTemplate(formulatedProduct, formulatedProduct.getNutList());

			nutsCalculatingProcess(formulatedProduct, null);
			for (VariantData variant : formulatedProduct.getVariants()) {
				nutsCalculatingProcess(formulatedProduct, variant);
			}

			if (formulatedProduct.getNutList() != null) {

				calculateNutListDataItem(formulatedProduct, false, formulatedProduct.hasCompoListEl(new VariantFilters<>()));
				computeFormulatedList(formulatedProduct, formulatedProduct.getNutList(), PLMModel.PROP_NUT_FORMULA,
						"message.formulate.nutList.error");
				calculateNutListDataItem(formulatedProduct, true, formulatedProduct.hasCompoListEl(new VariantFilters<>()));

			}
		}
		return true;
	}

	private void nutsCalculatingProcess(ProductData formulatedProduct, VariantData variant) {
		boolean hasCompo = formulatedProduct.hasCompoListEl((variant != null ? new VariantFilters<>(variant.getNodeRef()) : new VariantFilters<>()));
		if (hasCompo) {

			if (!propagateModeEnable) {

				visitComposition(formulatedProduct, formulatedProduct.getNutList(), new SimpleListQtyProvider() {

					Double netQty = FormulationHelper.getNetQtyForNuts(formulatedProduct);
					Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

					@Override
					public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
						return FormulationHelper.getQtyInKg(compoListDataItem);
					}

					@Override
					public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
						return FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
					}
					
					@Override
					public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
						return  0d;
					}


					@Override
					public Double getQty(ProcessListDataItem processListDataItem) {
						return  0d;
					}

					
					@Override
					public Double getNetWeight() {
						return netWeight;
					}

					@Override
					public Double getNetQty() {
						return netQty;
					}

					@Override
					public Boolean omitElement(CompoListDataItem compoListDataItem) {
						return DeclarationType.Omit.equals(compoListDataItem.getDeclType());
					}

				}

						, variant);

			} else {

				List<NutListDataItem> retainNodes = new ArrayList<>();
				Map<NodeRef, Double> totalQtiesValue = new HashMap<>();

				// compoList
				Double netQty = FormulationHelper.getNetQtyForNuts(formulatedProduct);

				for (CompoListDataItem compoItem : formulatedProduct.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					if (compoItem.getDeclType() != DeclarationType.Omit) {

						NodeRef part = compoItem.getProduct();
						ProductData partProduct = (ProductData) alfrescoRepository.findOne(part);

						Double weight = FormulationHelper.getQtyInKg(compoItem);
						Double vol = FormulationHelper.getNetVolume(compoItem, partProduct);

						Double qtyUsed = partProduct.isLiquid() ? vol : weight;

						if (qtyUsed != null) {
							if (!(partProduct instanceof LocalSemiFinishedProductData)) {
								visitPart(formulatedProduct, partProduct, formulatedProduct.getNutList(), retainNodes, qtyUsed, netQty,
										totalQtiesValue, variant);
							}
						}
					}
				}

				formulatedProduct.getNutList().retainAll(retainNodes);

				for (Map.Entry<NodeRef, List<NodeRef>> mandatoryCharact : getMandatoryCharacts(formulatedProduct, null).entrySet()) {
					if ((mandatoryCharact.getValue() != null) && !mandatoryCharact.getValue().isEmpty()) {
						MLText message = MLTextHelper.getI18NMessage(MESSAGE_UNDEFINED_CHARACT,
								mlNodeService.getProperty(mandatoryCharact.getKey(), BeCPGModel.PROP_CHARACT_NAME));

						formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message,
								mandatoryCharact.getKey(), mandatoryCharact.getValue(), RequirementDataType.Nutrient));
					}
				}

				if (formulatedProduct.isGeneric()) {
					formulateGenericRawMaterial(formulatedProduct.getNutList(), totalQtiesValue, netQty);
				}
			}
		}
	}

	private void calculateNutListDataItem(ProductData formulatedProduct, boolean onlyFormulaNutrient, boolean hasCompo) {
		formulatedProduct.getNutList().forEach(n -> {
			if (n.getNut() != null) {
				NutDataItem nut = (NutDataItem) alfrescoRepository.findOne(n.getNut());

				if ((onlyFormulaNutrient && (nut.getNutFormula() != null) && !nut.getNutFormula().isEmpty())
						|| (!onlyFormulaNutrient && ((nut.getNutFormula() == null) || nut.getNutFormula().isEmpty()))) {
					n.setGroup(nut.getNutGroup());
					n.setUnit(calculateUnit(formulatedProduct.getUnit(), formulatedProduct.getServingSizeUnit(), nut.getNutUnit()));

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
						if (n instanceof VariantAwareDataItem) {
							for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
								if (((VariantAwareDataItem) n).getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) != null) {
									((VariantAwareDataItem) n)
											.setValue(((((VariantAwareDataItem) n).getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i)
													* (100 - n.getLossPerc())) / 100), VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
								}
							}
						}
					}
					

					if ((formulatedProduct.getSecondaryYield() != null) && (formulatedProduct.getSecondaryYield() != 0d)) {
						Double preparedValue = n.getValue();
						if (preparedValue != null) {
							if ((formulatedProduct.getYield() != null) && (formulatedProduct.getYield() != 0d)) {
								preparedValue = preparedValue * (formulatedProduct.getYield() / 100d);
							}
							preparedValue = preparedValue / (formulatedProduct.getSecondaryYield() / 100d);
						   n.setPreparedValue(preparedValue);
						}
					}

					Double servingSize = FormulationHelper.getServingSizeInLorKg(formulatedProduct);
					if ((servingSize != null) && (n.getValue() != null)) {
						Double valuePerserving = (n.getValue() * (servingSize * 1000d)) / 100;
						n.setValuePerServing(valuePerserving);
						Double gda = nut.getNutGDA();
						if ((gda != null) && (gda != 0d)) {
							n.setGdaPerc((100 * n.getValuePerServing()) / gda);
						}
						Double ul = nut.getNutUL();
						if (ul != null) {
							if (valuePerserving > ul) {
								MLText message = MLTextHelper.getI18NMessage(MESSAGE_MAXIMAL_DAILY_VALUE, nut.getCharactName());

								formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, n.getNut(),
										new ArrayList<>(), RequirementDataType.Specification));
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

					RegulationFormulationHelper.extractRoundedValue(formulatedProduct, nut.getNutCode(), n);

					if (transientFormulation) {
						n.setTransient(true);
					}
				}
			}
		});

	}

	private List<ReqCtrlListDataItem> visitPart(ProductData formulatedProduct, ProductData partProduct, List<NutListDataItem> nutList,
			List<NutListDataItem> retainNodes, Double qtyUsed, Double netQty, Map<NodeRef, Double> totalQtiesValue, VariantData variant) {

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

					calculate(formulatedProduct, partProduct, newNutListDataItem, nutListDataItem, qtyUsed, netQty, variant);

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

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getNutList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_NUTLIST))) {
			return false;
		}
		return true;
	}

	/**
	 * Calculate the nutListUnit
	 *
	 * @param productUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @param servingSizeUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @param nutUnit a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String calculateUnit(ProductUnit productUnit, ProductUnit servingSizeUnit, String nutUnit) {
		if ((nutUnit == null) || nutUnit.contains("/")) {
			return nutUnit;
		}
		nutUnit += calculateSuffixUnit(productUnit, servingSizeUnit);
		return nutUnit;
	}

	/**
	 * Calculate the suffix of nutListUnit
	 *
	 * @param productUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @param servingSizeUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit, ProductUnit servingSizeUnit) {
		if ((servingSizeUnit != null) && !servingSizeUnit.equals(ProductUnit.kg)) {
			if (servingSizeUnit.isVolume()) {
				return NutListDataItem.UNIT_PER100ML;
			} else {
				return NutListDataItem.UNIT_PER100G;
			}
		} else {
			if ((productUnit != null) && productUnit.isVolume()) {
				return NutListDataItem.UNIT_PER100ML;
			} else {
				return NutListDataItem.UNIT_PER100G;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected List<NutListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getNutList();
	}

	/** {@inheritDoc} */
	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();
		for (Map.Entry<NodeRef, List<NodeRef>> kv : getMandatoryCharactsFromList(formulatedProduct.getNutList()).entrySet()) {
			if (kv.getKey() != null) {
				String formula = (String) nodeService.getProperty(kv.getKey(), PLMModel.PROP_NUT_FORMULA);
				if ((formula == null) || formula.isEmpty()) {
					mandatoryCharacts.put(kv.getKey(), kv.getValue());
				}
			}
		}
		return mandatoryCharacts;
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Nutrient;
	}
}
