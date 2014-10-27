package fr.becpg.repo.entity.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityTplPlugin;

@Service
public class EntityDefaultTplPlugin implements EntityTplPlugin{

	@Override
	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef) {
	   // Default plugin for spring injection or it will break designer
	   //Do nothing
	}


}
