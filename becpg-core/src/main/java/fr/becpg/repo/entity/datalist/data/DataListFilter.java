package fr.becpg.repo.entity.datalist.data;

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

	private String filterQuery = null;
	
	private NodeRef entityNodeRef= null;
	
	private NodeRef parentNodeRef= null;
	
	private NodeRef nodeRef= null;
	
	private Map<String, String> criteriaMap = null;
	
	private Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
	
	private QName dataType = null;
	
	private boolean isRepo = true;
	
	private String siteId = null;
	
	private String containerId = SiteService.DOCUMENT_LIBRARY;
	
	private boolean allFilter = false;
	
	private String filterId = ALL_FILTER;
	
	private String sortId = null;
	
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

	public NodeRef getEntityNodeRef() {
		return entityNodeRef;
	}

	public String getFilterId() {
		return filterId;
	}

	public Map<String, String> getCriteriaMap() {
		return criteriaMap;
	}

	public Map<String, Boolean> getSortMap() {
		return sortMap;
	}

	public boolean isDepthDefined(){
		return criteriaMap!=null &&
				criteriaMap.get("prop_bcpg_depthLevel")!=null;
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

	public void setEntityNodeRef(NodeRef entityNodeRef) {
		this.entityNodeRef = entityNodeRef;
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

	public void buildQueryFilter( String filterId, String filterData, String params ) throws JSONException {
		
		Pattern ftsQueryPattern = Pattern.compile("fts\\((.*)\\)");

		filterQuery = LuceneHelper.mandatory(LuceneHelper.getCondType(dataType));

		// Common types and aspects to filter from the UI
		String searchQueryDefaults = LuceneHelper.DEFAULT_IGNORE_QUERY; 

		
		
		if (filterId != null) {
			
			this.filterId = filterId;

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
		return "DataListFilter [filterQuery=" + filterQuery + ", entityNodeRef=" + entityNodeRef + ", parentNodeRef=" + parentNodeRef + ", nodeRef=" + nodeRef + ", criteriaMap="
				+ criteriaMap + ", sortMap=" + sortMap + ", dataType=" + dataType + ", isRepo=" + isRepo + ", siteId=" + siteId + ", containerId=" + containerId + ", allFilter="
				+ allFilter + "]";
	}


	

	


	
	
	
}
