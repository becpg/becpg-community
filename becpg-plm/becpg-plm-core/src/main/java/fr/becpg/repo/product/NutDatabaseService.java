package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.product.data.productList.NutListDataItem;

public interface NutDatabaseService {

	/**
	 * Returns the list of the nutrients databases names found in folder
	 * @return e.g. {"Ciqual", "USDA", "AHS"}
	 */
	List<FileInfo> getNutDatabases();
	
	/**
	 * Autocomplete
	 * @param dataBaseId - the NodeRef of the CSV File
	 * @param query - string to match the name of the product
	 * @return 
	 */
	ListValuePage suggest(String databaseName, String query, int pageNum, int pageSize);
	
	/**
	 * Returns the list of nutrients bound to this product in the db
	 * @param id - key of the product in db
	 * @return The list of NutListDataItem found in this product
	 */
	List<NutListDataItem> getNuts(NodeRef file, String id);
	
	
	/**
	 * Creates product with identifier id in csv file
	 * @param file - the NodeRef of the CSV File
	 * @param id - the id found in the file of the product to create
	 * @return the NodeRef of the created product
	 */
	NodeRef createProduct(NodeRef file, String id, NodeRef dest);
}
