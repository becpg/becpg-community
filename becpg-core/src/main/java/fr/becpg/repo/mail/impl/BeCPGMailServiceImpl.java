/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author matthieu
 * 
 */
public class BeCPGMailServiceImpl implements BeCPGMailService {

	private static final Log _logger = LogFactory.getLog(BeCPGMailServiceImpl.class);

 
	private NodeService nodeService;
	private ServiceRegistry serviceRegistry;
	private SearchService searchService;
	private Repository repository;
	private FileFolderService fileFolderService;
	private NamespaceService namespaceService;
	private ActionService actionService;
	private AuthorityService authorityService;
	private PersonService personService;
	private String mailFrom;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
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

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	@Override
	public void sendMailNewUser(NodeRef personNodeRef, String userName, String password) {
		_logger.info("Email new user");
		Map<String, Object> templateModel = new HashMap<>(8, 1.0f);

		templateModel.put("person", new TemplateNode(personNodeRef, serviceRegistry, null));
		templateModel.put("username", userName);
		templateModel.put("password", password);
		templateModel.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()));
		// current date/time is useful to have and isn't supplied by FreeMarker by default
		templateModel.put("date", new Date());

		String email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
		if (!StringUtils.isEmpty(email)) {
			List<NodeRef> users = new ArrayList<>(1);
			users.add(personService.getPerson(userName));
			sendMail(users, I18NUtil.getMessage("becpg.mail.newUser.title"), RepoConsts.EMAIL_NEW_USER_TEMPLATE, templateModel, false);
		}

	}


	@Override
	public NodeRef findTemplateNodeRef(String templateName, NodeRef folderNR){
		_logger.info("Finding template named "+templateName+" in folder "+folderNR);
		NodeRef templateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(folderNR, templateName);
		if(templateNodeRef == null){
			throw new RuntimeException("Template "+templateName+" not found in folder");
		}

		return templateNodeRef;
	}

	private NodeRef findLocalizedTemplateNodeRef(NodeRef templateNodeRef){	
		_logger.info("Finding sibling of template "+templateNodeRef);
		return fileFolderService.getLocalizedSibling(templateNodeRef);
	}

	@Override
	public void sendMail(List<NodeRef> recipientNodeRefs, String subject, String mailTemplate, Map<String, Object> templateArgs, boolean sendToSelf){

		NodeRef templateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), mailTemplate);
		_logger.debug("emails to receive email: "+recipientNodeRefs);
		Set<String> authorities = new HashSet<>();
		for (NodeRef recipientNodeRef : recipientNodeRefs) {
			String authorityName;
			QName type = nodeService.getType(recipientNodeRef);
			if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
				_logger.info(recipientNodeRef+" is a group, extracting...");
				authorities.addAll(extractAuthoritiesFromGroup(recipientNodeRef, sendToSelf));
			} else {
				authorityName = (String) nodeService.getProperty(recipientNodeRef, ContentModel.PROP_USERNAME);
				if (_logger.isDebugEnabled()) {
					_logger.debug("authorityName : " + authorityName);
				}
				if (!authorityName.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
					authorities.add(authorityName);
				}
			}

		}
		_logger.info("TemplateNodeRef: "+templateNodeRef);
		_logger.info("TemplateArgs: "+templateArgs);

		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, new ArrayList<>(authorities));
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, findLocalizedTemplateNodeRef(templateNodeRef));
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, mailFrom);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateArgs);

		AuthenticationUtil.runAsSystem(() -> {
			actionService.executeAction(mailAction, null, true, true);
			return null;
		});
	}

	private List<String> extractAuthoritiesFromGroup(NodeRef group, boolean sendToSelf){
		List<String> ret = new ArrayList<>();
		String authorityName = (String) nodeService.getProperty(group, ContentModel.PROP_AUTHORITY_NAME);
		for (String userAuth : authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false)) {
			if (sendToSelf || !userAuth.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
				ret.add(userAuth);
			}
		}
		_logger.info("Found "+ret.size()+" users in the group: "+ret);

		return ret;
	}

	@Override
	public NodeRef getEmailTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/.");
	}

	@Override
	public NodeRef getEmailWorkflowTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:workflownotification/.");
	}

	@Override
	public NodeRef getEmailNotifyTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/.");
	}

	@Override
	public NodeRef getEmailProjectTemplatesFolder(){
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:project/.");
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
