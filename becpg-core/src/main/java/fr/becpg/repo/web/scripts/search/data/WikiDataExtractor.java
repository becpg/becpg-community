package fr.becpg.repo.web.scripts.search.data;

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
		 
		 String name = (String) attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_NAME);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, name.replaceAll("_", " "));
		 ret.put(PROP_DESCRIPTION, attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_DESCRIPTION));
		
		 ret.put(PROP_MODIFIER,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "wikipage");
		 ret.put(PROP_SIZE, getSize(nodeRef));
	      
	      return ret;

	}
	


}
