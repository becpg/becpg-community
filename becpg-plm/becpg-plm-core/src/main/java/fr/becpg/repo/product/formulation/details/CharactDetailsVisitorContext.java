package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;

public class CharactDetailsVisitorContext {

	private ProductData rootProductData;
	
	private Integer maxLevel;
	
	private CharactDetails charactDetails;

	public CharactDetailsVisitorContext(ProductData rootProductData, Integer maxLevel, CharactDetails charactDetails) {
		super();
		this.rootProductData = rootProductData;
		this.maxLevel = maxLevel;
		this.charactDetails = charactDetails;
	}

	public ProductData getRootProductData() {
		return rootProductData;
	}

	public void setRootProductData(ProductData rootProductData) {
		this.rootProductData = rootProductData;
	}

	public Integer getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	public CharactDetails getCharactDetails() {
		return charactDetails;
	}

	public void setCharactDetails(CharactDetails charactDetails) {
		this.charactDetails = charactDetails;
	}
	
	
}
