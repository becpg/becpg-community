package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RequirementType;

/**
 * <p>RegulatoryEntityItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RegulatoryEntityItem {

	/**
	 * <p>getRegulatoryCountriesRef.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getRegulatoryCountriesRef();

	/**
	 * <p>setRegulatoryCountriesRef.</p>
	 *
	 * @param regulatoryCountries a {@link java.util.List} object
	 */
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries);

	/**
	 * <p>getRegulatoryUsagesRef.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getRegulatoryUsagesRef();

	/**
	 * <p>setRegulatoryUsagesRef.</p>
	 *
	 * @param regulatoryUsages a {@link java.util.List} object
	 */
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages);
	
	
	
	public MLText getRegulatoryMessage();
	
	public RequirementType getRegulatoryType();
	
	public void setRegulatoryMessage(MLText regulatoryMessage);
	
	public void setRegulatoryType(RequirementType type);
	
	


}
