package fr.becpg.repo.web.scripts.search.data;

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

public class ContentDataExtractor extends AbstractNodeDataExtractor  {

	
	private List<String> metadataFields = new ArrayList<String>();
	

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
			 ret.put(PROP_NODEDATA,  attributeExtractorService.extractNodeData(nodeRef,itemType,metadataFields,true));
		 }
		 
		 DictionaryService dd = this.services.getDictionaryService();
		  
		 if ( Boolean.valueOf(dd.isSubClass(itemType, BeCPGModel.TYPE_ENTITY_V2)) == true )
	      {
			 ret.put(PROP_TYPE, "entity");
	         ret.put(PROP_SIZE, -1);
	      } else if ( Boolean.valueOf((dd.isSubClass(itemType, ContentModel.TYPE_FOLDER) == true &&
                  dd.isSubClass(itemType, ContentModel.TYPE_SYSTEM_FOLDER) == false)))
	      {
	    	 ret.put(PROP_TYPE, "folder");
	         ret.put(PROP_SIZE, -1);
	      }
	      else
	      {
	    	 ret.put(PROP_TYPE, "document");
	         ret.put(PROP_SIZE, getSize((ContentData)props.get(ContentModel.PROP_CONTENT)));
	      }           
	      
	      return ret;
	
	}


	private String formatDate(Date date) {
		if(date!=null){
			return attributeExtractorService.formatDate(date);
		}
		// TODO Auto-generated method stub
		return null;
	}






}
