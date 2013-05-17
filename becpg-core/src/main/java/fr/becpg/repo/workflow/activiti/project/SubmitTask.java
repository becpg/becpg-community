/*
 * 
 */
package fr.becpg.repo.workflow.activiti.project;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.util.ApplicationContextHelper;

/**
 * Submit a workflow task
 * 
 * @author quere
 * 
 */
public class SubmitTask extends ScriptTaskListener {

	private final static Log logger = LogFactory.getLog(SubmitTask.class);
	
	private NodeService nodeService;
	
	private ContentService contentService;
	
	private ProjectActivityService projectActivityService;

	@Override
	public void notify(final DelegateTask task) {

		nodeService = getServiceRegistry().getNodeService();
		contentService = getServiceRegistry().getContentService();		
		projectActivityService = (ProjectActivityService)ApplicationContextHelper.getApplicationContext().getBean("projectActivityService");
		
		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		/**
		 * Add submitted comment on project
		 */
		String comment = (String) task.getVariable("bpm_comment");

		if (comment != null && !comment.isEmpty()) {

			NodeRef projectNodeRef = null;

			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(pkgNodeRef,
					WorkflowModel.ASSOC_PACKAGE_CONTAINS, RegexQNamePattern.MATCH_ALL);
			for (ChildAssociationRef childAssoc : childAssocs) {
				if (nodeService.getType(childAssoc.getChildRef()).equals(ProjectModel.TYPE_PROJECT)) {
					projectNodeRef = childAssoc.getChildRef();
					break;
				}
			}

			if (projectNodeRef != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Add comment '" + comment + "' on project " + projectNodeRef);
				}

				applyComment(projectNodeRef, comment);
				projectActivityService.postProjectCommentCreatedActivity(projectNodeRef, comment);
			}
		}
	}

	/**
     * This method applies the specified comment to the specified node.
     * As there is no CommentService or DiscussionService, we mimic here what the comments REST API does,
     * by manually creating the correct content structure using the nodeService. Behaviours will do some
     * of the work for us. See comments.post.json.js for comparison.
     * @param nr nodeRef to comment on.
     * @param comment the text of the comment.
     * @return the NodeRef of the fm:post comment node.
     * 
     */
    private NodeRef applyComment(NodeRef nr, String comment)
    {
        // There is no CommentService, so we have to create the node structure by hand.
        // This is what happens within e.g. comment.put.json.js when comments are submitted via the REST API.
        if (!nodeService.hasAspect(nr, ForumModel.ASPECT_DISCUSSABLE))
        {
            nodeService.addAspect(nr, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        // Forum node is created automatically by DiscussableAspect behaviour.
        NodeRef forumNode = nodeService.getChildAssocs(nr, ForumModel.ASSOC_DISCUSSION, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion")).get(0).getChildRef();
        
        final List<ChildAssociationRef> existingTopics = nodeService.getChildAssocs(forumNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"));
        NodeRef topicNode = null;
        if (existingTopics.isEmpty())
        {
            topicNode = nodeService.createNode(forumNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"), ForumModel.TYPE_TOPIC).getChildRef();
        }
        else
        {
            topicNode = existingTopics.get(0).getChildRef();
        }

        NodeRef postNode = nodeService.createNode(topicNode, ContentModel.ASSOC_CONTAINS, QName.createQName("comment" + System.currentTimeMillis()), ForumModel.TYPE_POST).getChildRef();
        nodeService.setProperty(postNode, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        ContentWriter writer = contentService.getWriter(postNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(comment);
        
        return postNode;
    }

}
