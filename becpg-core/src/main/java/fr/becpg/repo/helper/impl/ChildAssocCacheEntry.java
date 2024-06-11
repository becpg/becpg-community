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
/**
 * <p>ChildAssocCacheEntry class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ChildAssocCacheEntry implements Serializable {

	
	private static final long serialVersionUID = -5994096437225498709L;
	private List<NodeRef> items = new LinkedList<>();
	private Map<QName,List<NodeRef>> itemsByType = new HashMap<>();
	
	/**
	 * <p>add.</p>
	 *
	 * @param item a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 */
	public void add(NodeRef item, QName type) {
		 itemsByType.computeIfAbsent(type, k ->  new LinkedList<NodeRef>()).add(item);
		 items.add(item);
	}
	
	/**
	 * <p>Getter for the field <code>itemsByType</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<QName, List<NodeRef>> getItemsByType() {
		return itemsByType;
	}
	
	/**
	 * <p>sort.</p>
	 *
	 * @param commonDataListSort a {@link fr.becpg.repo.helper.impl.CommonDataListSort} object
	 */
	public void sort(CommonDataListSort commonDataListSort) {
			items.sort(commonDataListSort);
			for(List<NodeRef> toSort: itemsByType.values()) {
					toSort.sort(commonDataListSort);
			}
	}
	
	/**
	 * <p>get.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> get(QName type){
		if(type == null) {
			return Collections.unmodifiableList(items);
		}
		return Collections.unmodifiableList(itemsByType.computeIfAbsent(type, k ->  new LinkedList<NodeRef>()));
		
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(items);
	}


	/** {@inheritDoc} */
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
