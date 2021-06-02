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
package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>CompositionQtyCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompositionQtyCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompositionQtyCalculatingFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| formulatedProduct instanceof ProductSpecificationData) {
			return true;
		}
		
		// Take in account net weight
		if (formulatedProduct.getUnit() != null) {
			Double qty = null;
			if (formulatedProduct.getQty() != null) {
				Double unitFactor = formulatedProduct.getUnit().getUnitFactor();
				qty = formulatedProduct.getQty() / unitFactor;
			}

			if (formulatedProduct.getUnit().isWeight()) {
				formulatedProduct.setNetWeight(qty);
			} else if (formulatedProduct.getUnit().isVolume()) {
				formulatedProduct.setNetVolume(qty);
			}

		}

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl()) {
			logger.debug("no compo => no formulation");
			return true;
		}

		Double netWeight = formulatedProduct.getNetWeight() != null ? formulatedProduct.getNetWeight() : 100d;
		Composite<CompoListDataItem> compositeAll = CompositeHelper.getHierarchicalCompoList(formulatedProduct.getCompoList());

		// calculate on every item
		visitQtyChildren(formulatedProduct, netWeight, compositeAll);

		return true;
	}

	private void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			Double qtyInKg = calculateQtyInKg(component.getData());
			if(logger.isDebugEnabled()) {
				logger.debug("qtySubFormula: " + qtyInKg + " parentQty: " + parentQty);
			}
			if (qtyInKg != null) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {
					qtyInKg = (qtyInKg * parentQty) / 100;
				}

				// Take in account yield that is defined on component
				Double qty;
				if (component.isLeaf()) {
					qty = (qtyInKg * 100) / FormulationHelper.getYield(component.getData());
					if(formulatedProduct.getManualYield()!=null && formulatedProduct.getManualYield()!=0d) {
						qty = (qtyInKg * 100) / formulatedProduct.getManualYield();
					}
				} else {
					qty = qtyInKg;
				}
				component.getData().setQty(qty);
			}

			// calculate children
			if (!component.isLeaf()) {

				// take in account percentage
				if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {

					visitQtyChildren(formulatedProduct, parentQty, component);

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
					visitQtyChildren(formulatedProduct, component.getData().getQty(), component);
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

				if (componentProductData.getUnit() != null && componentProductData.getUnit().isP() && componentProductData.getQty() != null) {
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
}
