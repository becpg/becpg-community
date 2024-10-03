package fr.becpg.repo.product.formulation.nutrient;

import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.product.data.productList.NutListDataItem;

/**
 * <p>NutDatabaseService interface.</p>
 *
 * @author steven
 * @version $Id: $Id
 */
public interface NutDatabaseService {

	/**
	 * Returns the list of the nutrients databases names found in folder
	 *
	 * @return e.g. {"Ciqual", "USDA", "AHS"}
	 */
	List<FileInfo> getNutDatabases();
	
	/**
	 * Autocomplete
	 *
	 * @param databaseName - the NodeRef of the CSV File
	 * @param query - string to match the name of the product
	 * @param pageNum a int.
	 * @param pageSize a int.
	 * @return a {@link fr.becpg.repo.autocomplete.AutoCompletePage} object.
	 */
	AutoCompletePage suggest(String databaseName, String query, int pageNum, int pageSize);
	
	/**
	 * Returns the list of nutrients bound to this product in the db
	 *
	 * @param id - key of the product in db
	 * @return The list of NutListDataItem found in this product
	 * @param file a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	List<NutListDataItem> getNuts(NodeRef file, String id);
	
	
	/**
	 * Creates product with identifier id in csv file
	 *
	 * @param file - the NodeRef of the CSV File
	 * @param id - the id found in the file of the product to create
	 * @return the NodeRef of the created product
	 * @param dest a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createProduct(NodeRef file, String id, NodeRef dest);
	
	/**
	 * Returns the full name of the product in db if it exists
	 *
	 * @param file csv file
	 * @param id e.g. CIQUAL3200
	 * @return e.g. CIQUAL3200 - Broccoli, cru
	 */
	String getProductName(NodeRef file, String id);

}
