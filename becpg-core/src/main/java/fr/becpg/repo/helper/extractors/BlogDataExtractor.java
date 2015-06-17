/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.AttributeExtractorService;


public class BlogDataExtractor extends AbstractNodeDataExtractor  {



	public BlogDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super(services,attributeExtractorService);
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType, SiteInfo site) {
		 
		
		   
		   /**
		    * Find the direct child of the container
		    * Note: this only works for post which are direct children of the blog container
		    */
		  NodeRef childNodeRef = nodeRef;
		  NodeRef parent = getParent(childNodeRef);
		  
		   while ((parent != null) && (!parent.equals(site.getNodeRef())))
		   {
			   childNodeRef = parent;
		      parent = getParent(parent);
		   }
		
		 /**
	       * Find the direct child of the container
	       * Note: this only works for post which are direct children of the blog container
	       */

		
		Map<String, Object> ret = new HashMap<>();
		
		 ret.put(PROP_NODEREF, childNodeRef.toString());
		 ret.put(PROP_TAGS, attributeExtractorService.getTags(childNodeRef));
		 
		 String name = (String) nodeService.getProperty(childNodeRef,ContentModel.PROP_NAME);
		 String title = (String) nodeService.getProperty(childNodeRef,ContentModel.PROP_TITLE);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, title);
		
		 ret.put(PROP_MODIFIER,  nodeService.getProperty(childNodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  convertDateValue(nodeService.getProperty(childNodeRef, ContentModel.PROP_MODIFIED)));
		
		 ret.put(PROP_CREATED,  convertDateValue(nodeService.getProperty(childNodeRef, ContentModel.PROP_CREATED)));
		 ret.put(PROP_CREATOR,  nodeService.getProperty(childNodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "blogpost");
		 ret.put(PROP_SIZE, getSize(childNodeRef));
	      
	      return ret;
	     	
	 
	
	}

	


}
