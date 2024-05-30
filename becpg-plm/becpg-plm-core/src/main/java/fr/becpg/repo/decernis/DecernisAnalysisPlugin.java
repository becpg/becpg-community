package fr.becpg.repo.decernis;


import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;

public interface DecernisAnalysisPlugin {

	public static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	public static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";
	public static final String MESSAGE_FUNCTION_NOT_RECOGNIZED = "message.decernis.function.notRecognized";

	boolean isEnabled();
	
	boolean needsRecipeId();
	
	void extractRequirements(RegulatoryContext productContext, RegulatoryContextItem contextItem);

	void ingredientAnalysis(RegulatoryContext productContext, RegulatoryContextItem contextItem);
	


}
