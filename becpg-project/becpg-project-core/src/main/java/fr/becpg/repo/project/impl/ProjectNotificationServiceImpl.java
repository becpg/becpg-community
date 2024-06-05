package fr.becpg.repo.project.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.catalog.EntityCatalogObserver;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectNotificationEvent;
import fr.becpg.repo.project.data.projectList.TaskState;

/**
 * Class used to manage notification
 *
 * @author quere
 * @version $Id: $Id
 */
@Service("projectNotificationService")
public class ProjectNotificationServiceImpl implements ProjectNotificationService, EntityCatalogObserver {

	private static final Log logger = LogFactory.getLog(ProjectNotificationServiceImpl.class);

	/** Constant <code>MAIL_TEMPLATE="/app:company_home/app:dictionary/app:em"{trunked}</code> */
	public static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:project/cm:project-observer-email.html.ftl";

	/** Constant <code>ARG_ACTIVITY_TYPE="activityType"</code> */
	public static final String ARG_ACTIVITY_TYPE = "activityType";
	/** Constant <code>ARG_ACTIVITY_EVENT="activityEvent"</code> */
	public static final String ARG_ACTIVITY_EVENT = "activityEvent";
	/** Constant <code>ARG_TASK_TITLE="taskTitle"</code> */
	public static final String ARG_TASK_TITLE = "taskTitle";
	/** Constant <code>ARG_TASK_DESCRIPTION="taskDescription"</code> */
	public static final String ARG_TASK_DESCRIPTION = "taskDescription";
	/** Constant <code>ARG_DELIVERABLE_TITLE="deliverableDescription"</code> */
	public static final String ARG_DELIVERABLE_TITLE = "deliverableDescription";
	/** Constant <code>ARG_BEFORE_STATE="beforeState"</code> */
	public static final String ARG_BEFORE_STATE = "beforeState";
	/** Constant <code>ARG_AFTER_STATE="afterState"</code> */
	public static final String ARG_AFTER_STATE = "afterState";
	/** Constant <code>ARG_COMMENT="comment"</code> */
	public static final String ARG_COMMENT = "comment";
	/** Constant <code>ARG_PROJECT="project"</code> */
	public static final String ARG_PROJECT = "project";
	/** Constant <code>ARG_TASK_STATE="taskState"</code> */
	public static final String ARG_TASK_STATE = "taskState";
	/** Constant <code>ARG_TASK="task"</code> */
	public static final String ARG_TASK = "task";
	/** Constant <code>ARG_TASK_COMMENT="taskComment"</code> */
	public static final String ARG_TASK_COMMENT = "taskComment";

	private static final String PREFIX_LOCALIZATION_TASK_NAME = "listconstraint.pjt_taskStates.";

	private static final String MAIL_SUBJECT_KEY = "project.notification.mail.subject";

	@Autowired
	private ProjectService projectService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private BeCPGMailService beCPGMailService;

	/** {@inheritDoc} */
	@Override
	public void notifyTaskStateChanged(NodeRef projectNodeRef, NodeRef taskNodeRef, String beforeState, String afterState) {

		logger.debug("Notifying task state changed");
		String beforeStateMsg = I18NUtil.getMessage(PREFIX_LOCALIZATION_TASK_NAME + beforeState);
		String afterStateMsg = I18NUtil.getMessage(PREFIX_LOCALIZATION_TASK_NAME + afterState);

		String subject = createSubject(projectNodeRef, taskNodeRef, afterStateMsg);
		Map<String, Object> templateArgs = new HashMap<>(7);
		templateArgs.put(ARG_ACTIVITY_TYPE, ActivityType.State);
		templateArgs.put(ARG_TASK_TITLE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME));
		templateArgs.put(ARG_TASK_DESCRIPTION, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_DESCRIPTION));
		templateArgs.put(ARG_BEFORE_STATE, beforeStateMsg);
		templateArgs.put(ARG_AFTER_STATE, afterStateMsg);
		templateArgs.put(ARG_TASK_STATE, afterState);
		templateArgs.put(ARG_PROJECT, projectNodeRef);
		templateArgs.put(ARG_TASK, taskNodeRef);
		templateArgs.put(ARG_TASK_COMMENT, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_COMMENT));

		notifyObservers(projectNodeRef, taskNodeRef, subject, templateArgs, MAIL_TEMPLATE);
	}

	/** {@inheritDoc} */
	@Override
	public String createSubject(NodeRef projectNodeRef, NodeRef taskNodeRef, String afterStateMsg) {

		String code = (String) nodeService.getProperty(projectNodeRef, BeCPGModel.PROP_CODE);
		String projectName = (String) nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME);

		String taskName = taskNodeRef != null ? (String) nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME) : null;

		return I18NUtil.getMessage(MAIL_SUBJECT_KEY, "[" + projectName + (((code != null) && !projectName.contains(code)) ? " - " + code : "") + "]"
				+ (afterStateMsg != null ? " (" + afterStateMsg + ")" : "") + (taskName != null ? " " + taskName : ""));

	}

	/** {@inheritDoc} */
	@Override
	public void notifyComment(NodeRef commentNodeRef, ActivityEvent activityEvent, NodeRef projectNodeRef, NodeRef taskNodeRef,
			NodeRef deliverableNodeRef) {
		logger.debug("Notifying comments");
		String subject = createSubject(projectNodeRef, taskNodeRef, null);

		Map<String, Object> templateArgs = new HashMap<>(7);
		templateArgs.put(ARG_ACTIVITY_TYPE, ActivityType.Comment);
		templateArgs.put(ARG_ACTIVITY_EVENT, activityEvent);
		templateArgs.put(ARG_PROJECT, projectNodeRef);
		if (taskNodeRef != null) {
			templateArgs.put(ARG_TASK_TITLE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME));
			templateArgs.put(ARG_TASK_DESCRIPTION, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_DESCRIPTION));
			templateArgs.put(ARG_TASK, taskNodeRef);
		}

		if (deliverableNodeRef != null) {
			templateArgs.put(ARG_DELIVERABLE_TITLE, nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_DL_DESCRIPTION));
		}
		templateArgs.put(ARG_COMMENT, commentNodeRef);
		notifyObservers(projectNodeRef, taskNodeRef, subject, templateArgs, MAIL_TEMPLATE);
	}

	private void notifyObservers(NodeRef projectNodeRef, NodeRef taskNodeRef, String subject, Map<String, Object> templateArgs, String templateName) {

		List<NodeRef> observerNodeRefs = new ArrayList<>();

		if (projectNodeRef != null) {
			if (shouldNotify(projectNodeRef, templateArgs)) {
				observerNodeRefs.addAll(associationService.getTargetAssocs(projectNodeRef, ProjectModel.ASSOC_PROJECT_OBSERVERS));
			}
		}

		// Set the notification recipients
		if (taskNodeRef != null) {
			if (shouldNotify(taskNodeRef, templateArgs)) {
				
				observerNodeRefs.addAll(associationService.getTargetAssocs(taskNodeRef, ProjectModel.ASSOC_TL_OBSERVERS));
			}
		}

		List<NodeRef> finalObserverList = new ArrayList<>();
		
		for (NodeRef observer : observerNodeRefs) {
			NodeRef reassignedObserver = projectService.getReassignedResource(observer, new HashSet<>());
			if (reassignedObserver != null) {
				finalObserverList.add(reassignedObserver);
			} else {
				finalObserverList.add(observer);
			}
		}
		
		if (!finalObserverList.isEmpty()) {
			logger.debug("Notify "+finalObserverList.size()+" observers");	
			finalObserverList = projectService.extractResources(projectNodeRef, finalObserverList);
			Map<String, Object> argsMap = new HashMap<>();
			argsMap.put("args", templateArgs);
			beCPGMailService.sendMail(finalObserverList, subject, templateName, argsMap, false);
		}
	}
	

	/** {@inheritDoc} */
	@Override
	public void notifyAuditedFieldChange(String catalogId, NodeRef projectNodeRef) {
		logger.debug("Notifying properties");
		String subject = createSubject(projectNodeRef, null, null);

		Map<String, Object> templateArgs = new HashMap<>(7);
		templateArgs.put(ARG_ACTIVITY_TYPE, ActivityType.Entity);
		templateArgs.put(ARG_ACTIVITY_EVENT, ActivityEvent.Update);
		templateArgs.put(ARG_PROJECT, projectNodeRef);
		notifyObservers(projectNodeRef, null, subject, templateArgs, MAIL_TEMPLATE);
	}

	/** {@inheritDoc} */
	@Override
	public boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs) {
		return ProjectModel.TYPE_PROJECT.equals(type);
	}
	

	@SuppressWarnings("unchecked")
	private boolean shouldNotify(NodeRef nodeRef, Map<String, Object> templateArgs) {

		List<String> notificationEvents = (List<String>) nodeService.getProperty(nodeRef, ProjectModel.PROP_OBSERVERS_EVENTS);

		if ((notificationEvents == null) || notificationEvents.isEmpty()) {
			return true;
		} else {

			ActivityType type = (ActivityType) templateArgs.get(ARG_ACTIVITY_TYPE);
			String state = (String) templateArgs.get(ARG_TASK_STATE);
			boolean notify = false;
			for (String notificationEvent : notificationEvents) {
				ProjectNotificationEvent event = ProjectNotificationEvent.valueOf(notificationEvent);
				switch (event) {
				case All:
					notify = true;
					break;
				case Comment:
					if (ActivityType.Comment.equals(type)) {
						notify = true;
					}
					break;
				case TaskStart:
					if (TaskState.InProgress.toString().equals(state)) {
						notify = true;
					}
					break;
				case TaskEnd:
					if (TaskState.Completed.toString().equals(state) || TaskState.Cancelled.toString().equals(state)
							|| TaskState.OnHold.toString().equals(state)) {
						notify = true;
					}
					break;
				case TaskRefused:
					if (TaskState.Refused.toString().equals(state)) {
						notify = true;
					}
					break;
				case Properties:
					if (ActivityType.Entity.equals(type)) {
						notify = true;
					}
					break;
					
				default:
					break;
				}

				if (notify) {
					return true;
				}

			}

		}
		return false;
	}

}
