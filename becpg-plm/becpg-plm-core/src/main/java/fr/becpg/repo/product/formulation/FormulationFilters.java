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

import java.util.List;

import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * Commonly used filter combinations for formulation processing.
 */
public final class FormulationFilters
{

    public static final List<DataListFilter<ProductData, CompoListDataItem>> EFFECTIVE_VARIANT_COMPO =
            List.of(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>());

    public static final List<DataListFilter<ProductData, PackagingListDataItem>> EFFECTIVE_VARIANT_PACKAGING =
            List.of(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>());

    public static final List<DataListFilter<ProductData, ProcessListDataItem>> EFFECTIVE_VARIANT_PROCESS =
            List.of(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>());

    private FormulationFilters()
    {
    }
}
