package fr.becpg.repo.product.formulation.details;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;

/**
 * Quantity basis of the ingredient list, restricted to the composition lines with a positive quantity.
 * <p>
 * The ingredient list ignores the composition lines whose quantity is not positive (a co-product or a
 * brine removed with a negative quantity, see #37100) and spreads the ingredients over the quantity of
 * the positive lines only. The recipe quantity used and the net weight, on the other hand, are signed
 * sums. This class provides the factor that brings a signed basis back to the positive lines one, so that
 * the ingredient details match the ingredient list.
 *
 * @author matthieu
 */
public final class CompositionQtyBasis {

	private static final double NO_ADJUSTMENT = 1d;

	private CompositionQtyBasis() {
	}

	/**
	 * Tells whether a composition line is taken into account by the ingredient list.
	 *
	 * @param compoListDataItem the composition line
	 * @return true when the line has a positive quantity
	 */
	public static boolean hasPositiveQty(CompoListDataItem compoListDataItem) {
		Double qtySubFormula = compoListDataItem.getQtySubFormula();
		return (qtySubFormula != null) && (qtySubFormula > 0d);
	}

	/**
	 * Computes the ratio between the quantity of the positive composition lines and the signed recipe
	 * quantity.
	 * <p>
	 * Only the declared leaf lines count, as for the recipe quantity used: omitted lines, lines under an
	 * omitted parent and parent lines are skipped. The ratio is 1 when no line is negative, or when the
	 * signed quantity is not positive and the ratio would be meaningless.
	 *
	 * @param compoList the effective composition of the product
	 * @return the factor to apply to a signed quantity basis to get the positive lines one
	 */
	public static double computePositiveLinesFactor(List<CompoListDataItem> compoList) {
		if ((compoList == null) || compoList.isEmpty()) {
			return NO_ADJUSTMENT;
		}

		Set<CompoListDataItem> parents = collectParents(compoList);
		double signedQty = 0d;
		double positiveQty = 0d;

		for (CompoListDataItem compoListDataItem : compoList) {
			if (parents.contains(compoListDataItem) || isOmitted(compoListDataItem)) {
				continue;
			}
			double qty = (FormulationHelper.getQtyInKg(compoListDataItem) * FormulationHelper.getYield(compoListDataItem)) / 100d;
			signedQty += qty;
			if (hasPositiveQty(compoListDataItem)) {
				positiveQty += qty;
			}
		}

		if ((signedQty <= 0d) || (positiveQty == signedQty)) {
			return NO_ADJUSTMENT;
		}
		return positiveQty / signedQty;
	}

	private static Set<CompoListDataItem> collectParents(List<CompoListDataItem> compoList) {
		Set<CompoListDataItem> parents = Collections.newSetFromMap(new IdentityHashMap<>());
		for (CompoListDataItem compoListDataItem : compoList) {
			if (compoListDataItem.getParent() != null) {
				parents.add(compoListDataItem.getParent());
			}
		}
		return parents;
	}

	private static boolean isOmitted(CompoListDataItem compoListDataItem) {
		CompoListDataItem current = compoListDataItem;
		while (current != null) {
			if (DeclarationType.Omit.equals(current.getDeclType())) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}
}
