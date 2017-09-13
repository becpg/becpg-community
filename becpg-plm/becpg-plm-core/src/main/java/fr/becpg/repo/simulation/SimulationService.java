package fr.becpg.repo.simulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.productList.CompositionDataItem;

public interface SimulationService {

	void createBudget(NodeRef destNodeRef, SystemState state);

	NodeRef recurSimule(NodeRef entityNodeRef, CompositionDataItem dataListItem, List<NodeRef> dataListItemsNodeRefs);

	NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef);

}
