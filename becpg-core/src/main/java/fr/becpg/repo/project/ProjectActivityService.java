package fr.becpg.repo.project;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ProjectActivityService {

	void postTaskStateChangeActivity(NodeRef taskNodeRef,String beforeState,String afterState);
	
	void postProjectStateChangeActivity(NodeRef projectNodeRef,String beforeState,String afterState);
	
	void postDeliverableStateChangeActivity(NodeRef deliverableNodeRef,String beforeState,String afterState);

}
