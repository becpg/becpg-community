/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * <p>CompareEntityService interface.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface CompareEntityService {

	
	/**
	 * <p>compare.</p>
	 *
	 * @param entity1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entities a {@link java.util.List} object.
	 * @param compareResult a {@link java.util.List} object.
	 * @param structCompareResults a {@link java.util.Map} object.
	 * @return a {@link java.util.List} object.
	 */
	List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities,
										List<CompareResultDataItem> compareResult,
										Map<String, List<StructCompareResultDataItem>> structCompareResults);
	
	
	/**
	 * <p>compareStructDatalist.</p>
	 *
	 * @param entity1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entity2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistType a {@link org.alfresco.service.namespace.QName} object.
	 * @param structCompareResults a {@link java.util.Map} object.
	 */
	void compareStructDatalist(NodeRef entity1, NodeRef entity2, QName datalistType,
							   Map<String, List<StructCompareResultDataItem>> structCompareResults);
		
}
