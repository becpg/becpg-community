package fr.becpg.repo.regulatory;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>RegulatoryEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RegulatoryEntity {

	
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
	 * <p>getRegulatoryResult.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RegulatoryResult} object
	 */
	public RegulatoryResult getRegulatoryResult();

	/**
	 * <p>setRegulatoryResult.</p>
	 *
	 * @param regulatoryResult a {@link fr.becpg.repo.regulatory.RegulatoryResult} object
	 */
	public void setRegulatoryResult(RegulatoryResult regulatoryResult);
	
	/**
	 * <p>getRegulatoryRecipeId.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getRegulatoryRecipeId();
	
	/**
	 * <p>setRegulatoryRecipeId.</p>
	 *
	 * @param regulatoryRecipeId a {@link java.lang.String} object
	 */
	public void setRegulatoryRecipeId(String regulatoryRecipeId);

}
