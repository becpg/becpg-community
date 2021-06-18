package fr.becpg.repo.helper.impl;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class AssociationCacheRegion implements Serializable {
	private static final long serialVersionUID = -213050301938804468L;

	protected final NodeRef nodeRef;
	protected final QName assocQName;

	private final int hashCode;

	public AssociationCacheRegion(@Nonnull NodeRef nodeRef, @Nonnull QName assocQName) {
		this.nodeRef = nodeRef;
		this.assocQName = assocQName;
		this.hashCode = Objects.hash(nodeRef, assocQName);
	}

	public NodeRef getNodeRef() {
		return nodeRef;
	}
	
	public QName getAssocQName() {
		return assocQName;
	}

	@Override
	public String toString() {
		return nodeRef.toString() + "." + assocQName.toString();
	}

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

	@Override
	//Do not change, already compute for perfs reason
	public int hashCode() {
		return hashCode;
	}

}