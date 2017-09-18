package fr.becpg.repo.entity.simulation;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.version.EntityVersionService;

@Service
public class EntitySimpleSimulationPlugin implements EntitySimulationPlugin{


	@Autowired
	private EntityVersionService entityVersionService;
	
	
	@Override
	public boolean accept(String simulationMode) {
		return EntitySimulationPlugin.SIMPLE_MODE.equals(simulationMode);
	}

	@Override
	public List<NodeRef> simulateNodeRefs(NodeRef destNodeRef, List<NodeRef> entityNodeRefs) {
		List<NodeRef> ret = new ArrayList<>();
		
		for(NodeRef entityNodeRef : entityNodeRefs) {
			ret.add( entityVersionService.createBranch(entityNodeRef, destNodeRef));
		}
		return ret;
	}

}
