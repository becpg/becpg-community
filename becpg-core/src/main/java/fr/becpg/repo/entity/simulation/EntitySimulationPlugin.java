package fr.becpg.repo.entity.simulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntitySimulationPlugin {

	final static String DATALIST_MODE = "datalist";
	final static String SIMPLE_MODE = "simple";
	final static String RECUR_MODE = "recur";
		
	boolean accept(String simulationMode);

	List<NodeRef> simulateNodeRefs(NodeRef destNodeRef, List<NodeRef> nodeRefs);

}
