package fr.becpg.repo.toxicology;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ToxicologyService interface.</p>
 *
 * @author matthieu
 */
public interface ToxicologyService {

	/**
	 * <p>createOrUpdateToxIngNodeRef.</p>
	 *
	 * @param ingNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createOrUpdateToxIngNodeRef(NodeRef ingNodeRef, NodeRef toxNodeRef);

	/**
	 * <p>computeMaxValue.</p>
	 *
	 * @param ingNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.Double} object
	 */
	Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef);

}
