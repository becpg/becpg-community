package fr.becpg.repo.product.formulation.nutrient;

import java.util.Locale;

import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;

public interface NutrientRegulation {

	Double round(Double value, String nutrientTypeCode, String nutUnit);

	Double roundGDA(Double value, String nutrientTypeCode);

	NutrientDefinition getNutrientDefinition(String nutCode);
	
	String displayValue(Double value, Double roundedValue, String nutrientTypeCode, Locale locale);

}
