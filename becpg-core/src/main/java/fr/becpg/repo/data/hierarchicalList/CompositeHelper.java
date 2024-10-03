/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.data.hierarchicalList;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>CompositeHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompositeHelper {

	private static final Log logger = LogFactory.getLog(CompositeHelper.class);

	
	private CompositeHelper() {
		//Do nothing
	}
	
	/**
	 * <p>getHierarchicalCompoList.</p>
	 *
	 * @param items a {@link java.util.List} object.
	 * @return a {@link fr.becpg.repo.data.hierarchicalList.Composite} object.
	 */
	public static <T extends CompositeDataItem<T>> Composite<T> getHierarchicalCompoList(List<T> items) {

		Composite<T> composite = new Composite<>();
		loadChildren(composite, items);
		if (logger.isDebugEnabled()) {
			logger.debug(composite.toString());
		}
		return composite;
	}

	 private static <T extends CompositeDataItem<T>> void loadChildren(Composite<T> composite, List<T> items ){
		 for (T item : items){
			 if((item.getParent() == null && composite.getData() == null) 
					 ||  (item.getParent() != null && item.getParent().equals(composite.getData()))){
				 Composite<T> temp = new Composite<>(item);
				 composite.addChild(temp);
				 loadChildren(temp, items);
			 }
		 }
	 }

}
