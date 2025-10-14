package fr.becpg.repo.regulatory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;

/**
 * <p>RegulatoryContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RegulatoryContext {

	private ProductData product;
	
	private List<IngListDataItem> ingList = new CopyOnWriteArrayList<>();
	
	private List<RegulatoryBatch> regulatoryBatches = new CopyOnWriteArrayList<>();

	private Map<String, NodeRef> countryMap = new ConcurrentHashMap<>();
	private Map<String, String> usageMap = new ConcurrentHashMap<>();

	private List<RequirementListDataItem> requirements = new CopyOnWriteArrayList<>();

	private List<IngRegulatoryListDataItem> ingRegulatoryListDataItems = new CopyOnWriteArrayList<>();

	public List<IngListDataItem> getIngList() {
		return ingList;
	}
	
	public List<IngRegulatoryListDataItem> getIngRegulatoryListDataItems() {
		return ingRegulatoryListDataItems;
	}
	
	public List<RegulatoryBatch> getRegulatoryBatches() {
		return regulatoryBatches;
	}
	
	public void setRegulatoryBatches(List<RegulatoryBatch> regulatoryBatches) {
		this.regulatoryBatches = regulatoryBatches;
	}
	
	public void addCountry(String countryCode, NodeRef countryNodeRef) {
		countryMap.put(countryCode, countryNodeRef);
	}
	
	public void addUsage(String usageCode, String moduleName) {
		usageMap.put(usageCode, moduleName);
	}
	
	public String getUsageModule(String usageCode) {
		return usageMap.get(usageCode);
	}
	
	public NodeRef getCountryNodeRef(String countryCode) {
		return countryMap.get(countryCode);
	}

	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public ProductData getProduct() {
		return product;
	}
	
	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	public void setProduct(ProductData product) {
		this.product = product;
	}
	
	/**
	 * <p>Getter for the field <code>requirements</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<RequirementListDataItem> getRequirements() {
		return requirements;
	}
	
	/**
	 * <p>getRegulatoryMode.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RegulatoryMode} object
	 */
	public RegulatoryMode getRegulatoryMode() {
		return product.getRegulatoryMode();
	}
	
	/**
	 * <p>getRegulatoryRecipeId.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getRegulatoryRecipeId() {
		return product.getRegulatoryRecipeId();
	}
	
}
