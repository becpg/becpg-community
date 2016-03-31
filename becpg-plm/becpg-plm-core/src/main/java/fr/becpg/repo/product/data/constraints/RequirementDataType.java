package fr.becpg.repo.product.data.constraints;

/**
 * Represents a requirement data type : packaging constraint, nutrient constraint...
 * @author steven
 *
 */
public enum RequirementDataType {
	Packaging, Labelling, Ingredient, Allergen, Nutrient, Composition, Specification, Cost, Formulation, Completion, Validation, Physicochem, Labelclaim;

	public static RequirementDataType fromString(String dataType) {
		try {
			return RequirementDataType.valueOf(dataType);
		} catch(IllegalArgumentException | NullPointerException e){
			return null;
		}
	} 
}
