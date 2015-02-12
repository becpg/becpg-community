/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

public class CompositeHelper {

	private static Log logger = LogFactory.getLog(CompositeHelper.class);

	public static <T extends CompositeDataItem<T>> Composite<T> getHierarchicalCompoList(List<T> items) {

		Composite<T> composite = new Composite<T>();
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
				 Composite<T> temp = new Composite<T>(item);
				 composite.addChild(temp);
				 loadChildren(temp, items);
			 }
		 }
	 }

}
