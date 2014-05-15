package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

public interface AsyncECOService {

    public void applyAsync(NodeRef ecoNodeRef);
	
	public void doSimulationAsync(NodeRef ecoNodeRef);
	
}
