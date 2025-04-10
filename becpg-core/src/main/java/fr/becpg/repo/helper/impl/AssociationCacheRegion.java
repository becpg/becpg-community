package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * <p>AssociationCacheRegion class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AssociationCacheRegion implements Serializable {
	private static final long serialVersionUID = -213050301938804468L;

	/**
	 * The node reference for the association cache.
	 */
	protected final NodeRef nodeRef;
	
	/**
	 * The qualified name of the association.
	 */
	protected final QName assocQName;

	private final int hashCode;

	/**
	 * <p>Constructor for AssociationCacheRegion.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param assocQName a {@link org.alfresco.service.namespace.QName} object
	 */
	public AssociationCacheRegion(@Nonnull NodeRef nodeRef, @Nonnull QName assocQName) {
		this.nodeRef = nodeRef;
		this.assocQName = assocQName;
		this.hashCode = Objects.hash(nodeRef, assocQName);
	}

	/**
	 * <p>Getter for the field <code>nodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	/**
	 * <p>Getter for the field <code>assocQName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object
	 */
	public QName getAssocQName() {
		return assocQName;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return nodeRef.toString() + "." + assocQName.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AssociationCacheRegion other = (AssociationCacheRegion) obj;
		return (hashCode == other.hashCode) && Objects.equals(nodeRef, other.nodeRef) && Objects.equals(assocQName, other.assocQName);
	}

	/** {@inheritDoc} */
	@Override
	//Do not change, already compute for perfs reason
	public int hashCode() {
		return hashCode;
	}

}
