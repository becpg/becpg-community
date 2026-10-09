package fr.becpg.repo.product.formulation.nutrient;

import java.util.Optional;
import java.util.Set;

import org.alfresco.util.Pair;

/**
 * <p>ChineseNutrientRegulation class.</p>
 *
 * <p>Tolerances follow GB 28050-2011, table 2, on the amount declared per 100 g: energy, fat,
 * saturated fat, trans fat, cholesterol, sodium and sugars must not exceed 120 % of the declared
 * value, vitamins A and D must stay between 80 % and 180 % of it, and protein, unsaturated fat,
 * carbohydrate, fibers, the other vitamins and minerals and any added nutrient must reach 80 % of
 * it. GB 28050-2025, effective on 16 March 2027, changes vitamins A and D to at least 80 %.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ChineseNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * Nutrients that must not exceed 120 % of the declared value. Sugars are assumed not to be
	 * lactose, which the rule assesses as a nutrient to promote but the list does not tell apart.
	 */
	private static final Set<String> NUTRIENTS_TO_LIMIT = Set.of(NutrientCode.EnergykJ, NutrientCode.Energykcal, NutrientCode.Fat,
			NutrientCode.FatSaturated, NutrientCode.FatTrans, NutrientCode.Cholesterol, NutrientCode.Sodium, NutrientCode.Sugar);

	private static final Set<String> VITAMINS_A_AND_D = Set.of(NutrientCode.VitA, NutrientCode.VitD);

	private static final Set<String> NUTRIENTS_TO_PROMOTE = Set.of(NutrientCode.FatPolyunsaturated, NutrientCode.FatMonounsaturated,
			NutrientCode.CarbohydrateByDiff, NutrientCode.CarbohydrateWithFiber, NutrientCode.FiberDietary, NutrientCode.FiberSoluble,
			NutrientCode.FiberInsoluble);

	/** Codes the mineral list holds although they are not minerals of the rule. */
	private static final Set<String> NOT_MINERALS = Set.of(NutrientCode.Starch, NutrientCode.Salt);

	/**
	 * <p>Constructor for ChineseNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public ChineseNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Pair<Double, Double> tolerancesByCode(Double value, String nutrientTypeCode, NutrientToleranceCriteria criteria) {
		Optional<DeclaredValueTolerance> tolerance = findTolerance(nutrientTypeCode, criteria.added());
		if ((value == null) || tolerance.isEmpty()) {
			return null;
		}
		return tolerance.get().limits(value, ServingBasis.perHundredGrams(), v -> roundByCode(v, nutrientTypeCode));
	}

	/**
	 * <p>Gives the tolerance of a nutrient.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @param added whether the nutrient was added to the product, which makes any nutrient not
	 *            listed elsewhere a nutrient to promote
	 * @return the tolerance, empty when the rule does not assess the nutrient
	 */
	private Optional<DeclaredValueTolerance> findTolerance(String nutrientTypeCode, boolean added) {
		if (NUTRIENTS_TO_LIMIT.contains(nutrientTypeCode)) {
			return Optional.of(DeclaredValueTolerance.AT_MOST_120_PERCENT);
		}
		if (VITAMINS_A_AND_D.contains(nutrientTypeCode)) {
			return Optional.of(DeclaredValueTolerance.BETWEEN_80_AND_180_PERCENT);
		}
		if (isNutrientToPromote(nutrientTypeCode) || added) {
			return Optional.of(DeclaredValueTolerance.AT_LEAST_80_PERCENT);
		}
		return Optional.empty();
	}

	/**
	 * <p>Tells whether a nutrient must reach 80 % of its declared value whether added or not.</p>
	 *
	 * @param nutrientTypeCode the nutrient code
	 * @return {@code true} for protein, unsaturated fat, carbohydrate, fibers, vitamins and minerals
	 */
	private boolean isNutrientToPromote(String nutrientTypeCode) {
		return NUTRIENTS_TO_PROMOTE.contains(nutrientTypeCode) || nutrientTypeCode.startsWith(NutrientCode.Protein) || isVitamin(nutrientTypeCode)
				|| (isMineral(nutrientTypeCode) && !NOT_MINERALS.contains(nutrientTypeCode));
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {
		
		if(value != null){
			if (nutrientTypeCode.equals(NutrientCode.EnergykJ)) {
				//delta=1, limit to declare0 is <=17
				return roundValue(value<=17?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Protein)
					|| nutrientTypeCode.equals(NutrientCode.Fat)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateByDiff)
					|| nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)) {
				return roundValue(value<=0.5?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatSaturated)){
				return roundValue(value<=0.1?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans)){
				return roundValue(value<=0.3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol) 
					|| nutrientTypeCode.equals(NutrientCode.Sodium)){
				return roundValue(value<=5?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitA)
					|| nutrientTypeCode.equals(NutrientCode.FolicAcid)){
				return roundValue(value<=8?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Calcium)){
				return roundValue(value<=8?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitD)){
				return roundValue(value<=0.1?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.VitTocpha)
					|| nutrientTypeCode.equals(NutrientCode.VitB3)){
				return roundValue(value<=0.28?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitK1)){
				return roundValue(value<=1.6?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitB1)
					|| nutrientTypeCode.equals(NutrientCode.VitB2)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.Copper)){
				return roundValue(value<=0.03?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitB12)){
				return roundValue(value<=0.05?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.VitC)){
				return roundValue(value<=2?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.PantoAcid)){
				return roundValue(value<=0.1?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Biotin)){
				return roundValue(value<=0.6?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Choline)){
				return roundValue(value<=9?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Phosphorus)){
				return roundValue(value<=14?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Potassium)){
				return roundValue(value<=20?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Magnesium)){
				return roundValue(value<=6?0:value, 1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Iron)){
				return roundValue(value<=0.3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Zinc)){
				return roundValue(value<=0.3?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Iodine)){
				return roundValue(value<=3?0:value, 0.1d);
			} else if (nutrientTypeCode.equals(NutrientCode.Fluoride)){
				return roundValue(value<=0.02?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Manganese)){
				return roundValue(value<=0.06?0:value, 0.01d);
			} else if (nutrientTypeCode.equals(NutrientCode.Selenium)) {
				return roundValue(value<=1?0:value, 0.1d);
			}

		}
		
		return roundValue(value,0.1d);
	}

}
