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
package fr.becpg.repo.entity.datalist.data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>DataListFilter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListFilter {

	/** Constant <code>PATH_FILTER="path"</code> */
	public static final String PATH_FILTER = "path";

	/** Constant <code>NODE_PATH_FILTER="nodePath"</code> */
	public static final String NODE_PATH_FILTER = "nodePath";

	/** Constant <code>NODE_FILTER="node"</code> */
	public static final String NODE_FILTER = "node";

	/** Constant <code>ALL_FILTER="all"</code> */
	public static final String ALL_FILTER = "all";

	/** Constant <code>FORM_FILTER="filterform"</code> */
	public static final String FORM_FILTER = "filterform";

	/** Constant <code>FTS_FILTER="fts"</code> */
	public static final String FTS_FILTER = "fts";

	/** Constant <code>PROP_DEPTH_LEVEL="prop_bcpg_depthLevel"</code> */
	public static final String PROP_DEPTH_LEVEL = "prop_bcpg_depthLevel";

	private static final String VERSION_FILTER = "version";

	private String filterId = ALL_FILTER;

	private String filterParams = null;

	private String dataListName = null;

	private List<NodeRef> entityNodeRefs = null;

	private NodeRef parentNodeRef = null;

	private NodeRef nodeRef = null;

	private Map<String, String> criteriaMap = null;

	private Map<String, Boolean> sortMap = new LinkedHashMap<>();

	private QName dataType = null;

	private boolean isRepo = true;

	private boolean guessContainer = false;

	private String siteId = null;

	private String containerId = SiteService.DOCUMENT_LIBRARY;

	private String filterData = null;

	private String extraParams = null;

	private String sortId = null;

	private String format = null;

	private boolean effectiveFilterOn = false;

	DataListPagination pagination = new DataListPagination();

	private boolean hasWriteAccess = false;

	private boolean isDefaultSort = true;
	
	private static final Map<String, Boolean> DEFAULT_SORT = new LinkedHashMap<>();
	
	static {
		DEFAULT_SORT.put("@bcpg:sort", true);
		DEFAULT_SORT.put("@cm:created", true);
	}
	

	/**
	 * <p>Constructor for DataListFilter.</p>
	 */
	public DataListFilter() {
		super();
		sortMap.putAll(DEFAULT_SORT);
	}

	/**
	 * <p>Getter for the field <code>entityNodeRefs</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> getEntityNodeRefs() {
		return entityNodeRefs;
	}

	/**
	 * <p>getEntityNodeRef.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getEntityNodeRef() {
		if ((entityNodeRefs != null) && !entityNodeRefs.isEmpty()) {
			return entityNodeRefs.get(0);
		}
		return null;
	}

	/**
	 * <p>Getter for the field <code>filterId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilterId() {
		return filterId;
	}

	/**
	 * <p>Getter for the field <code>format</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * <p>Setter for the field <code>format</code>.</p>
	 *
	 * @param format a {@link java.lang.String} object.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * <p>Getter for the field <code>criteriaMap</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getCriteriaMap() {
		return criteriaMap;
	}

	/**
	 * <p>Getter for the field <code>sortMap</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, Boolean> getSortMap() {
		return sortMap;
	}

	/**
	 * <p>isDepthDefined.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDepthDefined() {
		return (criteriaMap != null) && (criteriaMap.get(PROP_DEPTH_LEVEL) != null);
	}

	/**
	 * <p>Getter for the field <code>parentNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	/**
	 * <p>Setter for the field <code>parentNodeRef</code>.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	/**
	 * <p>isGuessContainer.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isGuessContainer() {
		return guessContainer;
	}

	/**
	 * <p>isDefaultSort.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDefaultSort() {
		return isDefaultSort;
	}

	/**
	 * <p>Setter for the field <code>guessContainer</code>.</p>
	 *
	 * @param guessContainer a boolean.
	 */
	public void setGuessContainer(boolean guessContainer) {
		this.guessContainer = guessContainer;
	}

	/**
	 * <p>isRepo.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRepo() {
		return isRepo;
	}

	/**
	 * <p>setRepo.</p>
	 *
	 * @param isRepo a boolean.
	 */
	public void setRepo(boolean isRepo) {
		this.isRepo = isRepo;
	}

	/**
	 * <p>isEffectiveFilterOn.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEffectiveFilterOn() {
		return effectiveFilterOn;
	}

	/**
	 * <p>Setter for the field <code>effectiveFilterOn</code>.</p>
	 *
	 * @param effectiveFilterOn a boolean.
	 */
	public void setEffectiveFilterOn(boolean effectiveFilterOn) {
		this.effectiveFilterOn = effectiveFilterOn;
	}

	/**
	 * <p>Getter for the field <code>siteId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * <p>Setter for the field <code>siteId</code>.</p>
	 *
	 * @param siteId a {@link java.lang.String} object.
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * <p>Getter for the field <code>containerId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getContainerId() {
		return containerId;
	}

	/**
	 * <p>Setter for the field <code>containerId</code>.</p>
	 *
	 * @param containerId a {@link java.lang.String} object.
	 */
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	/**
	 * <p>Getter for the field <code>dataListName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDataListName() {
		return dataListName;
	}

	/**
	 * <p>Setter for the field <code>dataListName</code>.</p>
	 *
	 * @param dataListName a {@link java.lang.String} object.
	 */
	public void setDataListName(String dataListName) {
		this.dataListName = dataListName;
	}

	/**
	 * <p>Getter for the field <code>filterData</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilterData() {
		return filterData;
	}

	/**
	 * <p>getMaxDepth.</p>
	 *
	 * @return a int.
	 */
	public int getMaxDepth() {
		int maxLevel = 1;
		if (isDepthDefined()) {
			try {
				maxLevel = Integer.parseInt(criteriaMap.get(PROP_DEPTH_LEVEL));
			} catch (Exception e) {
				maxLevel = -1;
			}
		}
		return maxLevel;
	}

	/**
	 * <p>updateMaxDepth.</p>
	 *
	 * @param depth a int.
	 */
	public void updateMaxDepth(int depth) {
		if (criteriaMap == null) {
			criteriaMap = new HashMap<>();
		}
		if (depth != -1) {
			criteriaMap.put(PROP_DEPTH_LEVEL, Integer.toString(depth));
		} else {
			criteriaMap.put(PROP_DEPTH_LEVEL, "all");
		}
	}

	/**
	 * <p>Getter for the field <code>filterParams</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFilterParams() {
		return filterParams;
	}

	/**
	 * <p>Setter for the field <code>filterParams</code>.</p>
	 *
	 * @param filterParams a {@link java.lang.String} object.
	 */
	public void setFilterParams(String filterParams) {
		this.filterParams = filterParams;
	}

	/**
	 * <p>Setter for the field <code>filterId</code>.</p>
	 *
	 * @param filterId a {@link java.lang.String} object.
	 */
	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	/**
	 * <p>Setter for the field <code>filterData</code>.</p>
	 *
	 * @param filterData a {@link java.lang.String} object.
	 */
	public void setFilterData(String filterData) {
		this.filterData = filterData;
	}

	/**
	 * <p>Setter for the field <code>entityNodeRefs</code>.</p>
	 *
	 * @param entityNodeRefs a {@link java.util.List} object.
	 */
	public void setEntityNodeRefs(List<NodeRef> entityNodeRefs) {
		this.entityNodeRefs = entityNodeRefs;
	}

	/**
	 * <p>Setter for the field <code>criteriaMap</code>.</p>
	 *
	 * @param criteriaMap a {@link java.util.Map} object.
	 */
	public void setCriteriaMap(Map<String, String> criteriaMap) {
		this.criteriaMap = criteriaMap;
	}

	/**
	 * <p>Setter for the field <code>sortMap</code>.</p>
	 *
	 * @param sortMap a {@link java.util.Map} object.
	 */
	public void setSortMap(Map<String, Boolean> sortMap) {
		if ((sortMap != null) && !sortMap.isEmpty()) {
			this.isDefaultSort = false;
			this.sortMap = sortMap;
		}
	}

	/**
	 * <p>Getter for the field <code>sortId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSortId() {
		return sortId;
	}

	/**
	 * <p>Setter for the field <code>sortId</code>.</p>
	 *
	 * @param sortId a {@link java.lang.String} object.
	 */
	public void setSortId(String sortId) {
		this.sortId = sortId;
	}

	/**
	 * <p>Getter for the field <code>dataType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getDataType() {
		return dataType;
	}

	/**
	 * <p>Setter for the field <code>dataType</code>.</p>
	 *
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void setDataType(QName dataType) {
		this.dataType = dataType;
	}

	/**
	 * <p>getSearchQuery.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder getSearchQuery() {
		return getSearchQuery(this.parentNodeRef);
	}

	/**
	 * <p>Getter for the field <code>nodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * <p>Setter for the field <code>nodeRef</code>.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * <p>Getter for the field <code>extraParams</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExtraParams() {
		return extraParams;
	}

	/**
	 * <p>Setter for the field <code>extraParams</code>.</p>
	 *
	 * @param extraParams a {@link java.lang.String} object.
	 */
	public void setExtraParams(String extraParams) {
		this.extraParams = extraParams;
	}

	/**
	 * <p>Getter for the field <code>pagination</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.datalist.data.DataListPagination} object.
	 */
	public DataListPagination getPagination() {
		return pagination;
	}

	/**
	 * <p>hasWriteAccess.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasWriteAccess() {
		return hasWriteAccess;
	}

	/**
	 * <p>Setter for the field <code>hasWriteAccess</code>.</p>
	 *
	 * @param hasWriteAccess a boolean.
	 */
	public void setHasWriteAccess(boolean hasWriteAccess) {
		this.hasWriteAccess = hasWriteAccess;
	}

	/**
	 * <p>getSearchQuery.</p>
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.search.BeCPGQueryBuilder} object.
	 */
	public BeCPGQueryBuilder getSearchQuery(NodeRef parentNodeRef) {

		BeCPGQueryBuilder queryBuilder = createFilterQuery();

		if (!isSimpleItem()) {
			if (parentNodeRef != null) {
				queryBuilder.parent(parentNodeRef);
				if (isAllFilter()) {
					queryBuilder.inDB();
				} 
			} else {
				if (!isRepo && !DataListFilter.NODE_PATH_FILTER.equals(filterId) && !DataListFilter.PATH_FILTER.equals(filterId)) {
					queryBuilder.inSite(siteId, containerId);
				} else if (DataListFilter.PATH_FILTER.equals(filterId)) {
					queryBuilder.inPath(filterData);
				}
				queryBuilder.excludeDefaults();
				
				if("document".equals(dataListName)) {
					queryBuilder.withAspect(BeCPGModel.ASPECT_DOCUMENT_ASPECT);
				}
			}

			queryBuilder.addSort(sortMap);

			if (dataType != null && BeCPGQueryBuilder.isExcludedFromIndex(dataType)) {
				queryBuilder.inDBIfPossible();
			} 
		} else {
			// Force DB Mode
			queryBuilder.inDB();
		}

		return queryBuilder;
	}

	private BeCPGQueryBuilder createFilterQuery() {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery();

		Pattern ftsQueryPattern = Pattern.compile("fts\\((.*)\\)");

		queryBuilder.ofType(dataType);

		if (effectiveFilterOn) {

			Calendar startCal = Calendar.getInstance();

			String fromQuery = startCal.get(Calendar.YEAR) + "\\-" + (startCal.get(Calendar.MONTH) + 1) + "\\-" + startCal.get(Calendar.DAY_OF_MONTH);

			queryBuilder.andBetweenOrNull(BeCPGModel.PROP_START_EFFECTIVITY, "MIN", fromQuery);
			queryBuilder.andBetweenOrNull(BeCPGModel.PROP_END_EFFECTIVITY, fromQuery, "MAX");

		}

		if (filterId != null) {

			if (filterId.equals("recentlyAdded") || filterId.equals("recentlyModified") || filterId.equals("recentlyCreatedByMe")
					|| filterId.equals("recentlyModifiedByMe")) {
				boolean onlySelf = (filterId.indexOf("ByMe")) > 0;
				String dateField = (filterId.indexOf("Modified") > 0) ? "modified" : "created";
				String ownerField = ("created".equals(dateField)) ? "creator" : "modifier";

				// Default to 7 days - can be overridden using "days" argument
				int dayCount = 7;

				if ((filterParams != null) && filterParams.startsWith("day=")) {
					try {
						dayCount = Integer.parseInt(filterParams.replace("day=", ""));
					} catch (NumberFormatException ignored) {

					}
				}
				Calendar date = Calendar.getInstance();

				String toQuery = date.get(Calendar.YEAR) + "\\-" + (date.get(Calendar.MONTH) + 1) + "\\-" + date.get(Calendar.DAY_OF_MONTH);
				date.add(Calendar.DATE, -dayCount);
				String fromQuery = date.get(Calendar.YEAR) + "\\-" + (date.get(Calendar.MONTH) + 1) + "\\-" + date.get(Calendar.DAY_OF_MONTH);

				queryBuilder.andPropQuery(QName.createQName(ContentModel.USER_MODEL_URI, dateField),
						"[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]");

				if (onlySelf) {
					queryBuilder.andPropEquals(QName.createQName(ContentModel.USER_MODEL_URI, ownerField), getUserName());
				}

				sortMap.put("@cm:" + dateField, false);

			} else if (filterId.equals("createdByMe")) {
				queryBuilder.andPropEquals(ContentModel.PROP_CREATOR, getUserName());
			} else if (filterId.equals(NODE_FILTER)) {
				queryBuilder.andID(nodeRef);
			} else if (filterId.equals("tag")) {
				String fData = filterData;
				// Remove any trailing "/" character
				if (fData.charAt(fData.length() - 1) == '/') {
					fData = fData.substring(0, fData.length() - 2);
				}
				queryBuilder.members("/cm:taggable/cm:" + ISO9075.encode(fData));

				// beCPGQueryBuilder.andFTSQuery("TAG:" + tag);

			} else if (filterId.equals(FTS_FILTER)) {
				queryBuilder.andFTSQuery(filterData);
			} else if (filterParams != null) {
				Matcher ma = ftsQueryPattern.matcher(filterParams);
				if (ma.matches()) {
					queryBuilder.andFTSQuery(ma.group(1));
				}
			}
		}

		return queryBuilder;
	}

	/**
	 * <p>isSimpleItem.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSimpleItem() {
		return nodeRef != null;
	}

	/**
	 * <p>isAllFilter.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAllFilter() {
		return (filterId != null) && filterId.equals(ALL_FILTER) && (parentNodeRef != null) && effectiveFilterOn == false;
	}

	/**
	 * <p>isVersionFilter.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isVersionFilter() {
		return (filterId != null) && filterId.equals(VERSION_FILTER);
	}

	private String getUserName() {
		return AuthenticationUtil.getFullyAuthenticatedUser();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DataListFilter [filterId=" + filterId + ", filterParams=" + filterParams + ", dataListName=" + dataListName + ", entityNodeRefs="
				+ entityNodeRefs + ", parentNodeRef=" + parentNodeRef + ", nodeRef=" + nodeRef + ", criteriaMap=" + criteriaMap + ", sortMap="
				+ sortMap + ", dataType=" + dataType + ", isRepo=" + isRepo + ", guessContainer=" + guessContainer + ", siteId=" + siteId
				+ ", containerId=" + containerId + ", filterData=" + filterData + ", extraParams=" + extraParams + ", sortId=" + sortId + ", format="
				+ format + ", effectiveFilterOn=" + effectiveFilterOn + ", pagination=" + pagination + ", hasWriteAccess=" + hasWriteAccess + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((containerId == null) ? 0 : containerId.hashCode());
		result = (prime * result) + ((criteriaMap == null) ? 0 : criteriaMap.hashCode());
		result = (prime * result) + ((dataListName == null) ? 0 : dataListName.hashCode());
		result = (prime * result) + ((dataType == null) ? 0 : dataType.hashCode());
		result = (prime * result) + ((entityNodeRefs == null) ? 0 : entityNodeRefs.hashCode());
		result = (prime * result) + ((filterData == null) ? 0 : filterData.hashCode());
		result = (prime * result) + ((filterId == null) ? 0 : filterId.hashCode());
		result = (prime * result) + ((filterParams == null) ? 0 : filterParams.hashCode());
		result = (prime * result) + ((format == null) ? 0 : format.hashCode());
		result = (prime * result) + (isRepo ? 1231 : 1237);
		result = (prime * result) + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = (prime * result) + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		result = (prime * result) + ((siteId == null) ? 0 : siteId.hashCode());
		result = (prime * result) + ((sortId == null) ? 0 : sortId.hashCode());
		result = (prime * result) + ((sortMap == null) ? 0 : sortMap.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DataListFilter other = (DataListFilter) obj;
		if (containerId == null) {
			if (other.containerId != null) {
				return false;
			}
		} else if (!containerId.equals(other.containerId)) {
			return false;
		}
		if (criteriaMap == null) {
			if (other.criteriaMap != null) {
				return false;
			}
		} else if (!criteriaMap.equals(other.criteriaMap)) {
			return false;
		}
		if (dataListName == null) {
			if (other.dataListName != null) {
				return false;
			}
		} else if (!dataListName.equals(other.dataListName)) {
			return false;
		}
		if (dataType == null) {
			if (other.dataType != null) {
				return false;
			}
		} else if (!dataType.equals(other.dataType)) {
			return false;
		}
		if (entityNodeRefs == null) {
			if (other.entityNodeRefs != null) {
				return false;
			}
		} else if (!entityNodeRefs.equals(other.entityNodeRefs)) {
			return false;
		}
		if (filterData == null) {
			if (other.filterData != null) {
				return false;
			}
		} else if (!filterData.equals(other.filterData)) {
			return false;
		}
		if (filterId == null) {
			if (other.filterId != null) {
				return false;
			}
		} else if (!filterId.equals(other.filterId)) {
			return false;
		}
		if (filterParams == null) {
			if (other.filterParams != null) {
				return false;
			}
		} else if (!filterParams.equals(other.filterParams)) {
			return false;
		}
		if (format == null) {
			if (other.format != null) {
				return false;
			}
		} else if (!format.equals(other.format)) {
			return false;
		}
		if (isRepo != other.isRepo) {
			return false;
		}
		if (nodeRef == null) {
			if (other.nodeRef != null) {
				return false;
			}
		} else if (!nodeRef.equals(other.nodeRef)) {
			return false;
		}
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null) {
				return false;
			}
		} else if (!parentNodeRef.equals(other.parentNodeRef)) {
			return false;
		}
		if (siteId == null) {
			if (other.siteId != null) {
				return false;
			}
		} else if (!siteId.equals(other.siteId)) {
			return false;
		}
		if (sortId == null) {
			if (other.sortId != null) {
				return false;
			}
		} else if (!sortId.equals(other.sortId)) {
			return false;
		}
		if (sortMap == null) {
			if (other.sortMap != null) {
				return false;
			}
		} else if (!sortMap.equals(other.sortMap)) {
			return false;
		}
		return true;
	}

}
