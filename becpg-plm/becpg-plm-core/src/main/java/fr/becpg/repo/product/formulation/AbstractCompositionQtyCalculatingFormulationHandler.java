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
package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>AbstractCompositionQtyCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractCompositionQtyCalculatingFormulationHandler<T> extends FormulationBaseHandler<T> {

	private static final Log logger = LogFactory.getLog(AbstractCompositionQtyCalculatingFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}


	/**
	 * <p>visitQtyChildren.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param parentQty a {@link java.lang.Double} object
	 * @param composite a {@link fr.becpg.repo.data.hierarchicalList.Composite} object
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	protected void visitQtyChildren(ProductData formulatedProduct, Double parentQty, Composite<CompoListDataItem> composite) throws FormulateException {

		for (Composite<CompoListDataItem> component : composite.getChildren()) {

			BigDecimal qtyInKg = calculateQtyInKg(component.getData());
			if (logger.isDebugEnabled()) {
				logger.debug("qtySubFormula: " + qtyInKg + " parentQty: " + parentQty);
			}

			// take in account percentage
			if (ProductUnit.Perc.equals(component.getData().getCompoListUnit()) && (parentQty != null) && !parentQty.equals(0d)) {
				qtyInKg = qtyInKg.multiply(BigDecimal.valueOf(parentQty)).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
			}

			// Take in account yield that is defined on component

			if (component.isLeaf() && formulatedProduct!=null) {
				if ((formulatedProduct.getManualYield() != null) && (formulatedProduct.getManualYield() != 0d)) {
					qtyInKg = qtyInKg.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(formulatedProduct.getManualYield()), 10,
							RoundingMode.HALF_UP);
				} else {
					qtyInKg = qtyInKg.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(FormulationHelper.getYield(component.getData())),
							10, RoundingMode.HALF_UP);
				}
			}
			component.getData().setQty(qtyInKg.doubleValue());

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


	/**
	 * <p>calculateQtyInKg.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @return a {@link java.math.BigDecimal} object
	 */
	protected BigDecimal calculateQtyInKg(CompoListDataItem compoListDataItem) {
		Double qty = compoListDataItem.getQtySubFormula();
		ProductUnit compoListUnit = compoListDataItem.getCompoListUnit();

		ProductData componentProductData = alfrescoRepository.findOne(compoListDataItem.getProduct());
		
		return FormulationHelper.compoListUnitToKg(qty,compoListDataItem,componentProductData,  compoListUnit);
	}
}
