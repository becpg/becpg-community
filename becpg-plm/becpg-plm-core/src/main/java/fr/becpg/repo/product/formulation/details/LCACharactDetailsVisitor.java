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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ClientData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SupplierData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>LCACharactDetailsVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class LCACharactDetailsVisitor extends AbstractCostCharactDetailsVisitor<LCAListDataItem> {

	/** {@inheritDoc} */
	@Override
	protected List<LCAListDataItem> getDataListVisited(BeCPGDataObject product) {
		if (product instanceof ProductData) {
			return ((ProductData) product).getLcaList();
		} else if (product instanceof ClientData) {
			return ((ClientData) product).getLcaList();
		} else if (product instanceof SupplierData) {
			return ((SupplierData) product).getLcaList();
		}
		return new ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostFixedPropName() {
		return PLMModel.PROP_LCAFIXED;
	}

	/** {@inheritDoc} */
	@Override
	protected QName getCostUnitPropName() {
		return PLMModel.PROP_LCAUNIT;
	}

}
