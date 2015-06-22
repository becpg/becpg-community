/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


// TODO: Auto-generated Javadoc
/**
 * The Interface CompareEntityService.
 *
 * @author querephi
 */
public interface CompareEntityService {

	/**
	 * Compare some entities.
	 *
	 * @param entity1 the entity1
	 * @param entities the entities
	 * @return the list
	 */
	List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities,
										List<CompareResultDataItem> compareResult,
										Map<String, List<StructCompareResultDataItem>> structCompareResults);
	
	/**
	 * Do a structural comparison.
	 *
	 * @param entity1 the entity1
	 * @param entity2 the entity2
	 * @param datalistType the datalist type
	 * @param pivotProperty the pivot property
	 * @return the list
	 */
	void compareStructDatalist(NodeRef entity1, NodeRef entity2, QName datalistType,
							   Map<String, List<StructCompareResultDataItem>> structCompareResults);
		
}
