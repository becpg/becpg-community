package fr.becpg.repo.entity;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;

public interface EntityTplPlugin {

	public void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);

	public boolean shouldSynchronizeDataList(RepositoryEntity entity, QName dataListQName);

    public <T extends RepositoryEntity> void synchronizeDataList(RepositoryEntity entity, List<T> dataListItems,
			List<T> tplDataListItems);

}
