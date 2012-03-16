package fr.becpg.repo.entity.datalist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaginatedExtractedItems {

	List<Map<String,Object>> items;
	
	int fullListSize;
	

	public PaginatedExtractedItems(Integer pageSize) {
		items = new ArrayList<Map<String,Object>>(pageSize);
	}

	public List<Map<String, Object>> getItems() {
		return items;
	}

	public void setItems(List<Map<String, Object>> items) {
		this.items = items;
	}

	public int getFullListSize() {
		return fullListSize;
	}

	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}
	
	
}
