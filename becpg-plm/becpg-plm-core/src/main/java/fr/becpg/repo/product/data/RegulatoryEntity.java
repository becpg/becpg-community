package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RegulatoryResult;

public interface RegulatoryEntity {

	public List<NodeRef> getRegulatoryCountries();

	public void setRegulatoryCountries(List<NodeRef> regulatoryCountries);

	public List<NodeRef> getRegulatoryUsages();

	public void setRegulatoryUsages(List<NodeRef> regulatoryUsages);

	public RegulatoryResult getRegulatoryResult();

	public void setRegulatoryResult(RegulatoryResult regulatoryResult);
	
	public String getRegulatoryRecipeId();
	
	public void setRegulatoryRecipeId(String regulatoryRecipeId);

}
