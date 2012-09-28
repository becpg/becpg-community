package fr.becpg.repo.entity.datalist.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.web.scripts.search.data.AbstractNodeDataExtractor;

public abstract class AbstractDataListExtractor implements DataListExtractor {
	
	protected NodeService nodeService;

	protected ServiceRegistry services;

	protected AttributeExtractorService attributeExtractorService;
	
	protected PermissionService permissionService;
	
	protected AdvSearchService advSearchService;

	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setServices(ServiceRegistry services) {
		this.services = services;
	}

	

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}


	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}


	public static final String PROP_NODE = "nodeRef";
	public static final String PROP_TAGS = "tags";
	public static final String PROP_DISPLAYNAME = "displayName";
	public static final String PROP_NAME = "name";
	public static final String PROP_TITLE = "title";
	public static final String PROP_DESCRIPTION = "description";
	public static final String PROP_MODIFIER = "modifiedByUser";
	public static final String PROP_MODIFIED = "modifiedOn";
	public static final String PROP_CREATED = "createdOn";
	public static final String PROP_CREATOR = "createdByUser";
	public static final String PROP_PATH = "path";
	public static final String PROP_MODIFIER_DISPLAY = "modifiedBy";
	public static final String PROP_CREATOR_DISPLAY = "createdBy";
	public static final String PROP_NODEDATA = "itemData";
	public static final String PROP_ACTIONSET = "actionSet";
	public static final String PROP_PERMISSIONS = "permissions";
	public static final String PROP_ACTIONLABELS = "actionLabels";
	public static final String PROP_ACCESSRIGHT = "accessRight";
	

	private static Log logger = LogFactory.getLog(AbstractNodeDataExtractor.class);

	public Map<String, Object> extract(final NodeRef nodeRef, List<String> metadataFields, Map<String, Object> props) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			QName itemType = nodeService.getType(nodeRef);

			Map<String, Object> ret = new HashMap<String, Object>();


			ret.put(PROP_NODE, nodeRef);
			         
			ret.put(PROP_CREATED, attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_CREATED));
			
			String creator = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_CREATOR);
			
			Map<String,String> createdBy = new HashMap<String, String>(2);
			createdBy.put("value", creator);
			createdBy.put("displayValue",  attributeExtractorService.getPersonDisplayName(creator));
			ret.put(PROP_CREATOR_DISPLAY,createdBy);
			
			
			String modifier = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
			
			ret.put(PROP_MODIFIED, attributeExtractorService.getProperty(nodeRef, ContentModel.PROP_MODIFIED));
			Map<String,String> modifiedBy = new HashMap<String, String>(2);
			modifiedBy.put("value", modifier);
			modifiedBy.put("displayValue",  attributeExtractorService.getPersonDisplayName(modifier));
			
			ret.put(PROP_MODIFIER_DISPLAY,modifiedBy);
			
			ret.put(PROP_ACTIONSET, "");
			
			
			Map<String, Object> permissions = new HashMap<String, Object>(1);
			Map<String, Boolean> userAccess = new HashMap<String, Boolean>(3);
			
			boolean accessRight = (Boolean) (props.get(PROP_ACCESSRIGHT)!=null ? props.get(PROP_ACCESSRIGHT) : false) ;
			
			permissions.put("userAccess", userAccess);
			userAccess.put("delete", accessRight && (permissionService.hasPermission(nodeRef, "Delete") == AccessStatus.ALLOWED));
			userAccess.put("create", accessRight && (permissionService.hasPermission(nodeRef, "CreateChildren") == AccessStatus.ALLOWED));
			userAccess.put("edit", accessRight && (permissionService.hasPermission(nodeRef, "Write") == AccessStatus.ALLOWED));
			userAccess.put("sort", accessRight &&  nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST));
		    userAccess.put("details", accessRight &&  nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM));
			
			ret.put(PROP_PERMISSIONS, permissions);
			
			ret.put(PROP_TAGS, attributeExtractorService.getTags(nodeRef));
			ret.put(PROP_ACTIONLABELS, new HashMap<String, Object>());
			
			ret.put(PROP_NODEDATA, doExtract(nodeRef, itemType, metadataFields, props));
		

			return ret;

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	protected abstract Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<String> metadataFields, Map<String, Object> props);


}
