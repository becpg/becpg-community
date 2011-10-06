/*
 * 
 */
package fr.becpg.repo.product.formulation;

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

	/**
	 * Gets the qty.
	 *
	 * @param compoListDataItem the compo list data item
	 * @return the qty
	 * @throws FormulateException 
	 */
	public static float getQty(CompoListDataItem compoListDataItem) throws FormulateException{
		if(compoListDataItem.getQty() == null){
			throw new FormulateException("message.formulate.failure.qty");
		}
		
		float qty = compoListDataItem.getQty();		
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
	public static float getQty(PackagingListDataItem packagingListDataItem){
		
		float qty = packagingListDataItem.getQty();		
		PackagingListUnit packagingListUnit = packagingListDataItem.getPackagingListUnit();
		
		if(packagingListUnit == PackagingListUnit.PP){
			qty = 1 / qty;
		}
		
		return qty;
	}
}
