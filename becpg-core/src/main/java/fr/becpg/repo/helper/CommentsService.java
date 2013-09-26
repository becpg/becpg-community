package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author matthieu
 * Replace by commentService from alfresco when available
 */
@Deprecated 
public class CommentsService {
	
	private NodeService nodeService;
	
	private ContentService contentService;
	
	
	private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");
    private static final String COMMENTS_TOPIC_NAME = "Comments";
	
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}




	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


    public NodeRef createComment(final NodeRef discussableNode, String title, String comment, boolean suppressRollups)
    {
    	if(comment == null)
    	{
    		throw new IllegalArgumentException("Must provide a non-null comment");
    	}

        // There is no CommentService, so we have to create the node structure by hand.
        // This is what happens within e.g. comment.put.json.js when comments are submitted via the REST API.
        if (!nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            nodeService.addAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        if (!nodeService.hasAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP) && !suppressRollups)
        {
            nodeService.addAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP, null);
        }
        // Forum node is created automatically by DiscussableAspect behaviour.
        NodeRef forumNode = nodeService.getChildAssocs(discussableNode, ForumModel.ASSOC_DISCUSSION, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion")).get(0).getChildRef();
        
        final List<ChildAssociationRef> existingTopics = nodeService.getChildAssocs(forumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME);
        NodeRef topicNode = null;
        if (existingTopics.isEmpty())
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
            props.put(ContentModel.PROP_NAME, COMMENTS_TOPIC_NAME);
            topicNode = nodeService.createNode(forumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME, ForumModel.TYPE_TOPIC, props).getChildRef();
        }
        else
        {
            topicNode = existingTopics.get(0).getChildRef();
        }

        NodeRef postNode = nodeService.createNode(topicNode, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ForumModel.TYPE_POST).getChildRef();
        nodeService.setProperty(postNode, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        nodeService.setProperty(postNode, ContentModel.PROP_TITLE, title);
        ContentWriter writer = contentService.getWriter(postNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(comment);
//No activity
 //      
//    	// determine the siteId and activity data of the comment NodeRef
//        String siteId = getSiteId(discussableNode);
//        JSONObject activityData = getActivityData(siteId, discussableNode);
//
//        postActivity(siteId, ActivityType.COMMENT_CREATED, activityData);

        return postNode;
    }
    
}
