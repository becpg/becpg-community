package fr.becpg.repo.repository;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

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
	 * Optional Set to add extra aspects
	 * @return
	 */
	public Set<QName> getAspects();
	public void setAspects(Set<QName> aspects);
	
	/**
	 * Optional Map to put extra props
	 * @return
	 */
	public Map<QName, Serializable> getExtraProperties();
	public void setExtraProperties(Map<QName, Serializable> extraProperties);

	public boolean isTransient();
	
	
}
