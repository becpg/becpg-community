/*
 * 
 */
package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;

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
	 */
	public static float getQty(CompoListDataItem compoListDataItem){
		
		float qty = compoListDataItem.getQty();		
		CompoListUnit compoListUnit = compoListDataItem.getCompoListUnit();
		
		if(compoListUnit == CompoListUnit.g || compoListUnit == CompoListUnit.mL){
			qty = qty / 1000;
		}
		
		return qty;
	}
}
