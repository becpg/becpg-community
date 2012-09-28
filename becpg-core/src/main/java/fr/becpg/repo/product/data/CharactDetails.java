package fr.becpg.repo.product.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Contains charact details
 * @author matthieu
 *
 */
public class CharactDetails {

	List<NodeRef> computedCharacts = null;
	
	Map<NodeRef,Map<NodeRef,Double>> data = new HashMap<NodeRef,Map<NodeRef,Double>>();
	
	public CharactDetails(List<NodeRef> computedCharacts) {
		super();
		this.computedCharacts = computedCharacts;
	}

	public void addKeyValue(NodeRef charactNodeRef, NodeRef key, Double value) {
		Map<NodeRef,Double> tmp = data.get(charactNodeRef);
		if(tmp==null){
			tmp = new HashMap<NodeRef,Double>();
		}
		tmp.put(key, value);
		
		data.put(charactNodeRef, tmp);
	}

	public boolean hasElement(NodeRef charactNodeRef) {
		return computedCharacts==null || computedCharacts.isEmpty() || computedCharacts.contains(charactNodeRef);
	}

	


	public Map<NodeRef, Map<NodeRef, Double>> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "CharactDetails [computedCharacts=" + computedCharacts + ", data=" + data + "]";
	}
	
	
	

}
