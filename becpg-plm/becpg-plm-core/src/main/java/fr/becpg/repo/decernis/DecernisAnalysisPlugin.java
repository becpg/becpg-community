package fr.becpg.repo.decernis;

import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public interface DecernisAnalysisPlugin {

	public static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	public static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";
	public static final String MESSAGE_FUNCTION_NOT_RECOGNIZED = "message.decernis.function.notRecognized";

	boolean isEnabled();
	
	boolean needsRecipeId();
	
	List<ReqCtrlListDataItem> extractRequirements(JSONObject analysisResults, List<IngListDataItem> ings, String country, Integer moduleId);

	JSONObject postRecipeAnalysis(RegulatoryContext productContext, Set<String> countries, String usage, Integer moduleId);

	String extractAnalysisResult(JSONObject analysis);
	
}
