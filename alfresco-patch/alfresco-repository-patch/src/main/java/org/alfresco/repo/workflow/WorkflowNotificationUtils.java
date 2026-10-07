/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.common.base.Objects;

/**
 * Utility class containing methods to help when sending workflow notifications.
 *
 * @author Roy Wetherall
 * @since 4.0
 */
public class WorkflowNotificationUtils {
	
	/** Send EMail notifications property */
	public static final String PROP_SEND_EMAIL_NOTIFICATIONS = "bpm_sendEMailNotifications";
	/** Constant <code>PROP_PACKAGE="bpm_package"</code> */
	public static final String PROP_PACKAGE = "bpm_package";

	/** I18N */
	public static final String MSG_ASSIGNED_TASK = "assigned-task";
	/** Constant <code>MSG_NEW_POOLED_TASK="new-pooled-task"</code> */
	public static final String MSG_NEW_POOLED_TASK = "new-pooled-task";

	/** Args value names */
	public static final String ARG_WF_ID = "workflowId";
	/** Constant <code>ARG_WF_TITLE="workflowTitle"</code> */
	public static final String ARG_WF_TITLE = "workflowTitle";
	/** Constant <code>ARG_WF_DESCRIPTION="workflowDescription"</code> */
	public static final String ARG_WF_DESCRIPTION = "workflowDescription";
	/** Constant <code>ARG_WF_DUEDATE="workflowDueDate"</code> */
	public static final String ARG_WF_DUEDATE = "workflowDueDate";
	/** Constant <code>ARG_WF_PRIORITY="workflowPriority"</code> */
	public static final String ARG_WF_PRIORITY = "workflowPriority";
	/** Constant <code>ARG_WF_POOLED="workflowPooled"</code> */
	public static final String ARG_WF_POOLED = "workflowPooled";
	/** Constant <code>ARG_WF_DOCUMENTS="workflowDocuments"</code> */
	public static final String ARG_WF_DOCUMENTS = "workflowDocuments";
	/** Constant <code>ARG_WF_TENANT="workflowTenant"</code> */
	public static final String ARG_WF_TENANT = "workflowTenant";
	/** Constant <code>ARG_WF_TENANT="workflowInitiator"</code> */
	public static final String ARG_WF_INITIATOR = "workflowInitiator";
	/** Constant <code>ARG_WF_LOCALE="locale"</code> */
	public static final String ARG_WF_LOCALE = "locale";

	/* beCPG */
	/** Constant <code>ASSOC_WORKFLOW_TASK</code> */
	public static final QName ASSOC_WORKFLOW_TASK = QName.createQName("{http://www.bcpg.fr/model/project/1.0}workflowTask");
	private static final QName PROP_BECPG_CODE = QName.createQName("{http://www.bcpg.fr/model/becpg/1.0}code");
	private static final String ARG_PROJECT_TASK = "projectTask";

	/** Constant <code>ASSOC_WORKFLOW_ENTITY</code> */
	public static final QName ASSOC_WORKFLOW_ENTITY = QName.createQName("{http://www.bcpg.fr/model/becpg/1.0}workflowEntity");
	private static final String ARG_PROJECT = "project";
	private static final String ARG_ENTITY = "entity";
	
	/** Constant <code>PROP_WORKFLOW_INITIATOR</code> */
	public static final QName PROP_WORKFLOW_INITIATOR = QName.createQName("{http://www.bcpg.fr/model/becpg/1.0}workflowInitiator");

	/** Standard workflow assigned template */
	public static final String WF_ASSIGNED_TEMPLATE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "wf-email-html-ftl").toString();

	// service dependencies
	private WorkflowService workflowService;
	private NodeService nodeService;
	private NotificationService notificationService;
	private AuthorityService authorityService;
	private FileFolderService fileFolderService;
	private PersonService personService;
	private MutableAuthenticationService authenticationService;
	private TenantAdminService tenantAdminService;
	
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}
	
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
	
	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>workflowService</code>.</p>
	 *
	 * @param service a {@link org.alfresco.service.cmr.workflow.WorkflowService} object
	 */
	public void setWorkflowService(WorkflowService service) {
		workflowService = service;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param service a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService service) {
		nodeService = service;
	}

	/**
	 * <p>Setter for the field <code>notificationService</code>.</p>
	 *
	 * @param service a {@link org.alfresco.service.cmr.notification.NotificationService} object
	 */
	public void setNotificationService(NotificationService service) {
		notificationService = service;
	}


	/**
	 * <p>sendWorkflowAssignedNotificationEMail.</p>
	 *
	 * @param taskId a {@link java.lang.String} object
	 * @param taskTitle a {@link java.lang.String} object
	 * @param description a {@link java.lang.String} object
	 * @param dueDate a {@link java.util.Date} object
	 * @param priority a {@link java.lang.Integer} object
	 * @param workflowPackage a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param assignedAuthorites an array of {@link java.lang.String} objects
	 * @param pooled a boolean
	 */
	public void sendWorkflowAssignedNotificationEMail(String taskId, String taskTitle, String description, Date dueDate, Integer priority,
			NodeRef workflowPackage, String[] assignedAuthorites, boolean pooled) {

		// Get the workflow task
		WorkflowTask workflowTask = workflowService.getTaskById(taskId);
		Map<QName, Serializable> props = new HashMap<>();

		// Get the workflow properties
		if (workflowTask != null) {
			props = workflowTask.getProperties();
		}

		sendWorkflowAssignedNotificationEMail(taskId, taskTitle, description, dueDate, priority, workflowPackage, assignedAuthorites, pooled, props);
	}

	//beCPG
	/**
	 * <p>sendWorkflowAssignedNotificationEMail.</p>
	 *
	 * @param taskId a {@link java.lang.String} object
	 * @param taskTitle a {@link java.lang.String} object
	 * @param description a {@link java.lang.String} object
	 * @param dueDate a {@link java.util.Date} object
	 * @param priority a {@link java.lang.Integer} object
	 * @param workflowPackage a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param assignedAuthorites an array of {@link java.lang.String} objects
	 * @param pooled a boolean
	 * @param props a {@link java.util.Map} object
	 */
	public void sendWorkflowAssignedNotificationEMail(String taskId, String taskTitle, String description, Date dueDate, Integer priority,
			NodeRef workflowPackage, String[] assignedAuthorites, boolean pooled, Map<QName, Serializable> props) {
		
		Set<String> people = extractPeople(assignedAuthorites);
		people.removeIf(person -> !isExistingUser(person));
		if (people.isEmpty()) {
			return;
		}
		
		Locale commonLocale = getCommonLocale(people);
		
		if (commonLocale != null) {
			internalSendWorkflowAssignedNotificationEMail(taskId, taskTitle, description, dueDate, priority, workflowPackage, pooled, props,
					people.toArray(new String[0]), commonLocale);
		} else {
			for (String person : people) {
				internalSendWorkflowAssignedNotificationEMail(taskId, taskTitle, description, dueDate, priority, workflowPackage, pooled, props,
						new String[] { person }, resolveLocale(person));
			}
		}
	}
	
	private Set<String> extractPeople(String[] authorities) {
		Set<String> people = new HashSet<>();
		
		for (String authority : authorities) {
			
			AuthorityType authType = AuthorityType.getAuthorityType(authority);
			
			if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE)) {
				// Notify all members of the group
				Set<String> users;
				if (authType.equals(AuthorityType.GROUP)) {
					users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
				} else {
					users = authorityService.getAllAuthorities(AuthorityType.USER);
				}
				
				people.addAll(users);
				
			} else {
				people.add(authority);
			}
		}
		
		people.removeIf(this::emailTaskResourceDisabled);
		
		return people;
	}
	
	private boolean emailTaskResourceDisabled(String username) {
		if (!isExistingUser(username)) {
			return true;
		}
		NodeRef person = personService.getPerson(username);
		if (person != null) {
			Serializable emailTaskResourceDisabled = nodeService.getProperty(person,
					QName.createQName("http://www.bcpg.fr/model/becpg/1.0", "emailTaskResourceDisabled"));
			if (Boolean.TRUE.equals(emailTaskResourceDisabled)) {
				return true;
			}
		}
		return false;
	}
	
	private Locale getCommonLocale(Set<String> people) {
		
		Locale commonLocale = null;
		
		boolean isFirst = true;
		
		for (String person : people) {
			Locale personLocale = getPersonLocale(person);
			if (isFirst) {
				commonLocale = personLocale;
				isFirst = false;
			} else if (!Objects.equal(personLocale, commonLocale)) {
				return null;
			}
		}
		
		return commonLocale;
	}

	private Locale getPersonLocale(String person) {
		String localeString = (String) nodeService.getProperty(personService.getPerson(person), QName.createQName("http://www.bcpg.fr/model/becpg/1.0", "userLocale"));
		return localeString == null || localeString.isBlank() ? null : parseLocale(localeString);
	}
	
	private Locale resolveLocale(String person) {
		Locale locale = getPersonLocale(person);
		if (locale == null) {
			return I18NUtil.getLocale();
		}
		return locale;
	}
	
	private boolean isExistingUser(String person) {
		AuthorityType type = AuthorityType.getAuthorityType(person);
		if (type != null && !AuthorityType.USER.equals(type)) {
			return false;
		}
		return personService.personExists(person);
	}
	
	private Locale parseLocale(String key) {
		if (key.contains("_")) {
			return new Locale(key.split("_")[0], key.split("_")[1]);
		}
		return new Locale(key);
	}

	private void internalSendWorkflowAssignedNotificationEMail(String taskId, String taskTitle, String description, Date dueDate, Integer priority,
			NodeRef workflowPackage, boolean pooled, Map<QName, Serializable> props, String[] assignedAuthorites, Locale locale) {
		NotificationContext notificationContext = new NotificationContext();
		
		// Avoid using System as email sender to prevent NoSuchPersonException when resolving sender email
		String fromUser = AuthenticationUtil.getFullyAuthenticatedUser();
		if (fromUser != null && !isSystemUser(fromUser)) {
			notificationContext.setFrom(fromUser);
		}
		
		// Determine the subject of the notification
		String subject = null;
		if (!pooled) {
			subject = MSG_ASSIGNED_TASK;
		} else {
			subject = MSG_NEW_POOLED_TASK;
		}
		
		Locale currentLocale = I18NUtil.getLocale();
		
		try {
			
			if (locale != null) {
				I18NUtil.setLocale(locale);
			}
			
			// Set the email template
			notificationContext.setBodyTemplate(fileFolderService.getLocalizedSibling(new NodeRef(WF_ASSIGNED_TEMPLATE)).toString());
			
			Map<String, Serializable> templateArgs = new HashMap<>(7);
			// Build the template args
			templateArgs.put(ARG_WF_ID, taskId);
			templateArgs.put(ARG_WF_TITLE, taskTitle);
			templateArgs.put(ARG_WF_DESCRIPTION, description);
			templateArgs.put(ARG_WF_INITIATOR, props.get(PROP_WORKFLOW_INITIATOR));
			if (dueDate != null) {
				templateArgs.put(ARG_WF_DUEDATE, dueDate);
			}
			if (priority != null) {
				templateArgs.put(ARG_WF_PRIORITY, priority);
			}
			if (locale != null) {
				templateArgs.put(ARG_WF_LOCALE, locale);
			}
			
			// Indicate whether this is a pooled workflow item or not
			templateArgs.put(ARG_WF_POOLED, pooled);
			NodeRef entity = (NodeRef) props.get(ASSOC_WORKFLOW_ENTITY);
			
			if (entity != null) {
				
				templateArgs.put(ARG_ENTITY, entity);
				
				if (QName.createQName("{http://www.bcpg.fr/model/project/1.0}project").equals(nodeService.getType(entity))) {
					templateArgs.put(ARG_PROJECT, entity);
					
					NodeRef projectTask = (NodeRef) props.get(ASSOC_WORKFLOW_TASK);
					if (projectTask != null) {
						templateArgs.put(ARG_PROJECT_TASK, projectTask);
					}
					
					// beCPG
					if ((description != null) && !description.isEmpty()) {
						
						subject = AuthenticationUtil.runAsSystem(() -> {
							
							String name = (String) nodeService.getProperty(entity, ContentModel.PROP_NAME);
							
							String sub = null;
							
							if (name != null) {
								String[] splitted = description.split(Pattern.quote(name));
								
								String projectCode = (String) nodeService.getProperty(entity, PROP_BECPG_CODE);
								
								if ((projectCode == null) || (name.indexOf(projectCode) > -1)) {
									
									sub = "[" + name + "] " + (splitted.length > 1 ? splitted[1] : splitted[0]) + " ("
											+ I18NUtil.getMessage(pooled ? MSG_NEW_POOLED_TASK : MSG_ASSIGNED_TASK) + ")";
									
								} else {
									
									sub = "[" + name + " - " + projectCode + "] " + (splitted.length > 1 ? splitted[1] : splitted[0]) + " ("
											+ I18NUtil.getMessage(pooled ? MSG_NEW_POOLED_TASK : MSG_ASSIGNED_TASK) + ")";
								}
							} else {
								sub = description;
							}
							
							return sub;
						});
						
					}
					// beCPG
					templateArgs.put(ARG_WF_TITLE, I18NUtil.getMessage("projectAdhoc.task.adhocTask.title"));
				}
			}
			
			if (workflowPackage != null) {
				// Add details of associated content
				List<ChildAssociationRef> assocs = nodeService.getChildAssocs(workflowPackage);
				NodeRef[] docs = new NodeRef[assocs.size()];
				if (!assocs.isEmpty()) {
					int index = 0;
					for (ChildAssociationRef assoc : assocs) {
						docs[index] = assoc.getChildRef();
						index++;
						
					}
					templateArgs.put(ARG_WF_DOCUMENTS, docs);
				}
			}
			
			notificationContext.setSubject(escapeSubject(subject));
			
			// Add tenant, if in context of tenant
			String tenant = TenantUtil.getCurrentDomain();
			if (tenant != null) {
				templateArgs.put(ARG_WF_TENANT, tenant);
			}
			
			// Set the template args
			notificationContext.setTemplateArgs(templateArgs);
			
			// Set the notification recipients
			for (String assignedAuthority : assignedAuthorites) {
				if (shouldNotify(assignedAuthority)) {
					notificationContext.addTo(assignedAuthority);
				}
			}
			
			// If no recipients remain (e.g., only System was provided), skip sending any notification
			if (notificationContext.getTo() == null || notificationContext.getTo().isEmpty()) {
				return;
			}
			
			// Indicate that we want to execute the notification asynchronously
			notificationContext.setAsyncNotification(true);
			
			AuthenticationUtil.runAsSystem(() -> {
				// Send email notification
				notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);
				return null;
			});
		} finally {
			I18NUtil.setLocale(currentLocale);
		}
		
	}

	private boolean isSystemUser(String fromUser) {
		if (tenantAdminService != null && tenantAdminService.isEnabled()) {
			@SuppressWarnings("deprecation")
			List<Tenant> tenants = tenantAdminService.getAllTenants();
			for (Tenant tenant : tenants) {
				String tenantDomain = tenant.getTenantDomain();
				String systemUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
				if (fromUser.equals(systemUser)) {
					return true;
				}
			}
		}
		return AuthenticationUtil.SYSTEM_USER_NAME.equals(fromUser);
	}
	
	private boolean shouldNotify(String userName) {
		// Always return false for system user
		if (isSystemUser(userName)) {
			return false;
		}
		
		try {
			NodeRef person = personService.getPerson(userName);
			if (person != null && nodeService.exists(person)) {
				if (!authenticationService.isAuthenticationMutable(userName)
						&& nodeService.hasAspect(person, ContentModel.ASPECT_PERSON_DISABLED)) {
					return false;
				}
				return authenticationService.getAuthenticationEnabled(userName);
			}
			return false;
		} catch (org.alfresco.service.cmr.security.NoSuchPersonException e) {
			// User does not exist, return false to skip notification
			return false;
		}
	}

	private String escapeSubject(String subject) {
		if (subject != null) {
			return subject.replace("\n", " ").replace("\r", " ");
		}
		return subject;
	}

	/**
	 * Send workflow assigned email notification.
	 *
	 * @param taskId
	 *            workflow global task id
	 * @param assignedAuthorites
	 *            assigned authorities
	 * @param pooled
	 *            true if pooled task, false otherwise
	 * @param taskType a {@link java.lang.String} object
	 */
	public void sendWorkflowAssignedNotificationEMail(String taskId, String taskType, String[] assignedAuthorites, boolean pooled) {
		// Get the workflow task
		WorkflowTask workflowTask = workflowService.getTaskById(taskId);
		
		// Get the workflow properties
		Map<QName, Serializable> props = workflowTask.getProperties();

		// Get the title and description
		String title = taskType == null ? workflowTask.getTitle() : taskType + ".title";
		String description = (String) props.get(WorkflowModel.PROP_DESCRIPTION);

		// Get the duedate, priority and workflow package
		Date dueDate = (Date) props.get(WorkflowModel.PROP_DUE_DATE);
		Integer priority = (Integer) props.get(WorkflowModel.PROP_PRIORITY);
		NodeRef workflowPackage = workflowTask.getPath().getInstance().getWorkflowPackage();

		// Send notification
		sendWorkflowAssignedNotificationEMail(taskId, title, description, dueDate, priority, workflowPackage, assignedAuthorites, pooled, props);

	}

	/**
	 * Send workflow assigned email notification.
	 *
	 * @param taskId
	 *            workflow global task id
	 * @param assignedAuthority
	 *            assigned authority
	 * @param pooled
	 *            true if pooled task, false otherwise
	 * @param taskType a {@link java.lang.String} object
	 */
	public void sendWorkflowAssignedNotificationEMail(String taskId, String taskType, String assignedAuthority, boolean pooled) {
		sendWorkflowAssignedNotificationEMail(taskId, taskType, new String[] { assignedAuthority }, pooled);
	}
}
