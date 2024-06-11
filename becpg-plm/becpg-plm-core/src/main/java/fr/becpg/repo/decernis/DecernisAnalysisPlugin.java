package fr.becpg.repo.decernis;


import fr.becpg.repo.decernis.model.RegulatoryContext;
import fr.becpg.repo.decernis.model.RegulatoryContextItem;

/**
 * <p>DecernisAnalysisPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DecernisAnalysisPlugin {

	/** Constant <code>MESSAGE_PROHIBITED_ING="message.decernis.ingredient.prohibited"</code> */
	public static final String MESSAGE_PROHIBITED_ING = "message.decernis.ingredient.prohibited";
	/** Constant <code>MESSAGE_PERMITTED_ING="message.decernis.ingredient.permitted"</code> */
	public static final String MESSAGE_PERMITTED_ING = "message.decernis.ingredient.permitted";
	/** Constant <code>MESSAGE_NOTLISTED_ING="message.decernis.ingredient.notListed"</code> */
	public static final String MESSAGE_NOTLISTED_ING = "message.decernis.ingredient.notListed";
	/** Constant <code>MESSAGE_FUNCTION_NOT_RECOGNIZED="message.decernis.function.notRecognized"</code> */
	public static final String MESSAGE_FUNCTION_NOT_RECOGNIZED = "message.decernis.function.notRecognized";

	/**
	 * <p>isEnabled.</p>
	 *
	 * @return a boolean
	 */
	boolean isEnabled();
	
	/**
	 * <p>needsRecipeId.</p>
	 *
	 * @return a boolean
	 */
	boolean needsRecipeId();
	
	/**
	 * <p>extractRequirements.</p>
	 *
	 * @param productContext a {@link fr.becpg.repo.decernis.model.RegulatoryContext} object
	 * @param contextItem a {@link fr.becpg.repo.decernis.model.RegulatoryContextItem} object
	 */
	void extractRequirements(RegulatoryContext productContext, RegulatoryContextItem contextItem);

	/**
	 * <p>ingredientAnalysis.</p>
	 *
	 * @param productContext a {@link fr.becpg.repo.decernis.model.RegulatoryContext} object
	 * @param contextItem a {@link fr.becpg.repo.decernis.model.RegulatoryContextItem} object
	 */
	void ingredientAnalysis(RegulatoryContext productContext, RegulatoryContextItem contextItem);
	


}
