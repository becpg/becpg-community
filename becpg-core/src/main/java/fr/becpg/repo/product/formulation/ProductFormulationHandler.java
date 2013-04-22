package fr.becpg.repo.product.formulation;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		//First Reset 			
		if(productData.getCompoListView()!=null && productData.getCompoListView().getReqCtrlList()!=null){
			productData.getCompoListView().getReqCtrlList().clear();
		}		
		if(productData.getPackagingListView()!=null && productData.getPackagingListView().getReqCtrlList()!=null){
			productData.getPackagingListView().getReqCtrlList().clear();
		}		
		if(productData.getProcessListView()!=null && productData.getProcessListView().getReqCtrlList()!=null){
			productData.getProcessListView().getReqCtrlList().clear();
		}
		
		if ((productData.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) ||
		(productData.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))) {			
			
			// Continue
			return true;
		}
		// Skip formulation
		return false;

	}

}
