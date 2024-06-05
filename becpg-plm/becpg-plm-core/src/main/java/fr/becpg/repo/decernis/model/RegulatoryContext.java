package fr.becpg.repo.decernis.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>RegulatoryContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class RegulatoryContext {

	private ProductData product;
	
	private List<RegulatoryContextItem> contextItems = new ArrayList<>();
	private List<ReqCtrlListDataItem> requirements = new LinkedList<>();
	private List<IngRegulatoryListDataItem> ingRegulatoryList = new LinkedList<>();
	
	
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
	public List<ReqCtrlListDataItem> getRequirements() {
		return requirements;
	}
	
	/**
	 * <p>Getter for the field <code>contextItems</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<RegulatoryContextItem> getContextItems() {
		return contextItems;
	}
	

	/**
	 * <p>Getter for the field <code>ingRegulatoryList</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<IngRegulatoryListDataItem> getIngRegulatoryList() {
		return ingRegulatoryList;
	}

	/**
	 * <p>Setter for the field <code>ingRegulatoryList</code>.</p>
	 *
	 * @param ingRegulatoryList a {@link java.util.List} object
	 */
	public void setIngRegulatoryList(List<IngRegulatoryListDataItem> ingRegulatoryList) {
		this.ingRegulatoryList = ingRegulatoryList;
	}

	/**
	 * <p>getRegulatoryMode.</p>
	 *
	 * @return a {@link fr.becpg.repo.decernis.DecernisMode} object
	 */
	public DecernisMode getRegulatoryMode() {
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
	
	
	/**
	 * <p>isTreatable.</p>
	 *
	 * @return a boolean
	 */
	public boolean isTreatable() {
		if (!product.getAspects().contains(PLMModel.ASPECT_REGULATORY)) {
			return false;
		}
		if (product.getIngList() == null || product.getIngList().isEmpty()) {
			return false;
		}
		
		for (RegulatoryContextItem contextItem : contextItems) {
			if (!contextItem.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
}
