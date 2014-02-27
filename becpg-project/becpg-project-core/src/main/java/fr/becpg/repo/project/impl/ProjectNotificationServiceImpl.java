package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.project.ProjectNotificationService;

/**
 * Class used to manage notification
 * @author quere
 *
 */
@Service("projectNotificationService")
public class ProjectNotificationServiceImpl implements ProjectNotificationService {
	
	private static Log logger = LogFactory.getLog(ProjectNotificationServiceImpl.class);
	
	public static final String MSG_SUBMIT_TASK = "submit-task";
    public static String MAIL_TEMPLATE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "project-submit-task-email-html-ftl").toString();

    public static final String ARG_TASK_TITLE = "taskTitle";
    public static final String ARG_TASK_DESCRIPTION = "taskDescription";
    public static final String ARG_TASK_STATE = "taskState";
    public static final String ARG_PROJECT = "project";
    
    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private EntityListDAO entityListDAO;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AssociationService associationService;
    
	@Override
	public void sendObserverNotificationEMail(NodeRef taskNodeRef) {
		
		NotificationContext notificationContext = new NotificationContext();
        
        String subject = MSG_SUBMIT_TASK;       
        notificationContext.setSubject(subject);
        
        // Set the email template
        notificationContext.setBodyTemplate(MAIL_TEMPLATE);
        
        // Build the template args
        Map<String, Serializable>templateArgs = new HashMap<String, Serializable>(7);        
        templateArgs.put(ARG_TASK_TITLE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_TASK_NAME));
        templateArgs.put(ARG_TASK_DESCRIPTION, nodeService.getProperty(taskNodeRef, ContentModel.PROP_DESCRIPTION));
        templateArgs.put(ARG_TASK_STATE, nodeService.getProperty(taskNodeRef, ProjectModel.PROP_TL_STATE));
        
        NodeRef projectNodeRef = entityListDAO.getEntity(taskNodeRef);
        NodeRef[] projects = new NodeRef[1];
        projects[0] = projectNodeRef;
        templateArgs.put(ARG_PROJECT, projects);
        
        notificationContext.setTemplateArgs(templateArgs);
        
        // Set the notification recipients
        List<NodeRef> observerNodeRefs = associationService.getTargetAssocs(taskNodeRef, ProjectModel.ASSOC_TL_OBSERVERS);
        if(projectNodeRef != null){
        	observerNodeRefs.addAll(associationService.getTargetAssocs(projectNodeRef, ProjectModel.ASSOC_PROJECT_OBSERVERS));
        }        
        logger.info("###observerNodeRefs : " + observerNodeRefs);
        for (NodeRef observerNodeRef : observerNodeRefs){
        	String authorityName = (String)nodeService.getProperty(observerNodeRef, ContentModel.PROP_USERNAME);
        	notificationContext.addTo(authorityName);
        	logger.info("###authorityName : " + authorityName);
        }
       
        // Indicate that we want to execute the notification asynchronously
        notificationContext.setAsyncNotification(true);
        
        // Send email notification
        notificationService.sendNotification(EMailNotificationProvider.NAME, notificationContext);		
	}

}
