/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
package fr.becpg.repo.product.formulation.details;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class CostCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(CostCharactDetailsVisitor.class);

	private PackagingHelper packagingHelper;

	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	@Override
	public CharactDetails visit(ProductData formulatedProduct, List<NodeRef> dataListItems, Integer level) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (level == null) {
			level = 0;
		}

		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		SimpleCharactUnitProvider unitProvider = item -> {
			CostListDataItem c = (CostListDataItem) item;
			return CostsCalculatingFormulationHandler.calculateUnit(formulatedProduct.getUnit(),
					(String) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTCURRENCY),
					(Boolean) nodeService.getProperty(c.getCost(), PLMModel.PROP_COSTFIXED));
		};

		visitRecurCost(formulatedProduct, ret, 0, level, netQty, netQty, unitProvider);

		return ret;
	}

	public CharactDetails visitRecurCost(ProductData formulatedProduct, CharactDetails ret, Integer currLevel, Integer maxLevel, Double subQuantity,
			Double netQty, SimpleCharactUnitProvider unitProvider) throws FormulateException {

		if (formulatedProduct.getDefaultVariantPackagingData() == null) {
			formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
		}

		/*
		 * Composition
		 */

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(
					formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
			visitCompoListChildren(formulatedProduct, composite, ret, formulatedProduct.getProductLossPerc(), subQuantity, netQty,
					currLevel, maxLevel, unitProvider);
		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double qty = (FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, nodeService)
						/ FormulationHelper.getNetQtyForCost(formulatedProduct)) * subQuantity;

				visitPart(formulatedProduct.getNodeRef(), packagingListDataItem.getProduct(), ret, qty, null, netQty, currLevel, unitProvider);

				 if ((maxLevel < 0) || (currLevel < maxLevel)) {
					 logger.debug("Finding one packaging with nr=" + packagingListDataItem.getProduct());
					 
					 visitRecurCost((ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct()), ret, currLevel + 1, maxLevel, qty,
								netQty, unitProvider);
				 }

			}

		}

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			/*
			 * ProcessList
			 */

			for (ProcessListDataItem processListDataItem : formulatedProduct
					.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				Double qty = (FormulationHelper.getQty(formulatedProduct, processListDataItem)
						/ FormulationHelper.getNetQtyForCost(formulatedProduct)) * subQuantity;
				if ((processListDataItem.getResource() != null) && (qty != null)) {
					if (ProcessListUnit.P.equals(processListDataItem.getUnit()) && ProductUnit.P.equals(formulatedProduct.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}

					visitPart(formulatedProduct.getNodeRef(), processListDataItem.getResource(), ret, qty, null, netQty, currLevel, unitProvider);

					 if ((maxLevel < 0) || (currLevel < maxLevel)) {
					
						 visitRecurCost((ProductData) alfrescoRepository.findOne(processListDataItem.getResource()), ret, currLevel + 1, maxLevel, qty,
									netQty, unitProvider);
					 }

				}
			}

		}

		visiteTemplateCosts(formulatedProduct, ret);

		return ret;
	}

	private void visiteTemplateCosts(ProductData formulatedProduct, CharactDetails ret) {

		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {

			visitTemplateCostList(formulatedProduct, formulatedProduct.getEntityTpl().getNodeRef(), formulatedProduct.getEntityTpl().getCostList(),
					ret);
		}
		if (formulatedProduct.getClients() != null) {
			for (ClientData client : formulatedProduct.getClients()) {
				visitTemplateCostList(formulatedProduct, client.getNodeRef(), client.getCostList(), ret);
			}
		}

	}

	private void visitTemplateCostList(ProductData formulatedProduct, NodeRef entityNodeRef, List<CostListDataItem> templateCostLists,
			CharactDetails ret) {
		NodeRef plantNodeRef = formulatedProduct.getPlants().isEmpty() ? null : formulatedProduct.getPlants().get(0);

		for (CostListDataItem templateCostList : templateCostLists) {

			if ((templateCostList != null) && ret.hasElement(templateCostList.getCharactNodeRef()) && (templateCostList.getValue() != null)) {
				// plants
				if (templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)) {

					Double netQty = 1d;
					Double qtyUsed = 1d;

					if ((formulatedProduct.getUnit() != null) && (templateCostList.getUnit() != null)) {
						if (!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())) {
							if (FormulationHelper.isProductUnitP(formulatedProduct.getUnit())) {
								if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {

									qtyUsed = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);

								} else if (templateCostList.getUnit().endsWith("Pal")) {
									if ((formulatedProduct.getDefaultVariantPackagingData() != null)
											&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {

										netQty = (double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet();

									}
								}
							} else if (FormulationHelper.isProductUnitKg(formulatedProduct.getUnit())
									|| FormulationHelper.isProductUnitLiter(formulatedProduct.getUnit())) {
								if (templateCostList.getUnit().endsWith("P")) {
									qtyUsed = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);

								} else if (templateCostList.getUnit().endsWith("Pal")) {
									if ((formulatedProduct.getDefaultVariantPackagingData() != null)
											&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {

										netQty = ((double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet()
												* FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));

									}
								}
							}
						}
					}

					//All sorts of cost that would be unfiltered land here
					Double value = FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getValue(), netQty, templateCostList.getUnit());
					Double previous = templateCostList.getPreviousValue() != null ? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getPreviousValue(), netQty, templateCostList.getUnit()) : null;
					Double future = templateCostList.getFutureValue() != null ? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getFutureValue(), netQty, templateCostList.getUnit()) : null;
					Double mini = templateCostList.getMini() != null ? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getMini(), netQty, templateCostList.getUnit()) : null;
					Double maxi = templateCostList.getMaxi() != null ? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getMaxi(), netQty, templateCostList.getUnit()) : null;
					
					String unit = CostsCalculatingFormulationHandler.calculateUnit(formulatedProduct.getUnit(),
							(String) nodeService.getProperty(templateCostList.getCost(), PLMModel.PROP_COSTCURRENCY),
							(Boolean) nodeService.getProperty(templateCostList.getCost(), PLMModel.PROP_COSTFIXED));

					CharactDetailsValue key = new CharactDetailsValue(formulatedProduct.getNodeRef(), entityNodeRef, value, 0, unit);
					key.setPreviousValue(previous);
					key.setFutureValue(future);
					key.setMini(mini);
					key.setMaxi(maxi);
					
					
					logger.debug("Adding keyValue "+key);
					ret.addKeyValue(templateCostList.getCharactNodeRef(), key);

				}
			}
		}

	}

	private void visitCompoListChildren(ProductData productData, Composite<CompoListDataItem> composite, CharactDetails ret, Double parentLossRatio,
			Double subQty, Double netQty, Integer currLevel, Integer maxLevel, SimpleCharactUnitProvider unitProvider) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			if (!component.isLeaf()) {

				// take in account the loss perc
				Double lossPerc = component.getData().getLossPerc() != null ? component.getData().getLossPerc() : 0d;
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = component;
				visitCompoListChildren(productData, c, ret, newLossPerc, subQty, netQty, currLevel, maxLevel, unitProvider);
			} else {
				CompoListDataItem compoListDataItem = component.getData();

				Double qty = (FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio,
						ProductUnit.getUnit((String) nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)), CostsCalculatingFormulationHandler.keepProductUnit)
						/ FormulationHelper.getNetQtyForCost(productData)) * subQty;

				visitPart(productData.getNodeRef(), compoListDataItem.getProduct(), ret, qty, qty, netQty, currLevel, unitProvider);

				if (((maxLevel < 0) || (currLevel < maxLevel))
						&& !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {
					visitRecurCost((ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), ret, currLevel + 1, maxLevel, qty,
							netQty, unitProvider);
				}

			}
		}
	}
}
