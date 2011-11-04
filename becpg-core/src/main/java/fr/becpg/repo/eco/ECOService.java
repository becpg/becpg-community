package fr.becpg.repo.eco;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Engineering change order service
 * @author quere
 *
 */
public interface ECOService {	
	
	public void calculateWUsedList(NodeRef ecoNodeRef);
	
	public void apply(NodeRef ecoNodeRef);
	
	public void createSimulationComposants(NodeRef ecoNodeRef);
	
	public NodeRef generateSimulationReport(NodeRef ecoNodeRef);
	
	public void doSimulation(NodeRef ecoNodeRef);
}
