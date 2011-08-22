/*
 * 
 */
package fr.becpg.repo.product.data.productList.sort;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * Sort items by name.
 *
 * @author querephi
 */
public class CostListSortComparator implements Comparator<CostListDataItemDecorator> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(CostListDataItemDecorator o1, CostListDataItemDecorator o2) {
		
		return o1.getCostName().compareTo(o2.getCostName());
	}

}
