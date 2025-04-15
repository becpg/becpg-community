/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.mail;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>BeCPGMailService interface.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public interface BeCPGMailService {

	/**
	 * Send a mail to notify user import
	 *
	 * @param personNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param userName a {@link java.lang.String} object.
	 * @param password a {@link java.lang.String} object.
	 */
	void sendMailNewUser(NodeRef personNodeRef, String userName, String password);
	
	/**
	 * <p>sendMailNewPassword.</p>
	 *
	 * @param personNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param userName a {@link java.lang.String} object
	 * @param password a {@link java.lang.String} object
	 */
	void sendMailNewPassword(NodeRef personNodeRef, String userName, String password);

	/**
	 * <p>sendMail.</p>
	 *
	 * @param recipientNodeRefs a {@link java.util.List} object.
	 * @param title a {@link java.lang.String} object.
	 * @param emailTemplate a {@link java.lang.String} object.
	 * @param templateModel a {@link java.util.Map} object.
	 * @param sendToSelf a boolean.
	 */
	void sendMail(List<NodeRef> recipientNodeRefs, String title, String emailTemplate, Map<String, Object> templateModel, boolean sendToSelf);

	/**
	 * <p>sendMailOnAsyncAction.</p>
	 *
	 * @param userName a {@link java.lang.String} object.
	 * @param action a {@link java.lang.String} object.
	 * @param actionUrl a {@link java.lang.String} object.
	 * @param runWithSuccess a boolean.
	 * @param time a double.
	 * @param bodyParams a {@link java.lang.Object} object
	 */
	void sendMailOnAsyncAction(String userName, String action, String actionUrl, boolean runWithSuccess, double time, Object ... bodyParams);
	
	/**
	 * <p>findTemplateNodeRef.</p>
	 *
	 * @param templateName a {@link java.lang.String} object.
	 * @param folderNR a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef findTemplateNodeRef(String templateName, NodeRef folderNR);

	/**
	 * <p>getEmailTemplatesFolder.</p>
	 *
	 * @return the dictionary model mail nodeRef
	 */
	NodeRef getEmailTemplatesFolder();
	
	/**
	 * <p>getEmailWorkflowTemplatesFolder.</p>
	 *
	 * @return the workflow model mail nodeRef
	 */
	NodeRef getEmailWorkflowTemplatesFolder();

	/**
	 * <p>getEmailNotifyTemplatesFolder.</p>
	 *
	 * @return the notify model mail nodeRef
	 */
	NodeRef getEmailNotifyTemplatesFolder();
	
	/**
	 * <p>getEmailActivitiesTemplatesFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getEmailActivitiesTemplatesFolder();
	
	/**
	 * <p>getEmailInviteTemplatesFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getEmailInviteTemplatesFolder();
	
	/**
	 * <p>getEmailProjectTemplatesFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEmailProjectTemplatesFolder();

	/**
	 * <p>sendMLAwareMail.</p>
	 *
	 * @param authorities a {@link java.util.Set} object
	 * @param fromEmail a {@link java.lang.String} object
	 * @param subjectKey a {@link java.lang.String} object
	 * @param subjectParams an array of {@link java.lang.Object} objects
	 * @param mailTemplate a {@link java.lang.String} object
	 * @param templateArgs a {@link java.util.Map} object
	 */
	void sendMLAwareMail(Set<String> authorities, String fromEmail, String subjectKey, Object[] subjectParams, String mailTemplate,
			Map<String, Object> templateArgs);

}
