package fr.becpg.repo.entity.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityTplPlugin;
import fr.becpg.repo.repository.RepositoryEntity;

@Service
public class EntityDefaultTplPlugin implements EntityTplPlugin{

	@Override
	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef) {
	   // Default plugin for spring injection or it will break designer
	   //Do nothing
	}

	@Override
	public boolean shouldSynchronizeDataList(RepositoryEntity entity, QName dataListQName) {
		return false;
	}

	@Override
	public <T extends RepositoryEntity> void synchronizeDataList(RepositoryEntity entity, List<T> dataListItems,
			List<T> tplDataListItems){
		
	}



}
