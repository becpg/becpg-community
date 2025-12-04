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
package fr.becpg.repo.product.data;

import java.util.Date;
import java.util.function.Predicate;

import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.EffectiveDataItem;

/**
 * <p>EffectiveFilters class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EffectiveFilters<T extends EffectiveDataItem> implements DataListFilter<ProductData, T> {

	/** Constant <code>EFFECTIVE="EFFECTIVE"</code> */
	public final static String EFFECTIVE = "EFFECTIVE";
	/** Constant <code>FUTUR="FUTUR"</code> */
	public final static String FUTUR = "FUTUR";
	/** Constant <code>ALL="ALL"</code> */
	public final static String ALL = "ALL";

	public final static String EFFECTIVE_OR_FUTURE = "EFFECTIVE_OR_FUTURE";
	
	private String effectiveState = EFFECTIVE;
	
	private Date currentDate = null;

	/**
	 * <p>Constructor for EffectiveFilters.</p>
	 *
	 * @param effectiveState a {@link java.lang.String} object.
	 */
	public EffectiveFilters(String effectiveState) {
		super();
		this.effectiveState = effectiveState;
	}
	
	

	/**
	 * <p>Constructor for EffectiveFilters.</p>
	 *
	 * @param currentDate a {@link java.util.Date} object.
	 */
	public EffectiveFilters(Date currentDate) {
		super();
		this.currentDate = currentDate;
		this.effectiveState = EFFECTIVE;
	}



	/** {@inheritDoc} */
	@Override
	public Predicate<T> createPredicate(final ProductData data) {
		Date productEndEffectivity = data.getEndEffectivity();
		Date productStartEffectivity = data.getStartEffectivity();
		return createPredicate(productStartEffectivity, productEndEffectivity);
	}

	public Predicate<T> createPredicate(Date productStartEffectivity, Date productEndEffectivity) {
		final Date now = currentDate!=null ? currentDate : new Date();
		final Date startEffectivity = (productStartEffectivity != null) && (productStartEffectivity.getTime() > now.getTime())
				? productStartEffectivity : now;
				
		return item -> {
			if (FUTUR.equals(effectiveState)) {
				return isFuture(productStartEffectivity, now, item);
			}
			if (EFFECTIVE.equals(effectiveState)) {
				return isEffective(productEndEffectivity, now, startEffectivity, item);
			}
			if (EFFECTIVE_OR_FUTURE.equals(effectiveState)) {
				return isEffective(productEndEffectivity, now, startEffectivity, item) || isFuture(productStartEffectivity, now, item);
			}
			return true;
		};
	}

	private boolean isEffective(Date productEndEffectivity, final Date now, final Date startEffectivity, T item) {
		return ((item.getStartEffectivity() == null) || (item.getStartEffectivity().getTime() <= startEffectivity.getTime()))
				&& ((item.getEndEffectivity() == null)
						|| ((productEndEffectivity != null) && (item.getEndEffectivity().getTime() <= productEndEffectivity.getTime()))
						|| (item.getEndEffectivity().getTime() > now.getTime()));
	}

	private boolean isFuture(Date productStartEffectivity, final Date now, T item) {
		return (item.getEndEffectivity() == null)
				|| ((productStartEffectivity != null) && (item.getEndEffectivity().getTime() > productStartEffectivity.getTime()))
				|| (item.getEndEffectivity().getTime() > now.getTime());
	}
}
