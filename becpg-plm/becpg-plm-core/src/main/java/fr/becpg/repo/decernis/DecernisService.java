package fr.becpg.repo.decernis;

import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.regulatory.RequirementListDataItem;

/**
 * <p>DecernisService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DecernisService {


	 /** Constant <code>DECERNIS_CHAIN_ID="decernis"</code> */
	 public static final String DECERNIS_CHAIN_ID = "decernis";
	 
	/** Constant <code>MODULE_SUFFIX=" module"</code> */
	public static final String MODULE_SUFFIX = " module";

	/** Constant <code>NOT_APPLICABLE="NA"</code> */
	public static final String NOT_APPLICABLE = "NA";

	 /**
	  * <p>isEnabled.</p>
	  *
	  * @return a boolean
	  */
	 public boolean isEnabled();
	 
	/**
	 * <p>extractDecernisRequirements.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @return a {@link java.util.List} object.
	 */
	List<RequirementListDataItem> extractRequirements(ProductData product);

}
