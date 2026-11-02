/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * Keeps the "with yield" percentages of composite ingredients consistent with their sub-ingredients once the
 * evaporation has been applied on an ingredient list.
 * <p>
 * The evaporation rate of a composite ingredient prevails over the rates of its sub-ingredients: once a
 * composite is used in a recipe, it is the composite that loses water, whatever its sub-ingredients would lose
 * on their own (see #34702). A sub-ingredient rate only applies when none of its ancestors carries one.
 * <p>
 * Items are tracked by identity: {@link IngListDataItem#equals(Object)} is value-based on mutable fields,
 * among which the very percentages adjusted here.
 *
 * @author matthieu
 */
public class CompositeIngredientYieldBalancer {

	private static final Log logger = LogFactory.getLog(CompositeIngredientYieldBalancer.class);

	private static final int MAX_DEPTH = 256;

	private final List<IngListDataItem> ingList;

	private final Set<IngListDataItem> withEvaporationRate = newIdentitySet();

	private final Set<IngListDataItem> supersededEvaporation = newIdentitySet();

	private final Set<IngListDataItem> evaporatingComposites = newIdentitySet();

	/**
	 * Creates a balancer over a formulated ingredient list.
	 *
	 * @param ingList the formulated product ingredient list
	 * @param hasEvaporationData tells whether an ingredient carries an evaporation rate
	 */
	public CompositeIngredientYieldBalancer(List<IngListDataItem> ingList, Predicate<IngListDataItem> hasEvaporationData) {
		this.ingList = ingList;
		for (IngListDataItem item : ingList) {
			if ((item.getIng() != null) && hasEvaporationData.test(item)) {
				withEvaporationRate.add(item);
			}
		}
		collectSupersededEvaporation();
		collectEvaporatingComposites();
	}

	/**
	 * Tells whether the evaporation rate of an ingredient is applied.
	 *
	 * @param item the ingredient
	 * @return true when the ingredient carries a rate and none of its ancestors does
	 */
	public boolean isEvaporating(IngListDataItem item) {
		return withEvaporationRate.contains(item) && !supersededEvaporation.contains(item);
	}

	/**
	 * Recomputes each composite ingredient "with yield" percentages as the sum of its direct children
	 * percentages, when the children fully declare their parent.
	 * <p>
	 * A composite ingredient is, by definition, the sum of its sub-ingredients. When an evaporation rate is
	 * present on a sub-ingredient, the leaf percentages already reflect the lost water, whereas the parent
	 * percentage was re-concentrated independently by the product yield, producing top-level sums above 100 %.
	 * Aggregating the children back into the parent restores the invariant for both the primary and secondary
	 * yield percentages. The computation runs bottom-up so multi-level hierarchies are resolved consistently.
	 * <p>
	 * A partially declared composite keeps its own computed percentages: summing incomplete children would
	 * under-evaluate it, and summing children without percentages at all would empty the column (see #34702).
	 * An evaporating composite keeps them too: its own value carries the water it lost, which its children
	 * only receive afterwards (see {@link #spreadEvaporationOverSubIngredients()}).
	 */
	public void aggregateParentQtyPercWithYield() {

		Map<IngListDataItem, List<IngListDataItem>> childrenByParent = groupChildrenByParent(parent -> !evaporatingComposites.contains(parent));

		for (IngListDataItem parent : sortByDepth(childrenByParent.keySet(), true)) {
			List<IngListDataItem> children = childrenByParent.get(parent);
			if (!isFullyDeclaredBySubIngredients(parent, children)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Partially declared composite ingredient, keeping its own with-yield percentages: " + parent.getName());
				}
				continue;
			}
			aggregateChildren(parent, children, IngListDataItem::getQtyPercWithYield, IngListDataItem::setQtyPercWithYield);
			aggregateChildren(parent, children, IngListDataItem::getQtyPercWithSecondaryYield, IngListDataItem::setQtyPercWithSecondaryYield);
		}
	}

	/**
	 * Shares the water lost by a composite ingredient over all its sub-ingredients, proportionally to their
	 * declared percentages.
	 * <p>
	 * The breakdown of a composite ingredient is a declaration of its make-up, and reads as such on the label,
	 * so it must keep its declared proportions whether the water is lost by the composite itself or by one of
	 * its sub-ingredients (see #34702). Only the split inside the composite changes: its own percentage - and
	 * therefore every total - is left untouched.
	 * <p>
	 * The redistribution runs top-down so that multi-level hierarchies stay consistent with the parent they
	 * detail, and only applies to composites fully declared by their sub-ingredients: a partially declared one
	 * is not the sum of its children, so spreading its value over them would be wrong.
	 */
	public void spreadEvaporationOverSubIngredients() {

		Set<IngListDataItem> affectedComposites = collectCompositesAffectedByEvaporation();
		if (affectedComposites.isEmpty()) {
			return;
		}

		Map<IngListDataItem, List<IngListDataItem>> childrenByParent = groupChildrenByParent(affectedComposites::contains);

		for (IngListDataItem parent : sortByDepth(childrenByParent.keySet(), false)) {
			List<IngListDataItem> children = childrenByParent.get(parent);
			if (!isFullyDeclaredBySubIngredients(parent, children)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Partially declared composite ingredient, not spreading its evaporation: " + parent.getName());
				}
				continue;
			}
			spreadOverChildren(parent, children, IngListDataItem::getQtyPercWithYield, IngListDataItem::setQtyPercWithYield);
			spreadOverChildren(parent, children, IngListDataItem::getQtyPercWithSecondaryYield, IngListDataItem::setQtyPercWithSecondaryYield);
		}
	}

	/**
	 * Collects the sub-ingredients whose own evaporation rate is superseded by the rate of one of their
	 * ancestors.
	 * <p>
	 * The same ingredient may legitimately be used both as a simple component and as a composite one, so an
	 * evaporation rate is often set on a parent and on one of its sub-ingredients at the same time. Both would
	 * then compete for the evaporation budget while the parent quantity already includes the child one,
	 * counting the same water twice. The composite is what is used in the recipe, so its rate prevails.
	 */
	private void collectSupersededEvaporation() {
		for (IngListDataItem item : withEvaporationRate) {
			if (hasAncestorIn(item, withEvaporationRate)) {
				supersededEvaporation.add(item);
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring evaporation rate of sub-ingredient of an evaporating composite ingredient: " + item.getName());
				}
			}
		}
	}

	/**
	 * Collects the composite ingredients losing water on their own account.
	 */
	private void collectEvaporatingComposites() {
		for (IngListDataItem item : ingList) {
			IngListDataItem parent = item.getParent();
			if ((parent != null) && isEvaporating(parent)) {
				evaporatingComposites.add(parent);
			}
		}
	}

	/**
	 * Collects the composites whose sub-ingredients must be re-split after the evaporation: the ancestors of an
	 * evaporating sub-ingredient, the evaporating composites and every composite below one of them.
	 *
	 * @return the composites to spread top-down
	 */
	private Set<IngListDataItem> collectCompositesAffectedByEvaporation() {

		Set<IngListDataItem> affected = newIdentitySet();

		for (IngListDataItem item : ingList) {
			IngListDataItem parent = item.getParent();
			if (parent == null) {
				continue;
			}
			if (isEvaporating(item)) {
				addAncestors(item, affected);
			}
			if (evaporatingComposites.contains(parent) || hasAncestorIn(parent, evaporatingComposites)) {
				affected.add(parent);
			}
		}

		return affected;
	}

	/**
	 * Splits a parent "with yield" percentage over its children, proportionally to their declared percentages,
	 * leaving them untouched when the column is incomplete.
	 *
	 * @param parent the composite ingredient
	 * @param children its direct sub-ingredients
	 * @param getter the accessor of the column to spread
	 * @param setter the mutator of the column to spread
	 */
	private void spreadOverChildren(IngListDataItem parent, List<IngListDataItem> children, Function<IngListDataItem, Double> getter,
			BiConsumer<IngListDataItem, Double> setter) {

		Double parentValue = getter.apply(parent);
		if (parentValue == null) {
			return;
		}

		double totalQtyPerc = 0d;
		for (IngListDataItem child : children) {
			if ((child.getQtyPerc() == null) || (getter.apply(child) == null)) {
				return;
			}
			totalQtyPerc += child.getQtyPerc();
		}

		if (totalQtyPerc <= 0d) {
			return;
		}

		for (IngListDataItem child : children) {
			setter.accept(child, (parentValue * child.getQtyPerc()) / totalQtyPerc);
		}
	}

	/**
	 * Sets a parent "with yield" percentage to the sum of its children, leaving it untouched when at least one
	 * child has no value for that column.
	 *
	 * @param parent the composite ingredient
	 * @param children its direct sub-ingredients
	 * @param getter the accessor of the column to aggregate
	 * @param setter the mutator of the column to aggregate
	 */
	private void aggregateChildren(IngListDataItem parent, List<IngListDataItem> children, Function<IngListDataItem, Double> getter,
			BiConsumer<IngListDataItem, Double> setter) {

		double sum = 0d;
		for (IngListDataItem child : children) {
			Double value = getter.apply(child);
			if (value == null) {
				return;
			}
			sum += value;
		}

		setter.accept(parent, sum);
	}

	/**
	 * Tells whether the sub-ingredients of a composite ingredient cover its whole quantity.
	 * <p>
	 * Only a fully declared composite is equal to the sum of its sub-ingredients. Sub-ingredients are frequently
	 * declared partially - a few known components of a raw material, or components without any percentage - in
	 * which case the parent percentages must be kept as computed. A parent without percentage, or declared at
	 * 0 % (label-only wrapper whose children carry the real percentages), can never be covered by its children.
	 *
	 * @param parent the composite ingredient
	 * @param children its direct sub-ingredients
	 * @return true when the children percentages sum up to the parent percentage
	 */
	static boolean isFullyDeclaredBySubIngredients(IngListDataItem parent, List<IngListDataItem> children) {

		Double parentQtyPerc = parent.getQtyPerc();
		if ((parentQtyPerc == null) || (parentQtyPerc <= 0d)) {
			return false;
		}

		double childrenQtyPerc = 0d;
		for (IngListDataItem child : children) {
			if (child.getQtyPerc() == null) {
				return false;
			}
			childrenQtyPerc += child.getQtyPerc();
		}

		return Math.abs(childrenQtyPerc - parentQtyPerc) <= Math.max(0.001d, parentQtyPerc / 100d);
	}

	/**
	 * Groups the items of the list by their direct parent, keeping only the parents accepted by the filter.
	 *
	 * @param parentFilter the parents to keep
	 * @return the direct children of each kept parent
	 */
	private Map<IngListDataItem, List<IngListDataItem>> groupChildrenByParent(Predicate<IngListDataItem> parentFilter) {
		Map<IngListDataItem, List<IngListDataItem>> childrenByParent = new IdentityHashMap<>();
		for (IngListDataItem item : ingList) {
			if ((item.getParent() != null) && parentFilter.test(item.getParent())) {
				childrenByParent.computeIfAbsent(item.getParent(), parent -> new ArrayList<>()).add(item);
			}
		}
		return childrenByParent;
	}

	/**
	 * Sorts composite ingredients by their depth in the hierarchy.
	 *
	 * @param parents the composite ingredients
	 * @param deepestFirst true for a bottom-up order, false for a top-down one
	 * @return the sorted composites
	 */
	private static List<IngListDataItem> sortByDepth(Set<IngListDataItem> parents, boolean deepestFirst) {
		List<IngListDataItem> sorted = new ArrayList<>(parents);
		Comparator<IngListDataItem> byDepth = Comparator.comparingInt(CompositeIngredientYieldBalancer::getIngDepth);
		sorted.sort(deepestFirst ? byDepth.reversed() : byDepth);
		return sorted;
	}

	/**
	 * Returns the depth of an ingredient in the ingredient hierarchy, walking up the parent chain when the
	 * stored depth level is not available.
	 *
	 * @param ingListDataItem the ingredient item
	 * @return the depth (1 for a top-level ingredient)
	 */
	private static int getIngDepth(IngListDataItem ingListDataItem) {
		if (ingListDataItem.getDepthLevel() != null) {
			return ingListDataItem.getDepthLevel();
		}
		int depth = 1;
		IngListDataItem parent = ingListDataItem.getParent();
		while ((parent != null) && (depth < MAX_DEPTH)) {
			depth++;
			parent = parent.getParent();
		}
		return depth;
	}

	/**
	 * Tells whether one of the ancestors of an ingredient belongs to a set.
	 *
	 * @param item the ingredient
	 * @param candidates the ingredients looked for
	 * @return true when an ancestor of the ingredient is in the set
	 */
	private static boolean hasAncestorIn(IngListDataItem item, Set<IngListDataItem> candidates) {
		IngListDataItem parent = item.getParent();
		int depth = 0;
		while ((parent != null) && (depth < MAX_DEPTH)) {
			if (candidates.contains(parent)) {
				return true;
			}
			parent = parent.getParent();
			depth++;
		}
		return false;
	}

	/**
	 * Adds every ancestor of an ingredient to a set.
	 *
	 * @param item the ingredient
	 * @param ancestors the set to fill
	 */
	private static void addAncestors(IngListDataItem item, Set<IngListDataItem> ancestors) {
		IngListDataItem parent = item.getParent();
		int depth = 0;
		while ((parent != null) && (depth < MAX_DEPTH)) {
			ancestors.add(parent);
			parent = parent.getParent();
			depth++;
		}
	}

	private static Set<IngListDataItem> newIdentitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}
}
