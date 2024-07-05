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
package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rating.RatingService;
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

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.EntityListState;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.repo.search.AdvSearchService;

/**
 * <p>Abstract AbstractDataListExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractDataListExtractor implements DataListExtractor {

	protected NodeService nodeService;

	protected ServiceRegistry services;

	protected AttributeExtractorService attributeExtractorService;

	protected PermissionService permissionService;

	protected AdvSearchService advSearchService;

	protected DataListExtractorFactory dataListExtractorFactory;

	protected EntityDictionaryService entityDictionaryService;
	
	protected RatingService ratingService;
	
	private BeCPGLicenseManager beCPGLicenseManager;
	
	private boolean isDefaultExtractor = false;
	
	private int priority = 0;

	/** {@inheritDoc} */
	@Override
	public boolean isDefaultExtractor() {
		return isDefaultExtractor;
	}
	
	public void setBeCPGLicenseManager(BeCPGLicenseManager beCPGLicenseManager) {
		this.beCPGLicenseManager = beCPGLicenseManager;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>setDefaultExtractor.</p>
	 *
	 * @param isDefaultExtractor a boolean.
	 */
	public void setDefaultExtractor(boolean isDefaultExtractor) {
		this.isDefaultExtractor = isDefaultExtractor;
	}

	/**
	 * <p>Setter for the field <code>dataListExtractorFactory</code>.</p>
	 *
	 * @param dataListExtractorFactory a {@link fr.becpg.repo.entity.datalist.DataListExtractorFactory} object.
	 */
	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}

	/**
	 * <p>Setter for the field <code>advSearchService</code>.</p>
	 *
	 * @param advSearchService a {@link fr.becpg.repo.search.AdvSearchService} object.
	 */
	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>services</code>.</p>
	 *
	 * @param services a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServices(ServiceRegistry services) {
		this.services = services;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
	
	/**
	 * <p>Setter for the field <code>ratingService</code>.</p>
	 *
	 * @param ratingService a {@link org.alfresco.service.cmr.rating.RatingService} object.
	 */
	public void setRatingService(RatingService ratingService) {
		this.ratingService = ratingService;
	}



	/** Constant <code>PROP_NODE="nodeRef"</code> */
	public static final String PROP_NODE = "nodeRef";
	/** Constant <code>PROP_TITLE="title"</code> */
	public static final String PROP_TITLE = "title";
	/** Constant <code>PROP_SHORTNAME="shortName"</code> */
	public static final String PROP_SHORTNAME = "shortName";
	/** Constant <code>PROP_MODIFIED="modifiedOn"</code> */
	public static final String PROP_MODIFIED = "modifiedOn";
	/** Constant <code>PROP_CREATED="createdOn"</code> */
	public static final String PROP_CREATED = "createdOn";
	/** Constant <code>PROP_PATH="path"</code> */
	public static final String PROP_PATH = "path";
	/** Constant <code>PROP_MODIFIER_DISPLAY="modifiedBy"</code> */
	public static final String PROP_MODIFIER_DISPLAY = "modifiedBy";
	/** Constant <code>PROP_CREATOR_DISPLAY="createdBy"</code> */
	public static final String PROP_CREATOR_DISPLAY = "createdBy";
	/** Constant <code>PROP_NODEDATA="itemData"</code> */
	public static final String PROP_NODEDATA = "itemData";
	/** Constant <code>PROP_PERMISSIONS="permissions"</code> */
	public static final String PROP_PERMISSIONS = "permissions";
	/** Constant <code>PROP_ACCESSRIGHT="accessRight"</code> */
	public static final String PROP_ACCESSRIGHT = "accessRight";
	/** Constant <code>PROP_SITE="site"</code> */
	public static final String PROP_SITE = "site";
	@Deprecated
	/** Constant <code>PROP_SITE_ID="siteId"</code> */
	public static final String PROP_SITE_ID = "siteId";
	/** Constant <code>PROP_CONTAINER="container"</code> */
	public static final String PROP_CONTAINER = "container";
	/** Constant <code>PROP_TYPE="itemType"</code> */
	public static final String PROP_TYPE = "itemType";
	/** Constant <code>PROP_VERSION="version"</code> */
	public static final String PROP_VERSION = "version";
	/** Constant <code>PROP_COLOR="color"</code> */
	public static final String PROP_COLOR = "color";
	/** Constant <code>PROP_USERACCESS="userAccess"</code> */
	public static final String PROP_USERACCESS = "userAccess";

	private static final Log logger = LogFactory.getLog(AbstractDataListExtractor.class);

	/**
	 * <p>init.</p>
	 */
	public void init() {
		dataListExtractorFactory.registerExtractor(this);
	}

	/**
	 * <p>extractJSON.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param props a {@link java.util.Map} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, Object> extractJSON(final NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			if (cache.containsKey(nodeRef)) {
				return cache.get(nodeRef);
			}

			QName itemType = nodeService.getType(nodeRef);
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

			Map<String, Object> ret = new HashMap<>(20);

			ret.put(PROP_NODE, nodeRef.toString());

			if (properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL) != null && !((String)properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL)).isBlank()) {
				ret.put(PROP_VERSION, properties.get(BeCPGModel.PROP_MANUAL_VERSION_LABEL));
			} else if(nodeService.hasAspect(nodeRef,BeCPGModel.ASPECT_COMPOSITE_VERSION)){
				ret.put(PROP_VERSION, properties.get(BeCPGModel.PROP_VERSION_LABEL));
			} else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {
				ret.put(PROP_VERSION, properties.get(ContentModel.PROP_VERSION_LABEL));
			}

			ret.put(PROP_TYPE, itemType.toPrefixString(services.getNamespaceService()));
			ret.put(PROP_CREATED, convertDateValue(properties.get(ContentModel.PROP_CREATED), FormatMode.JSON));
			ret.put(PROP_CREATOR_DISPLAY, extractPerson((String) properties.get(ContentModel.PROP_CREATOR)));
			ret.put(PROP_MODIFIED, convertDateValue(properties.get(ContentModel.PROP_MODIFIED), FormatMode.JSON));
			ret.put(PROP_MODIFIER_DISPLAY, extractPerson((String) properties.get(ContentModel.PROP_MODIFIER)));

			if (properties.get(BeCPGModel.PROP_COLOR) != null) {
				ret.put(PROP_COLOR, properties.get(BeCPGModel.PROP_COLOR));
			}

			Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
			Map<String, Boolean> userAccess = new HashMap<>(5);

			boolean accessRight = (Boolean) (props.get(PROP_ACCESSRIGHT) != null ? props.get(PROP_ACCESSRIGHT) : false);
			boolean isLocked = isLocked(nodeRef);
			boolean hasWrite = hasWriteAccess(nodeRef);
			boolean hasRead = hasReadAccess(nodeRef);
			boolean isLockAvailable = isLockAvailable(itemType);
			
			permissions.put(PROP_USERACCESS, userAccess);
			userAccess.put("delete", accessRight && !isLocked && (permissionService.hasPermission(nodeRef, "Delete") == AccessStatus.ALLOWED));
			userAccess.put("create", accessRight && hasWrite && !isLocked && (permissionService.hasPermission(nodeRef, "CreateChildren") == AccessStatus.ALLOWED));
			userAccess.put("edit", accessRight && hasWrite && !isLocked);
			userAccess.put("sort", accessRight && hasWrite && !isLocked && nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_SORTABLE_LIST));
			userAccess.put("details", hasRead && isDetailable(nodeRef));
			userAccess.put("lock", hasWrite && isLockAvailable && !isLocked);
			userAccess.put("unlock", hasWrite && isLockAvailable && isLocked);
			userAccess.put("wused", hasRead);
			userAccess.put("content", accessRight && hasWrite && !isLocked && hasContentField(metadataFields));
			

			ret.put(PROP_PERMISSIONS, permissions);

			ret.put(PROP_NODEDATA, doExtract(nodeRef, itemType, metadataFields, FormatMode.JSON, properties, props, cache));
			
			if(nodeService.hasAspect(nodeRef,ContentModel.ASPECT_LIKES_RATING_SCHEME_ROLLUPS)) {
				
				Map<String, Object> likes = new HashMap<>(20);
				
				likes.put("isLiked",ratingService.getRatingByCurrentUser(nodeRef, "likesRatingScheme")!=null);
				likes.put("totalLikes",ratingService.getTotalRating(nodeRef, "likesRatingScheme"));
				
				ret.put("likes", likes);
			}
			

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

	private boolean isLockAvailable(QName itemType) {
		return !BeCPGModel.TYPE_ACTIVITY_LIST.equals(itemType) && !AuthorityHelper.isCurrentUserExternal();
	}

	private boolean isLocked(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLIST_STATE)
				&& EntityListState.Valid.toString().equals(nodeService.getProperty(nodeRef, BeCPGModel.PROP_ENTITYLIST_STATE));
	}

	private boolean hasWriteAccess(NodeRef nodeRef) {
		return (permissionService.hasPermission(nodeRef, "Write") == AccessStatus.ALLOWED) && beCPGLicenseManager.hasWriteLicense();
	}
	
	private boolean hasReadAccess(NodeRef nodeRef) {
		return (permissionService.hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED);
	}

	private boolean hasContentField(List<AttributeExtractorStructure> metadataFields) {
		for(AttributeExtractorStructure metadataField : metadataFields) {
			if(ContentModel.PROP_CONTENT.equals(metadataField.getFieldQname())) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * <p>extractExport.</p>
	 *
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param props a {@link java.util.Map} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, Object> extractExport(FormatMode mode, NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields,
			Map<String, Object> props, Map<NodeRef, Map<String, Object>> cache) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			if (cache.containsKey(nodeRef)) {
				return cache.get(nodeRef);
			}

			QName itemType = nodeService.getType(nodeRef);
			Map<QName, Serializable> properties = null;

			boolean isMLAware = MLPropertyInterceptor.isMLAware();

			try {
				if (MLTextHelper.shouldExtractMLText()) {
					MLPropertyInterceptor.setMLAware(true);
				}

				properties = nodeService.getProperties(nodeRef);

			} finally {
				if (MLTextHelper.shouldExtractMLText()) {
					MLPropertyInterceptor.setMLAware(isMLAware);
				}
			}

			Map<String, Object> ret = doExtract(nodeRef, itemType, metadataFields, mode, properties, props, cache);

			cache.put(nodeRef, ret);

			return ret;

		} finally {
			if (logger.isDebugEnabled() && watch !=null) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + "s");
			}
		}
	}

	/**
	 * <p>extractPerson.</p>
	 *
	 * @param person a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, String> extractPerson(String person) {
		Map<String, String> ret = new HashMap<>(2);
		ret.put("value", person);
		ret.put("displayValue", attributeExtractorService.getPersonDisplayName(person));
		return ret;
	}

	/**
	 * <p>convertDateValue.</p>
	 *
	 * @param value a {@link java.io.Serializable} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String convertDateValue(Serializable value, FormatMode mode) {
		if (value instanceof Date) {
			return formatDate((Date) value, mode);
		}
		return null;
	}

	/**
	 * <p>formatDate.</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String formatDate(Date date, FormatMode mode) {
		if (date != null) {
			return attributeExtractorService.getPropertyFormats(mode,false).formatDate(date);
		}
		return null;
	}

	private boolean isDetailable(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * <p>doExtract.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param extraProps a {@link java.util.Map} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected abstract Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			FormatMode mode, Map<QName, Serializable> properties, Map<String, Object> extraProps,
			Map<NodeRef, Map<String, Object>> cache);

}
