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
package fr.becpg.repo.entity.datalist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class PaginatedExtractedItems {
	

	List<Map<String,Object>> items;
	
	List<AttributeExtractorStructure> computedFields;
	
	int fullListSize;
	

	public PaginatedExtractedItems(Integer pageSize) {
		items = new ArrayList<Map<String,Object>>(pageSize);
	}

	
	public boolean addItem(Map<String, Object> item){
		return items.add(item);
	}
	
	public List<Map<String, Object>> getPageItems() {
		return items;
	}


	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	public List<AttributeExtractorStructure> getComputedFields() {
		return computedFields;
	}

	public void setComputedFields(List<AttributeExtractorStructure> computedFields) {
		this.computedFields = computedFields;
	}
	

}
