package fr.becpg.repo.entity.datalist.data;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.json.JSONException;

public class DataListFilter {

	public static final String NODE_FILTER = "node";
	
	public static final String ALL_FILTER = "all";

	public static final String FORM_FILTER = "filterform";

	private String filterQuery = null;
	
	private NodeRef entityNodeRef= null;
	
	private NodeRef dataListNodeRef= null;
	
	private NodeRef nodeRef= null;
	
	private Map<String, String> criteriaMap = null;
	
	private Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
	
	private QName dataType = null;
	
	private boolean allFilter = false;
	
	
	
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

	public NodeRef getDataListNodeRef() {
		return dataListNodeRef;
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

	public void setDataListNodeRef(NodeRef dataListNodeRef) {
		this.dataListNodeRef = dataListNodeRef;
	}

	public void setCriteriaMap(Map<String, String> criteriaMap) {
		this.criteriaMap = criteriaMap;
	}

	public void setSortMap(Map<String, Boolean> sortMap) {
		if(sortMap!=null && sortMap.size()>0){
			this.sortMap = sortMap;
		}
	}

	public QName getDataType() {
		return dataType;
	}

	public void setDataType(QName dataType) {
		this.dataType = dataType;
	}
	
	public String getSearchQuery (){
		return getSearchQuery(this.dataListNodeRef);
	}
	
	
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getSearchQuery(NodeRef dataListNodeRef) {
		return filterQuery + (dataListNodeRef!=null ? " +PARENT:\"" + dataListNodeRef + "\" ":"");
	}
	
	public boolean isSimpleItem() {
		return nodeRef!=null;
	}


	public boolean isAllFilter() {
		return allFilter && dataListNodeRef!=null;
	}

	public void buildQueryFilter( String filterId, String filterData, String argDays ) throws JSONException {
		
		

		filterQuery = " +TYPE:\"" + dataType.toString() + "\"";

		// Common types and aspects to filter from the UI
		String searchQueryDefaults = " -TYPE:\"systemfolder\""  + " -@cm\\:lockType:READ_ONLY_LOCK";

		if (filterId != null) {

			if (filterId.equals("recentlyAdded") || filterId.equals("recentlyModified") || filterId.equals("recentlyCreatedByMe") || filterId.equals("recentlyModifiedByMe")) {
				boolean onlySelf = (filterId.indexOf("ByMe")) > 0 ? true : false;
				String dateField = (filterId.indexOf("Modified") > 0) ? "modified" : "created";
				String ownerField = (dateField == "created") ? "creator" : "modifier";

				// Default to 7 days - can be overridden using "days" argument
				int dayCount = 7;
				if (argDays != null) {
					try {
						dayCount = Integer.parseInt(argDays);
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
			}
		}

		 filterQuery += searchQueryDefaults;

	}
	
//TODO	
//	        "Alfresco.component.PrioriryFilter"
//			"Alfresco.component.DueFilter"
//			"Alfresco.component.AllFilter"
//			"Alfresco.component.StartedFilter"
//	   <filters-parameters>
//	      <!--
//	         Turns the filters form the filter's config files into url parameters by matching the filter id and data against
//	         the filter patterns below. A wildcard ("*") matches any value as long as it exists and isn't empty.
//	         The parameters will later be added to the end of the base repo webscript url used to retrieve the values.
//
//	         Note that it is possible to create dynamic values by using the following keys inside "{}":
//	          * {id} - resolves to the filter id value
//	          * {data} - resolveds to the filter data value
//	          * {0dt} - resolves to a iso08601 datetime representation of the current date and time
//	          * {0d} -  resolves to a iso8601 date respresentation of the current day
//	          * {-7d} -  resolves to a iso8601 date respresentation of the current day rolled the given number of days back
//	          * {+7d} -  resolves to a iso8601 date respresentation of the current day rolled the given number of days forward
//	      -->
//	      <filter id="due"           data="today"        parameters="dueAfter={-1d}&amp;dueBefore={0d}"/>
//	      <filter id="due"           data="tomorrow"     parameters="dueAfter={0d}&amp;dueBefore={1d}"/>
//	      <filter id="due"           data="next7Days"    parameters="dueAfter={0d}&amp;dueBefore={8d}"/>
//	      <filter id="due"           data="overdue"      parameters="dueBefore={-1d}"/>
//	      <filter id="due"           data="noDate"       parameters="dueBefore=null"/>
//	      <filter id="started"       data="last7Days"    parameters="startedAfter={-7d}"/>
//	      <filter id="started"       data="last14Days"   parameters="startedAfter={-14d}"/>
//	      <filter id="started"       data="last28Days"   parameters="startedAfter={-28d}"/>
//	      <filter id="priority"      data="*"            parameters="priority={data}"/>
//	      <filter id="workflowType"  data="*"            parameters="definitionName={data}"/>
//	   </filters-parameters>
	
	

	private String getUserName() {
		return AuthenticationUtil.getFullyAuthenticatedUser();
	}

	@Override
	public String toString() {
		return "DataListFilter [filterQuery=" + filterQuery + ", entityNodeRef=" + entityNodeRef + ", dataListNodeRef=" + dataListNodeRef + ", criteriaMap=" + criteriaMap
				+ ", sortMap=" + sortMap + ", dataType=" + dataType + "]";
	}

	


	
	
	
}
