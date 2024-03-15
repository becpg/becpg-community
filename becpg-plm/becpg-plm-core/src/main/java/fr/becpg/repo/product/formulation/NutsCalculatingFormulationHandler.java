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
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
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

	protected boolean propagateModeEnable(ProductData formulatedProduct) {
		return formulatedProduct.getAspects().contains(PLMModel.ASPECT_PROPAGATE_UP)
				|| Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.nutList.propagateUpEnable"));
	}

	/** {@inheritDoc} */
	@Override
	protected Class<NutListDataItem> getInstanceClass() {
		return NutListDataItem.class;
	}

	@Override
	protected NutListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		NutListDataItem newNutListDataItem = new NutListDataItem();
		newNutListDataItem.setNut(charactNodeRef);
		return newNutListDataItem;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {
		
		boolean accept = accept(formulatedProduct);

		if (accept) {
			logger.debug("Nuts calculating visitor");

			if (formulatedProduct.getNutList() == null) {
				formulatedProduct.setNutList(new LinkedList<>());
			}

			formulateSimpleList(formulatedProduct, formulatedProduct.getNutList(), new DefaultSimpleListQtyProvider(formulatedProduct) {

				@Override
				public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
					return 0d;
				}

				@Override
				public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
					return 0d;
				}

				@Override
				public Double getNetQty(VariantData variant) {
					return FormulationHelper.getNetQtyForNuts(formulatedProduct, variant);
				}

			}, formulatedProduct.hasCompoListEl(new VariantFilters<>()));
			
			
			if (formulatedProduct.getNutList() != null) {

				calculateNutListDataItem(formulatedProduct, false, formulatedProduct.hasCompoListEl(new VariantFilters<>()));
				computeFormulatedList(formulatedProduct, formulatedProduct.getNutList(), PLMModel.PROP_NUT_FORMULA,
						"message.formulate.nutList.error");
				calculateNutListDataItem(formulatedProduct, true, formulatedProduct.hasCompoListEl(new VariantFilters<>()));

			}
		}

		return true;
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
							preparedValue = preparedValue / (formulatedProduct.getSecondaryYield() / 100d);
							n.setPreparedValue(preparedValue);
						}
					} else {
						n.setPreparedValue(null);
					}

					Double servingSize = FormulationHelper.getServingSizeInLorKg(formulatedProduct);
					Double valueForServing = formulatedProduct.isPrepared() && n.getPreparedValue() != null ? n.getPreparedValue() : n.getValue();

					if ((servingSize != null) && (valueForServing != null)) {
						Double valuePerserving = (valueForServing * (servingSize * 1000d)) / 100;
						n.setValuePerServing(valuePerserving);
						Double gda = nut.getNutGDA();
						if ((gda != null) && (gda != 0d)) {
							n.setGdaPerc((100 * n.getValuePerServing()) / gda);
						}
						Double ul = nut.getNutUL();
						if (ul != null) {
							if (n.getValuePerServing() > ul) {
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

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getNutList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_NUTLIST)));

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
			if(PLMModel.TYPE_RAWMATERIAL.equals(componentType)) {
			for (Map.Entry<NodeRef, List<NodeRef>> kv : getMandatoryCharactsFromList(formulatedProduct.getNutList()).entrySet()) {
				if (kv.getKey() != null) {
					String formula = (String) nodeService.getProperty(kv.getKey(), PLMModel.PROP_NUT_FORMULA);
					if ((formula == null) || formula.isEmpty()) {
						mandatoryCharacts.put(kv.getKey(), kv.getValue());
					}
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
