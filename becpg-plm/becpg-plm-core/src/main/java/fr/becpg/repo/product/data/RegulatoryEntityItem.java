package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface RegulatoryEntityItem {

	public List<NodeRef> getRegulatoryCountriesRef();

	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries);

	public List<NodeRef> getRegulatoryUsagesRef();

	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages);

}
