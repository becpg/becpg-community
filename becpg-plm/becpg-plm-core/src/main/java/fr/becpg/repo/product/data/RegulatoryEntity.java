package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RegulatoryResult;

public interface RegulatoryEntity {

	public List<NodeRef> getRegulatoryCountriesRef();

	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries);

	public List<NodeRef> getRegulatoryUsagesRef();

	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages);

	public RegulatoryResult getRegulatoryResult();

	public void setRegulatoryResult(RegulatoryResult regulatoryResult);
	
	public String getRegulatoryRecipeId();
	
	public void setRegulatoryRecipeId(String regulatoryRecipeId);

}
