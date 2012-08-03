package fr.becpg.repo.entity.datalist.data;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.json.JSONException;

import fr.becpg.model.BeCPGModel;

public class DataListFilter {

	public static final String NODE_FILTER = "node";
	
	public static final String ALL_FILTER = "all";

	public static final String FORM_FILTER = "filterform";

	private String filterQuery = null;
	
	private NodeRef entityNodeRef= null;
	
	private NodeRef dataListNodeRef= null;
	
	private NodeRef nodeRef= null;
	
	private Map<String, String> criteriaMap = null;
	
	private List<Pair<QName, Boolean>> sortProps = new LinkedList<Pair<QName, Boolean>>();
	
	private Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();
	
	private QName dataType = null;
	
	private boolean allFilter = false;
	
	
	
	public DataListFilter() {
		super();
		//TODO doublon
		sortMap.put("@bcpg:sort", true);
		sortMap.put("@cm:created", true);
		sortProps.add(new Pair<QName, Boolean>(BeCPGModel.PROP_SORT,true));
		sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED,true));
	}

	public List<Pair<QName, Boolean>> getSortProps() {
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
		this.sortMap = sortMap;
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
		return filterQuery + " +PARENT:\"" + dataListNodeRef + "\" ";
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
	

	private String getUserName() {
		return AuthenticationUtil.getFullyAuthenticatedUser();
	}

	@Override
	public String toString() {
		return "DataListFilter [filterQuery=" + filterQuery + ", entityNodeRef=" + entityNodeRef + ", dataListNodeRef=" + dataListNodeRef + ", criteriaMap=" + criteriaMap
				+ ", sortMap=" + sortMap + ", dataType=" + dataType + "]";
	}

	


	
	
	
}
