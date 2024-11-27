package fr.becpg.repo.toxicology;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ToxicologyService interface.</p>
 *
 * @author matthieu
 */
public interface ToxicologyService {

	void updateToxIngAfterIngUpdate(NodeRef ingNodeRef);
	
	void updateToxIngAfterToxUpdate(NodeRef toxNodeRef);
	
	void deleteToxIngBeforeIngDelete(NodeRef ingNodeRef);
	
	void deleteToxIngBeforeToxDelete(NodeRef toxNodeRef);

	/**
	 * <p>computeMaxValue.</p>
	 *
	 * @param ingNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.Double} object
	 */
	Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef);

}
