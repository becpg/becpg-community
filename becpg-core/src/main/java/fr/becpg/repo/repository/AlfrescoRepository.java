package fr.becpg.repo.repository;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Should implement Spring Data
 * Used to retrieve entity from repository
 * @author matthieu
 *
 * @param <T>
 * @since 1.5
 */
public interface AlfrescoRepository<T extends RepositoryEntity>  extends CrudRepository<T, NodeRef> {

	boolean hasDataList(NodeRef entityNodeRef, QName datalistContainerQname);
	List<T> loadDataList(NodeRef entityNodeRef, QName datalistContainerQname, QName datalistQname);
	
	T create(NodeRef parentNodeRef, T entity);
	NodeRef getOrCreateDataListContainer(T entity);
	void saveDataList(NodeRef listContainerNodeRef, QName dataListContainerType, QName dataListType, List<? extends RepositoryEntity> dataList);

}
