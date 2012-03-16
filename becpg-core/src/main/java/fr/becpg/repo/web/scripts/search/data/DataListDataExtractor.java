package fr.becpg.repo.web.scripts.search.data;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.DataListModel;
import fr.becpg.repo.helper.AttributeExtractorService;

public class DataListDataExtractor extends AbstractNodeDataExtractor  {



	public DataListDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super(services,attributeExtractorService);
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef,QName itemType, SiteInfo site) {
	
		Map<String, Object> ret = new HashMap<String, Object>();
		
		 ret.put(PROP_NODEREF, nodeRef.toString());

		 if(itemType.equals(DataListModel.TYPE_DATALIST)){
			 ret.put(PROP_NAME,  attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_NAME));
			 ret.put(PROP_TYPE, "datalist");
			 ret.put(PROP_DISPLAYNAME, attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_TITLE)); 
			 ret.put(PROP_DESCRIPTION, attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_DESCRIPTION));
		 } else  { //"{http://www.alfresco.org/model/datalist/1.0}dataListItem"
			 ret.put(PROP_NAME, attributeExtractorService.getProperty(getParent(nodeRef),ContentModel.PROP_NAME) );// used to generate link to parent datalist - not ideal
			 ret.put(PROP_TYPE, "datalistitem");
			 ret.put(PROP_DISPLAYNAME, attributeExtractorService.getProperty(nodeRef,ContentModel.PROP_NAME)); 
		 }
		 
		 ret.put(PROP_MODIFIER,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIER));
		 ret.put(PROP_MODIFIED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
		
		 ret.put(PROP_CREATED,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATED));
		 ret.put(PROP_CREATOR,  attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATOR));
	
		
		 ret.put(PROP_SIZE, -1);
	      
	      return ret;


	}
	


}
