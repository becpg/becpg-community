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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;

public class ContentDataExtractor extends AbstractNodeDataExtractor  {

	
	
	private List<String> metadataFields = new ArrayList<String>();
	

	
	public ContentDataExtractor(ServiceRegistry serviceRegistry,AttributeExtractorService attributeExtractorService){
		super(serviceRegistry,attributeExtractorService);
	}
	
	public ContentDataExtractor(List<String> metadataFields,ServiceRegistry serviceRegistry,AttributeExtractorService attributeExtractorService) {
		super(serviceRegistry,attributeExtractorService);
		this.metadataFields = metadataFields;
	}


	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType ,SiteInfo site) {
		 
		Map<String, Object> ret = new HashMap<String, Object>();
		
		 ret.put(PROP_NODEREF, nodeRef.toString());
		 ret.put(PROP_TAGS,  attributeExtractorService.getTags(nodeRef));
		 
		 Map<QName,Serializable> props =  nodeService.getProperties(nodeRef);
		 
		 String name = (String) props.get( ContentModel.PROP_NAME);
		 ret.put(PROP_DISPLAYNAME, name);
		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_TITLE,  props.get( ContentModel.PROP_TITLE));
		 ret.put(PROP_DESCRIPTION,  props.get( ContentModel.PROP_DESCRIPTION));
		 ret.put(PROP_MODIFIER,  props.get( ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  formatDate((Date)props.get( ContentModel.PROP_MODIFIED)));
		 ret.put(PROP_CREATED,  formatDate((Date)props.get( ContentModel.PROP_CREATED)));
		 ret.put(PROP_CREATOR,  props.get( ContentModel.PROP_CREATOR));
		 if(!metadataFields.isEmpty()){
			 ret.put(PROP_NODEDATA,  attributeExtractorService.extractNodeData(nodeRef,itemType,metadataFields,AttributeExtractorMode.SEARCH));
		 }
		 
		 DictionaryService dd = this.services.getDictionaryService();
		  
		 if ( dd.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2) )
	      {
			 ret.put(PROP_TYPE, "entity");
	         ret.put(PROP_SIZE, -1);
	      } else if ( dd.isSubClass(itemType, ContentModel.TYPE_FOLDER) &&
                  !dd.isSubClass(itemType, ContentModel.TYPE_SYSTEM_FOLDER))
	      {
	    	 ret.put(PROP_TYPE, "folder");
	         ret.put(PROP_SIZE, -1);
	      }
	      else
	      {
	    	 ret.put(PROP_TYPE, "document");
	         ret.put(PROP_SIZE, getSize((ContentData)props.get(ContentModel.PROP_CONTENT)));
	      }           
	      
		 List<String> aspects = new ArrayList<String>();
		 for(QName aspect : nodeService.getAspects(nodeRef)){
			 aspects.add(aspect.toPrefixString(services.getNamespaceService()));
		 }
		 
		 ret.put(PROP_ASPECTS, aspects);
		 
		 
	      return ret;
	
	}


	private String formatDate(Date date) {
		if(date!=null){
			return attributeExtractorService.formatDate(date);
		}
		return null;
	}






}
