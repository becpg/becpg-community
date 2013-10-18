package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.filters.EffectiveFilters;
import fr.becpg.repo.variant.filters.VariantFilters;

@Service
public class TareFormulationHandler extends FormulationBaseHandler<ProductData> {
	
	private static Log logger = LogFactory.getLog(TareFormulationHandler.class);
	
	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("Tare visitor");		
		
		// no compo => no formulation
		if(!formulatedProduct.hasCompoListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT) && 
				!formulatedProduct.hasPackagingListEl(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			logger.debug("no compoList, no packagingList => no formulation");
			return true;
		}
		
		// Tare
		Double tare = calculateTareOfComposition(formulatedProduct);
		tare += calculateTareOfPackaging(formulatedProduct);		

		formulatedProduct.setTare(tare * 1000);
		formulatedProduct.setTareUnit(TareUnit.g);
		
		return true;
	}	
	
	@SuppressWarnings("unchecked")
	private Double calculateTareOfComposition(ProductData formulatedProduct){
		Double totalTare = 0d;
		for(CompoListDataItem compoList : formulatedProduct.getCompoList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){			
			Double qty = compoList.getQty();
			CompoListUnit unit = compoList.getCompoListUnit();
			if(compoList.getProduct() != null && qty != null && unit != null){		
				
				ProductData productData = alfrescoRepository.findOne(compoList.getProduct());				
				Double productQty = productData.getQty();
				ProductUnit productUnit = productData.getUnit();
				Double tare = FormulationHelper.getTareInKg(productData.getTare(), productData.getTareUnit());
				
				if(tare != null && productUnit != null && productQty != null){
					
					if(FormulationHelper.isProductUnitP(productUnit)){
						productQty = 1d;
						qty = compoList.getQtySubFormula();
					}
					else if(FormulationHelper.isProductUnitLiter(productUnit)){						
						int compoFactor = unit.equals(CompoListUnit.L) ? 1000 : 1;
						int productFactor = productUnit.equals(ProductUnit.L) ? 1000 : 1;						
						qty = compoList.getQtySubFormula() * compoFactor / productFactor;					
					}
					else if(FormulationHelper.isProductUnitKg(productUnit)){
						if(productUnit.equals(ProductUnit.g)){
							qty = qty * 1000;
						}
					}
					
					logger.debug("compo tare: " + tare + " qty " + qty + " productQty " + productQty);					
					totalTare += (tare * qty / productQty);
				}				
			}
		}			
		return totalTare;
	}
	
	@SuppressWarnings("unchecked")
	private Double calculateTareOfPackaging(ProductData formulatedProduct){
		Double totalTare = 0d;
		for(PackagingListDataItem packList : formulatedProduct.getPackagingList(EffectiveFilters.EFFECTIVE, VariantFilters.DEFAULT_VARIANT)){
			
			// take in account only primary
			if(packList.getPkgLevel() != null && packList.getPkgLevel().equals(PackagingLevel.Primary)){
							
				Double tare = 0d;
				if(FormulationHelper.isPackagingListUnitKg(packList.getPackagingListUnit())){
					tare = FormulationHelper.getQty(packList);
				}else{
					ProductData productData = alfrescoRepository.findOne(packList.getProduct());
					tare = FormulationHelper.getQty(packList) * FormulationHelper.getTareInKg(productData.getTare(), productData.getTareUnit());
				}					
				logger.debug("pack tare " + tare);
				totalTare += tare;
			}			
		}			
		return totalTare;
	}
	
}
