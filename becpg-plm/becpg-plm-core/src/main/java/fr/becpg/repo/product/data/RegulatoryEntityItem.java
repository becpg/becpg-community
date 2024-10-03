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
	
	
	
	/**
	 * <p>getRegulatoryMessage.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public MLText getRegulatoryMessage();
	
	/**
	 * <p>getRegulatoryType.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	public RequirementType getRegulatoryType();
	
	/**
	 * <p>setRegulatoryMessage.</p>
	 *
	 * @param regulatoryMessage a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setRegulatoryMessage(MLText regulatoryMessage);
	
	/**
	 * <p>setRegulatoryType.</p>
	 *
	 * @param type a {@link fr.becpg.repo.product.data.constraints.RequirementType} object
	 */
	public void setRegulatoryType(RequirementType type);
	
	


}
