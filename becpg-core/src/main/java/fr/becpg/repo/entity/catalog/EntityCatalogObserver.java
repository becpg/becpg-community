package fr.becpg.repo.entity.catalog;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>EntityCatalogObserver interface.</p>
 *
 * @author matthieu
 */
public interface EntityCatalogObserver {

	/**
	 * <p>notifyAuditedFieldChange.</p>
	 *
	 * @param string a {@link java.lang.String} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void notifyAuditedFieldChange(String string, NodeRef entityNodeRef);

	/**
	 * <p>acceptCatalogEvents.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param listNodeRefs a {@link java.util.Set} object
	 * @return a boolean
	 */
	boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs);
	
}
