package fr.becpg.repo.web.scripts.search.data;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.PropertyService;

public class ForumDataExtractor extends AbstractNodeDataExtractor  {



	public ForumDataExtractor(ServiceRegistry services,PropertyService propertyService) {
		super(services,propertyService);
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType, SiteInfo site) {
		
		// try to find the first fm:topic node, that's what we return as search result
		  NodeRef topicNode = nodeRef;
		   while ((topicNode != null) && (!nodeService.getType(topicNode).equals(ForumModel.TYPE_TOPIC)))
		   {
		      topicNode =  getParent(topicNode);
		   }
		   if (topicNode == null)
		   {
		      return null;
		   }

		   //"cm:contains"
		   NodeRef postNode = nodeService.getChildAssocs(topicNode).get(0).getChildRef();

			
			Map<String, Object> ret = new HashMap<String, Object>();
			
			 ret.put(PROP_NODEREF, topicNode.toString());
			 ret.put(PROP_TAGS, propertyService.getTags(topicNode));
			 
			 String name = (String) propertyService.getProperty(topicNode,ContentModel.PROP_NAME);
			 String title = (String) propertyService.getProperty(postNode,ContentModel.PROP_TITLE);

			 ret.put(PROP_NAME,  name);
			 ret.put(PROP_DISPLAYNAME, title);
			 ret.put(PROP_DESCRIPTION,  propertyService.getProperty(topicNode, ContentModel.PROP_DESCRIPTION));
			 
			 ret.put(PROP_MODIFIER,  propertyService.getProperty(topicNode, ContentModel.PROP_MODIFIER));
			 ret.put(PROP_MODIFIED,  propertyService.getProperty(topicNode, ContentModel.PROP_MODIFIED));
			
			 ret.put(PROP_CREATED,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATED));
			 ret.put(PROP_CREATOR,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
		
			 ret.put(PROP_TYPE, "forumpost");
			 ret.put(PROP_SIZE, getSize(topicNode));
		      
		      return ret;
	     	
	 
	
	}
	


}
