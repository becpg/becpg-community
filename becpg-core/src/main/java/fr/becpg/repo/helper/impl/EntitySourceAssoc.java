package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntitySourceAssoc class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntitySourceAssoc implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4418227651517939879L;
	private NodeRef entityNodeRef;
	private NodeRef dataListItemNodeRef;
	private NodeRef sourceNodeRef;

	/**
	 * <p>Constructor for EntitySourceAssoc.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dataListItemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param sourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public EntitySourceAssoc(NodeRef entityNodeRef, NodeRef dataListItemNodeRef, NodeRef sourceNodeRef) {
		super();
		this.entityNodeRef = entityNodeRef;
		this.dataListItemNodeRef = dataListItemNodeRef;
		this.sourceNodeRef = sourceNodeRef;
	}

	/**
	 * <p>Getter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	/**
	 * <p>Getter for the field <code>dataListItemNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getDataListItemNodeRef() {
		return dataListItemNodeRef;
	}

	/**
	 * <p>Getter for the field <code>sourceNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getSourceNodeRef() {
		return sourceNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(dataListItemNodeRef, entityNodeRef, sourceNodeRef);
	}

	/** {@inheritDoc} */
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
