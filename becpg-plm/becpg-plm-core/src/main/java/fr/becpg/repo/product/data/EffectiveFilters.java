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
package fr.becpg.repo.product.data;

import java.util.Date;

import org.apache.commons.collections.Predicate;

import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.EffectiveDataItem;

public class EffectiveFilters {
	
	public static final DataListFilter<ProductData> FUTUR = new DataListFilter<ProductData>() {

		@Override
		public Predicate createPredicate(final ProductData data) {

			final Date now = new Date();

			return new Predicate() {

				@Override
				public boolean evaluate(Object obj) {
					
					if (obj instanceof EffectiveDataItem) {
						EffectiveDataItem item = ((EffectiveDataItem) obj);

						return  (item.getEndEffectivity() == null
										|| (data.getStartEffectivity() != null && item.getEndEffectivity().getTime() > data.getStartEffectivity().getTime()) || item
										.getEndEffectivity().getTime() > now.getTime());

					}
					
					return true;
				}
			};
		}
	};
	public static final DataListFilter<ProductData> EFFECTIVE = new DataListFilter<ProductData>() {

		@Override
		public Predicate createPredicate(final ProductData data) {

			final Date now = new Date();
			final Date startEffectivity = data.getStartEffectivity() != null && data.getStartEffectivity().getTime()>now.getTime() ? data.getStartEffectivity() : now;

			return new Predicate() {

				@Override
				public boolean evaluate(Object obj) {
					
					if (obj instanceof EffectiveDataItem) {
						EffectiveDataItem item = ((EffectiveDataItem) obj);
						
						
						return  (item.getStartEffectivity() == null || item.getStartEffectivity().getTime() <= startEffectivity.getTime())
								&& (item.getEndEffectivity() == null
										|| (data.getEndEffectivity() != null && item.getEndEffectivity().getTime() <= data.getEndEffectivity().getTime()) || item
										.getEndEffectivity().getTime() > now.getTime());

					}
					
					return true;
				}
			};
		}
	};
	public static final DataListFilter<ProductData> ALL = new DataListFilter<ProductData>() {

		@Override
		public Predicate createPredicate(ProductData data) {
			return new Predicate() {

				@Override
				public boolean evaluate(Object obj) {
					return true;
				}
			};
		}
	};

}
