package fr.becpg.repo.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityTplPlugin {

	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);

}
