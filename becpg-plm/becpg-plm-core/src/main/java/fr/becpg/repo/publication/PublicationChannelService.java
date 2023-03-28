package fr.becpg.repo.publication;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PublicationChannelService {
	
	enum PublicationChannelAction {
		RESET, RETRY, STOP
	}

	enum PublicationChannelStatus {
		COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
	}

	 NodeRef getChannelById(String channelId);
	 List<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef);
	
}
