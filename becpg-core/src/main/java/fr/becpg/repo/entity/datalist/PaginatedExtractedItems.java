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
