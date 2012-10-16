package fr.becpg.repo.mail;

import java.util.List;
import java.util.Map;

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
	 * @param emails
	 * @param title
	 * @param emailTemplate
	 * @param templateModel
	 */
	void sendMail(List<String> emails, String title, String emailTemplate, Map<String, Object> templateModel);

	/**
	 * 
	 * @return the dictionary model mail nodeRef
	 */
	public NodeRef getEmailTemplatesFolder();
	
	/**
	 * 
	 * @return the workflow model mail nodeRef
	 */
	public NodeRef getEmailWorkflowTemplatesFolder();



}
