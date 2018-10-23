package fr.becpg.repo.product.formulation.nutrient;

import fr.becpg.repo.product.formulation.nutrient.AbstractNutrientRegulation.NutrientDefinition;

public interface NutrientRegulation {

	Double round(Double value, String nutrientTypeCode, String nutUnit);

	Double roundGDA(Double value);

	NutrientDefinition getNutrientDefinition(String nutCode);

}
