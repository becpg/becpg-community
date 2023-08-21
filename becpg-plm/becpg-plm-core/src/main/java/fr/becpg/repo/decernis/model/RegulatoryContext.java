package fr.becpg.repo.decernis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.decernis.DecernisMode;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

public class RegulatoryContext {

	private ProductData product;
	private Set<String> countries = new HashSet<>();
	private Set<String> usages = new HashSet<>();
	private Integer moduleId;
	
	private List<RegulatoryContextItem> contextItems = new ArrayList<>();
	
	private Map<String, List<IngListDataItem>> ingRegulatoryMapping = new HashMap<>();
	
	private List<ReqCtrlListDataItem> requirements = new LinkedList<>();
	
	public void setCountries(Set<String> countries) {
		this.countries = countries;
	}
	
	public void setUsages(Set<String> usages) {
		this.usages = usages;
	}
	
	public Integer getModuleId() {
		return moduleId;
	}
	
	public void setModuleId(Integer moduleId) {
		this.moduleId = moduleId;
	}
	
	public Set<String> getCountries() {
		return countries;
	}
	
	public Set<String> getUsages() {
		return usages;
	}

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
	
	public Map<String, List<IngListDataItem>> getIngRegulatoryMapping() {
		return ingRegulatoryMapping;
	}
	
	public void setIngRegulatoryMapping(Map<String, List<IngListDataItem>> ingRegulatoryMapping) {
		this.ingRegulatoryMapping = ingRegulatoryMapping;
	}

	public boolean isTreatable() {
		if (!product.getAspects().contains(PLMModel.ASPECT_REGULATORY)) {
			return false;
		}
		if (product.getIngList() == null || product.getIngList().isEmpty()) {
			return false;
		}
		
		if (!usages.isEmpty() && !countries.isEmpty()) {
			return true;
		}
		
		for (RegulatoryContextItem contextItem : contextItems) {
			if (!contextItem.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public DecernisMode getRegulatoryMode() {
		return product.getRegulatoryMode();
	}
	
	public String getRegulatoryRecipeId() {
		return product.getRegulatoryRecipeId();
	}
	
}
