package fr.becpg.repo.entity.remote;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.common.BeCPGException;

public interface EntityProviderCallBack {

	public NodeRef provideNode(NodeRef nodeRef) throws BeCPGException;

}
