package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

public class PhysicoRequirementScanner extends SimpleListRequirementScanner<PhysicoChemListDataItem> {


	@Override
	protected String getSpecErrorMessageKey() {
		return MESSAGE_PHYSICO_NOT_IN_RANGE;
	}
	

	public static final String MESSAGE_PHYSICO_NOT_IN_RANGE = "message.formulate.physicoChem.notInRangeValue";
	

	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList()!=null ? partProduct.getPhysicoChemList() : new ArrayList<>();
	}


	
}
