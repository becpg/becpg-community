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

	public static final Double DEFAULT_NET_WEIGHT = 0d;

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
		Double qty = null;
		if(FormulationHelper.isCompoUnitLiter(compoListDataItem.getCompoListUnit())){
			if(compoListDataItem.getQtySubFormula() != null){
				if(compoListDataItem.getCompoListUnit().equals(CompoListUnit.mL)){
					qty = compoListDataItem.getQtySubFormula() / 1000;
				}	
				else{
					qty = compoListDataItem.getQtySubFormula();
				}
			}					
		}
		else{
			qty = compoListDataItem.getQty();
		}		
		return qty != null ? qty : DEFAULT_COMPONANT_QUANTITY; 
	}
	
	public static Double getQtyInKg(CompoListDataItem compoListDataItem) throws FormulateException{				
		return compoListDataItem.getQty();
	}
	
	public static Double getQtySubFormula(CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException{				
		
		Double qty = compoListDataItem.getQtySubFormula();
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		if(qty != null && compoListUnit != null){
			
			if(compoListUnit.equals(CompoListUnit.kg)){
				return qty;
			}
			else if(compoListUnit.equals(CompoListUnit.g)){
				return qty / 1000;
			}
			else if(compoListUnit.equals(CompoListUnit.P)){
				Double productQty = FormulationHelper.getProductQty(compoListDataItem.getProduct(), nodeService);
				if(productQty == null){
					productQty = 1d;
				}
				return FormulationHelper.getNetWeight(compoListDataItem.getProduct(), nodeService) * qty / productQty;
			}
			else if(compoListUnit.equals(CompoListUnit.L) || compoListUnit.equals(CompoListUnit.mL)){
				Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
				
				if(compoListUnit.equals(CompoListUnit.mL)){
					qty = qty / 1000;
				}
				return qty * density;
			}			
			return qty;
		}	
		
		return null;
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
		return FormulationHelper.getQtyWithLost(FormulationHelper.getQty(compoListDataItem), FormulationHelper.calculateLossPerc(parentLossRatio, lossPerc));		
	}
	
	public static Double calculateLossPerc(Double parentLossRatio, Double lossPerc){	
		return 100 * ((1 + lossPerc / 100) * (1 + parentLossRatio / 100) - 1);		
	}
	
	public static Double getQtyWithLost(Double qty, Double lossPerc){		
		return (1 + lossPerc / 100) * qty;		
	}
	
	public static Double getQtyWithLost(PackagingListDataItem packagingListDataItem){		
		Double lossPerc = packagingListDataItem.getLossPerc() != null ? packagingListDataItem.getLossPerc() : 0d;
		return FormulationHelper.getQtyWithLost(FormulationHelper.getQty(packagingListDataItem), lossPerc);		
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
		
		if(qty>0 && packagingListUnit != null){
			if(packagingListUnit.equals(PackagingListUnit.PP)){
				qty = 1 / qty;
			}
			else if(packagingListUnit.equals(PackagingListUnit.g)){
				qty = qty / 1000;
			}
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

	public static Double getProductQty(NodeRef nodeRef, NodeService nodeService) {
		return (Double)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_QTY);
	}
	
	public static Double getDensity(NodeRef nodeRef, NodeService nodeService) {
		Double density = (Double)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_DENSITY);
		return density != null ? density : DEFAULT_DENSITY;
	}
	
	public static ProductUnit getProductUnit(NodeRef nodeRef, NodeService nodeService) {
		String strProductUnit = (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_UNIT);
		return strProductUnit != null ? ProductUnit.valueOf(strProductUnit) : null;
	}
	
	public static boolean isProductUnitLiter(ProductUnit unit) {
		return unit != null && (unit.equals(ProductUnit.L) || unit.equals(ProductUnit.mL));
	}
	
	public static boolean isProductUnitKg(ProductUnit unit) {
		return unit != null && (unit.equals(ProductUnit.kg) || unit.equals(ProductUnit.g));
	}
	
	public static boolean isProductUnitP(ProductUnit unit) {
		return unit != null && unit.equals(ProductUnit.P);
	}
	
	public static boolean isCompoUnitLiter(CompoListUnit unit) {
		return unit != null && (unit.equals(CompoListUnit.L) || unit.equals(CompoListUnit.mL));
	}
	
	public static boolean isCompoUnitKg(CompoListUnit unit) {
		return unit != null && (unit.equals(CompoListUnit.kg) || unit.equals(CompoListUnit.g));
	}
	
	public static boolean isCompoUnitP(CompoListUnit unit) {
		return unit != null && unit.equals(CompoListUnit.P);
	}
	
	public static boolean isPackagingListUnitKg(PackagingListUnit unit) {
		return unit != null && (unit.equals(PackagingListUnit.kg) || unit.equals(PackagingListUnit.g));
	}
	
	/**
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getNetWeight(NodeRef nodeRef, NodeService nodeService) {
		
		Double netWeight = (Double)nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_NET_WEIGHT);
		if(netWeight != null){	
			return netWeight;
		}
		else{			
			ProductUnit productUnit = getProductUnit(nodeRef, nodeService);									
			if(productUnit != null){
				Double qty = getProductQty(nodeRef, nodeService);
				if(qty != null){
					if(FormulationHelper.isProductUnitKg(productUnit) || FormulationHelper.isProductUnitLiter(productUnit)){					
						if(productUnit.equals(ProductUnit.g) || productUnit.equals(ProductUnit.mL)){
							qty = qty / 1000;
						}
						if(FormulationHelper.isProductUnitLiter(productUnit)){
							Double density = FormulationHelper.getDensity(nodeRef, nodeService);
							qty = qty * density;
						}
						return qty;
					}						
				}
				else if(FormulationHelper.isProductUnitP(productUnit)){
					return QTY_FOR_PIECE;
				}								
			}
		}	
				
		return DEFAULT_NET_WEIGHT;
	}
		
	public static Double getNetVolume(NodeRef nodeRef, NodeService nodeService) {
		
		Double qty = getProductQty(nodeRef, nodeService);
		if(qty == null){
			return null;
		}
		else{
			ProductUnit productUnit = getProductUnit(nodeRef, nodeService);					
			if(productUnit != null && (productUnit.equals(ProductUnit.mL) || productUnit.equals(ProductUnit.L))){
				if(productUnit.equals(ProductUnit.mL)){
					return qty / 1000;
				}
				else if(productUnit.equals(ProductUnit.L)){
					return  qty;
				}				
			}				
			return null;
		}		
	}
	
	public static Double getNetVolume(CompoListDataItem compoListDataItem, NodeService nodeService) throws FormulateException {
		
		Double volume = FormulationHelper.getNetVolume(compoListDataItem.getProduct(), nodeService);
		if(volume == null){			
			Double overrun = (Double)nodeService.getProperty(compoListDataItem.getNodeRef(), BeCPGModel.PROP_COMPOLIST_OVERRUN_PERC);
			Double qty = FormulationHelper.getQty(compoListDataItem);
			if(overrun != null && qty != null){
				Double density = FormulationHelper.getDensity(compoListDataItem.getProduct(), nodeService);
				if(density == null || density.equals(0d)){
					logger.warn("Cannot calculate volume since density is null or equals to 0");
				}
				else{
					volume = (100 + overrun) * qty / (density * 100);					
				}
			}
		}
		return volume;
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
			return null;
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
