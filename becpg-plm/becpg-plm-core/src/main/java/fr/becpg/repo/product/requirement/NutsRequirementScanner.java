package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;

public class NutsRequirementScanner extends SimpleListRequirementScanner<NutListDataItem> {


	public static final String MESSAGE_NUT_NOT_IN_RANGE = "message.formulate.nut.notInRangeValue";
	

	@Override
	protected String getSpecErrorMessageKey() {
		return MESSAGE_NUT_NOT_IN_RANGE;
	}

	protected List<NutListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getNutList()!=null ? partProduct.getNutList() : new ArrayList<>();
	}


	
}
