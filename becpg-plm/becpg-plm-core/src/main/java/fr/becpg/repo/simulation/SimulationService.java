package fr.becpg.repo.simulation;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;

public interface SimulationService {

	void createBudget(NodeRef destNodeRef, SystemState state);

}
