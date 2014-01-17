package fr.becpg.repo.variant.filters;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.Predicate;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.repo.variant.model.VariantDataItem;

public class VariantFilters implements DataListFilter<ProductData> {

	private NodeRef variantNodeRef;

	private Boolean isDefaultVariant;

	public VariantFilters(NodeRef variantNodeRef) {
		super();
		this.variantNodeRef = variantNodeRef;
	}

	public static VariantFilters DEFAULT_VARIANT = new VariantFilters(true);

	public VariantFilters(Boolean isDefaultVariant) {
		super();
		this.isDefaultVariant = isDefaultVariant;

	}

	@Override
	public Predicate createPredicate(final ProductData entity) {

		if (variantNodeRef == null && entity.getVariants()!=null) {
			for (VariantData variant : entity.getVariants()) {
				if (variant.getIsDefaultVariant()) {
					this.variantNodeRef = variant.getNodeRef();
					break;
				}
			}
		}

		return new Predicate() {

			@Override
			public boolean evaluate(Object obj) {

				if (variantNodeRef!=null && obj instanceof VariantDataItem) {
					VariantDataItem item = ((VariantDataItem) obj);

					if (isDefaultVariant != null) {

						if (item.getVariants() == null || item.getVariants().isEmpty()) {
							if (isDefaultVariant != null && isDefaultVariant) {
								return true;
							}
						}

						if (isDefaultVariant && item.getVariants().contains(variantNodeRef)) {
							return true;
						}

					} else if (variantNodeRef != null && item.getVariants().contains(variantNodeRef)) {
						return true;
					}
					return false;
				}
				return true;
			}
		};
	}

}
