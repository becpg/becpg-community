package fr.becpg.repo.web.scripts.search.data;

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
	
		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		 ret.put(PROP_NODEREF, nodeRef.toString());
		 ret.put(PROP_TAGS, attributeExtractorService.getTags(nodeRef));
		 
		 String name = (String) attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_NAME);
		 String title = (String) attributeExtractorService.getProperty(nodeRef,PROP_WHAT_EVENT);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, title);
		 ret.put(PROP_DESCRIPTION, attributeExtractorService.getProperty(nodeRef,PROP_DESCRIPTION_EVENT));
		
		 ret.put(PROP_MODIFIER, attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "calendarevent");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;
	     	
	 
	
	}
	


}
