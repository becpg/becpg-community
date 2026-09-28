/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.productList.SpecCompatibilityDataItem;

/**
 * Keeps the non-compatible products list of a product specification in line with the
 * products tested by the specification formulation batch.
 *
 * <p>A retested product loses its previous rows before its new result is added, so a product
 * that became compliant disappears and a product that is still non-compliant is not listed twice.
 * Once the batch is over, only the rows of skipped products and of non-compliant products remain.</p>
 */
public final class SpecCompatibilityListUpdater {

	private SpecCompatibilityListUpdater() {
	}

	/**
	 * Replaces the rows of a retested product with its new result.
	 *
	 * @param specCompatibilityList the non-compatible products list of the specification
	 * @param productNodeRef the retested product
	 * @param newRows the rows produced by the new test, empty when the product is compliant
	 */
	public static void replaceProductRows(List<SpecCompatibilityDataItem> specCompatibilityList, NodeRef productNodeRef,
			List<SpecCompatibilityDataItem> newRows) {
		specCompatibilityList.removeIf(row -> productNodeRef.equals(row.getSourceItem()));
		specCompatibilityList.addAll(newRows);
	}

	/**
	 * Removes the rows of the products that are neither skipped nor found non-compliant by the
	 * batch, such as products that left the tested scope.
	 *
	 * @param specCompatibilityList the non-compatible products list of the specification
	 * @param skippedProducts the products not retested because nothing changed since the last run
	 * @param nonCompliantProducts the products found non-compliant by the batch
	 */
	public static void removeObsoleteRows(List<SpecCompatibilityDataItem> specCompatibilityList, Set<NodeRef> skippedProducts,
			Set<NodeRef> nonCompliantProducts) {
		specCompatibilityList
				.removeIf(row -> !skippedProducts.contains(row.getSourceItem()) && !nonCompliantProducts.contains(row.getSourceItem()));
	}
}
