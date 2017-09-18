package fr.becpg.repo.entity.simulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author matthieu
 *
 */
public interface EntitySimulationService {

	void simuleDataListItems(NodeRef entityNodeRef, List<NodeRef> dataListItemsNodeRefs);

	NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef destNodeRef);
	
	NodeRef createSimulationNodeRefs( List<NodeRef> entityNodeRefs, NodeRef destNodeRef, String mode);

}
