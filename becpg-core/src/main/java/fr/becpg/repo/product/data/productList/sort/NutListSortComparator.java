/*
 * 
 */
package fr.becpg.repo.product.data.productList.sort;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.data.productList.NutGroup;

// TODO: Auto-generated Javadoc
/**
 * Sort on group first, then on name (ascending).
 *
 * @author querephi
 */
public class NutListSortComparator implements Comparator<NutListDataItemDecorator>{

	/** The logger. */
	private static Log logger = LogFactory.getLog(NutListSortComparator.class);
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(NutListDataItemDecorator o1, NutListDataItemDecorator o2) {
		
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;	    
		int comparison = EQUAL;
				
		if(!o1.getNutListDataItem().getGroup().equals(o2.getNutListDataItem().getGroup())){
			
			NutGroup o1NutGroup = NutGroup.parse(o1.getNutListDataItem().getGroup());
			NutGroup o2NutGroup = NutGroup.parse(o2.getNutListDataItem().getGroup());
			
			if(o1NutGroup == NutGroup.Group1){
				comparison = BEFORE;
			}
			else if(o1NutGroup == NutGroup.Other){
				comparison = AFTER;
			}
			else if(o2NutGroup == NutGroup.Group1){
				comparison = AFTER;
			}
		}
		else{
			comparison = o1.getNutName().compareTo(o2.getNutName());
		}
				
		return comparison;
	}

}
