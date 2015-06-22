/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.mail.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.mail.BeCPGMailService;

/**
 * 
 * @author matthieu
 * 
 */
public class BeCPGMailServiceImpl implements BeCPGMailService {

	private static final Log _logger = LogFactory.getLog(BeCPGMailServiceImpl.class);

	
	private NodeService nodeService;
	private TemplateService templateService;
	private JavaMailSender mailService;
	private ServiceRegistry serviceRegistry;
	
	private SearchService searchService;
	private Repository repository;
	private FileFolderService fileFolderService;
	private NamespaceService namespaceService;

	private String mailFrom;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	public void setMailService(JavaMailSender mailService) {
		this.mailService = mailService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}


	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void sendMailNewUser(NodeRef personNodeRef, String userName, String password) {
		Map<String, Object> templateModel = new HashMap<>(8, 1.0f);

		templateModel.put("person", new TemplateNode(personNodeRef, serviceRegistry, null));
		templateModel.put("username", userName);
		templateModel.put("password", password);
		templateModel.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()));
		// current date/time is useful to have and isn't supplied by FreeMarker by default
		templateModel.put("date", new Date());

		String email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
		if (!StringUtils.isEmpty(email)) {
			List<String> emails = new ArrayList<>(1);
			emails.add(email);
			sendMail(emails, I18NUtil.getMessage("becpg.mail.newUser.title"), RepoConsts.EMAIL_NEW_USER_TEMPLATE, templateModel);
		}

	}
	
	//TODO
	
//	 Action mail = actionService.createAction(MailActionExecuter.NAME);
//     mail.setParameterValue(MailActionExecuter.PARAM_FROM, getEmail(inviter));
//     mail.setParameterValue(MailActionExecuter.PARAM_TO, getEmail(invitee));
//     mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, buildSubject(properties));
//     mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, getEmailTemplateNodeRef());
//     mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, 
//             (Serializable)buildMailTextModel(properties, inviter, invitee));
//     mail.setParameterValue(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, true);
//     actionService.executeAction(mail, getWorkflowPackage(properties));
//	

	@Override
	public void sendMail(List<String> emails, String subjet, String templateName, Map<String, Object> templateModel) {

		NodeRef templateNodeRef = nodeService.getChildByName(getEmailTemplatesFolder(), ContentModel.ASSOC_CONTAINS, templateName);
		String text = null;
		boolean isHTML = false;
		if (templateName != null && templateNodeRef != null) {
			text = templateService.processTemplate("freemarker", templateNodeRef.toString(), templateModel);

			if (text != null) {
				// Note: only simplistic match here - expects <html tag at the
				// start of the text
				String htmlPrefix = "<html";
				if (text.length() >= htmlPrefix.length()
						&& text.substring(0, htmlPrefix.length()).equalsIgnoreCase(htmlPrefix)) {
					isHTML = true;
				}
			}
		}

		for (String email : emails) {
			if (!StringUtils.isEmpty(email)) {
				try {
					MimeMessage l_mimeMessage = mailService.createMimeMessage();
					MimeMessageHelper messageHelper = new MimeMessageHelper(l_mimeMessage, true);
					messageHelper.setTo(email);
					messageHelper.setSubject(subjet);

					if (text != null) {
						messageHelper.setText(text, isHTML);
						_logger.debug("Message subject = " + l_mimeMessage.getSubject());
						_logger.debug("Message content = " + text);
					} else {
						_logger.warn("Mail model not found : [NOK]");
						messageHelper.setText(I18NUtil.getMessage("becpg.mail.template.notfound"), true);
					}

					messageHelper.setFrom(mailFrom);

					mailService.send(l_mimeMessage);

				} catch (Exception e) {
					_logger.error("Cannot send email " + email + "", e);
				}
			}
		}
	}

	@Override
	public NodeRef getEmailTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/.");
	}

	
	@Override
	public NodeRef getEmailWorkflowTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:workflownotification/.");

	}
	
	private NodeRef searchFolder(String xpath){
		
		List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(),xpath , null,
				this.namespaceService, false);

		if (nodeRefs.size() == 1) {
			// Now localise this
			NodeRef base = nodeRefs.get(0);
			return fileFolderService.getLocalizedSibling(base);
		}  else {
			throw new RuntimeException("Cannot find the email template folder !");
		}
	}

}
