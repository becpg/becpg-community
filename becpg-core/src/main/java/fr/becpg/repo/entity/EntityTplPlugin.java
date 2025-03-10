package fr.becpg.repo.entity;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>EntityTplPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityTplPlugin {

	/**
	 * <p>beforeSynchronizeEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void beforeSynchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);
	
	/**
	 * <p>synchronizeEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityTplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void synchronizeEntity(NodeRef entityNodeRef, NodeRef entityTplNodeRef);

	/**
	 * <p>shouldSynchronizeDataList.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param dataListQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	boolean shouldSynchronizeDataList(RepositoryEntity entity, QName dataListQName);

    /**
     * <p>synchronizeDataList.</p>
     *
     * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object.
     * @param dataListItems a {@link java.util.List} object.
     * @param tplDataListItems a {@link java.util.List} object.
     * @param <T> a T class
     */
    <T extends RepositoryEntity> void synchronizeDataList(RepositoryEntity entity, List<T> dataListItems,
														  List<T> tplDataListItems);

}
