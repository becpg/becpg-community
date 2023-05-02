package fr.becpg.repo.product.data.constraints;

/**
 * Represents a requirement data type : packaging constraint, nutrient constraint...
 *
 * @author steven
 * @version $Id: $Id
 */
public enum RequirementDataType {
	Packaging, Labelling, Ingredient, Allergen, Nutrient, Composition, Specification, Cost, Lca, Formulation, Completion, Validation, Physicochem, Labelclaim;

	/**
	 * <p>fromString.</p>
	 *
	 * @param dataType a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementDataType} object.
	 */
	public static RequirementDataType fromString(String dataType) {
		try {
			return RequirementDataType.valueOf(dataType);
		} catch(IllegalArgumentException | NullPointerException e){
			return null;
		}
	} 
	
	
}
