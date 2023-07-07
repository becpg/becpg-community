package fr.becpg.repo.decernis;

import fr.becpg.repo.decernis.model.RegulatoryContext;

public interface DecernisAnalysisPlugin {

	boolean isEnabled();
	
	boolean needsRecipeId();
	
	void analyzeRecipe(RegulatoryContext context);
	
}
