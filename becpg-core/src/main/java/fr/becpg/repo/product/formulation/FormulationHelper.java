/*
 * 
 */
package fr.becpg.repo.product.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;

// TODO: Auto-generated Javadoc
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
	public static Double getQty(CompoListDataItem compoListDataItem) throws FormulateException{
		if(compoListDataItem.getQty() == null){
			logger.warn("Composition element doesn't have any quantity");
		} 
		
		Double qty = compoListDataItem.getQty()!=null ? compoListDataItem.getQty() : DEFAULT_QUANTITY ;		
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		
		if(compoListUnit == CompoListUnit.g || compoListUnit == CompoListUnit.mL){
			qty = qty / 1000;
		}
		return qty;
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
	 * 
	 * @param productData
	 * @return
	 */
	public static Double getDensity(ProductData productData) {
		return (productData.getDensity() != null) ? productData.getDensity():DEFAULT_DENSITY;
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
}
