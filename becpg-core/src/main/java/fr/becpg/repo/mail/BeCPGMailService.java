/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
	NodeRef getEmailTemplatesFolder();
	
	/**
	 * 
	 * @return the workflow model mail nodeRef
	 */
	NodeRef getEmailWorkflowTemplatesFolder();

	/**
	 * 
	 * @return the notify model mail nodeRef
	 */
	NodeRef getEmailNotifyTemplatesFolder();

}
