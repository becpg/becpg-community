/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AbstractCostListDataItem;
import fr.becpg.repo.product.helper.SimulationCostHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * The Class CostCalculatingVisitor.
 *
 * @author querephi
 * @version $Id: $Id
 */
public abstract class AbstractCostCalculatingFormulationHandler<T extends AbstractCostListDataItem<T>> extends AbstractSimpleListFormulationHandler<T> {

	private static final Log logger = LogFactory.getLog(AbstractCostCalculatingFormulationHandler.class);

	protected EntityTplService entityTplService;

	protected PackagingHelper packagingHelper;

	protected AlfrescoRepository<ProductData> alfrescoRepositoryProductData;
	
	private static AbstractCostCalculatingFormulationHandler<?> instance;
	
	public AbstractCostCalculatingFormulationHandler() {
		instance = this;
	}

	protected boolean internalKeepProductUnit() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.costList.keepProductUnit"));
	}
	
	public static boolean keepProductUnit() {
		return instance.internalKeepProductUnit();
	}
	

	@Override
	protected boolean propagateModeEnable(ProductData formulatedProduct) {
		return false;
	}


	/**
	 * <p>Setter for the field <code>entityTplService</code>.</p>
	 *
	 * @param entityTplService a {@link fr.becpg.repo.entity.EntityTplService} object.
	 */
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	/**
	 * <p>Setter for the field <code>packagingHelper</code>.</p>
	 *
	 * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object.
	 */
	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepositoryProductData</code>.</p>
	 *
	 * @param alfrescoRepositoryProductData a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepositoryProductData(AlfrescoRepository<ProductData> alfrescoRepositoryProductData) {
		this.alfrescoRepositoryProductData = alfrescoRepositoryProductData;
	}


	protected abstract void setDataListVisited(ProductData formulatedProduct);

	protected abstract void afterProcess(ProductData formulatedProduct);

	protected abstract List<T> getDataListVisited(ClientData client);

	protected abstract List<T> getDataListVisited(SupplierData supplier);
	
	protected abstract QName getCostFormulaPropName();
	
	protected abstract QName getCostFixedPropName();
	
	protected abstract QName getCostUnitPropName();

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Cost calculating visitor");

			if (getDataListVisited(formulatedProduct) == null) {
				setDataListVisited(formulatedProduct);
			}

			if (formulatedProduct.getDefaultVariantPackagingData() == null) {
				formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
			}

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

		  
			formulateSimpleList(formulatedProduct, getDataListVisited(formulatedProduct), new CostListQtyProvider(formulatedProduct) ,  hasCompoEl);
			
			
			// simulation: take in account cost of components defined on
			// formulated product

			if (hasCompoEl) {
				calculateSimulationCosts(formulatedProduct);
			}

			if (getDataListVisited(formulatedProduct) != null) {

				computeFormulatedList(formulatedProduct, getDataListVisited(formulatedProduct), getCostFormulaPropName(),
						getFormulationErrorMessage());

				ProductUnit unit = formulatedProduct.getUnit();

				for (T c : getDataListVisited(formulatedProduct)) {
					if ((unit != null) && (c.getCharactNodeRef() != null)) {
						Boolean fixed = (Boolean) nodeService.getProperty(c.getCharactNodeRef(), getCostFixedPropName());

						c.setUnit(calculateUnit(unit, (String) nodeService.getProperty(c.getCharactNodeRef(), getCostUnitPropName()), fixed));

						if (!Boolean.TRUE.equals(fixed) && hasCompoEl) {
							if (!internalKeepProductUnit() && unit.isLb()) {
								c.setValue(ProductUnit.lbToKg(c.getValue()));
								c.setMaxi(ProductUnit.lbToKg(c.getMaxi()));
								for (String forecastColumn : c.getForecastColumns()) {
									c.setForecastValue(forecastColumn, ProductUnit.lbToKg(c.getForecastValue(forecastColumn)));
								}
							} else if (!internalKeepProductUnit() && unit.isGal()) {
								c.setValue(ProductUnit.GalToL(c.getValue()));
								c.setMaxi(ProductUnit.GalToL(c.getMaxi()));
								for (String forecastColumn : c.getForecastColumns()) {
									c.setForecastValue(forecastColumn, ProductUnit.GalToL(c.getForecastValue(forecastColumn)));
								}
							}
						}
					}

					if (transientFormulation) {
						c.setTransient(true);
					}
				}
			}

			Composite<T> composite = CompositeHelper.getHierarchicalCompoList(getDataListVisited(formulatedProduct));
			calculateParentCost(formulatedProduct, composite);

			// profitability
			afterProcess(formulatedProduct);
		}
		return true;
	}

	protected abstract String getFormulationErrorMessage();

	/**
	 * Calculate the costListUnit
	 *
	 * @param productUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @param costUnit a {@link java.lang.String} object.
	 * @param isFixed a {@link java.lang.Boolean} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String calculateUnit(ProductUnit productUnit, String costUnit, Boolean isFixed) {

		if (Boolean.TRUE.equals(isFixed)) {
			return costUnit;
		} else {
			return costUnit + calculateSuffixUnit(productUnit);
		}
	}

	/**
	 * Calculate the suffix of the costListUnit
	 *
	 * @param productUnit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (!instance.internalKeepProductUnit()) {
			return UNIT_SEPARATOR + productUnit.getMainUnit().toString();
		}
		return UNIT_SEPARATOR + productUnit.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void synchronizeTemplate(ProductData formulatedProduct, List<T> simpleListDataList, List<T> toRemove) {

		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			getDataListVisited(formulatedProduct.getEntityTpl())
					.forEach(templateCostList -> synchronizeCost(formulatedProduct, templateCostList, simpleListDataList, true, toRemove));

			// check sorting
			int lastSort = 0;
			for (T sl : simpleListDataList) {
				if (sl.getCharactNodeRef() != null) {
					boolean isFound = false;
					for (T tsl : getDataListVisited(formulatedProduct.getEntityTpl())) {
						if (sl.getCharactNodeRef().equals(tsl.getCharactNodeRef())) {
							isFound = true;
							lastSort = (tsl.getSort() != null ? tsl.getSort() : 0) * 100;
							sl.setSort(lastSort);
						}
					}

					if (!isFound) {
						sl.setSort(++lastSort);
					}
				}
			}

		}
		if (formulatedProduct.getClients() != null) {
			for (ClientData client : formulatedProduct.getClients()) {
				getDataListVisited(client).forEach(templateCostList -> synchronizeCost(formulatedProduct, templateCostList, simpleListDataList, false, toRemove));
			}
		}
		

		if (formulatedProduct.getSuppliers() != null) {
			for (NodeRef supplierNodeRef : formulatedProduct.getSuppliers()) {
				SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
				getDataListVisited(supplier).forEach(templateCostList -> synchronizeCost(formulatedProduct, templateCostList, simpleListDataList, false, toRemove));
			}
		}

	}

	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();
	
		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);
	
		if (entityTplNodeRef != null) {
	
			List<T> costList = getDataListVisited(alfrescoRepositoryProductData.findOne(entityTplNodeRef));
	
			for (T costListDataItem : getDataListVisited(formulatedProduct)) {
				for (T c : costList) {
					if ((c.getCharactNodeRef() != null) && c.getCharactNodeRef().equals(costListDataItem.getCharactNodeRef()) && isCharactFormulated(costListDataItem)) {
						mandatoryCharacts.put(c.getCharactNodeRef(), new ArrayList<>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}
	
	private void synchronizeCost(ProductData formulatedProduct, T templateCostListItem, List<T> costList,
			boolean isTemplateCost, List<T> toRemove) {

		boolean addCost = !costList.isEmpty() || templateCostListItem.getPlants().isEmpty() || !Collections.disjoint(templateCostListItem.getPlants(), formulatedProduct.getAllPlants());
		for (T costListItem : costList) {
			// plants
			if (templateCostListItem.getPlants().isEmpty()
					|| !Collections.disjoint(templateCostListItem.getPlants(), formulatedProduct.getAllPlants())) {
				// same cost
				if ((costListItem.getCharactNodeRef() != null) && costListItem.getCharactNodeRef().equals(templateCostListItem.getCharactNodeRef())) {
					if (isTemplateCost) {
						if (templateCostListItem.getParent() != null) {
							costListItem.setParent(findParentByCharactName(costList, templateCostListItem.getParent().getCharactNodeRef()));
						} else {
							costListItem.setParent(null);
						}
					}
					// manual
					if (!Boolean.TRUE.equals(costListItem.getIsManual())) {
						copyTemplateCost(formulatedProduct, templateCostListItem, costListItem);
					}
					toRemove.remove(costListItem);
					addCost = false;
					break;
				}
			} else {
				addCost = false;
			}
		}
		
		
		if (addCost) {
			T costListDataItem = templateCostListItem.copy();
			costListDataItem.setNodeRef(null);
			costListDataItem.setPlants(new ArrayList<>());
			costListDataItem.setParentNodeRef(null);
			if (costListDataItem.getParent() != null) {
				costListDataItem.setParent(findParentByCharactName(costList, costListDataItem.getParent().getCharactNodeRef()));
			}
			copyTemplateCost(formulatedProduct, templateCostListItem, costListDataItem);
			costList.add(costListDataItem);
		}

	}

	private void copyTemplateCost(ProductData formulatedProduct, T templateCostList, T costList) {

		if (logger.isDebugEnabled()) {
			logger.debug("copy cost " + nodeService.getProperty(templateCostList.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME) + " unit "
					+ templateCostList.getUnit() + " PackagingData " + formulatedProduct.getDefaultVariantPackagingData());
		}
		boolean isCalculated = false;

		if ((formulatedProduct.getUnit() != null) && (templateCostList.getUnit() != null)) {
			if (!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())) {
				if (formulatedProduct.getUnit().isP()) {
					if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {
						calculateValues(templateCostList, costList, false, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if ((formulatedProduct.getDefaultVariantPackagingData() != null)
								&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {
							Double productQty = formulatedProduct.getQty() != null ? formulatedProduct.getQty() : FormulationHelper.QTY_FOR_PIECE;
							calculateValues(templateCostList, costList, true,
									(double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() * productQty);
						}
						isCalculated = true;
					}
				} else if (formulatedProduct.getUnit().isWeight() || formulatedProduct.getUnit().isVolume()) {
					if (templateCostList.getUnit().endsWith("P")) {
						calculateValues(templateCostList, costList, true, FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						isCalculated = true;
					} else if (templateCostList.getUnit().endsWith("Pal")) {
						if ((formulatedProduct.getDefaultVariantPackagingData() != null)
								&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {
							calculateValues(templateCostList, costList, true,
									(double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet()
											* FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));
						}
						isCalculated = true;
					}
				}
			}
		}

		if (!isCalculated) {
			calculateValues(templateCostList, costList, null, null);
		}
	}

	private void calculateValues(T templateCostList, T costList, Boolean divide, Double qty) {

		if (logger.isDebugEnabled()) {
			logger.debug("calculateValues " + nodeService.getProperty(templateCostList.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME));
		}

		Double value = templateCostList.getValue();
		Double maxi = templateCostList.getMaxi();

		if ((divide != null) && (qty != null)) {
			if (Boolean.TRUE.equals(divide)) {
				value = divide(value, qty);
				maxi = divide(maxi, qty);
			} else {
				value = multiply(value, qty);
				maxi = multiply(maxi, qty);
			}
		}

		if (value != null) {
			costList.setValue(value);
		}
		if (maxi != null) {
			costList.setMaxi(maxi);
		}
		for (String forecastColumn : costList.getForecastColumns()) {
			if (templateCostList.getForecastValue(forecastColumn) != null) {
				Double forecastValue = templateCostList.getForecastValue(forecastColumn);
				if ((divide != null) && (qty != null)) {
					if (Boolean.TRUE.equals(divide)) {
						forecastValue = divide(forecastValue, qty);
					} else {
						forecastValue = multiply(forecastValue, qty);
					}
				}
				costList.setForecastValue(forecastColumn, forecastValue);
			}
		}
		
	}

	private Double divide(Double a, Double b) {
		if ((a != null) && (b != null) && (b != 0d)) {
			return a / b;
		}
		return null;
	}

	private Double multiply(Double a, Double b) {
		if ((a != null) && (b != null)) {
			return a * b;
		}
		return null;
	}

	private void calculateParentCost(ProductData formulatedProduct, Composite<T> composite) {
		if (!composite.isLeaf()) {

			Double value = 0d;
			Double maxi = 0d;
			Map<String, Double> variantValues = new HashMap<>();
			Map<String, Double> forecastValues = new HashMap<>();
			
			for (Composite<T> component : composite.getChildren()) {
				calculateParentCost(formulatedProduct, component);
				T costListDataItem = component.getData();
				if (costListDataItem.getComponentNodeRef() != null) {
					return;
				}
				if (costListDataItem.getValue() != null) {
					value += costListDataItem.getValue();
				}
				if (costListDataItem.getMaxi() != null) {
					maxi += costListDataItem.getMaxi();
				}
				for (String forecastColumn : costListDataItem.getForecastColumns()) {
					if (costListDataItem.getForecastValue(forecastColumn) != null) {
						Double forecastValue = forecastValues.get(forecastColumn);
						if (forecastValue == null) {
							forecastValue = 0d;
						}
						forecastValue += costListDataItem.getForecastValue(forecastColumn);
						forecastValues.put(forecastColumn, forecastValue);
					}
				}
					
				if (costListDataItem instanceof VariantAwareDataItem) {
					for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
						Double variantValue = costListDataItem.getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
						if (variantValue != null) {
							if (variantValues.get(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) != null) {
								variantValues.put(VariantAwareDataItem.VARIANT_COLUMN_NAME + i,
										variantValues.get(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) + variantValue);
							} else {
								variantValues.put(VariantAwareDataItem.VARIANT_COLUMN_NAME + i, variantValue);
							}
						}
					}
				}
			}
			if (!composite.isRoot()) {
				composite.getData().setValue(value);
				composite.getData().setMaxi(maxi);
				for (String forecastColumn : composite.getData().getForecastColumns()) {
					composite.getData().setForecastValue(forecastColumn, forecastValues.get(forecastColumn));
				}

				if (composite.getData() instanceof VariantAwareDataItem) {
					for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
						if (variantValues.get(VariantAwareDataItem.VARIANT_COLUMN_NAME + i) != null) {
							((VariantAwareDataItem) composite.getData()).setValue(variantValues.get(VariantAwareDataItem.VARIANT_COLUMN_NAME + i),
									VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
						}
					}
				}
			}
		}
	}

	private void calculateSimulationCosts(ProductData formulatedProduct) {
		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct,null);

		for (T simulatedCost : getDataListVisited(formulatedProduct)) {
			if ((simulatedCost.getComponentNodeRef() != null) && (simulatedCost.getParent() != null)) {

				ProductData componentData = alfrescoRepositoryProductData.findOne(simulatedCost.getComponentNodeRef());
				Double qtyComponent = SimulationCostHelper.getComponentQuantity(formulatedProduct, componentData);

				if ((simulatedCost.getSimulatedValue() != null) && simulatedCost.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					simulatedCost.getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}

				for (T c2 : getDataListVisited(componentData)) {
					if (c2.getCharactNodeRef().equals(simulatedCost.getParent().getCharactNodeRef()) && (simulatedCost.getSimulatedValue() != null) ) {

						if (logger.isDebugEnabled()) {
							logger.debug("add simulationCost " + "c2 value " + c2.getValue() + "c simulated value " + simulatedCost.getSimulatedValue()
									+ " qty component " + qtyComponent + " netQty " + netQty);
						}
						if (c2.getValue() != null) {
							simulatedCost.setValue(((simulatedCost.getSimulatedValue() - c2.getValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						} else {
							simulatedCost.setValue(((simulatedCost.getSimulatedValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						}
						if (simulatedCost.getParent().getValue() != null) {
							simulatedCost.getParent().setValue(simulatedCost.getParent().getValue() + simulatedCost.getValue());
						} else {
							simulatedCost.getParent().setValue(simulatedCost.getValue());
						}
						break;
					}
				}
			}
			if (simulatedCost.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) && (simulatedCost.getSimulatedValue() == null) && (simulatedCost.getParent() != null)
					&& !nodeService.hasAspect(simulatedCost.getParent().getCharactNodeRef(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
				simulatedCost.getParent().getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
			}
		}
	}

}
