/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AbstractCostListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.CostCalculatingHelper;
import fr.becpg.repo.product.formulation.CostListQtyProvider;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.FormulatedQties;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>CostCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractCostCharactDetailsVisitor<T extends AbstractCostListDataItem<T>> extends SimpleCharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(AbstractCostCharactDetailsVisitor.class);

	private PackagingHelper packagingHelper;

	/**
	 * <p>Setter for the field <code>packagingHelper</code>.</p>
	 *
	 * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object.
	 */
	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	/** {@inheritDoc} */
	@Override
	public CharactDetails visit(ProductData formulatedProduct, List<NodeRef> dataListItems, Integer level) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (level == null) {
			level = 0;
		}

		SimpleCharactUnitProvider unitProvider = item -> {
			AbstractCostListDataItem<?> c = (AbstractCostListDataItem<?>) item;
			return CostsCalculatingFormulationHandler.calculateUnit(formulatedProduct.getUnit(),
					(String) nodeService.getProperty(c.getCost(), getCostUnitPropName()),
					(Boolean) nodeService.getProperty(c.getCost(), getCostFixedPropName()));
		};

		visitRecurCost(formulatedProduct, ret, 0, level, 1d, unitProvider);

		if ((formulatedProduct.getUnit() != null) && (formulatedProduct.getUnit().isLb() || formulatedProduct.getUnit().isGal())) {
			for (NodeRef costItemNodeRef : dataListItems) {
				AbstractCostListDataItem<?> c = (AbstractCostListDataItem<?>) alfrescoRepository.findOne(costItemNodeRef);
				Boolean fixed = (Boolean) nodeService.getProperty(c.getCost(), getCostFixedPropName());
				if (!Boolean.TRUE.equals(fixed)) {
					if (ret.getData().containsKey(c.getCharactNodeRef())) {

						for (CharactDetailsValue value : ret.getData().get(c.getCharactNodeRef())) {
							if (formulatedProduct.getUnit().isLb()) {
								value.setValue(ProductUnit.lbToKg(value.getValue()));
								value.setMaxi(ProductUnit.lbToKg(value.getMaxi()));
								value.setPreviousValue(ProductUnit.lbToKg(value.getPreviousValue()));
								value.setFutureValue(ProductUnit.lbToKg(value.getFutureValue()));
							} else {
								value.setValue(ProductUnit.GalToL(value.getValue()));
								value.setMaxi(ProductUnit.GalToL(value.getMaxi()));
								value.setPreviousValue(ProductUnit.GalToL(value.getPreviousValue()));
								value.setFutureValue(ProductUnit.GalToL(value.getFutureValue()));
							}

						}
					}

				}
			}
		}

		return ret;
	}

	/**
	 * <p>visitRecurCost.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param ret a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param currLevel a {@link java.lang.Integer} object.
	 * @param maxLevel a {@link java.lang.Integer} object.
	 * @param subQuantity a {@link java.lang.Double} object.
	 * @param netQty a {@link java.lang.Double} object.
	 * @param unitProvider a SimpleCharactUnitProvider object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	private CharactDetails visitRecurCost(ProductData formulatedProduct, CharactDetails ret, Integer currLevel, Integer maxLevel, Double ratio,
			SimpleCharactUnitProvider unitProvider) throws FormulateException {

		CostListQtyProvider qtyProvider = new CostListQtyProvider(formulatedProduct);

		if (formulatedProduct.getDefaultVariantPackagingData() == null) {
			formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
		}

		/*
		 * Composition
		 */

		if (formulatedProduct.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			Composite<CompoListDataItem> composite = CompositeHelper.getHierarchicalCompoList(
					formulatedProduct.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>())));
			visitCompoListChildren(formulatedProduct, composite, ret, formulatedProduct.getProductLossPerc(), ratio, currLevel, maxLevel, qtyProvider,
					unitProvider);
		}

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			/*
			 * PackagingList
			 */

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				if ((packagingListDataItem.getProduct() != null)
						&& ((packagingListDataItem.getIsRecycle() == null) || !packagingListDataItem.getIsRecycle())) {

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());

					Double qty = qtyProvider.getQty(packagingListDataItem, partProduct) * ratio;

					FormulatedQties qties = new FormulatedQties(qty, qty, qtyProvider.getNetQty(null), qtyProvider.getNetWeight(null));

					Double newRatio = qty / qtyProvider.getNetQty(null);

					visitPart(formulatedProduct, partProduct, packagingListDataItem.getNodeRef(), ret, qties, currLevel, unitProvider);

					if ((maxLevel < 0) || (currLevel < maxLevel)) {
						logger.debug("Finding one packaging with nr=" + packagingListDataItem.getProduct());

						visitRecurCost(partProduct, ret, currLevel + 1, maxLevel, newRatio, unitProvider);
					}
				}

			}

		}

		if (formulatedProduct.hasProcessListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
			/*
			 * ProcessList
			 */
			Double netQtyForCost = qtyProvider.getNetQty(null);

			for (ProcessListDataItem processListDataItem : formulatedProduct
					.getProcessList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				Double qty = qtyProvider.getQty(processListDataItem,null) * ratio;

				if ((processListDataItem.getResource() != null) && (qty != null)) {
					if (ProductUnit.P.equals(processListDataItem.getUnit()) && ProductUnit.P.equals(formulatedProduct.getUnit())) {
						netQtyForCost = FormulationHelper.QTY_FOR_PIECE;
					}

					ProductData partProduct = (ProductData) alfrescoRepository.findOne(processListDataItem.getResource());

					FormulatedQties qties = new FormulatedQties(qty, null, netQtyForCost, null);
					Double newRatio = qty / qtyProvider.getNetQty(null);
					
					visitPart(formulatedProduct, partProduct, processListDataItem.getNodeRef(), ret, qties, currLevel, unitProvider);

					if ((maxLevel < 0) || (currLevel < maxLevel)) {

						visitRecurCost(partProduct, ret, currLevel + 1, maxLevel, newRatio, unitProvider);
					}

				}
			}

		}

		visiteTemplateCosts(formulatedProduct, ret);

		return ret;
	}

	private void visiteTemplateCosts(ProductData formulatedProduct, CharactDetails ret) {

		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {

			visitTemplateCostList(formulatedProduct, formulatedProduct.getEntityTpl().getNodeRef(), getDataListVisited(formulatedProduct.getEntityTpl()),
					ret);
		}
		if (formulatedProduct.getClients() != null) {
			for (ClientData client : formulatedProduct.getClients()) {
				visitTemplateCostList(formulatedProduct, client.getNodeRef(), getDataListVisited(client), ret);
			}
		}

		if ((formulatedProduct instanceof RawMaterialData) && (((RawMaterialData) formulatedProduct).getSuppliers() != null)) {
			for (NodeRef supplierNodeRef : ((RawMaterialData) formulatedProduct).getSuppliers()) {
				SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
				visitTemplateCostList(formulatedProduct, supplier.getNodeRef(), getDataListVisited(supplier), ret);
			}
		}

		if ((formulatedProduct instanceof PackagingMaterialData) && (((PackagingMaterialData) formulatedProduct).getSuppliers() != null)) {
			for (NodeRef supplierNodeRef : ((PackagingMaterialData) formulatedProduct).getSuppliers()) {
				SupplierData supplier = (SupplierData) alfrescoRepository.findOne(supplierNodeRef);
				visitTemplateCostList(formulatedProduct, supplier.getNodeRef(), getDataListVisited(supplier), ret);
			}
		}

	}

	private void visitTemplateCostList(ProductData formulatedProduct, NodeRef entityNodeRef, List<T> templateCostLists,
			CharactDetails ret) {
		NodeRef plantNodeRef = formulatedProduct.getPlants().isEmpty() ? null : formulatedProduct.getPlants().get(0);

		for (AbstractCostListDataItem<?> templateCostList : templateCostLists) {

			if ((templateCostList != null) && ret.hasElement(templateCostList.getCharactNodeRef()) && (templateCostList.getValue() != null)) {
				// plants
				if (templateCostList.getPlants().isEmpty() || templateCostList.getPlants().contains(plantNodeRef)) {

					Double netQty = 1d;
					Double qtyUsed = 1d;

					if ((formulatedProduct.getUnit() != null) && (templateCostList.getUnit() != null)) {
						if (!templateCostList.getUnit().endsWith(formulatedProduct.getUnit().toString())) {
							if (formulatedProduct.getUnit().isP()) {
								if (templateCostList.getUnit().endsWith("kg") || templateCostList.getUnit().endsWith("L")) {

									qtyUsed = FormulationHelper.getNetQtyInLorKg(formulatedProduct, 0d);

								} else if (templateCostList.getUnit().endsWith("Pal")) {
									if ((formulatedProduct.getDefaultVariantPackagingData() != null)
											&& (formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet() != null)) {

										netQty = (double) formulatedProduct.getDefaultVariantPackagingData().getProductPerPallet();

									}
								}
							} else if (formulatedProduct.getUnit().isWeight() || formulatedProduct.getUnit().isVolume()) {
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
					Double value = FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getValue(), netQty);

					String unit = CostsCalculatingFormulationHandler.calculateUnit(formulatedProduct.getUnit(),
							(String) nodeService.getProperty(templateCostList.getCost(), getCostUnitPropName()),
							(Boolean) nodeService.getProperty(templateCostList.getCost(), getCostFixedPropName()));

					CharactDetailsValue key = new CharactDetailsValue(formulatedProduct.getNodeRef(), entityNodeRef, null, value, 0, unit);
					if (!ret.isMultiple()) {

						Double previous = templateCostList.getPreviousValue() != null
								? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getPreviousValue(), netQty)
								: null;
						Double future = templateCostList.getFutureValue() != null
								? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getFutureValue(), netQty)
								: null;
						Double maxi = templateCostList.getMaxi() != null
								? FormulationHelper.calculateValue(0d, qtyUsed, templateCostList.getMaxi(), netQty)
								: null;

						key.setPreviousValue(previous);
						key.setFutureValue(future);
						key.setMaxi(maxi);
					}

					ret.addKeyValue(templateCostList.getCharactNodeRef(), key);

				}
			}
		}

	}

	private void visitCompoListChildren(ProductData productData, Composite<CompoListDataItem> composite, CharactDetails ret, Double parentLossRatio,
			Double ratio, Integer currLevel, Integer maxLevel, CostListQtyProvider qtyProvider, SimpleCharactUnitProvider unitProvider)
			throws FormulateException {

		if (productData.isGeneric()) {
			return;
		}

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			CompoListDataItem compoListDataItem = component.getData();
			ProductData componentProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());
			if (!component.isLeaf()) {

				// take in account the loss perc
				Double lossPerc = FormulationHelper.getComponentLossPerc(componentProduct, compoListDataItem);
				Double newLossPerc = FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);
				if (logger.isDebugEnabled()) {
					logger.debug("parentLossRatio: " + parentLossRatio + " - lossPerc: " + lossPerc + " - newLossPerc: " + newLossPerc);
				}

				// calculate children
				Composite<CompoListDataItem> c = component;
				visitCompoListChildren(productData, c, ret, newLossPerc, ratio, currLevel, maxLevel, qtyProvider, unitProvider);
			} else {

				Double qty = qtyProvider.getQty(compoListDataItem, parentLossRatio, componentProduct) * ratio;

				FormulatedQties qties = new FormulatedQties(qty, qtyProvider.getVolume(compoListDataItem, parentLossRatio, componentProduct) * ratio,
						qtyProvider.getNetQty(null), qtyProvider.getNetWeight(null));

				Double newRatio = qty / qtyProvider.getNetQty(null);

				visitPart(productData, componentProduct, component.getData().getNodeRef(), ret, qties, currLevel, unitProvider);

				if (((maxLevel < 0) || (currLevel < maxLevel))
						&& !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {
					visitRecurCost(componentProduct, ret, currLevel + 1, maxLevel, newRatio, unitProvider);
				}

			}
		}
	}

	protected abstract List<T> getDataListVisited(BeCPGDataObject product);

	protected abstract QName getCostFixedPropName();
	
	protected abstract QName getCostUnitPropName();

	
	@Override
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return CostCalculatingHelper.extractValue(formulatedProduct, partProduct, simpleCharact);
	}
}
