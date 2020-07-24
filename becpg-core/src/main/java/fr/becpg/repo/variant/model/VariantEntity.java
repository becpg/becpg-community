package fr.becpg.repo.variant.model;

import java.util.List;

/**
 * <p>VariantEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface VariantEntity {

	
	/**
	 * <p>getDefaultVariantData.</p>
	 *
	 * @return a {@link fr.becpg.repo.variant.model.VariantData} object.
	 */
	VariantData getDefaultVariantData();
	/**
	 * <p>getVariants.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<VariantData> getVariants();
	
}
