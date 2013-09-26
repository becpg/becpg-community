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

		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		 ret.put(PROP_NODEREF, childNodeRef.toString());
		 ret.put(PROP_TAGS, attributeExtractorService.getTags(childNodeRef));
		 
		 String name = (String) attributeExtractorService.getProperty(childNodeRef,ContentModel.PROP_NAME);
		 String title = (String) attributeExtractorService.getProperty(childNodeRef,ContentModel.PROP_TITLE);

		 ret.put(PROP_NAME,  name);
		 ret.put(PROP_DISPLAYNAME, title);
		
		 ret.put(PROP_MODIFIER,  attributeExtractorService.getProperty(childNodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  attributeExtractorService.getProperty(childNodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		 ret.put(PROP_TYPE, "blogpost");
		 ret.put(PROP_SIZE, getSize(childNodeRef));
	      
	      return ret;
	     	
	 
	
	}
	


}
