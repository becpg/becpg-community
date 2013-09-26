package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


//TODO merge with BaseObject and BeCPGDataObject
public interface RepositoryEntity {

	public NodeRef getNodeRef();
	public void setNodeRef(NodeRef nodeRef);
	
	public NodeRef getParentNodeRef();
	public void setParentNodeRef(NodeRef parentNodeRef);
	public String getName();
	
	
	
	/**
	 * Optional Map to put extra props
	 * @return
	 */
	public Map<QName, Serializable> getExtraProperties();
	public void setExtraProperties(Map<QName, Serializable> extraProperties);

	public boolean isTransient();
	
	
}
