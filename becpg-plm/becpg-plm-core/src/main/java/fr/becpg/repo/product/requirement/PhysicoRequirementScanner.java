package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

/**
 * <p>PhysicoRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PhysicoRequirementScanner extends SimpleListRequirementScanner<PhysicoChemListDataItem> {


	/** {@inheritDoc} */
	@Override
	protected String getSpecErrorMessageKey() {
		return MESSAGE_PHYSICO_NOT_IN_RANGE;
	}
	

	/** Constant <code>MESSAGE_PHYSICO_NOT_IN_RANGE="message.formulate.physicoChem.notInRang"{trunked}</code> */
	public static final String MESSAGE_PHYSICO_NOT_IN_RANGE = "message.formulate.physicoChem.notInRangeValue";
	

	/** {@inheritDoc} */
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList()!=null ? partProduct.getPhysicoChemList() : new ArrayList<>();
	}


	
}
