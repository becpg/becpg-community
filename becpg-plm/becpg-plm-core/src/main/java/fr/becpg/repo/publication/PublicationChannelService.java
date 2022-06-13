package fr.becpg.repo.publication;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PublicationChannelService {

	 NodeRef getChannelById(String channelId);
	 List<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef);
	
}
