package fr.becpg.repo.security.impl;

import org.alfresco.repo.ownable.impl.OwnableServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;

public class BeCPGOwnableServiceImpl extends OwnableServiceImpl {

	
	private boolean disableOwner = false;

	public void setDisableOwner(boolean disableOwner) {
		this.disableOwner = disableOwner;
	}
	
	
	@Override
	public String getOwner(NodeRef nodeRef) {
		if(disableOwner) {
			return OwnableService.NO_OWNER;
		}
		
		return super.getOwner(nodeRef);
	}
}
