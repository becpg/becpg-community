package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.ControlPointData;

public interface ControlPointDAO {

	public NodeRef create(NodeRef parentNodeRef, ControlPointData cpData);	
	public void update(NodeRef cpNodeRef, ControlPointData cpData);		
	public ControlPointData find(NodeRef cpNodeRef);
	public void delete(NodeRef cpNodeRef);
}
