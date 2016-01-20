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
package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

public class CompositionQtyCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompositionQtyCalculatingFormulationHandler.class);

	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(new VariantFilters<>())) {
			logger.debug("no compo => no formulation");
			return true;
		}

		// Take in account net weight

		if (formulatedProduct.getUnit() != null) {
			Double qty = null;

			if ((formulatedProduct.getQty() != null)
					&& (ProductUnit.g.equals(formulatedProduct.getUnit()) || ProductUnit.mL.equals(formulatedProduct.getUnit()))) {
				qty = formulatedProduct.getQty() / 1000;
			} else if ((formulatedProduct.getQty() != null)
					&& ProductUnit.cL.equals(formulatedProduct.getUnit())) {
				qty = formulatedProduct.getQty() / 100;
			} else {
				qty = formulatedProduct.getQty();
			}
			
			
			if (ProductUnit.g.equals(formulatedProduct.getUnit()) || ProductUnit.kg.equals(formulatedProduct.getUnit())) {
				formulatedProduct.setNetWeight(qty);
			} else if (ProductUnit.mL.equals(formulatedProduct.getUnit()) || ProductUnit.L.equals(formulatedProduct.getUnit())) {
				formulatedProduct.setNetVolume(qty);
			}

		}
		Double netWeight = formulatedProduct.getNetWeight();
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList());

		// calculate on every item
		visitQtyChildren(formulatedProduct, netWeight, compositeAll);

		return true;
	}

	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double qtyInKg = calculateQtyInKg(component.getData());
			logger.debug("qtySubFormula: " + qtyInKg + " parentQty: " + parentQty);
			if (qtyInKg != null) {

				// take in account percentage
				if (CompoListUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {
					qtyInKg = (qtyInKg * parentQty) / 100;
				}

				// Take in account yield that is defined on component
				Double qty;
				if (component.isLeaf()) {
					qty = (qtyInKg * 100) / FormulationHelper.getYield(component.getData());
				} else {
					qty = qtyInKg;
				}
				component.getData().setQty(qty);
			}

			// calculate children
			if (!component.isLeaf()) {

				// take in account percentage
				if (CompoListUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {

					visitQtyChildren(formulatedProduct, parentQty, component);

					// no yield but calculate % of composite
					Double compositePerc = 0d;
					boolean isUnitPerc = true;
					for (Composite<CompoListDataItem> child : component.getChildren()) {
						compositePerc += child.getData().getQtySubFormula();
						isUnitPerc = isUnitPerc && CompoListUnit.Perc.equals(child.getData().getCompoListUnit());
						if (!isUnitPerc) {
							break;
						}
					}
					if (isUnitPerc) {
						component.getData().setQtySubFormula(compositePerc);
						component.getData().setQty((compositePerc * parentQty) / 100);
					}
				} else {
					visitQtyChildren(formulatedProduct, component.getData().getQty(), component);
				}
			}
		}
	}

	private Double calculateQtyInKg(CompoListDataItem compoListDataItem) {
		Double qty = compoListDataItem.getQtySubFormula();
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		if ((qty != null) && (compoListUnit != null)) {

			if (compoListUnit.equals(CompoListUnit.kg)) {
				return qty;
			} else if (compoListUnit.equals(CompoListUnit.g)) {
				return qty / 1000;
			} else if (compoListUnit.equals(CompoListUnit.mg)) {
				return qty / 1000000;
			} else if (compoListUnit.equals(CompoListUnit.P)) {
				return FormulationHelper.getNetWeight(compoListDataItem.getProduct(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT) * qty;
			} else if (compoListUnit.equals(CompoListUnit.L) || compoListUnit.equals(CompoListUnit.mL)) {

				if (compoListUnit.equals(CompoListUnit.mL)) {
					qty = qty / 1000;
				}

				Double overrun = compoListDataItem.getOverrunPerc();
				if (compoListDataItem.getOverrunPerc() == null) {
					overrun = FormulationHelper.DEFAULT_OVERRUN;
				}

				Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
				if ((density == null) || density.equals(0d)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Cannot calculate qty since density is null or equals to 0");
					}
				} else {
					return (qty * density * 100) / (100 + overrun);

				}

			} else if (compoListUnit.equals(CompoListUnit.m) || compoListUnit.equals(CompoListUnit.m2)) {
				Double productQty = FormulationHelper.getProductQty(compoListDataItem.getProduct(), nodeService);
				if (productQty == null) {
					productQty = 1d;
				}
				return (FormulationHelper.getNetWeight(compoListDataItem.getProduct(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT) * qty)
						/ productQty;
			}
			return qty;
		}

		return FormulationHelper.DEFAULT_COMPONANT_QUANTITY;
	}
}
