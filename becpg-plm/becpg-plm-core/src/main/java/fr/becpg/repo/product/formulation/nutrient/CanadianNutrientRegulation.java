package fr.becpg.repo.product.formulation.nutrient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * <p>CanadianNutrientRegulation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CanadianNutrientRegulation extends AbstractNutrientRegulation {

	/**
	 * <p>Constructor for CanadianNutrientRegulation.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public CanadianNutrientRegulation(String path)  {
		super(path);
	}

	/** {@inheritDoc} */
	@Override
	protected Double roundByCode(Double value, String nutrientTypeCode) {

		if (value == null) {
			return null;
		}

		if(nutrientTypeCode != null){
			if (nutrientTypeCode.equals(NutrientCode.EnergykcalUS)) {
				if (value > 50) {
					return roundValue(value,10d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,5d);
				} else{
					return roundValue(value, 1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatTrans) || nutrientTypeCode.equals(NutrientCode.Fat) 
					|| nutrientTypeCode.equals(NutrientCode.FatSaturated)) {
				if (value > 5) {
					return roundValue(value,1d);
				} else if ((value >= 0.5) && (value <= 5)) {
					return roundValue(value,0.5d);
				} else if(value>0){
					return roundValue(value,0.1d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Sodium)) {
				
				if (value > 140) {
					return roundValue(value,10d);
				}
				else if ((value >= 5) && (value <= 140)) {
					return roundValue(value,5d);
				} else if(value>0){
					return roundValue(value,1d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Cholesterol)) {
				if (value > 0) {
					return roundValue(value,5d);
				} else {
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.CarbohydrateWithFiber)
					|| nutrientTypeCode.equals(NutrientCode.FiberDietary)
					|| nutrientTypeCode.equals(NutrientCode.FiberSoluble) 
					|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.Sugar)
					|| nutrientTypeCode.equals(NutrientCode.SugarAdded)){
				if (value >= 0.5) {
					return roundValue(value,1d);
				} else {
					return 0.0;
				}		
			} else if (nutrientTypeCode.startsWith(NutrientCode.Protein)){
				if (value >= 0.5) {
					return roundValue(value,1d);
				} else {
					return roundValue(value,0.1d);
				}		
			} else if (nutrientTypeCode.equals(NutrientCode.Potassium)
					|| nutrientTypeCode.equals(NutrientCode.Calcium)) {
				
				if (value > 250) {
					return roundValue(value,50d);
				} else if ((value > 50) && (value <= 250)) {
					return roundValue(value,25d);
				} else if ((value >= 5) && (value <= 50)) {
					return roundValue(value,10d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Iron)) {
				
				if (value > 2.5) {
					return roundValue(value,0.5d);
				} else if ((value > 0.5) && (value <= 2.5)) {
					return roundValue(value,0.25d);
				} else if ((value >= 0.05) && (value <= 0.5)) {
					return roundValue(value,0.1d);
				} else{
					return 0.0;
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FatPolyunsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatMonounsaturated)
					|| nutrientTypeCode.equals(NutrientCode.FatOmega3)
					|| nutrientTypeCode.equals(NutrientCode.FatOmega6)) {
				
				if (value > 5) {
					return roundValue(value,1d);
				} else if (value >= 1) {
					return roundValue(value,0.5d);
				} else{
					return roundValue(value,0.1d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.FiberSoluble)
					|| nutrientTypeCode.equals(NutrientCode.FiberInsoluble)
					|| nutrientTypeCode.equals(NutrientCode.Polyols)
					|| nutrientTypeCode.equals(NutrientCode.Starch)) {
				
				if (value >= 0.5) {
					return roundValue(value,1d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.VitA)) {
				
				if (value >= 250) {
					return roundValue(value,100d);
				} else if (value >= 50) {
					return roundValue(value,50d);
				} else if (value >= 5) {
					return roundValue(value,10d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.VitC)
					|| nutrientTypeCode.equals(NutrientCode.VitD)) {
				
				if (value >= 5) {
					return roundValue(value,1d);
				} else if (value >= 1) {
					return roundValue(value,0.5d);
				} else if (value >= 0.1) {
					return roundValue(value,0.2d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.VitE)
					|| nutrientTypeCode.equals(NutrientCode.VitK1)
					|| nutrientTypeCode.equals(NutrientCode.VitK2)
					|| nutrientTypeCode.equals(NutrientCode.Niacin)
					|| nutrientTypeCode.equals(NutrientCode.Biotin)
					|| nutrientTypeCode.equals(NutrientCode.Zinc)
					|| nutrientTypeCode.equals(NutrientCode.Molybdenum)) {
				
				if (value >= 2.5) {
					return roundValue(value,0.5d);
				} else if (value >= 0.5) {
					return roundValue(value,0.25d);
				} else if (value >= 0.05) {
					return roundValue(value,0.1d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Thiamin)
					|| nutrientTypeCode.equals(NutrientCode.Riboflavin)
					|| nutrientTypeCode.equals(NutrientCode.VitB6)
					|| nutrientTypeCode.equals(NutrientCode.VitB12)
					|| nutrientTypeCode.equals(NutrientCode.Manganese)) {
				
				if (value >= 0.25) {
					return roundValue(value,0.05d);
				} else if (value >= 0.05) {
					return roundValue(value,0.025d);
				} else if (value >= 0.005) {
					return roundValue(value,0.01d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Folate)
					|| nutrientTypeCode.equals(NutrientCode.FolateDFE)
					|| nutrientTypeCode.equals(NutrientCode.Choline)
					|| nutrientTypeCode.equals(NutrientCode.Iodine)
					|| nutrientTypeCode.equals(NutrientCode.Magnesium)) {
				
				if (value >= 50) {
					return roundValue(value,10d);
				} else if (value >= 10) {
					return roundValue(value,5d);
				} else if (value >= 1) {
					return roundValue(value,2d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Phosphorus)
					|| nutrientTypeCode.equals(NutrientCode.Chloride)) {
				
				if (value >= 250) {
					return roundValue(value,50d);
				} else if (value >= 50) {
					return roundValue(value,25d);
				} else if (value >= 5) {
					return roundValue(value,10d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.PantoAcid)) {
				
				if (value >= 0.5) {
					return roundValue(value,0.1d);
				} else if (value >= 0.1) {
					return roundValue(value,0.05d);
				} else if (value >= 0.01) {
					return roundValue(value,0.02d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Selenium)) {
				
				if (value >= 5) {
					return roundValue(value,1d);
				} else if (value >= 1) {
					return roundValue(value,0.5d);
				} else if (value >= 0.1) {
					return roundValue(value,0.2d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Copper)) {
				
				if (value >= 0.05) {
					return roundValue(value,0.01d);
				} else if (value >= 0.025) {
					return roundValue(value,0.025d);
				} else if (value >= 0.0015) {
					return roundValue(value,0.01d);
				} else{
					return roundValue(value,0d);
				}
			} else if (nutrientTypeCode.equals(NutrientCode.Chromium)) {
				
				if (value >= 2.5) {
					return roundValue(value,0.5d);
				} else if (value >= 0.5) {
					return roundValue(value,0.25d);
				} else if (value >= 0.05) {
					return roundValue(value,0.1d);
				} else{
					return roundValue(value,0d);
				}
			}
			
		}
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.round(new MathContext(3,RoundingMode.HALF_EVEN));
		return bd.doubleValue();
	}

}
