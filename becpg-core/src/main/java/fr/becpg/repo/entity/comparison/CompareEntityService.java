/*
 * 
 */
package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


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
	
	
}
