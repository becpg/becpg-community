/*
 * 
 */
package fr.becpg.repo.product.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.TareUnit;
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

	public static final Double DEFAULT_PRODUCT_QUANTITY = 1d;

	public static final Double DEFAULT_COMPONANT_QUANTITY = 0d;

	public static final Double DEFAULT_DENSITY = 1d;

	public static final Double QTY_FOR_PIECE = 1d;
	
	public static final Double DEFAULT_YIELD = 100d;
	
	private static Log logger = LogFactory.getLog(FormulationHelper.class);
	
	/**
	 * Gets the qty.
	 *
	 * @param compoListDataItem the compo list data item
	 * @return the qty
	 * @throws FormulateException 
	 */
	public static Double getQty(CompoListDataItem compoListDataItem) throws FormulateException{
		return compoListDataItem.getQty();
	}
	
	public static Double getQtySubFormula(CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException{				
		return getQtyInKg(compoListDataItem.getQtySubFormula(), compoListDataItem, nodeService);
	}
	
	private static Double getQtyInKg(Double qty, CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException{
		
		if(qty==null){
			return null;
		}
		else{
			CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
			Double density = DEFAULT_DENSITY;
			
			if(compoListUnit != null){
				if(compoListUnit.equals(CompoListUnit.g) || compoListUnit.equals(CompoListUnit.mL)){
					qty = qty / 1000;
				}
				
				if(!compoListUnit.equals(CompoListUnit.g) && !compoListUnit.equals(CompoListUnit.kg) && !compoListUnit.equals(CompoListUnit.Perc)){
					density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
				}			
			}
			
			return qty * density;
		}				
	}
	
	/**
	 * Gets the qty with lost.
	 *
	 * @param compoListDataItem the compo list data item
	 * @return the qty
	 * @throws FormulateException 
	 */
	public static Double getQtyWithLost(CompoListDataItem compoListDataItem, Double parentLossRatio) throws FormulateException{
		
		Double lossPerc = compoListDataItem.getLossPerc() != null ? compoListDataItem.getLossPerc() : 0d;
		
		return compoListDataItem.getQty() * FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc);		
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
		
		Double qty = packagingListDataItem.getQty()!=null ? packagingListDataItem.getQty() : DEFAULT_COMPONANT_QUANTITY ;	
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
	public static Double getNetWeight(ProductData productData) {
		
		Double qty = productData.getQty() != null ? productData.getQty() : DEFAULT_PRODUCT_QUANTITY;
		if(qty == null){
			return null;
		}
		else{
			ProductUnit productUnit = productData.getUnit();					
			if(productUnit != null && (productUnit.equals(ProductUnit.g) || productUnit.equals(ProductUnit.mL))){
				qty = qty / 1000;
			}
			Double density = getDensity(productData);
			return  qty * density;
		}		
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
	
	public static Double getTareInKg(NodeRef packagingNodeRef, NodeService nodeService){
		
		Double tare = (Double)nodeService.getProperty(packagingNodeRef, PackModel.PROP_TARE);
		String strTareUnit = (String)nodeService.getProperty(packagingNodeRef, PackModel.PROP_TARE_UNIT);
		if(tare == null || strTareUnit == null){
			return DEFAULT_COMPONANT_QUANTITY;
		}
		else{
			
			TareUnit tareUnit = TareUnit.parse(strTareUnit);
			
			if(tareUnit == TareUnit.g || tareUnit == TareUnit.gPerm2){
				tare = tare / 1000;
			}
			
			return tare;
		}				
	}
}
