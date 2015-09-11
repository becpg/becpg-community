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

public class CalendarDataExtractor extends AbstractNodeDataExtractor  {

	
	public CalendarDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super(services,attributeExtractorService);
	}

	/** DataList Model URI. */
	static final String MODEL_1_0_URI = "http://www.alfresco.org/model/calendar";	
	
	/** DataList Prefix. */
	static final String MODEL_PREFIX = "ia";
	
	/** The Constant TYPE_DATALIST. */
	static final QName PROP_WHAT_EVENT = QName.createQName(MODEL_1_0_URI, "whatEvent");
	
	static final QName PROP_DESCRIPTION_EVENT = QName.createQName(MODEL_1_0_URI, "descriptionEvent");
	


	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType, SiteInfo site) {
		
		
//		  // only process nodes of the correct type
//		   if (node.type != "{http://www.alfresco.org/model/calendar}calendarEvent")
//		   {
//		      return null;
//		   }
	
		
		Map<String, Object> ret = new HashMap<>();
		
		 ret.put(PROP_NODEREF, nodeRef.toString());
		 ret.put(PROP_TAGS, attributeExtractorService.getTags(nodeRef));
		 
		 String name = (String) nodeService.getProperty(nodeRef,ContentModel.PROP_NAME);
		 String title = (String) nodeService.getProperty(nodeRef,PROP_WHAT_EVENT);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, title);
		 ret.put(PROP_DESCRIPTION, nodeService.getProperty(nodeRef,PROP_DESCRIPTION_EVENT));
		
		 ret.put(PROP_MODIFIER, nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  convertDateValue(nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED)));
		
		 ret.put(PROP_CREATED,  convertDateValue(nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED)));
		 ret.put(PROP_CREATOR,  nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "calendarevent");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;
	     	
	 
	
	}
	


}
