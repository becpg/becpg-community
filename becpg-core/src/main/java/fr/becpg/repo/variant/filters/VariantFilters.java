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

	public static VariantFilters DEFAULT_VARIANT = new VariantFilters(true);

	public VariantFilters(NodeRef variantNodeRef) {
		super();
		this.variantNodeRef = variantNodeRef;
	}

	public VariantFilters(Boolean isDefaultVariant) {
		super();
		this.isDefaultVariant = isDefaultVariant;
	}

	@Override
	public Predicate createPredicate(final ProductData data) {

		return new Predicate() {

			@Override
			public boolean evaluate(Object obj) {
				if (obj instanceof VariantDataItem) {

					VariantDataItem item = ((VariantDataItem) obj);
					if (item.getVariants() == null || item.getVariants().isEmpty()) {
						if (isDefaultVariant == null || isDefaultVariant) {
							return true;
						}
						return false;
					}

					if (isDefaultVariant != null) {
						for (VariantData variant : data.getVariants()) {
							if (isDefaultVariant.equals(variant.getIsDefaultVariant())) {
								for (NodeRef nodeRef : item.getVariants()) {
									if (nodeRef.equals(variant.getNodeRef())) {
										return true;
									}
								}
							}
						}
					} else if (variantNodeRef != null) {
						for (NodeRef nodeRef : item.getVariants()) {
							if (variantNodeRef.equals(nodeRef)) {
								return true;
							}
						}
					}
					return false;
				}
				return true;
			}
		};
	}

}
