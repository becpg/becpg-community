package fr.becpg.repo.publication;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;

public interface PublicationChannelService {
	
	enum PublicationChannelAction {
		RESET, RETRY, STOP
	}

	enum PublicationChannelStatus {
		COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
	}

	 NodeRef getChannelById(String channelId);
	 
	 PagingResults<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef, PagingRequest request);
	
}
