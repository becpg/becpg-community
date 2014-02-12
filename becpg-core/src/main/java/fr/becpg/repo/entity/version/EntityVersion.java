/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.util.Date;

import org.alfresco.repo.version.common.VersionImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * The Class VersionData.
 * 
 * @author querephi
 */
public class EntityVersion extends VersionImpl {
	
	/**
	 * generated serial number
	 */
	private static final long serialVersionUID = -4165153505281567656L;

	private NodeRef entityNodeRef;
	
	private NodeRef entityVersionNodeRef;
	
	private NodeRef entityBranchFromNodeRef;
	
	private Date createdDate = null;
	
	public NodeRef getEntityVersionNodeRef() {
		return entityVersionNodeRef;
	}

	public NodeRef getEntityBranchFromNodeRef() {
		return entityBranchFromNodeRef;
	}

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public EntityVersion(Version version, NodeRef entityNodeRef, NodeRef entityVersionNodeRef, NodeRef entityBranchFromNodeRef) {
		super(version.getVersionProperties(), version.getFrozenStateNodeRef());
		this.entityVersionNodeRef = entityVersionNodeRef;
		this.entityBranchFromNodeRef = entityBranchFromNodeRef;
		this.entityNodeRef = entityNodeRef;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.version.common.VersionImpl#getFrozenModifiedDate()
	 */
	@Override
	public Date getFrozenModifiedDate() {
		if(createdDate!=null) {
			return createdDate;
		}
		return super.getFrozenModifiedDate();
	}


   


}
