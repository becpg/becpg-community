package fr.becpg.repo.entity;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

/**
 * 
 * @author matthieu
 *
 */
@Service
public interface EntitySystemService {

	/**
	 * 
	 * @param parentNodeRef
	 * @param path
	 * @param entitySystemDataLists
	 * @return create systeme entity
	 */
	public NodeRef createSystemEntity(NodeRef parentNodeRef, String path, Map<String, QName> entitySystemDataLists);

	/**
	 * 
	 * @param parentNodeRef
	 * @param systemEntityPath
	 * @return system entity for given systemEntityPath
	 */
	public NodeRef getSystemEntity(NodeRef parentNodeRef, String systemEntityPath);

	/**
	 * 
	 * @param systemEntityNodeRef
	 * @param dataListPath
	 * @returns system entity datalist
	 */
	public NodeRef getSystemEntityDataList(NodeRef systemEntityNodeRef, String dataListPath);

	/**
	 * 
	 * @param parentNodeRef
	 * @param systemEntityPath
	 * @param dataListPath
	 * @return system entity datalist
	 */
	public NodeRef getSystemEntityDataList(NodeRef parentNodeRef, String systemEntityPath, String dataListPath);

	/**
	 * 
	 * @return entities of type TYPE_SYSTEM_ENTITY
	 */
	public List<NodeRef> getSystemEntities();

}
