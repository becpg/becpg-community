/*
 * 
 */
package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;

/**
 * The Class FormulationHelper.
 *
 * @author querephi
 */
public class FormulationHelper {	

	public static final Double DEFAULT_QUANTITY = 0d;

	public static final Double DEFAULT_DENSITY = 1d;

	public static final Double QTY_FOR_PIECE = 1d;
	
	private static Log logger = LogFactory.getLog(FormulationHelper.class);
	
	/**
	 * Gets the qty.
	 *
	 * @param compoListDataItem the compo list data item
	 * @return the qty
	 * @throws FormulateException 
	 */
	public static Double getQty(CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException{
		if(compoListDataItem.getQty() == null){
			logger.warn("Composition element doesn't have any quantity");
		} 
		
		Double qty = compoListDataItem.getQty()!=null ? compoListDataItem.getQty() : DEFAULT_QUANTITY ;		
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		
		if(compoListUnit == CompoListUnit.g || compoListUnit == CompoListUnit.mL){
			qty = qty / 1000;
		}
		
		Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
		
		return qty * density;
	}
	
	/**
	 * Gets the qty with lost.
	 *
	 * @param compoListDataItem the compo list data item
	 * @return the qty
	 * @throws FormulateException 
	 */
	public static Double getQtyWithLost(CompoListDataItem compoListDataItem, NodeService nodeService, Double parentLossRatio) throws FormulateException{
		
		Double qty = FormulationHelper.getQty(compoListDataItem, nodeService);
		Double lossPerc = compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0d;
		
		return qty * FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);		
	}
	
	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc) throws FormulateException{
		
		return (1 + lossPerc / 100) * parentLossRatio;		
	}

	/**
	 * Gets the qty of a packaging item
	 * @param packagingListDataItem
	 * @return
	 */
	public static Double getQty(PackagingListDataItem packagingListDataItem){
		
		if(packagingListDataItem.getQty() == null){
			logger.warn("Packaging element doesn't have any quantity");
		} 
		
		Double qty = packagingListDataItem.getQty()!=null ? packagingListDataItem.getQty() : DEFAULT_QUANTITY ;	
		PackagingListUnit packagingListUnit = packagingListDataItem.getPackagingListUnit();
		
		if(packagingListUnit == PackagingListUnit.PP && qty>0){
			qty = 1 / qty;
		}
		
		return qty;
	}
	
	/**
	 * Gets the qty of a process item
	 * @param processListDataItem
	 * @return
	 */
	public static Double getQty(ProductData formulatedProduct, ProcessListDataItem processListDataItem){
		
		Double qty = 0d;
		Double productQtyToTransform = processListDataItem.getQty() != null ? processListDataItem.getQty() : formulatedProduct.getQty();
						
		if(productQtyToTransform != null){			
			
			// process cost depends of rateProcess (€/h)
			if(processListDataItem.getRateProcess() != null && processListDataItem.getRateProcess() != 0d && processListDataItem.getQtyResource() != null){				
				qty = productQtyToTransform * processListDataItem.getQtyResource() / processListDataItem.getRateProcess();
			}
			// process cost doesn't depend of rateProcess (€/kg)
			else{
				qty = productQtyToTransform;
			}			
		}			
		
		return qty;
	}

	/**
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getDensity(ProductData productData) {
		return (productData.getDensity() != null) ? productData.getDensity():DEFAULT_DENSITY;
	}
	
	public static Double getDensity(NodeRef nodeRef, NodeService nodeService) {
		Double density = (Double)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_DENSITY);
		return (density != null) ? density:DEFAULT_DENSITY;
	}

	/**
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getQty(ProductData productData) {
		Double qty =  (productData.getUnit() != ProductUnit.P) ? productData.getQty(): QTY_FOR_PIECE; //unit => qty == 1
		if(qty==null){
			qty = DEFAULT_QUANTITY;
		}
		return qty;
	}

	/**
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getNetWeight(ProductData productData) {
		Double qty = getQty(productData); 
		Double density = getDensity(productData); 
		return  qty * density;
	}
	
	public static Double calculateValue(Double totalValue, Double qtyUsed, Double value, Double netWeight){
		
		if(totalValue == null && value == null){
			return null;
		}
		
		totalValue = totalValue != null ? totalValue : 0d;
		value = value != null ? value : 0d;		
		totalValue += qtyUsed * value / netWeight;		
		return totalValue;
	}
}
