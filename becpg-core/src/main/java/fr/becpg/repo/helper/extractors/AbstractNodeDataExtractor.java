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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.SiteHelper;

public abstract class AbstractNodeDataExtractor implements NodeDataExtractor {

	protected NodeService nodeService;

	protected ServiceRegistry services;
	
	protected AttributeExtractorService attributeExtractorService;

	protected static final String PROP_NODEREF = "nodeRef";
	protected static final String PROP_TAGS = "tags";
	protected static final String PROP_DISPLAYNAME = "displayName";
	protected static final String PROP_NAME = "name";
	protected static final String PROP_TITLE = "title";
	protected static final String PROP_DESCRIPTION = "description";
	protected static final String PROP_MODIFIER = "modifiedByUser";
	protected static final String PROP_MODIFIED = "modifiedOn";
	protected static final String PROP_CREATED = "createdOn";
	protected static final String PROP_CREATOR = "createdByUser";
	protected static final String PROP_PATH = "path";
	protected static final String PROP_MODIFIER_DISPLAY = "modifiedBy";
	protected static final String PROP_CREATOR_DISPLAY = "createdBy";
	protected static final String PROP_NODEDATA = "itemData";
	protected static final String PROP_TYPE = "type";
	protected static final String PROP_SIZE = "size";
	protected static final String PROP_ITEMTYPE = "itemType";
	protected static final String PROP_SHORTNAME = "shortName";
	protected static final String PROP_CONTAINER = "container";
	protected static final String PROP_SITE = "site";
	protected static final String PROP_ASPECTS = "aspects";
	protected static final String PROP_METADATA = "metadata";
	

	public AbstractNodeDataExtractor(ServiceRegistry services,AttributeExtractorService attributeExtractorService) {
		super();
		this.attributeExtractorService = attributeExtractorService;
		this.services = services;
		this.nodeService = services.getNodeService();
	}


	private static Log logger = LogFactory.getLog(AbstractNodeDataExtractor.class);

	@Override
	public Map<String, Object> extract(final NodeRef nodeRef) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

					String path = nodeService.getPath(nodeRef).toPrefixString(services.getNamespaceService());
					String displayPath = attributeExtractorService.getDisplayPath(nodeRef);

					String siteId = SiteHelper.extractSiteId(path, displayPath);
					String containerId = SiteHelper.extractContainerId(path);
					String retPath = SiteHelper.extractDisplayPath(path,displayPath);

					QName itemType = nodeService.getType(nodeRef);
					SiteService siteService = services.getSiteService();
					
					
					SiteInfo site = null;
					if(siteId!=null){
						site = siteService.getSite(siteId);
					}
					//Keep order
					Map<String, Object> ret = doExtract(nodeRef, itemType, site);
					
					ret.put(PROP_ITEMTYPE, itemType.toPrefixString(services.getNamespaceService()));
					ret.put(PROP_MODIFIER_DISPLAY, attributeExtractorService.getPersonDisplayName((String) ret.get(PROP_MODIFIER)));
					ret.put(PROP_CREATOR_DISPLAY, attributeExtractorService.getPersonDisplayName((String) ret.get(PROP_CREATOR)));
					if (containerId != null) {
						ret.put(PROP_CONTAINER, containerId);
					}
					ret.put(PROP_PATH, retPath);
					

					if (site != null) {
						Map<String, Object> siteData = new HashMap<String, Object>();
						siteData.put(PROP_SHORTNAME, siteId);
						siteData.put(PROP_TITLE, site.getTitle());
						ret.put(PROP_SITE, siteData);
					}

					return ret;
			
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug( getClass().getSimpleName()+" extract metadata in  "
						+ watch.getTotalTimeSeconds()+"s" );
			}
		}
	}

	protected abstract Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, SiteInfo site);

	
	public NodeRef getParent(NodeRef nodeRef) {
		return nodeService.getPrimaryParent(nodeRef).getParentRef();
	}

	
	
	public Long getSize(NodeRef nodeRef) {
		return getSize((ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
	}

	
	public Long getSize(ContentData contentData) {
		if(contentData==null){
			return null;
		}
		return contentData.getSize();
	}
	

	protected String convertDateValue(Serializable value) {
		if (value instanceof Date) {
			return formatDate((Date) value);
		}
		return null;
	}
	
	
	protected String formatDate(Date date) {
		if(date!=null){
			return attributeExtractorService.getPropertyFormats(AttributeExtractorMode.SEARCH).formatDate(date);
		}
		return null;
	}
}
