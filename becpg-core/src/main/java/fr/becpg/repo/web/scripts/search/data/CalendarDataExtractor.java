package fr.becpg.repo.web.scripts.search.data;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.PropertyService;

public class CalendarDataExtractor extends AbstractNodeDataExtractor  {

	
	public CalendarDataExtractor(ServiceRegistry services,PropertyService propertyService) {
		super(services,propertyService);
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
		 ret.put(PROP_TAGS, propertyService.getTags(nodeRef));
		 
		 String name = (String) propertyService.getProperty(nodeRef,ContentModel.PROP_NAME);
		 String title = (String) propertyService.getProperty(nodeRef,PROP_WHAT_EVENT);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, title);
		 ret.put(PROP_DESCRIPTION, propertyService.getProperty(nodeRef,PROP_DESCRIPTION_EVENT));
		
		 ret.put(PROP_MODIFIER, propertyService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  propertyService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "calendarevent");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;
	     	
	 
	
	}
	


}
