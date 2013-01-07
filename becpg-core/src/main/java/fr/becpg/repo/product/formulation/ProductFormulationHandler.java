package fr.becpg.repo.product.formulation;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;

public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if ((productData.getCompoList() != null && !productData.getCompoList().isEmpty()) ||
		(productData.getPackagingList() != null && !productData.getPackagingList().isEmpty()) ||
		(productData.getProcessList() != null && !productData.getProcessList().isEmpty())) {
			//First Reset 
			
			if(productData.getReqCtrlList()!=null){
				productData.getReqCtrlList().clear();
			}
			
			// Continue
			return true;
		}
		// Skip formulation
		return false;

	}

}
