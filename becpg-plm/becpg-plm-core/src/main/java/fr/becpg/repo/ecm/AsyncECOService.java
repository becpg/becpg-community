package fr.becpg.repo.ecm;

import org.alfresco.service.cmr.repository.NodeRef;

public interface AsyncECOService {

    void applyAsync(NodeRef ecoNodeRef);
	
	void doSimulationAsync(NodeRef ecoNodeRef);
	
}
