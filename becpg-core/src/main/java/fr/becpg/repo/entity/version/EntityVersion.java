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
 * @version $Id: $Id
 */
public class EntityVersion extends VersionImpl {
	
	/**
	 * generated serial number
	 */
	private static final long serialVersionUID = -4165153505281567656L;

	private final NodeRef entityNodeRef;
	
	private final NodeRef entityVersionNodeRef;
	
	private final NodeRef entityBranchFromNodeRef;
	
	private Date createdDate = null;
	
	/**
	 * <p>Getter for the field <code>entityVersionNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityVersionNodeRef() {
		return entityVersionNodeRef;
	}

	/**
	 * <p>Getter for the field <code>entityBranchFromNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityBranchFromNodeRef() {
		return entityBranchFromNodeRef;
	}

	/**
	 * <p>Getter for the field <code>entityNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	/**
	 * <p>Constructor for EntityVersion.</p>
	 *
	 * @param version a {@link org.alfresco.service.cmr.version.Version} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityVersionNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param entityBranchFromNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public EntityVersion(Version version, NodeRef entityNodeRef, NodeRef entityVersionNodeRef, NodeRef entityBranchFromNodeRef) {
		super(version.getVersionProperties(), version.getFrozenStateNodeRef());
		this.entityVersionNodeRef = entityVersionNodeRef;
		this.entityBranchFromNodeRef = entityBranchFromNodeRef;
		this.entityNodeRef = entityNodeRef;
	}

	/**
	 * <p>Setter for the field <code>createdDate</code>.</p>
	 *
	 * @param createdDate a {@link java.util.Date} object.
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.version.common.VersionImpl#getFrozenModifiedDate()
	 */
	/** {@inheritDoc} */
	@Override
	public Date getFrozenModifiedDate() {
		if(createdDate!=null) {
			return createdDate;
		}
		return super.getFrozenModifiedDate();
	}


   


}
