package fr.becpg.repo.product.data;

import fr.becpg.repo.product.data.constraints.RegulatoryResult;

/**
 * <p>RegulatoryEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RegulatoryEntity extends RegulatoryEntityItem {

	/**
	 * <p>getRegulatoryResult.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.RegulatoryResult} object
	 */
	public RegulatoryResult getRegulatoryResult();

	/**
	 * <p>setRegulatoryResult.</p>
	 *
	 * @param regulatoryResult a {@link fr.becpg.repo.product.data.constraints.RegulatoryResult} object
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
