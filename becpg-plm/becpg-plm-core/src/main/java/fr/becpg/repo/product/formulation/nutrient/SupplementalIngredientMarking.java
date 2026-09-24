package fr.becpg.repo.product.formulation.nutrient;

import java.io.Serializable;
import java.util.Collection;

import fr.becpg.repo.PlmRepoConsts;

/**
 * <p>Reads the marking a formulator puts on a nutrient line to tell that the nutrient was added to
 * the product. The marking is the {@link PlmRepoConsts#REPORT_KIND_SUPPLEMENTAL_INGREDIENT} report
 * kind carried by the line: it declares the nutrient in the supplemented food facts table and makes
 * the Canadian regulation assess it as a class I nutrient.</p>
 *
 * @author matthieu
 */
public final class SupplementalIngredientMarking {

	private SupplementalIngredientMarking() {
	}

	/**
	 * <p>Tells whether the report kinds of a nutrient line mark it as an added nutrient.</p>
	 *
	 * @param reportKinds the value of the report kinds property, a single code or a collection of codes
	 * @return {@code true} when the line is marked as a supplemental ingredient
	 */
	public static boolean isMarked(Serializable reportKinds) {
		if (reportKinds instanceof Collection<?> kinds) {
			return kinds.contains(PlmRepoConsts.REPORT_KIND_SUPPLEMENTAL_INGREDIENT);
		}
		return PlmRepoConsts.REPORT_KIND_SUPPLEMENTAL_INGREDIENT.equals(reportKinds);
	}
}
