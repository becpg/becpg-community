package fr.becpg.repo.web.scripts.search.data;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.helper.PropertyService;

public class LinkDataExtractor extends AbstractNodeDataExtractor  {

	
	/** DataList Model URI. */
	static final String MODEL_1_0_URI = "http://www.alfresco.org/model/linksmodel/1.0";	
	
	/** DataList Prefix. */
	static final String MODEL_PREFIX = "lnk";
	
	/** The Constant TYPE_DATALIST. */
	static final QName PROP_LINK_TITLE = QName.createQName(MODEL_1_0_URI, "title");
	

	public LinkDataExtractor(ServiceRegistry services,PropertyService propertyService) {
		super(services,propertyService);
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
		 ret.put(PROP_TAGS, propertyService.getTags(nodeRef));

		 ret.put(PROP_NAME,  propertyService.getProperty(nodeRef,ContentModel.PROP_NAME));
		 ret.put(PROP_DISPLAYNAME, propertyService.getProperty(nodeRef,PROP_LINK_TITLE));
		 ret.put(PROP_DESCRIPTION, propertyService.getProperty(nodeRef,ContentModel.PROP_DESCRIPTION));
		
		 ret.put(PROP_MODIFIER,  propertyService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  propertyService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  propertyService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "link");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;

	}
	


}
