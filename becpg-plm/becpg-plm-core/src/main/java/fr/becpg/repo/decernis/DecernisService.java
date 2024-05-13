package fr.becpg.repo.decernis;

import java.util.List;

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
	 public static final String DECERNIS_CHAIN_ID = "decernis";
	 
	public static final String MODULE_SUFFIX = " module";

	 public boolean isEnabled();
	 
	/**
	 * <p>extractDecernisRequirements.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param countries a {@link java.util.Set} object.
	 * @param usages a {@link java.util.Set} object.
	 * @return a {@link java.util.List} object.
	 */
	List<ReqCtrlListDataItem> extractRequirements(ProductData product);

}
