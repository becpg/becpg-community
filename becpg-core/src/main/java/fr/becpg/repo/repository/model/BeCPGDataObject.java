package fr.becpg.repo.repository.model;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;

public  abstract class  BeCPGDataObject  extends BaseObject implements RepositoryEntity {

	protected NodeRef nodeRef;
	
	protected NodeRef parentNodeRef;
	
	protected String name;
	
	protected Set<QName> aspects = new HashSet<QName>();
	
	
	public BeCPGDataObject() {
		super();
	}

	public BeCPGDataObject(NodeRef nodeRef, String name) {
		super();
		this.nodeRef = nodeRef;
		this.name = name;
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	@AlfProp
	@AlfQname(qname="cm:name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	
	public Set<QName> getAspects() {
		return aspects;
	}

	public void setAspects(Set<QName> aspects) {
		this.aspects = aspects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeCPGDataObject other = (BeCPGDataObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		return true;
	}
	
	

	
}
