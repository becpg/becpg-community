/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * @author querephi
 */
public interface CompareEntityService {

	
	List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities,
										List<CompareResultDataItem> compareResult,
										Map<String, List<StructCompareResultDataItem>> structCompareResults);
	
	
	void compareStructDatalist(NodeRef entity1, NodeRef entity2, QName datalistType,
							   Map<String, List<StructCompareResultDataItem>> structCompareResults);
		
}
