/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

public class WikiDataExtractor extends AbstractNodeDataExtractor  {



	public WikiDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super(services,attributeExtractorService);
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType, SiteInfo site) {
		 
		
//		 // only process documents
//		   if (!node.isDocument)
//		   {
//		      return null;
//		   }
		   
		Map<String, Object> ret = new HashMap<String, Object>();
		
		 ret.put(PROP_NODEREF, nodeRef.toString());
		 ret.put(PROP_TAGS, attributeExtractorService.getTags(nodeRef));
		 
		 String name = (String) nodeService.getProperty(nodeRef,ContentModel.PROP_NAME);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, name.replaceAll("_", " "));
		 ret.put(PROP_DESCRIPTION, nodeService.getProperty(nodeRef,ContentModel.PROP_DESCRIPTION));
		
		 ret.put(PROP_MODIFIER,  nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  attributeExtractorService.convertDateValue(nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED)));
		
		 ret.put(PROP_CREATED,  attributeExtractorService.convertDateValue(nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED)));
		 ret.put(PROP_CREATOR,  nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "wikipage");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;

	}
	


}
