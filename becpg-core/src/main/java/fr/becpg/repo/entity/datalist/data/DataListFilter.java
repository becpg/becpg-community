/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.json.JSONException;

import fr.becpg.repo.helper.LuceneHelper;

public class DataListFilter {

	public static final String NODE_FILTER = "node";
	
	public static final String ALL_FILTER = "all";

	public static final String FORM_FILTER = "filterform";

	public static final String FTS_FILTER = "fts";
	
	public static final String PROP_DEPTH_LEVEL = "prop_bcpg_depthLevel";

	private static final String VERSION_FILTER = "version";
	
	private String filterId = ALL_FILTER;
	
	private String filterQuery = null;
	
	private String dataListName = null; 
	
	private List<NodeRef> entityNodeRefs= null;
	
	private NodeRef parentNodeRef= null;
	
	private NodeRef nodeRef= null;
	
	private Map<String, String> criteriaMap = null;
	
	private Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
	
	private QName dataType = null;
	
	private boolean isRepo = true;
	
	private String siteId = null;
	
	private String containerId = SiteService.DOCUMENT_LIBRARY;
	
	private boolean allFilter = false;
	
	private String filterData = null;
	
	private String sortId = null;
	
	private String format = null;
	
	public DataListFilter() {
		super();
		sortMap.put("@bcpg:sort", true);
		sortMap.put("@cm:created", true);
	}

	public List<Pair<QName, Boolean>> getSortProps(NamespaceService namespaceService) {

		List<Pair<QName, Boolean>> sortProps = new LinkedList<Pair<QName, Boolean>>();
		
		for(Map.Entry<String, Boolean> entry : sortMap.entrySet()){
			
			sortProps.add(new Pair<QName, Boolean>(QName.createQName(entry.getKey().replace("@",""), namespaceService),entry.getValue()));
		}

		return sortProps;
	}
	
	
	public String getFilterQuery() {
		return filterQuery;
	}

	public List<NodeRef> getEntityNodeRefs() {
		if(isVersionFilter()) {
			return Arrays.asList(new NodeRef(filterData));
		}
		return entityNodeRefs;
	}

	public String getFilterId() {
		return filterId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Map<String, String> getCriteriaMap() {
		return criteriaMap;
	}

	public Map<String, Boolean> getSortMap() {
		return sortMap;
	}

	public boolean isDepthDefined(){
		return criteriaMap!=null &&
				criteriaMap.get(PROP_DEPTH_LEVEL)!=null;
	}
	
	
	public NodeRef getParentNodeRef() {
		return parentNodeRef;
	}

	public void setParentNodeRef(NodeRef parentNodeRef) {
		this.parentNodeRef = parentNodeRef;
	}

	public boolean isRepo() {
		return isRepo;
	}

	public void setRepo(boolean isRepo) {
		this.isRepo = isRepo;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	public String getContainerId() {
		return containerId;
	}
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	

	public String getDataListName() {
		return dataListName;
	}

	public void setDataListName(String dataListName) {
		this.dataListName = dataListName;
	}

	public String getFilterData() {
		return filterData;
	}

	public int getMaxDepth() {
		int maxLevel = 1;
		if(isDepthDefined()){
			try {
				maxLevel = Integer.parseInt(criteriaMap.get("prop_bcpg_depthLevel"));
			} catch (Exception e) {
				maxLevel = -1;
			}
		}
		return maxLevel;
	}

	public void setFilterQuery(String filterQuery) {
		this.filterQuery = filterQuery;
	}

	public void setEntityNodeRefs(List<NodeRef> entityNodeRefs) {
		this.entityNodeRefs = entityNodeRefs;
	}

	public void setCriteriaMap(Map<String, String> criteriaMap) {
		this.criteriaMap = criteriaMap;
	}

	public void setSortMap(Map<String, Boolean> sortMap) {
		if(sortMap!=null && !sortMap.isEmpty()){
			this.sortMap = sortMap;
		}
	}

	public String getSortId() {
		return sortId;
	}

	public void setSortId(String sortId) {
		this.sortId = sortId;
	}

	public QName getDataType() {
		return dataType;
	}

	public void setDataType(QName dataType) {
		this.dataType = dataType;
	}
	
	public String getSearchQuery (){
		return getSearchQuery(this.parentNodeRef);
	}
	
	
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getSearchQuery(NodeRef parentNodeRef) {
		String searchQuery = filterQuery + (parentNodeRef!=null ? " +PARENT:\"" + parentNodeRef + "\" ":"");
		
		
		if (!isRepo && parentNodeRef==null) {
			searchQuery = 	LuceneHelper.mandatory(LuceneHelper.getSiteSearchPath( siteId, containerId))+ " AND ("+searchQuery+")";
		}
		return searchQuery;
	}
	
	public boolean isSimpleItem() {
		return nodeRef!=null;
	}


	public boolean isAllFilter() {
		return allFilter && parentNodeRef!=null;
	}

	public boolean isVersionFilter() {
		return filterId!=null && filterId.equals(VERSION_FILTER);
	}
	
	public void buildQueryFilter( String filterId, String filterData, String params ) throws JSONException {
		
		Pattern ftsQueryPattern = Pattern.compile("fts\\((.*)\\)");

		filterQuery = LuceneHelper.mandatory(LuceneHelper.getCondType(dataType));

		// Common types and aspects to filter from the UI
		String searchQueryDefaults = LuceneHelper.DEFAULT_IGNORE_QUERY; 

		
		
		if (filterId != null) {
			
			this.filterId = filterId;
			this.filterData = filterData;

			if (filterId.equals("recentlyAdded") || filterId.equals("recentlyModified") || filterId.equals("recentlyCreatedByMe") || filterId.equals("recentlyModifiedByMe")) {
				boolean onlySelf = (filterId.indexOf("ByMe")) > 0 ? true : false;
				String dateField = (filterId.indexOf("Modified") > 0) ? "modified" : "created";
				String ownerField = (dateField == "created") ? "creator" : "modifier";

				// Default to 7 days - can be overridden using "days" argument
				int dayCount = 7;
				
				if (params != null && params.startsWith("day=")) {
					try {
						dayCount = Integer.parseInt(params.replace("day=", ""));
					} catch (NumberFormatException e) {

					}
				}
				Calendar date = Calendar.getInstance();
				String toQuery = date.get(Calendar.YEAR) + "\\-" + (date.get(Calendar.MONTH) + 1) + "\\-" + date.get(Calendar.DAY_OF_MONTH);
				date.add(Calendar.DATE, -dayCount);
				String fromQuery = date.get(Calendar.YEAR) + "\\-" + (date.get(Calendar.MONTH) + 1) + "\\-" + date.get(Calendar.DAY_OF_MONTH);

				filterQuery += " +@cm\\:" + dateField + ":[" + fromQuery + "T00\\:00\\:00.000 TO " + toQuery + "T23\\:59\\:59.999]";
				if (onlySelf) {
					filterQuery += " +@cm\\:" + ownerField + ":\"" + getUserName() + '"';
				}
				filterQuery += " -TYPE:\"folder\"";

				sortMap.put("@cm:" + dateField, false);

			} else if (filterId.equals("createdByMe")) {
				filterQuery += " +@cm\\:creator:\"" + getUserName() + '"';
				filterQuery += " -TYPE:\"folder\"";
			} else if (filterId.equals(NODE_FILTER)) {
				filterQuery = "+ID:\"" + nodeRef + "\"";
			} else if (filterId.equals("tag")) {
				// Remove any trailing "/" character
				if (filterData.charAt(filterData.length() - 1) == '/') {
					filterData = filterData.substring(0, filterData.length() - 2);
				}
				filterQuery += "+PATH:\"/cm:taggable/cm:" + ISO9075.encode(filterData) + "/member\"";
			}  else if (filterId.equals(ALL_FILTER)) {
				allFilter = true;
			} else if(filterId.equals(FTS_FILTER)){
				filterQuery += " "+filterData;
			} else if(params!=null) {
				Matcher ma = ftsQueryPattern.matcher(params);
				if(ma.matches()){
					filterQuery += " "+ma.group(1);
				}
			}
		}

		 filterQuery += searchQueryDefaults;

	}
	
	

	private String getUserName() {
		return AuthenticationUtil.getFullyAuthenticatedUser();
	}

	@Override
	public String toString() {
		return "DataListFilter [filterId=" + filterId + ", filterQuery=" + filterQuery + ", dataListName=" + dataListName + ", entityNodeRefs=" + entityNodeRefs
				+ ", parentNodeRef=" + parentNodeRef + ", nodeRef=" + nodeRef + ", criteriaMap=" + criteriaMap + ", sortMap=" + sortMap + ", dataType=" + dataType + ", isRepo="
				+ isRepo + ", siteId=" + siteId + ", containerId=" + containerId + ", allFilter=" + allFilter + ", filterData=" + filterData + ", sortId=" + sortId + ", format="
				+ format + "]";
	}


	


	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allFilter ? 1231 : 1237);
		result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
		result = prime * result + ((criteriaMap == null) ? 0 : criteriaMap.hashCode());
		result = prime * result + ((dataListName == null) ? 0 : dataListName.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((entityNodeRefs == null) ? 0 : entityNodeRefs.hashCode());
		result = prime * result + ((filterData == null) ? 0 : filterData.hashCode());
		result = prime * result + ((filterId == null) ? 0 : filterId.hashCode());
		result = prime * result + ((filterQuery == null) ? 0 : filterQuery.hashCode());
		result = prime * result + ((format == null) ? 0 : format.hashCode());
		result = prime * result + (isRepo ? 1231 : 1237);
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
		result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
		result = prime * result + ((sortId == null) ? 0 : sortId.hashCode());
		result = prime * result + ((sortMap == null) ? 0 : sortMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataListFilter other = (DataListFilter) obj;
		if (allFilter != other.allFilter)
			return false;
		if (containerId == null) {
			if (other.containerId != null)
				return false;
		} else if (!containerId.equals(other.containerId))
			return false;
		if (criteriaMap == null) {
			if (other.criteriaMap != null)
				return false;
		} else if (!criteriaMap.equals(other.criteriaMap))
			return false;
		if (dataListName == null) {
			if (other.dataListName != null)
				return false;
		} else if (!dataListName.equals(other.dataListName))
			return false;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (entityNodeRefs == null) {
			if (other.entityNodeRefs != null)
				return false;
		} else if (!entityNodeRefs.equals(other.entityNodeRefs))
			return false;
		if (filterData == null) {
			if (other.filterData != null)
				return false;
		} else if (!filterData.equals(other.filterData))
			return false;
		if (filterId == null) {
			if (other.filterId != null)
				return false;
		} else if (!filterId.equals(other.filterId))
			return false;
		if (filterQuery == null) {
			if (other.filterQuery != null)
				return false;
		} else if (!filterQuery.equals(other.filterQuery))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		if (isRepo != other.isRepo)
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (parentNodeRef == null) {
			if (other.parentNodeRef != null)
				return false;
		} else if (!parentNodeRef.equals(other.parentNodeRef))
			return false;
		if (siteId == null) {
			if (other.siteId != null)
				return false;
		} else if (!siteId.equals(other.siteId))
			return false;
		if (sortId == null) {
			if (other.sortId != null)
				return false;
		} else if (!sortId.equals(other.sortId))
			return false;
		if (sortMap == null) {
			if (other.sortMap != null)
				return false;
		} else if (!sortMap.equals(other.sortMap))
			return false;
		return true;
	}

	


	
	
	
}
