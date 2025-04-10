/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.FormatMode;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;

/**
 * <p>Abstract AbstractNodeDataExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractNodeDataExtractor implements NodeDataExtractor {

	/**
	 * The node service for node operations.
	 */
	protected final NodeService nodeService;

	/**
	 * The service registry for accessing services.
	 */
	protected final ServiceRegistry services;

	/**
	 * The attribute extractor service for attribute operations.
	 */
	protected final AttributeExtractorService attributeExtractorService;

	/** Constant <code>PROP_NODEREF="nodeRef"</code> */
	protected static final String PROP_NODEREF = "nodeRef";
	/** Constant <code>PROP_TAGS="tags"</code> */
	protected static final String PROP_TAGS = "tags";
	/** Constant <code>PROP_DISPLAYNAME="displayName"</code> */
	protected static final String PROP_DISPLAYNAME = "displayName";
	/** Constant <code>PROP_NAME="name"</code> */
	protected static final String PROP_NAME = "name";
	/** Constant <code>PROP_TITLE="title"</code> */
	protected static final String PROP_TITLE = "title";
	/** Constant <code>PROP_DESCRIPTION="description"</code> */
	protected static final String PROP_DESCRIPTION = "description";
	/** Constant <code>PROP_MODIFIER="modifiedByUser"</code> */
	protected static final String PROP_MODIFIER = "modifiedByUser";
	/** Constant <code>PROP_MODIFIED="modifiedOn"</code> */
	protected static final String PROP_MODIFIED = "modifiedOn";
	/** Constant <code>PROP_CREATED="createdOn"</code> */
	protected static final String PROP_CREATED = "createdOn";
	/** Constant <code>PROP_CREATOR="createdByUser"</code> */
	protected static final String PROP_CREATOR = "createdByUser";
	/** Constant <code>PROP_PATH="path"</code> */
	protected static final String PROP_PATH = "path";
	/** Constant <code>PROP_MODIFIER_DISPLAY="modifiedBy"</code> */
	protected static final String PROP_MODIFIER_DISPLAY = "modifiedBy";
	/** Constant <code>PROP_CREATOR_DISPLAY="createdBy"</code> */
	protected static final String PROP_CREATOR_DISPLAY = "createdBy";
	/** Constant <code>PROP_NODEDATA="itemData"</code> */
	protected static final String PROP_NODEDATA = "itemData";
	/** Constant <code>PROP_TYPE="type"</code> */
	protected static final String PROP_TYPE = "type";
	/** Constant <code>PROP_SIZE="size"</code> */
	protected static final String PROP_SIZE = "size";
	/** Constant <code>PROP_ITEMTYPE="itemType"</code> */
	protected static final String PROP_ITEMTYPE = "itemType";
	/** Constant <code>PROP_SHORTNAME="shortName"</code> */
	protected static final String PROP_SHORTNAME = "shortName";
	/** Constant <code>PROP_CONTAINER="container"</code> */
	protected static final String PROP_CONTAINER = "container";
	/** Constant <code>PROP_SITE="site"</code> */
	protected static final String PROP_SITE = "site";
	/** Constant <code>PROP_ASPECTS="aspects"</code> */
	protected static final String PROP_ASPECTS = "aspects";
	/** Constant <code>PROP_METADATA="metadata"</code> */
	protected static final String PROP_METADATA = "metadata";
	/** Constant <code>PROP_PERMISSIONS="permissions"</code> */
	protected static final String PROP_PERMISSIONS = "permissions";

	/**
	 * <p>Constructor for AbstractNodeDataExtractor.</p>
	 *
	 * @param services a {@link org.alfresco.service.ServiceRegistry} object.
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public AbstractNodeDataExtractor(ServiceRegistry services, AttributeExtractorService attributeExtractorService) {
		super();
		this.attributeExtractorService = attributeExtractorService;
		this.services = services;
		this.nodeService = services.getNodeService();
	}

	private static final Log logger = LogFactory.getLog(AbstractNodeDataExtractor.class);

	/** {@inheritDoc} */
	@Override
	public Map<String, Object> extract(final NodeRef nodeRef) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			Path path = nodeService.getPath(nodeRef);
			String pathString = path.toPrefixString(services.getNamespaceService());
			String displayPath = path.toDisplayPath(nodeService, services.getPermissionService());

			String siteId = SiteHelper.extractSiteId(pathString);
			String containerId = SiteHelper.extractContainerId(pathString);
			String retPath = SiteHelper.extractDisplayPath(pathString, displayPath);

			QName itemType = nodeService.getType(nodeRef);
			SiteService siteService = services.getSiteService();

			SiteInfo site = null;
			if (siteId != null) {
				site = siteService.getSite(siteId);
			}
			// Keep order
			Map<String, Object> ret = doExtract(nodeRef, itemType, site);

			ret.put(PROP_ITEMTYPE, itemType.toPrefixString(services.getNamespaceService()));
			ret.put(PROP_MODIFIER_DISPLAY, attributeExtractorService.getPersonDisplayName((String) ret.get(PROP_MODIFIER)));
			ret.put(PROP_CREATOR_DISPLAY, attributeExtractorService.getPersonDisplayName((String) ret.get(PROP_CREATOR)));
			if (containerId != null) {
				ret.put(PROP_CONTAINER, containerId);
			}
			ret.put(PROP_PATH, retPath);

			if (site != null) {
				Map<String, Object> siteData = new HashMap<>();
				siteData.put(PROP_SHORTNAME, siteId);
				siteData.put(PROP_TITLE, site.getTitle());
				ret.put(PROP_SITE, siteData);
			}

			Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
			Map<String, Boolean> userAccess = new HashMap<>(5);

			boolean hasWrite = (services.getPermissionService().hasPermission(nodeRef, "Write") == AccessStatus.ALLOWED);

			permissions.put("userAccess", userAccess);
			userAccess.put("delete", (services.getPermissionService().hasPermission(nodeRef, "Delete") == AccessStatus.ALLOWED));
			userAccess.put("create", (services.getPermissionService().hasPermission(nodeRef, "CreateChildren") == AccessStatus.ALLOWED));
			userAccess.put("edit", hasWrite);

			ret.put(PROP_PERMISSIONS, permissions);

			return ret;

		} finally {
			if (logger.isDebugEnabled()) {
				assert watch != null;
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	/**
	 * <p>doExtract.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param site a {@link org.alfresco.service.cmr.site.SiteInfo} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected abstract Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, SiteInfo site);

	/**
	 * <p>getParent.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getParent(NodeRef nodeRef) {
		return nodeService.getPrimaryParent(nodeRef).getParentRef();
	}

	/**
	 * <p>getSize.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getSize(NodeRef nodeRef) {
		return getSize((ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
	}

	/**
	 * <p>getSize.</p>
	 *
	 * @param contentData a {@link org.alfresco.service.cmr.repository.ContentData} object.
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getSize(ContentData contentData) {
		if (contentData == null) {
			return null;
		}
		return contentData.getSize();
	}

	/**
	 * <p>convertDateValue.</p>
	 *
	 * @param value a {@link java.io.Serializable} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String convertDateValue(Serializable value) {
		if (value instanceof Date) {
			return formatDate((Date) value);
		}
		return null;
	}

	/**
	 * <p>formatDate.</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String formatDate(Date date) {
		if (date != null) {
			return attributeExtractorService.getPropertyFormats(FormatMode.SEARCH, false).formatDate(date);
		}
		return null;
	}
}
