package fr.becpg.repo.mail;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface BeCPGMailService {

	/**
	 * Send a mail to notify user import
	 * @param personNodeRef
	 */
	void sendMailNewUser(NodeRef personNodeRef, String userName, String password);

	/**
	 * 
	 * @return the dictionary model mail nodeRef
	 */
	public NodeRef getModelMailNodeRef();
	
	/**
	 * 
	 * @return the workflow model mail nodeRef
	 */
	public NodeRef getWorkflowModelMailNodeRef();

	

}
