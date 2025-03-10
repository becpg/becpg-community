package fr.becpg.repo.entity.simulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntitySimulationService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntitySimulationService {

	/**
	 * <p>simuleDataListItems.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataListItemsNodeRefs a {@link java.util.List} object.
	 * @param branch a boolean.
	 */
	void simuleDataListItems(NodeRef entityNodeRef, List<NodeRef> dataListItemsNodeRefs, boolean branch);

	/**
	 * <p>createSimulationNodeRef.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef destNodeRef);
	
	/**
	 * <p>createSimulationNodeRefs.</p>
	 *
	 * @param entityNodeRefs a {@link java.util.List} object.
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param mode a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createSimulationNodeRefs( List<NodeRef> entityNodeRefs, NodeRef destNodeRef, String mode);

}
