package fr.becpg.repo.decernis.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public class RegulatoryContext {

	private ProductData product;
	
	private List<RegulatoryContextItem> contextItems = new ArrayList<>();
	private List<ReqCtrlListDataItem> requirements = new LinkedList<>();
	private List<IngRegulatoryListDataItem> ingRegulatoryList = new LinkedList<>();
	
	
	public ProductData getProduct() {
		return product;
	}
	
	public void setProduct(ProductData product) {
		this.product = product;
	}
	
	public List<ReqCtrlListDataItem> getRequirements() {
		return requirements;
	}
	
	public List<RegulatoryContextItem> getContextItems() {
		return contextItems;
	}
	

	public List<IngRegulatoryListDataItem> getIngRegulatoryList() {
		return ingRegulatoryList;
	}

	public void setIngRegulatoryList(List<IngRegulatoryListDataItem> ingRegulatoryList) {
		this.ingRegulatoryList = ingRegulatoryList;
	}

	public DecernisMode getRegulatoryMode() {
		return product.getRegulatoryMode();
	}
	
	public String getRegulatoryRecipeId() {
		return product.getRegulatoryRecipeId();
	}
	
	
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
