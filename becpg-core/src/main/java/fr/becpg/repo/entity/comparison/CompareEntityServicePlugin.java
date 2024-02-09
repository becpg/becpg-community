package fr.becpg.repo.entity.comparison;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>CompareEntityServicePlugin interface.</p>
 *
 * @author matthieu, kevin
 * @version $Id: $Id
 */
public interface CompareEntityServicePlugin {
	
	/**
	 * <p>isDefault.</p>
	 *
	 * @return a boolean.
	 */
	boolean isDefault();
	
	/**
	 * <p>compareEntities.</p>
	 *
	 * @param entity1NodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entity2NodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nbEntities a int.
	 * @param comparisonPosition a int.
	 * @param comparisonMap a {@link java.util.Map} object.
	 * @param structCompareResults a {@link java.util.Map} object.
	 */
	void compareEntities(NodeRef entity1NodeRef, NodeRef entity2NodeRef, int nbEntities, int comparisonPosition,
			Map<String, CompareResultDataItem> comparisonMap, Map<String, List<StructCompareResultDataItem>> structCompareResults);
	
	/**
	 * <p>isComparableProperty.</p>
	 *
	 * @param qName a {@link org.alfresco.service.namespace.QName} object.
	 * @param isDataList a boolean.
	 * @return a boolean.
	 */
	boolean isComparableProperty(QName qName, boolean isDataList);

	/**
	 * <p>compareStructDatalist.</p>
	 *
	 * @param entity1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entity2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param datalistType a {@link org.alfresco.service.namespace.QName} object.
	 * @param structCompareResults a {@link java.util.Map} object.
	 */
	void compareStructDatalist(NodeRef entity1NodeRef, NodeRef entity2NodeRef, QName datalistType,
			Map<String, List<StructCompareResultDataItem>> structCompareResults);

	/**
	 * <p>applyTo.</p>
	 *
	 * @param entityType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean applyTo(QName entityType);
}
