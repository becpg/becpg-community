package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

//Immutable sorted cache entry
public class ChildAssocCacheEntry implements Serializable {

	
	private static final long serialVersionUID = -5994096437225498709L;
	private List<NodeRef> items = new LinkedList<>();
	private Map<QName,List<NodeRef>> itemsByType = new HashMap<>();
	
	public void add(NodeRef item, QName type) {
		 itemsByType.computeIfAbsent(type, k ->  new LinkedList<NodeRef>()).add(item);
		 items.add(item);
	}
	
	public Map<QName, List<NodeRef>> getItemsByType() {
		return itemsByType;
	}
	
	public void sort(CommonDataListSort commonDataListSort) {
			items.sort(commonDataListSort);
			for(List<NodeRef> toSort: itemsByType.values()) {
					toSort.sort(commonDataListSort);
			}
	}
	
	public List<NodeRef> get(QName type){
		if(type == null) {
			return Collections.unmodifiableList(items);
		}
		return Collections.unmodifiableList(itemsByType.computeIfAbsent(type, k ->  new LinkedList<NodeRef>()));
		
	}


	@Override
	public int hashCode() {
		return Objects.hash(items);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChildAssocCacheEntry other = (ChildAssocCacheEntry) obj;
		return Objects.equals(items, other.items);
	}



	
}
