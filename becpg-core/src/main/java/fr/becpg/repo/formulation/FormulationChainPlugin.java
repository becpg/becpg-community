package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;

public interface FormulationChainPlugin {

	String getChainId();
	
	boolean isChainActiveOnEntity(NodeRef entityNodeRef);
}
