package fr.becpg.repo.product.formulation.details;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.FormulateException;
import fr.becpg.repo.product.formulation.FormulationHelper;

public class CostCharactDetailsVisitor extends SimpleCharactDetailsVisitor {
	
	private static Log logger = LogFactory.getLog(CostCharactDetailsVisitor.class);

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {
		   
		  CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		
		  // no compo => no formulation
		   if(productData.getCompoList() == null){			
				logger.debug("no compo => no formulation");
				return ret;
		   }
		   
			Double netWeight = FormulationHelper.getNetWeight(productData);
			
			/*
			 * Calculate cost details 
			 */
		   for(CompoListDataItem compoItem : productData.getCompoList()){		

				Double lossPerc = compoItem.getLossPerc() != null ? compoItem.getLossPerc() : 0d;
				
				visitPart(compoItem.getProduct(), ret ,netWeight, (1 + lossPerc / 100));
			}
		   
		   /*
			 * Calculate the costs of the packaging
			 */
			if(productData.getPackagingList() != null && productData.getPackagingList().size()>0){
				for(PackagingListDataItem packagingListDataItem : productData.getPackagingList()){
					Double qty = FormulationHelper.getQty(packagingListDataItem);
					visitPart(packagingListDataItem.getProduct(), ret ,netWeight,qty);
					
				}
			}
			
			/*
			 * Calculate the costs of the processes
			 */
			if(productData.getProcessList() != null && productData.getProcessList().size()>0){
				
				
				for(ProcessListDataItem processListDataItem : productData.getProcessList()){
					
					Double stepDuration = null;
					//step : calculate step duration
					if(processListDataItem.getStep() != null && 
							processListDataItem.getRateProcess() != null && processListDataItem.getRateProcess() != 0d){
						stepDuration = processListDataItem.getQty() / processListDataItem.getRateProcess();
					}
					
					if(processListDataItem.getResource() != null && processListDataItem.getQtyResource() != null){
						
						Double qty = stepDuration * processListDataItem.getQtyResource();
						visitPart(processListDataItem.getResource(), ret ,netWeight, qty);										
					}					
					
					
															
				}
			}
		
		return ret;
	}



	
}
