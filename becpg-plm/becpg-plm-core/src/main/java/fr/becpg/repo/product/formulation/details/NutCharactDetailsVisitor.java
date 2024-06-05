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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.CharactDetailAdditionalValue;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>NutCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class NutCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	/** {@inheritDoc} */
	@Override
	public CharactDetails visit(ProductData formulatedProduct, List<NodeRef> dataListItems, Integer level)  {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (level == null) {
			level = 0;
		}

		Double netQty = FormulationHelper.getNetQtyForNuts(formulatedProduct,null);
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netVol = FormulationHelper.getNetVolume(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		visitRecur(formulatedProduct, formulatedProduct, ret, 0, level, netWeight, netVol, netQty);

		return ret;
	}
	
	/** {@inheritDoc} */
	@Override
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact, String unit, Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		NutListDataItem nutListDataItem = (NutListDataItem) simpleCharact;
		Double value = nutListDataItem.getPreparedValue() != null ? nutListDataItem.getPreparedValue() : nutListDataItem.getValue();
		Double servingSize = FormulationHelper.getServingSizeInLorKg(rootProduct);
		
		if (servingSize != null && value != null) {
			Double valuePerServing = (value * (servingSize * 1000d)) / 100;
			String newUnit = unit.split("/")[0];
			CharactDetailAdditionalValue additionalValue = new CharactDetailAdditionalValue(I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_nutListValuePerServing.title"),
					FormulationHelper.calculateValue(0d, qtyUsed, valuePerServing, netQty), newUnit);
			currentCharactDetailsValue.getAdditionalValues().add(additionalValue);
		}
	}

}
