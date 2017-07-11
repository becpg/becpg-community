/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
package fr.becpg.repo.product.data;

import java.util.Date;
import java.util.function.Predicate;

import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.EffectiveDataItem;

public class EffectiveFilters<T extends EffectiveDataItem> implements DataListFilter<ProductData, T> {

	public final static String EFFECTIVE = "EFFECTIVE";
	public final static String FUTUR = "FUTUR";
	public final static String ALL = "ALL";

	private String effectiveState = EFFECTIVE;

	public EffectiveFilters(String effectiveState) {
		super();
		this.effectiveState = effectiveState;
	}

	@Override
	public Predicate<T> createPredicate(final ProductData data) {

		final Date now = new Date();
		final Date startEffectivity = (data.getStartEffectivity() != null) && (data.getStartEffectivity().getTime() > now.getTime())
				? data.getStartEffectivity() : now;

		return item -> {

			if (FUTUR.equals(effectiveState)) {

				return ((item.getEndEffectivity() == null)
						|| ((data.getStartEffectivity() != null) && (item.getEndEffectivity().getTime() > data.getStartEffectivity().getTime()))
						|| (item.getEndEffectivity().getTime() > now.getTime()));
			} else if (EFFECTIVE.equals(effectiveState)) {

				return ((item.getStartEffectivity() == null) || (item.getStartEffectivity().getTime() <= startEffectivity.getTime()))
						&& ((item.getEndEffectivity() == null)
								|| ((data.getEndEffectivity() != null) && (item.getEndEffectivity().getTime() <= data.getEndEffectivity().getTime()))
								|| (item.getEndEffectivity().getTime() > now.getTime()));
			} else {
				return true;
			}

		};

	}
}
