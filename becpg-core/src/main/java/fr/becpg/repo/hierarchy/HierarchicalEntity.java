package fr.becpg.repo.hierarchy;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>HierarchicalEntity interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface HierarchicalEntity {

	/**
	 * <p>getHierarchy1.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getHierarchy1();
	/**
	 * <p>getHierarchy2.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getHierarchy2();
	
	
}
