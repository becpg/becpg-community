package fr.becpg.repo.quality.formulation;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulaHelper;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class BatchCompositionFormulationHandler extends FormulationBaseHandler<BatchData> {

	private static Log logger = LogFactory.getLog(BatchCompositionFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private SpelFormulaService formulaService;

	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(BatchData batchData) {

		if (!(batchData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL))) {

			Double batchQty = batchData.getBatchQty();

			if (batchQty == null) {
				batchQty = 1d;
			}

			if ((batchData.getUnit() != null) && (batchData.getUnit().isVolume() || batchData.getUnit().isWeight())) {
				batchQty = batchQty / batchData.getUnit().getUnitFactor();
			}

			if ((batchData.getProduct() != null) && !batchData.hasCompoListEl()) {

				ProductData productData = batchData.getProduct();

				Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);

				// 500 product of 5 Kg

				Double ratio;
				if (batchData.getUnit() != null && batchData.getUnit().isP()) {
					ratio = batchQty;
				} else if (batchData.getUnit() != null && batchData.getUnit().isPerc()) {
					ratio = batchQty / 100;
				} else {
					ratio = batchQty / productNetWeight;
				}

				for (CompoListDataItem compoListItem : productData
						.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

					CompoListDataItem toAdd = new CompoListDataItem(compoListItem);
					toAdd.setName(null);
					toAdd.setParentNodeRef(null);
					toAdd.setNodeRef(null);
					toAdd.setVariants(new ArrayList<>());
					toAdd.setDeclType(DeclarationType.DoNotDetails);
					if (toAdd.getQtySubFormula() != null) {
						if (!ProductUnit.Perc.equals(compoListItem.getCompoListUnit())) {
							toAdd.setQtySubFormula(toAdd.getQtySubFormula() * ratio);
						}
					}
					batchData.getCompoList().add(toAdd);
				}

			}

			Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(batchData.getCompoList());

			// calculate on every item	
			visitQtyChildren(batchQty, compositeAll);

			copyTemplateDynamicCharactLists(batchData);

			StandardEvaluationContext context = formulaService.createEntitySpelContext(batchData);

			FormulaHelper.computeFormula(batchData, context, batchData.getCompoListView(), null);

		}

		return true;
	}

	private void visitQtyChildren(Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double qtyInKg = calculateQtyInKg(component.getData());
			if (logger.isDebugEnabled()) {
				logger.debug("qtySubFormula: " + qtyInKg + " parentQty: " + parentQty);
			}
			if (qtyInKg != null) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {
					qtyInKg = (qtyInKg * parentQty) / 100;
				}

				component.getData().setQty(qtyInKg);
			}

			// calculate children
			if (!component.isLeaf()) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {

					visitQtyChildren(parentQty, component);

					// no yield but calculate % of composite
					Double compositePerc = 0d;
					boolean isUnitPerc = true;
					for (Composite<CompoListDataItem> child : component.getChildren()) {
						compositePerc += child.getData().getQtySubFormula();
						isUnitPerc = isUnitPerc && ProductUnit.Perc.equals(child.getData().getCompoListUnit());
						if (!isUnitPerc) {
							break;
						}
					}
					if (isUnitPerc) {
						component.getData().setQtySubFormula(compositePerc);
						component.getData().setQty((compositePerc * parentQty) / 100);
					}
				} else {
					visitQtyChildren(component.getData().getQty(), component);
				}
			}
		}
	}

	private Double calculateQtyInKg(CompoListDataItem compoListDataItem) {
		Double qty = compoListDataItem.getQtySubFormula();
		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();

		ProductData componentProductData = alfrescoRepository.findOne(compoListDataItem.getProduct());

		if ((qty != null) && (compoListUnit != null)) {

			Double unitFactor = compoListUnit.getUnitFactor();

			if (compoListUnit.isWeight()) {
				return qty / unitFactor;
			} else if (compoListUnit.isP()) {

				Double productQty = FormulationHelper.QTY_FOR_PIECE;

				if ((componentProductData.getUnit() != null) && componentProductData.getUnit().isP() && (componentProductData.getQty() != null)) {
					productQty = componentProductData.getQty();
				}

				return (FormulationHelper.getNetWeight(componentProductData, FormulationHelper.DEFAULT_NET_WEIGHT) * qty) / productQty;

			} else if (compoListUnit.isVolume()) {

				qty = qty / unitFactor;

				Double overrun = compoListDataItem.getOverrunPerc();
				if (compoListDataItem.getOverrunPerc() == null) {
					overrun = FormulationHelper.DEFAULT_OVERRUN;
				}

				Double density = componentProductData.getDensity();
				if ((density == null) || density.equals(0d)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot calculate qty since density is null or equals to 0");
					}
				} else {
					return (qty * density * 100) / (100 + overrun);

				}

			}
			return qty;
		}

		return FormulationHelper.DEFAULT_COMPONANT_QUANTITY;
	}


	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	private void copyTemplateDynamicCharactLists(BatchData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			BatchData templateBatchData = formulatedProduct.getEntityTpl();

			FormulaHelper.copyTemplateDynamicCharactList(templateBatchData.getCompoListView().getDynamicCharactList(),
					formulatedProduct.getCompoListView().getDynamicCharactList());

		}

	}
	

}
