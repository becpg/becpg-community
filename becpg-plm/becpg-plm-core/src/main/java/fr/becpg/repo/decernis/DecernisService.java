package fr.becpg.repo.decernis;

import java.util.List;
import java.util.Set;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * <p>DecernisService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DecernisService {


	 /** Constant <code>DECERNIS_CHAIN_ID="decernis"</code> */
	 static final String DECERNIS_CHAIN_ID = "decernis";
	
	/**
	 * <p>extractDecernisRequirements.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param countries a {@link java.util.Set} object.
	 * @param usages a {@link java.util.Set} object.
	 * @return a {@link java.util.List} object.
	 */
	List<ReqCtrlListDataItem> extractDecernisRequirements(ProductData product, Set<String> countries, Set<String> usages);

	String createDecernisChecksum(ProductData formulatedProduct);
	
}
