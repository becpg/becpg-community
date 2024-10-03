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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.mail.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
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
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>BeCPGMailServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
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
	private PersonService personService;
	private String mailFrom;
	private SysAdminParams sysAdminParams;

	
	
	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>searchService</code>.</p>
	 *
	 * @param searchService a {@link org.alfresco.service.cmr.search.SearchService} object.
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object.
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>actionService</code>.</p>
	 *
	 * @param actionService a {@link org.alfresco.service.cmr.action.ActionService} object.
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object.
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>Setter for the field <code>mailFrom</code>.</p>
	 *
	 * @param mailFrom a {@link java.lang.String} object.
	 */
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	/** {@inheritDoc} */
	@Override
	public void sendMailNewUser(NodeRef personNodeRef, String userName, String password, boolean sendToSelf) {
		_logger.debug("Email new user");
		Map<String, Object> templateModel = new HashMap<>(8, 1.0f);

		templateModel.put("person", new TemplateNode(personNodeRef, serviceRegistry, null));
		templateModel.put("username", userName);
		templateModel.put("password", password);
		templateModel.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(sysAdminParams));
		// current date/time is useful to have and isn't supplied by FreeMarker
		// by default
		templateModel.put("date", new Date());

		String email = (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL);
		if (email!=null && !email.isEmpty()) {
			List<NodeRef> users = new ArrayList<>(1);
			users.add(personNodeRef);
			sendMail(users, I18NUtil.getMessage("becpg.mail.newUser.title"), RepoConsts.EMAIL_NEW_USER_TEMPLATE, templateModel, sendToSelf);
		}

	}
	
	/** {@inheritDoc} */
	@Override
	public NodeRef findTemplateNodeRef(String templateName, NodeRef folderNR) {
		_logger.debug("Finding template named " + templateName + " in folder " + folderNR);
		NodeRef templateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(folderNR, templateName);
		if (templateNodeRef == null) {
			throw new IllegalStateException("Template " + templateName + " not found in folder");
		}

		return templateNodeRef;
	}

	private NodeRef findLocalizedTemplateNodeRef(NodeRef templateNodeRef, Locale locale) {
		_logger.debug("Finding sibling of template " + templateNodeRef);
		
		Locale currentLocale = I18NUtil.getLocale();
		
		try {
			if (locale != null) {
				I18NUtil.setLocale(locale);
			}
			return fileFolderService.getLocalizedSibling(templateNodeRef);
		} finally {
			I18NUtil.setLocale(currentLocale);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sendMail(List<NodeRef> recipientNodeRefs, String subject, String mailTemplate, Map<String, Object> templateArgs, boolean sendToSelf) {

		Set<String> authorities = new HashSet<>();
		for (NodeRef recipientNodeRef : recipientNodeRefs) {
			String authorityName;
			QName type = nodeService.getType(recipientNodeRef);
			if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
				_logger.debug(recipientNodeRef + " is a group, extracting...");
				authorities.addAll(AuthorityHelper.extractAuthoritiesFromGroup(recipientNodeRef, sendToSelf));
			} else {
				authorityName = (String) nodeService.getProperty(recipientNodeRef, ContentModel.PROP_USERNAME);
				
				if (sendToSelf || !authorityName.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
					if (_logger.isDebugEnabled()) {
						_logger.debug("Adding mail authorityName : " + authorityName);
					}
					authorities.add(authorityName);
				} else if(_logger.isDebugEnabled()){
					_logger.debug("Skipping self : " + authorityName);
				}
					
			}

		}
		if(!authorities.isEmpty()) {
			sendMLAwareMail(authorities, null, subject, null, mailTemplate, templateArgs);
		} else if(_logger.isDebugEnabled()){
			_logger.debug("No recipients to send mail to (sendToSelf:"+sendToSelf+")");
		}

	}

	/** {@inheritDoc} */
	@Override
	public void sendMLAwareMail(Set<String> authorities, String fromEmail, String subjectKey, Object[] subjectParams, String mailTemplate, Map<String, Object> templateArgs) {
		
		Set<String> people = AuthorityHelper.extractPeople(authorities);
		
		Locale commonLocale = AuthorityHelper.getCommonLocale(people);
		
		if (commonLocale != null) {
			
			String localizedSubject = I18NUtil.getMessage(subjectKey, commonLocale, subjectParams);
			
			if (localizedSubject == null) {
				localizedSubject = subjectKey;
			}
			
			internalSendMail(authorities, fromEmail, localizedSubject, mailTemplate, templateArgs, commonLocale);
		} else {
			for (String person : people) {
				Locale locale = null;
				if (personService.personExists(person)) {
					String localeString = (String) nodeService.getProperty(personService.getPerson(person), BeCPGModel.PROP_USER_LOCALE);
					locale = localeString == null ? I18NUtil.getLocale() : MLTextHelper.parseLocale(localeString);
				}
				String localizedSubject = I18NUtil.getMessage(subjectKey, locale, subjectParams);
				
				if (localizedSubject == null) {
					localizedSubject = subjectKey;
				}
				internalSendMail(Set.of(person), fromEmail, localizedSubject, mailTemplate, templateArgs, locale);
			}
		}
	}

	private void internalSendMail(Set<String> singleAuthorities, String fromEmail, String subject, String mailTemplate, Map<String, Object> templateArgs, Locale locale) {
		
		NodeRef templateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), mailTemplate);
		
		if(_logger.isTraceEnabled()) {
			_logger.trace("TemplateNodeRef: " + templateNodeRef);
			_logger.trace("TemplateArgs: " + templateArgs);
		}
		
		Action mailAction = actionService.createAction(MailActionExecuter.NAME);
		mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, new ArrayList<>(singleAuthorities));
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, findLocalizedTemplateNodeRef(templateNodeRef, locale));
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, mailFrom);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateArgs);
		mailAction.setParameterValue(MailActionExecuter.PARAM_LOCALE, locale);
		mailAction.setParameterValue(MailActionExecuter.PARAM_FROM, fromEmail);
		
		AuthenticationUtil.runAsSystem(() -> {
			actionService.executeAction(mailAction, null, true, true);
			return null;
		});
	}

	/** {@inheritDoc} */
	@Override
	public void sendMailOnAsyncAction(String userName, String action, String actionUrl, boolean runWithSuccess, double time, Object ... bodyParams) {
		if(personService.personExists(userName)) {
			Map<String, Object> templateArgs = new HashMap<>();
			templateArgs.put(RepoConsts.ARG_ACTION_STATE, runWithSuccess);
			templateArgs.put(RepoConsts.ARG_ACTION_URL, actionUrl);
			templateArgs.put(RepoConsts.ARG_ACTION_RUN_TIME, time);
	
			String subject = I18NUtil.getMessage("message.async-mail." + action + ".subject");
			templateArgs.put(RepoConsts.ARG_ACTION_BODY, I18NUtil.getMessage("message.async-mail." + action + ".body", bodyParams));
	
			List<NodeRef> recipientsNodeRef = Arrays.asList(personService.getPerson(userName));
	
			Map<String, Object> templateModel = new HashMap<>();
			templateModel.put("args", templateArgs);
	
			sendMail(recipientsNodeRef, subject, RepoConsts.EMAIL_ASYNC_ACTIONS_TEMPLATE, templateModel, true);
		}

	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/.");
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailWorkflowTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:workflownotification/.");
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailNotifyTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/.");
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailProjectTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:project/.");
	}

	private NodeRef searchFolder(String xpath) {
		List<NodeRef> nodeRefs = searchService.selectNodes(repository.getRootHome(), xpath, null, this.namespaceService, false);

		if (nodeRefs.size() == 1) {
			// Now localise this
			NodeRef base = nodeRefs.get(0);
			return fileFolderService.getLocalizedSibling(base);
		} else {
			throw new IllegalStateException("Cannot find the email template folder !");
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailActivitiesTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:activities/.");
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getEmailInviteTemplatesFolder() {
		return searchFolder("app:company_home/app:dictionary/app:email_templates/cm:invite/.");
	}

}
