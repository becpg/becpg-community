package fr.becpg.repo.thumbnail;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface BeCPGThumbnailService {
	
	/**
	 * This method is in charge to retrieved the thumbnail 
	 *  nodes associated with the current BeCPG node
	 */
	public NodeRef render(NodeRef sourceNodeRef);
	

}
