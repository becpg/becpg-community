package fr.becpg.repo.publication;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>PublicationChannelService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface PublicationChannelService {
	
	enum PublicationChannelAction {
		RESET, RETRY, STOP
	}

	enum PublicationChannelStatus {
		COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
	}

	 /**
	  * <p>getChannelById.</p>
	  *
	  * @param channelId a {@link java.lang.String} object
	  * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	  */
	 NodeRef getChannelById(String channelId);
	 
	 /**
	  * <p>getEntitiesByChannel.</p>
	  *
	  * @param channelNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	  * @param request a {@link org.alfresco.query.PagingRequest} object
	  * @return a {@link org.alfresco.query.PagingResults} object
	  */
	 PagingResults<NodeRef> getEntitiesByChannel(NodeRef channelNodeRef, PagingRequest request);
	
}
