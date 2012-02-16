package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.ProductVisitor;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

public class ProcessCalculatingVisitor implements ProductVisitor {

	private static Log logger = LogFactory.getLog(ProcessCalculatingVisitor.class);	
	
	@Override
	public ProductData visit(ProductData formulatedProduct) throws FormulateException {

		logger.debug("process calculating visitor");
		
		// no compo => no formulation
		if(formulatedProduct.getProcessList() == null){			
			logger.debug("no process => no formulation");
			return formulatedProduct;
		}
		
		// visit resources and steps from the end to the beginning
		Float minRateProcess = null;
		for(int z_idx=formulatedProduct.getProcessList().size()-1 ; z_idx>=0 ; z_idx--){
			
			ProcessListDataItem p = formulatedProduct.getProcessList().get(z_idx);
			
			if(p.getResource() != null && p.getRateResource() != null && p.getQtyResource() != null){
				
				// rateProcess
				Float rateProcess = p.getQtyResource() * p.getRateResource(); 
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
				
				// rateProduct
				if(minRateProcess != null && minRateProcess != 0f && p.getQty() != null){
					Float rateProduct = minRateProcess / p.getQty();
					p.setRateProduct(rateProduct);
				}				
				
				// reset
				minRateProcess = null;
			}
		}
		
		return formulatedProduct;
	}	
	
}
