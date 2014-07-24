package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
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
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.projectList.ActivityEvent;
import fr.becpg.repo.project.data.projectList.ActivityType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Class used to manage notification
 * 
 * @author quere
 * 
 */
@Service("projectNotificationService")
public class ProjectNotificationServiceImpl implements ProjectNotificationService {

	private static Log logger = LogFactory.getLog(ProjectNotificationServiceImpl.class);

	public static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:project/cm:project-observer-email.html.ftl";

	public static final String ARG_ACTIVITY_TYPE = "activityType";
	public static final String ARG_ACTIVITY_EVENT = "activityEvent";
	public static final String ARG_TASK_TITLE = "taskTitle";
	public static final String ARG_TASK_DESCRIPTION = "taskDescription";
	public static final String ARG_DELIVERABLE_TITLE = "deliverableDescription";
	public static final String ARG_BEFORE_STATE = "beforeState";
	public static final String ARG_AFTER_STATE = "afterState";
	public static final String ARG_COMMENT = "comment";
	public static final String ARG_PROJECT = "project";

	private static final String PREFIX_LOCALIZATION_TASK_NAME = "listconstraint.pjt_taskStates.";

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private ActionService actionService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private Repository repositoryHelper;

	@Override
	public void notifyTaskStateChanged(NodeRef projectNodeRef, NodeRef taskNodeRef, String beforeState, String afterState) {

		String beforeStateMsg = I18NUtil.getMessage(PREFIX_LOCALIZATION_TASK_NAME + beforeState);
		String afterStateMsg = I18NUtil.getMessage(PREFIX_LOCALIZATION_TASK_NAME + afterState);
		
		
		String subject = createSubject(projectNodeRef,taskNodeRef, afterStateMsg);

		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>(7);
		templateArgs.put(ARG_ACTIVITY_TYPE, ActivityType.State);
		templateArgs.put(ARG_TASK_TITLE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME));
		templateArgs.put(ARG_TASK_DESCRIPTION, nodeService.getProperty(taskNodeRef, ContentModel.PROP_DESCRIPTION));
		templateArgs.put(ARG_BEFORE_STATE, beforeStateMsg);
		templateArgs.put(ARG_AFTER_STATE, afterStateMsg);
		templateArgs.put(ARG_PROJECT, projectNodeRef);

		notify(projectNodeRef, taskNodeRef, subject, templateArgs);
	}

	private String createSubject(NodeRef projectNodeRef, NodeRef taskNodeRef, String afterStateMsg) {
		String code =  (String) nodeService.getProperty(projectNodeRef, BeCPGModel.PROP_CODE);
		return  "[" + nodeService.getProperty(projectNodeRef, ContentModel.PROP_NAME) + (code!=null ? " - "
				+ code  : "")+ "] "
				+ nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME) + (afterStateMsg!=null? " (" + afterStateMsg + ")" : "");
	}

	@Override
	public void notifyComment(NodeRef commentNodeRef, ActivityEvent activityEvent, NodeRef projectNodeRef, NodeRef taskNodeRef,
			NodeRef deliverableNodeRef) {

		String subject = createSubject(projectNodeRef, taskNodeRef,null);

		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>(7);
		templateArgs.put(ARG_ACTIVITY_TYPE, ActivityType.Comment);
		templateArgs.put(ARG_ACTIVITY_EVENT, activityEvent);
		templateArgs.put(ARG_PROJECT, projectNodeRef);
		if (taskNodeRef != null) {
			templateArgs.put(ARG_TASK_TITLE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME));
			templateArgs.put(ARG_TASK_DESCRIPTION, nodeService.getProperty(taskNodeRef, ContentModel.PROP_DESCRIPTION));
		}
		if (deliverableNodeRef != null) {
			templateArgs.put(ARG_DELIVERABLE_TITLE, nodeService.getProperty(deliverableNodeRef, ProjectModel.PROP_DL_DESCRIPTION));
		}
		templateArgs.put(ARG_COMMENT, commentNodeRef);

		notify(projectNodeRef, taskNodeRef, subject, templateArgs);
	}

	private void notify(NodeRef projectNodeRef, NodeRef taskNodeRef, String subject, Map<String, Serializable> templateArgs) {

		NodeRef templateNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(), MAIL_TEMPLATE);

		if (templateNodeRef == null) {
			logger.warn("Template not found.");
		} else {

			List<String> authorities = new ArrayList<>();

			// Set the notification recipients
			List<NodeRef> observerNodeRefs = associationService.getTargetAssocs(taskNodeRef, ProjectModel.ASSOC_TL_OBSERVERS);
			if (observerNodeRefs == null) {
				observerNodeRefs = new ArrayList<>();
			}

			if (projectNodeRef != null) {
				observerNodeRefs.addAll(associationService.getTargetAssocs(projectNodeRef, ProjectModel.ASSOC_PROJECT_OBSERVERS));
			}
			if (!observerNodeRefs.isEmpty()) {
				
				
				observerNodeRefs  = projectService.updateTaskResources(projectNodeRef, taskNodeRef, observerNodeRefs, false);

				for (NodeRef observerNodeRef : observerNodeRefs) {
					String authorityName = null;
					QName type = nodeService.getType(observerNodeRef);
					if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
						authorityName = (String) nodeService.getProperty(observerNodeRef, ContentModel.PROP_AUTHORITY_NAME);
					} else {
						authorityName = (String) nodeService.getProperty(observerNodeRef, ContentModel.PROP_USERNAME);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("authorityName : " + authorityName);
					}
					if(!authorityName.equals(AuthenticationUtil.getFullyAuthenticatedUser())){
						authorities.add(authorityName);
					}
				}

				try {
					if(!authorities.isEmpty()){
						Action mailAction = actionService.createAction(MailActionExecuter.NAME);
						mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subject);
						mailAction.setParameterValue(MailActionExecuter.PARAM_TO_MANY, (Serializable) authorities);
						mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, fileFolderService.getLocalizedSibling(templateNodeRef));
	
						Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
						templateModel.put("args", (Serializable) templateArgs);
						mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
	
						actionService.executeAction(mailAction, null);
					}
				} catch (Exception e) {
					logger.error("Cannot send email project notify email :" + e.getMessage(), e);
				}
			}
		}
	}

}
