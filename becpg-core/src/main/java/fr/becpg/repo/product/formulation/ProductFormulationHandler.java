package fr.becpg.repo.product.formulation;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.filters.EffectiveFilters;

public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if ((productData.hasCompoListEl(EffectiveFilters.ALL)) ||
		(productData.hasPackagingListEl(EffectiveFilters.ALL)) ||
		(productData.hasProcessListEl(EffectiveFilters.ALL))) {
			//First Reset 
			
			if(productData.getCompoListView()!=null && productData.getCompoListView().getReqCtrlList()!=null){
				productData.getCompoListView().getReqCtrlList().clear();
			}
			
			if(productData.getIngLabelingList()!=null){
				productData.getIngLabelingList().clear();
			}
			
			// Continue
			return true;
		}
		// Skip formulation
		return false;

	}

}
