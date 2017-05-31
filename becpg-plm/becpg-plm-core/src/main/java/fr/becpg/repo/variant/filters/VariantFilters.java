/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
package fr.becpg.repo.variant.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.filters.DataListFilter;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.repo.variant.model.VariantDataItem;

public class VariantFilters<T extends VariantDataItem> implements DataListFilter<ProductData, T> {

	private Set<NodeRef> variantNodeRefs = new HashSet<>();

	private Boolean isDefaultVariant = true;

	public VariantFilters() {
		super();
	}

	public VariantFilters(NodeRef variantNodeRef) {
		super();
		this.variantNodeRefs.add(variantNodeRef);
	}

	public VariantFilters(Boolean isDefaultVariant) {
		super();
		this.isDefaultVariant = isDefaultVariant;

	}

	@Override
	public Predicate<T> createPredicate(final ProductData entity) {

		if ((variantNodeRefs.isEmpty()) && (entity.getVariants() != null)) {
			for (VariantData variant : entity.getVariants()) {
				if (variant.getIsDefaultVariant()) {
					this.variantNodeRefs.add(variant.getNodeRef());
				}
			}
		}

		return obj -> {
			if ((!variantNodeRefs.isEmpty()) && (obj instanceof VariantDataItem)) {
				VariantDataItem item = (obj);

				if (isDefaultVariant != null) {

					if ((item.getVariants() == null) || item.getVariants().isEmpty()) {
						if ((isDefaultVariant != null) && isDefaultVariant) {
							return true;
						}
					}

					for(NodeRef variantNodeRef : variantNodeRefs) {
						if (isDefaultVariant && item.getVariants().contains(variantNodeRef)) {
							return true;
						}
					}

				} else if (!variantNodeRefs.isEmpty()) {
					for(NodeRef variantNodeRef : variantNodeRefs) {
						if (item.getVariants().contains(variantNodeRef)) {
							return true;
						}
					}
					
		
				}
				return false;
			}
			return true;
		};
	}

}
