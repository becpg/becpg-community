package fr.becpg.repo.product.data;

import java.util.Date;

import org.apache.commons.collections.Predicate;

import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.repository.model.EffectiveDataItem;

public class EffectiveFilters {
	
	public static DataListFilter<ProductData> FUTUR = new DataListFilter<ProductData>() {

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
	public static DataListFilter<ProductData> EFFECTIVE = new DataListFilter<ProductData>() {

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
	public static DataListFilter<ProductData> ALL = new DataListFilter<ProductData>() {

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
