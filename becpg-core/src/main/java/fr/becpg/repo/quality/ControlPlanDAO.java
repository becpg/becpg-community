package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.ControlPlanData;

public interface ControlPlanDAO {

	public NodeRef create(NodeRef parentNodeRef, ControlPlanData cpData);	
	public void update(NodeRef cpNodeRef, ControlPlanData cpData);		
	public ControlPlanData find(NodeRef cpNodeRef);
	public void delete(NodeRef cpNodeRef);
}
