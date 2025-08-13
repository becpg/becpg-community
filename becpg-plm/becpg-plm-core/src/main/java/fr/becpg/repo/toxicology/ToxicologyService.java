package fr.becpg.repo.toxicology;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ToxicologyService interface.</p>
 *
 * @author matthieu
 */
public interface ToxicologyService {

	/**
	 * <p>updateToxIngAfterIngUpdate.</p>
	 *
	 * @param ingNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void updateIngredient(NodeRef ingNodeRef);
	
	/**
	 * <p>updateToxIngAfterToxUpdate.</p>
	 *
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void updateIngredientsFromTox(NodeRef toxNodeRef);
	
	/**
	 * <p>deleteToxIngBeforeToxDelete.</p>
	 *
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	void removeToxFromIngredients(NodeRef toxNodeRef);

	/**
	 * <p>getMaxValue.</p>
	 *
	 * @param ingNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param toxNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef);

}
