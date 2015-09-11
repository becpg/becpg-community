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
package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.extractors.AbstractNodeDataExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.AdvSearchService;

public abstract class AbstractDataListExtractor implements DataListExtractor {

	protected NodeService nodeService;

	protected ServiceRegistry services;

	protected AttributeExtractorService attributeExtractorService;

	protected PermissionService permissionService;

	protected AdvSearchService advSearchService;

	protected DataListExtractorFactory dataListExtractorFactory;

	protected EntityDictionaryService entityDictionaryService;

	private boolean isDefaultExtractor = false;

	@Override
	public boolean isDefaultExtractor() {
		return isDefaultExtractor;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setDefaultExtractor(boolean isDefaultExtractor) {
		this.isDefaultExtractor = isDefaultExtractor;
	}

	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}

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
	public static final String PROP_TITLE = "title";
	public static final String PROP_SHORTNAME = "shortName";
	public static final String PROP_MODIFIED = "modifiedOn";
	public static final String PROP_CREATED = "createdOn";
	public static final String PROP_PATH = "path";
	public static final String PROP_MODIFIER_DISPLAY = "modifiedBy";
	public static final String PROP_CREATOR_DISPLAY = "createdBy";
	public static final String PROP_NODEDATA = "itemData";
	public static final String PROP_PERMISSIONS = "permissions";
	public static final String PROP_ACCESSRIGHT = "accessRight";
	public static final String PROP_SITE = "site";
	@Deprecated
	public static final String PROP_SITE_ID = "siteId";
	public static final String PROP_CONTAINER = "container";
	public static final String PROP_TYPE = "itemType";
	public static final String PROP_VERSION = "version";
	public static final String PROP_COLOR = "color";

	private static final Log logger = LogFactory.getLog(AbstractNodeDataExtractor.class);

	public void init() {
		dataListExtractorFactory.registerExtractor(this);
	}

	public Map<String, Object> extractJSON(final NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			if (cache.containsKey(nodeRef))
				return cache.get(nodeRef);

			QName itemType = nodeService.getType(nodeRef);
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

			Map<String, Object> ret = new HashMap<>(20);

			ret.put(PROP_NODE, nodeRef);

			if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				ret.put(PROP_VERSION, properties.get(ContentModel.PROP_VERSION_LABEL));
			}

			ret.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
			ret.put(PROP_CREATED, convertDateValue(properties.get(ContentModel.PROP_CREATED), AttributeExtractorMode.JSON));
			ret.put(PROP_CREATOR_DISPLAY, extractPerson((String) properties.get(ContentModel.PROP_CREATOR)));
			ret.put(PROP_MODIFIED, convertDateValue(properties.get(ContentModel.PROP_MODIFIED), AttributeExtractorMode.JSON));
			ret.put(PROP_MODIFIER_DISPLAY, extractPerson((String) properties.get(ContentModel.PROP_MODIFIER)));

			if (properties.get(BeCPGModel.PROP_COLOR) != null) {
				ret.put(PROP_COLOR, properties.get(BeCPGModel.PROP_COLOR));
			}

			Map<String, Object> permissions = new HashMap<>(1);
			Map<String, Boolean> userAccess = new HashMap<>(5);

			boolean accessRight = (Boolean) (props.get(PROP_ACCESSRIGHT) != null ? props.get(PROP_ACCESSRIGHT) : false);

			permissions.put("userAccess", userAccess);
			userAccess.put("delete", accessRight && (permissionService.hasPermission(nodeRef, "Delete") == AccessStatus.ALLOWED));
			userAccess.put("create", accessRight && (permissionService.hasPermission(nodeRef, "CreateChildren") == AccessStatus.ALLOWED));
			userAccess.put("edit", accessRight && (permissionService.hasPermission(nodeRef, "Write") == AccessStatus.ALLOWED));
			userAccess.put("sort", accessRight && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST));
			userAccess.put("details", accessRight && isDetaillable(nodeRef));

			ret.put(PROP_PERMISSIONS, permissions);

			ret.put(PROP_NODEDATA, doExtract(nodeRef, itemType, metadataFields, AttributeExtractorMode.JSON, properties, props, cache));

			if (!entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				Path path = nodeService.getPath(nodeRef);
				String pathString = path.toPrefixString(services.getNamespaceService());
				String displayPath = path.toDisplayPath(nodeService, permissionService);

				String siteId = SiteHelper.extractSiteId(pathString);
				String containerId = SiteHelper.extractContainerId(pathString);
				String retPath = SiteHelper.extractDisplayPath(pathString, displayPath);
				if (containerId != null) {
					ret.put(PROP_CONTAINER, containerId);
				}
				ret.put(PROP_PATH, retPath);

				if (siteId != null) {

					Map<String, Object> siteData = new HashMap<>();
					siteData.put(PROP_SHORTNAME, siteId);

					SiteInfo site = services.getSiteService().getSite(siteId);
					if (site != null) {
						siteData.put(PROP_TITLE, site.getTitle());
					} else {
						siteData.put(PROP_TITLE, "#AccessDenied");
					}
					ret.put(PROP_SITE, siteData);
					ret.put(PROP_SITE_ID, siteId);

				}
			}

			cache.put(nodeRef, ret);

			return ret;

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	protected Map<String, Object> extractExport(AttributeExtractorMode mode, NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields,
			Map<String, Object> props, Map<NodeRef, Map<String, Object>> cache) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			if (cache.containsKey(nodeRef))
				return cache.get(nodeRef);

			QName itemType = nodeService.getType(nodeRef);
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

			Map<String, Object> ret = doExtract(nodeRef, itemType, metadataFields, mode, properties, props, cache);

			cache.put(nodeRef, ret);

			return ret;

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	protected Map<String, String> extractPerson(String person) {
		Map<String, String> ret = new HashMap<>(2);
		ret.put("value", person);
		ret.put("displayValue", attributeExtractorService.getPersonDisplayName(person));
		return ret;
	}

	protected String convertDateValue(Serializable value, AttributeExtractorMode mode) {
		if (value instanceof Date)
			return formatDate((Date) value, mode);
		return null;
	}

	protected String formatDate(Date date, AttributeExtractorMode mode) {
		if (date != null)
			return attributeExtractorService.getPropertyFormats(mode).formatDate(date);
		return null;
	}

	private boolean isDetaillable(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
	}

	protected abstract Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			AttributeExtractorMode mode, Map<QName, Serializable> properties, Map<String, Object> extraProps,
			Map<NodeRef, Map<String, Object>> cache);

}
