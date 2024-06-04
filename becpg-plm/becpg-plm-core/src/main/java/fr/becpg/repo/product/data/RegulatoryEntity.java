package fr.becpg.repo.product.data;

import fr.becpg.repo.product.data.constraints.RegulatoryResult;

public interface RegulatoryEntity extends RegulatoryEntityItem {

	public RegulatoryResult getRegulatoryResult();

	public void setRegulatoryResult(RegulatoryResult regulatoryResult);
	
	public String getRegulatoryRecipeId();
	
	public void setRegulatoryRecipeId(String regulatoryRecipeId);

}
