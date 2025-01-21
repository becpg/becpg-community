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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.data.hierarchicalList.Composite;
import fr.becpg.repo.data.hierarchicalList.CompositeHelper;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

/**
 * <p>CompositionQtyCalculatingFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompositionQtyCalculatingFormulationHandler extends AbstractCompositionQtyCalculatingFormulationHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(CompositionQtyCalculatingFormulationHandler.class);


	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Composition calculating visitor");

		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
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

}
