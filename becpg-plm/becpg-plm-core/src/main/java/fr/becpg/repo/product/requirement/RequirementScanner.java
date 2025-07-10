package fr.becpg.repo.product.requirement;

import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.regulatory.RequirementListDataItem;

/**
 * <p>RequirementScanner interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface RequirementScanner {

	/**
	 * <p>checkRequirements.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param specifications a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	List<RequirementListDataItem> checkRequirements(ProductData formulatedProduct, List<ProductSpecificationData> specifications);

}
