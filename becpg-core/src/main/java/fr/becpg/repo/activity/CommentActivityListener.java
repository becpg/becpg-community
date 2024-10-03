package fr.becpg.repo.activity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.workflow.PackageManager;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.htmlcleaner.ContentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;

/**
 * <p>CommentActivityListener class.</p>
 *
 * @author matthieu
 */
@Service
public class CommentActivityListener implements InitializingBean, EntityActivityListener {

	@Autowired
	private PersonService personService;

	@Autowired
	private ContentService contentService;

	@Autowired
	@Qualifier("WorkflowService")
	private WorkflowService workflowService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private VersionService versionService;

	private PackageManager packageMgr;

	private static final Pattern commentNotificationPattern = Pattern.compile("@[a-zA-Z0-9]([^\\s]+)");

	private static final Log logger = LogFactory.getLog(CommentActivityListener.class);
	
	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		packageMgr = new PackageManager(workflowService, nodeService, policyBehaviourFilter, null);
	}

	/** {@inheritDoc} */
	@Override
	public void notify(NodeRef entityNodeRef, ActivityListDataItem activityListDataItem) {
		JSONObject activityData = new JSONObject(activityListDataItem.getActivityData());

		if (ActivityType.Comment.equals(activityListDataItem.getActivityType())) {

			NodeRef commentNodeRef = new NodeRef(activityData.getString("commentNodeRef"));
			
			ContentReader reader = contentService.getReader(commentNodeRef, ContentModel.PROP_CONTENT);

			if (reader != null) {
				String comment = reader.getContentString();
				sendCommentNotification(comment, entityNodeRef, commentNodeRef);
			}


		} else if (ActivityType.Version.equals(activityListDataItem.getActivityType())) {

			String versionLabel = activityData.getString("versionLabel");

			VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);

			if (versionHistory != null) {
				Version version = versionHistory.getVersion(versionLabel);

				if (version != null) {
					sendCommentNotification(version.getDescription(), entityNodeRef, null);
				}
			}
		}
	}

	private void sendCommentNotification(String comment, NodeRef entityNodeRef, NodeRef commentNodeRef) {

		if (comment != null && !comment.isBlank()) {
			
			comment = escapeHtml(comment);

			String itemName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);

			Matcher matcher = commentNotificationPattern.matcher(comment);

			ArrayList<String> usernames = new ArrayList<>();

			String commentText = comment;
			
			while (matcher.find()) {
				String match = matcher.group();
				String username = match.substring(1);

				usernames.add(username);

				commentText = commentText.replace(match + " ", "");
			}
			
			for (String username : usernames) {

				if (commentNodeRef != null && personService.personExists(username)) {
					
					String workflowInstanceId = (String) nodeService.getProperty(commentNodeRef, ContentModel.PROP_DESCRIPTION);
					if (workflowInstanceId == null || workflowInstanceId.isBlank()) {
						NodeRef packageRef = packageMgr.create(null);
						
						Map<QName, Serializable> params = new HashMap<>();
						
						params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
						params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, commentText);
						params.put(WorkflowModel.ASSOC_ASSIGNEE, (Serializable) Arrays.asList(personService.getPerson(username)));
						params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 2);
						params.put(WorkflowModel.PROP_SEND_EMAIL_NOTIFICATIONS, true);
						
						nodeService.addChild(packageRef, entityNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(itemName)));
						
						WorkflowPath workflowPath = workflowService.startWorkflow("activiti$activitiAdhoc:1:4", params);
						
						if (workflowPath != null) {
							nodeService.setProperty(commentNodeRef, ContentModel.PROP_DESCRIPTION, workflowPath.getInstance().getId());
						}
					} else {
						WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
						tasksQuery.setTaskState(null);
						tasksQuery.setActive(null);
						tasksQuery.setProcessId(workflowInstanceId);
						List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery, true);
						
						if (tasks != null) {
							for (WorkflowTask task : tasks) {
								if ("wf:adhocTask".equals(task.getName())) {
									Map<QName, Serializable> taskProperties = task.getProperties();
									taskProperties.put(WorkflowModel.PROP_DESCRIPTION, commentText);
									workflowService.updateTask(task.getId(), taskProperties, null, null);
									break;
								}
							}
						}
					}
				}
				
			}
		}
	}
	
	private String escapeHtml(String htmlContent) {
		HtmlCleaner cleaner = new HtmlCleaner(htmlContent);
		
		try {
			cleaner.clean();
		} catch (IOException e) {
			logger.error("Could not parse HTML comment", e);
		}
		
		return escapeHtml(cleaner.getBodyNode(), null);
	}

	private String escapeHtml(TagNode node, StringBuilder contentBuilder) {

		if (contentBuilder == null) {
			contentBuilder = new StringBuilder();
		}
		
		for (Object child : node.getChildren()) {
			if (child instanceof ContentToken) {
				contentBuilder.append(StringEscapeUtils.unescapeHtml4(((ContentToken) child).getContent()));
			} else if (child instanceof TagNode) {
				escapeHtml((TagNode) child, contentBuilder);
			}
		}
		
		return contentBuilder.toString();
	}

}
