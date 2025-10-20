package fr.becpg.repo.regulatory;

import java.util.List;

import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * @author Valentin
 */
public interface RegulatoryPlugin {

	void checkRecipe(RegulatoryContext context, RegulatoryBatch checkContext);
	
	void checkIngredients(RegulatoryContext context, RegulatoryBatch checkContext);

	String fetchIngredientId(IngListDataItem ingListDataItem);

	List<CountryBatch> splitCountries(RegulatoryContext context, List<String> countries);

	List<UsageBatch> splitUsages(RegulatoryContext context, List<String> usages);

	Integer getBatchThreads();
	
}
