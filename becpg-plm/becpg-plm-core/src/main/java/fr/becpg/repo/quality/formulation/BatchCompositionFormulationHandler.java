/*******************************************************************************
 * Copyright (C) 2010-2025 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.quality.formulation;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.StockType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.AbstractCompositionQtyCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulaHelper;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>BatchCompositionFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchCompositionFormulationHandler extends AbstractCompositionQtyCalculatingFormulationHandler<BatchData> {

	private SpelFormulaService formulaService;

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/** {@inheritDoc} */
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
				if ((batchData.getUnit() != null) && batchData.getUnit().isP()) {
					ratio = batchQty;
				} else if ((batchData.getUnit() != null) && batchData.getUnit().isPerc()) {
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
					toAdd.setStockType(StockType.Product);
					if(productData.getProductLossPerc()!=null) {
						if(toAdd.getLossPerc()!=null) {
							toAdd.setLossPerc(FormulationHelper.calculateLossPerc(productData.getProductLossPerc(), toAdd.getLossPerc()));
						} else {
							toAdd.setLossPerc(productData.getProductLossPerc());
						}
					}
					
					if ((toAdd.getQtySubFormula() != null) && !ProductUnit.Perc.equals(compoListItem.getCompoListUnit())) {
						toAdd.setQtySubFormula(toAdd.getQtySubFormula() * ratio);
					}
					batchData.getCompoList().add(toAdd);
				}

			}

			Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(batchData.getCompoList());

			// calculate on every item
			visitQtyChildren(batchData.getProduct(), batchQty, compositeAll);

			copyTemplateDynamicCharactLists(batchData);

			StandardEvaluationContext context = formulaService.createEntitySpelContext(batchData);

			FormulaHelper.computeFormula(batchData, context, batchData.getCompoListView(), null);

		}

		return true;
	}

	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct
	 */
	private void copyTemplateDynamicCharactLists(BatchData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			BatchData templateBatchData = formulatedProduct.getEntityTpl();

			FormulaHelper.copyTemplateDynamicCharactList(templateBatchData.getCompoListView().getDynamicCharactList(),
					formulatedProduct.getCompoListView().getDynamicCharactList());

		}

	}

}
