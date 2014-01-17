package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Engineering change order service
 * @author quere
 *
 */
public interface ECOService {	
	
	public void calculateWUsedList(NodeRef ecoNodeRef);
	
	public void apply(NodeRef ecoNodeRef, boolean requireNewTx);
	
	public void doSimulation(NodeRef ecoNodeRef);
}
