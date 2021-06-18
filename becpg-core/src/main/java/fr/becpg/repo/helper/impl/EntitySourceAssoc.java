package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

public class EntitySourceAssoc implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4418227651517939879L;
	private NodeRef entityNodeRef;
	private NodeRef dataListItemNodeRef;
	private NodeRef sourceNodeRef;

	public EntitySourceAssoc(NodeRef entityNodeRef, NodeRef dataListItemNodeRef, NodeRef sourceNodeRef) {
		super();
		this.entityNodeRef = entityNodeRef;
		this.dataListItemNodeRef = dataListItemNodeRef;
		this.sourceNodeRef = sourceNodeRef;
	}

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public NodeRef getDataListItemNodeRef() {
		return dataListItemNodeRef;
	}

	public NodeRef getSourceNodeRef() {
		return sourceNodeRef;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataListItemNodeRef, entityNodeRef, sourceNodeRef);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntitySourceAssoc other = (EntitySourceAssoc) obj;
		return Objects.equals(dataListItemNodeRef, other.dataListItemNodeRef) && Objects.equals(entityNodeRef, other.entityNodeRef)
				&& Objects.equals(sourceNodeRef, other.sourceNodeRef);
	}
	
	

}
