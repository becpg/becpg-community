/*
 * 
 */
package fr.becpg.repo.entity.version;

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

	private NodeRef entityVersionNodeRef;
	
	public NodeRef getEntityVersionNodeRef() {
		return entityVersionNodeRef;
	}

	public EntityVersion(Version version, NodeRef entityVersionNodeRef) {
		super(version.getVersionProperties(), version.getFrozenStateNodeRef());
		this.entityVersionNodeRef = entityVersionNodeRef;
	}


}
