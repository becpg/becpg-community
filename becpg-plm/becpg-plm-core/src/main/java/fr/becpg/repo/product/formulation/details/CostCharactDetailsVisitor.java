/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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
import fr.becpg.repo.repository.model.UnitAwareDataItem;
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
		
		if(level == null){
			level = 0;
		}

		Double netQty = FormulationHelper.getNetQtyForCost(formulatedProduct);

		visitRecur(formulatedProduct,ret,0, level, netQty,  netQty);

		return ret;
	}
	
	
	public CharactDetails visitRecur(ProductData formulatedProduct, CharactDetails ret, Integer currLevel, Integer maxLevel, Double subQuantity , Double netQty)
			throws FormulateException {
		
		
		if (formulatedProduct.getDefaultVariantPackagingData() == null) {
			formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
		}

		/*
		 * Composition
		 */

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(
					formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
			visitCompoListChildren(formulatedProduct, composite, ret, CostsCalculatingFormulationHandler.DEFAULT_LOSS_RATIO, subQuantity, netQty , currLevel, maxLevel);
		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				Double qty = FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, nodeService)
						/ FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT) * subQuantity;

				visitPart(formulatedProduct.getNodeRef(), packagingListDataItem.getProduct(), ret, qty, netQty, currLevel);
				
//				if ((maxLevel < 0) || (currLevel < maxLevel)) {
//
//					visitRecur((ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), ret, currLevel++, maxLevel, qty);
//				}
				
			}

		}

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			/*
			 * ProcessList
			 */

			for (ProcessListDataItem processListDataItem : formulatedProduct
					.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				Double qty = FormulationHelper.getQty(formulatedProduct, processListDataItem)
						/ FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT) * subQuantity;
				if (processListDataItem.getResource() != null && qty != null) {
					if (ProcessListUnit.P.equals(processListDataItem.getUnit())) {
						netQty = FormulationHelper.QTY_FOR_PIECE;
					}

					visitPart(formulatedProduct.getNodeRef(), processListDataItem.getResource(), ret, qty, netQty,currLevel);
					
//					if ((maxLevel < 0) || (currLevel < maxLevel)) {
//
//						visitRecur((ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), ret, currLevel++, maxLevel, qty);
//					}
					
				}
			}

		}

		visiteTemplateCosts(formulatedProduct, ret);
	

		return ret;
	}
	

	private void visiteTemplateCosts(ProductData formulatedProduct, CharactDetails ret) {

		if (formulatedProduct.getEntityTpl() != null && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {

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

			if (templateCostList != null && ret.hasElement(templateCostList.getCharactNodeRef()) && templateCostList.getValue()!=null) {
				// plants
				if (templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)) {

					Double netQty = 1d;
					Double qtyUsed = 1d;

					if (formulatedProduct.getUnit() != null && templateCostList.getUnit() != null) {
						if (!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())) {
							if (FormulationHelper.isProductUnitP(formulatedProduct.getUnit())) {
								if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {

									qtyUsed = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);

								} else if (templateCostList.getUnit().endsWith("Pal")) {
									if (formulatedProduct.getDefaultVariantPackagingData() != null
											&& formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null
											&& formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null) {

										netQty = ((double) formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes()
												* formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet());

									}
								}
							} else if (FormulationHelper.isProductUnitKg(formulatedProduct.getUnit())
									|| FormulationHelper.isProductUnitLiter(formulatedProduct.getUnit())) {
								if (templateCostList.getUnit().endsWith("P")) {
									qtyUsed =  FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);

								} else if (templateCostList.getUnit().endsWith("Pal")) {
									if (formulatedProduct.getDefaultVariantPackagingData() != null
											&& formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null
											&& formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet() != null) {

										netQty = ((double) formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes()
														* formulatedProduct.getDefaultVariantPackagingData().getBoxesPerPallet()
														* FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d));

									}
								}
							}
						}
					}

					String unit = null;
					if(templateCostList instanceof UnitAwareDataItem){
						unit = ((UnitAwareDataItem)templateCostList).getUnit();
					}
					Double value = FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getValue(), netQty, unit);

					ret.addKeyValue(templateCostList.getCharactNodeRef(), new CharactDetailsValue(formulatedProduct.getNodeRef(), entityNodeRef, value,0 , templateCostList.getUnit()));

				}
			}
		}

	}

	private void visitCompoListChildren(ProductData productData, Composite<CompoListDataItem> composite, CharactDetails ret,
			Double parentLossRatio, Double subQty, Double netQty, Integer  currLevel, Integer maxLevel) throws FormulateException {

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
				visitCompoListChildren(productData, c, ret, newLossPerc, subQty, netQty, currLevel, maxLevel);
			} else {
				CompoListDataItem compoListDataItem = component.getData();

				Double qty = FormulationHelper.getQtyForCost(compoListDataItem, parentLossRatio,
						ProductUnit.getUnit((String) nodeService.getProperty(compoListDataItem.getProduct(), PLMModel.PROP_PRODUCT_UNIT)))
						/ FormulationHelper.getNetQtyInLorKg(productData, FormulationHelper.DEFAULT_NET_WEIGHT) * subQty;
			
				visitPart(productData.getNodeRef(), compoListDataItem.getProduct(), ret, qty, netQty, currLevel);
				
				if (((maxLevel < 0) || (currLevel < maxLevel)) && !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {
					visitRecur((ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct()), ret, currLevel+1, maxLevel, qty, netQty);
				}
				

			}
		}
	}
}
