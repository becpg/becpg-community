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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.helper.SimulationCostHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

public class LCACalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<LCAListDataItem> {
	
	private static final Log logger = LogFactory.getLog(LCACalculatingFormulationHandler.class);

	private EntityTplService entityTplService;

	private PackagingHelper packagingHelper;

	private AlfrescoRepository<ProductData> alfrescoRepositoryProductData;
	
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}
	
	public void setAlfrescoRepositoryProductData(AlfrescoRepository<ProductData> alfrescoRepositoryProductData) {
		this.alfrescoRepositoryProductData = alfrescoRepositoryProductData;
	}

	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Lca calculating visitor");

			if (formulatedProduct.getLcaList() == null) {
				formulatedProduct.setLcaList(new LinkedList<>());
			}

			if (formulatedProduct.getDefaultVariantPackagingData() == null) {
				formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
			}

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

		  
			formulateSimpleList(formulatedProduct, formulatedProduct.getLcaList(), new CostListQtyProvider(formulatedProduct) ,  hasCompoEl);
			
			
			// simulation: take in account cost of components defined on
			// formulated product

			if (hasCompoEl) {
				calculateSimulationLCAs(formulatedProduct);
			}

			if (formulatedProduct.getLcaList() != null) {

				computeFormulatedList(formulatedProduct, formulatedProduct.getLcaList(), PLMModel.PROP_LCA_FORMULA,
						"message.formulate.costList.error");

				ProductUnit unit = formulatedProduct.getUnit();

				for (LCAListDataItem c : formulatedProduct.getLcaList()) {
					if ((unit != null) && (c.getLca() != null)) {
						Boolean fixed = (Boolean) nodeService.getProperty(c.getLca(), PLMModel.PROP_LCAFIXED);

						c.setUnit(CostsCalculatingFormulationHandler.calculateUnit(unit, (String) nodeService.getProperty(c.getLca(), PLMModel.PROP_LCAUNIT), fixed));

						if (!Boolean.TRUE.equals(fixed) && hasCompoEl) {
							if (unit.isLb()) {
								c.setValue(ProductUnit.lbToKg(c.getValue()));
								c.setMaxi(ProductUnit.lbToKg(c.getMaxi()));
								c.setPreviousValue(ProductUnit.lbToKg(c.getPreviousValue()));
								c.setFutureValue(ProductUnit.lbToKg(c.getFutureValue()));
							} else if (unit.isGal()) {
								c.setValue(ProductUnit.GalToL(c.getValue()));
								c.setMaxi(ProductUnit.GalToL(c.getMaxi()));
								c.setPreviousValue(ProductUnit.GalToL(c.getPreviousValue()));
								c.setFutureValue(ProductUnit.GalToL(c.getFutureValue()));
							}
						}
					}

					if (transientFormulation) {
						c.setTransient(true);
					}
				}
			}
			
			Composite<LCAListDataItem> composite = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getLcaList());
			calculateParentLCA(formulatedProduct, composite);

		}
		return true;
	}
	
	private void calculateParentLCA(ProductData formulatedProduct, Composite<LCAListDataItem> composite) {
		if (!composite.isLeaf()) {

			Double value = 0d;
			Double maxi = 0d;
			Double previousValue = 0d;
			Double futureValue = 0d;
			Map<String, Double> variantValues = new HashMap<>();
			for (Composite<LCAListDataItem> component : composite.getChildren()) {
				calculateParentLCA(formulatedProduct, component);
				LCAListDataItem lcaListDataItem = component.getData();
				if (lcaListDataItem.getComponentNodeRef() != null) {
					return;
				}
				if (lcaListDataItem.getValue() != null) {
					value += lcaListDataItem.getValue();
				}
				if (lcaListDataItem.getMaxi() != null) {
					maxi += lcaListDataItem.getMaxi();
				}
				if (lcaListDataItem.getPreviousValue() != null) {
					previousValue += lcaListDataItem.getPreviousValue();
				}
				if (lcaListDataItem.getFutureValue() != null) {
					futureValue += lcaListDataItem.getFutureValue();
				}
				if (lcaListDataItem instanceof VariantAwareDataItem) {
					for (int i = 1; i <= VariantAwareDataItem.VARIANT_COLUMN_SIZE; i++) {
						Double variantValue = lcaListDataItem.getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME + i);
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
				composite.getData().setPreviousValue(previousValue);
				composite.getData().setFutureValue(futureValue);

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
	
	private void calculateSimulationLCAs(ProductData formulatedProduct) {
		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		for (LCAListDataItem c : formulatedProduct.getLcaList()) {
			if ((c.getComponentNodeRef() != null) && (c.getParent() != null)) {

				ProductData componentData = alfrescoRepositoryProductData.findOne(c.getComponentNodeRef());
				Double qtyComponent = SimulationCostHelper.getComponentQuantity(formulatedProduct, componentData);

				if ((c.getSimulatedValue() != null) && c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					c.getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}

				for (LCAListDataItem c2 : componentData.getLcaList()) {
					if (c2.getLca().equals(c.getParent().getLca()) && (c.getSimulatedValue() != null)) {

						if (logger.isDebugEnabled()) {
							logger.debug("add simulationLCA " + "c2 value " + c2.getValue() + "c simulated value " + c.getSimulatedValue()
									+ " qty component " + qtyComponent + " netQty " + netQty);
						}
						if (c2.getValue() != null) {
							c.setValue(((c.getSimulatedValue() - c2.getValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						} else {
							c.setValue(((c.getSimulatedValue()) * qtyComponent) / (netQty != 0 ? netQty : 1d));
						}
						if (c.getParent().getValue() != null) {
							c.getParent().setValue(c.getParent().getValue() + c.getValue());
						} else {
							c.getParent().setValue(c.getValue());
						}
						break;
					}
				}
			}
			if (c.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM) && (c.getSimulatedValue() == null) && (c.getParent() != null)
					&& !nodeService.hasAspect(c.getParent().getLca(), BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
				c.getParent().getAspectsToRemove().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
			}
		}
	}

	@Override
	protected Class<LCAListDataItem> getInstanceClass() {
		return LCAListDataItem.class;
	}

	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getLcaList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_LCALIST)));

	}

	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.LCA;
	}

	@Override
	protected List<LCAListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLcaList();
	}

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {

		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();

		NodeRef entityTplNodeRef = entityTplService.getEntityTpl(componentType);

		if (entityTplNodeRef != null) {

			List<LCAListDataItem> lcaList = alfrescoRepositoryProductData.findOne(entityTplNodeRef).getLcaList();

			for (LCAListDataItem lcaListDataItem : formulatedProduct.getLcaList()) {
				for (LCAListDataItem c : lcaList) {
					if ((c.getLca() != null) && c.getLca().equals(lcaListDataItem.getLca()) && isCharactFormulated(lcaListDataItem)) {
						mandatoryCharacts.put(c.getLca(), new ArrayList<>());
						break;
					}
				}
			}
		}
		return mandatoryCharacts;
	}

	
}
