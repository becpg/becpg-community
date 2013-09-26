package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class ProcessCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(ProcessCalculatingFormulationHandler.class);	
	
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("process calculating visitor");
		
		// no compo => no formulation
		if(!formulatedProduct.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no process => no formulation");
			return true;
		}
		
		// visit resources and steps from the end to the beginning
		Double minRateProcess = null;
		for(int z_idx=formulatedProduct.getProcessList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT).size()-1 ; z_idx>=0 ; z_idx--){
			
			ProcessListDataItem p = formulatedProduct.getProcessList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT).get(z_idx);
			
			if(p.getRateResource() != null && p.getQtyResource() != null){
				
				// rateProcess
				Double rateProcess = p.getQtyResource() * p.getRateResource(); 
				p.setRateProcess(rateProcess);
				
				// minRateProcess
				if(minRateProcess==null){
					minRateProcess = rateProcess;
				}
				else if(rateProcess < minRateProcess){
					minRateProcess = rateProcess;
				}
			}
			
			if(p.getStep() != null){
				
				p.setRateProcess(minRateProcess);					
				Double productQtyToTransform = p.getQty() != null ? p.getQty() : formulatedProduct.getQty();
				
				// rateProduct
				if(minRateProcess != null && minRateProcess != 0d && productQtyToTransform != null){
					p.setRateProduct(minRateProcess / productQtyToTransform);
				}
				else{
					p.setRateProduct(null);
				}
				
				// reset
				minRateProcess = null;
			}
		}
		
		return true;
	}	
	
}
