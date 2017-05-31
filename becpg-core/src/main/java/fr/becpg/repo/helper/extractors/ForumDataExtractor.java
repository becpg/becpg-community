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
package fr.becpg.repo.helper.extractors;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.AttributeExtractorService;

public class ForumDataExtractor extends AbstractNodeDataExtractor  {



	public ForumDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super(services,attributeExtractorService);
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

			
			Map<String, Object> ret = new HashMap<>();
			
			 ret.put(PROP_NODEREF, topicNode.toString());
			 ret.put(PROP_TAGS, attributeExtractorService.getTags(topicNode));
			 
			 String name = (String) nodeService.getProperty(topicNode,ContentModel.PROP_NAME);
			 String title = (String) nodeService.getProperty(postNode,ContentModel.PROP_TITLE);

			 ret.put(PROP_NAME,  name);
			 ret.put(PROP_DISPLAYNAME, title);
			 ret.put(PROP_DESCRIPTION,  nodeService.getProperty(topicNode, ContentModel.PROP_DESCRIPTION));
			 
			 ret.put(PROP_MODIFIER,  nodeService.getProperty(topicNode, ContentModel.PROP_MODIFIER));
			 ret.put(PROP_MODIFIED,  convertDateValue(nodeService.getProperty(topicNode, ContentModel.PROP_MODIFIED)));
			
			 ret.put(PROP_CREATED,  convertDateValue(nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED)));
			 ret.put(PROP_CREATOR,  nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
		
			 ret.put(PROP_TYPE, "forumpost");
			 ret.put(PROP_SIZE, getSize(topicNode));
		      
		      return ret;
	     	
	 
	
	}
	


}
