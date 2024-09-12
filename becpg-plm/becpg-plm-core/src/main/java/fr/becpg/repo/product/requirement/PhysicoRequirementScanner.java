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
	protected String getSpecErrorMessageKey(PhysicoChemListDataItem specDataItem) {
		return MESSAGE_PHYSICO_NOT_IN_RANGE;
	}
	
	/** {@inheritDoc} */
	@Override
	protected String getSpecInfoMessageKey(PhysicoChemListDataItem specDataItem) {
		return MESSAGE_PHYSICO_NOT_IN_RANGE_INFO;
	}
	

	/** Constant <code>MESSAGE_PHYSICO_NOT_IN_RANGE="message.formulate.physicoChem.notInRang"{trunked}</code> */
	public static final String MESSAGE_PHYSICO_NOT_IN_RANGE = "message.formulate.physicoChem.notInRangeValue";
	
	/** Constant <code>MESSAGE_PHYSICO_NOT_IN_RANGE_INFO="message.formulate.info.physicoChem.notI"{trunked}</code> */
	public static final String MESSAGE_PHYSICO_NOT_IN_RANGE_INFO = "message.formulate.info.physicoChem.notInRangeValue";
	

	/** {@inheritDoc} */
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList()!=null ? partProduct.getPhysicoChemList() : new ArrayList<>();
	}

	@Override
	protected Double getValue(PhysicoChemListDataItem specDataItem, PhysicoChemListDataItem listDataItem) {
		return listDataItem.getValue();
	}


	
}
