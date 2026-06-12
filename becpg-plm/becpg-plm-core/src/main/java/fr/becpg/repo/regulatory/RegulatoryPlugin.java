package fr.becpg.repo.regulatory;

import java.util.List;

import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.regulatory.decernis.*;

/**
 * <p>RegulatoryPlugin interface.</p>
 *
 * @author Valentin
 */
public interface RegulatoryPlugin {

	/**
	 * <p>checkRecipe.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 * @param checkContext a {@link RegulatoryBatch} object
	 */
	void checkRecipe(RegulatoryContext context, RegulatoryBatch checkContext);
	
	/**
	 * <p>checkIngredients.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 * @param checkContext a {@link RegulatoryBatch} object
	 */
	void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext);

	/**
	 * <p>fetchIngredientId.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	String fetchIngredientId(IngListDataItem ingListDataItem);

	/**
	 * <p>splitCountries.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 * @param countries a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries);

	/**
	 * <p>splitUsages.</p>
	 *
	 * @param context a {@link RegulatoryContext} object
	 * @param usages a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages);

	/**
	 * <p>getBatchThreads.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	Integer getBatchThreads();

}
