package fr.becpg.repo.web.scripts.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.search.AdvSearchService;

public abstract class AbstractSearchWebSrcipt extends AbstractWebScript {

	/** The Constant PARAM_QUERY. */
	protected static final String PARAM_QUERY = "query";

	/** The Constant PARAM_SORT. */
	protected static final String PARAM_SORT = "sort";

	/** The Constant PARAM_TERM. */
	protected static final String PARAM_TERM = "term";

	/** The Constant PARAM_TAG. */
	protected static final String PARAM_TAG = "tag";

	/** The Constant PARAM_CONTAINER. */
	protected static final String PARAM_CONTAINER = "container";

	/** The Constant PARAM_SITE. */
	protected static final String PARAM_SITE = "site";

	/** The Constant PARAM_REPOSITORY. */
	protected static final String PARAM_REPOSITORY = "repo";
	
	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_NODEREF = "nodeRef";

	protected static final String PARAM_ITEMTYPE = "itemType";
	
	/** Pagination **/

	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";


	protected static final String PARAM_MAX_RESULTS = "maxResults";

	/** Fields **/

	protected static final String PARAM_FIELDS = "metadataFields";

	/** Services **/

	protected NodeService nodeService;
	
	protected AdvSearchService advSearchService;

	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	protected NamespaceService namespaceService;

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}



	protected Map<String, Boolean> extractSortMap(String sort) {

		Map<String, Boolean> sortMap = new HashMap<String, Boolean>();
		if (sort != null && sort.length() != 0) {
			boolean asc = true;
			int separator = sort.indexOf("|");
			if (separator != -1) {
				asc = (sort.substring(separator + 1) == "true");
				sort = sort.substring(0, separator);				
			}
			String column;
			if (sort.charAt(0) == '.') {
				// handle pseudo cm:content fields
				column = "@{http://www.alfresco.org/model/content/1.0}content" + sort;
			} else if (sort.indexOf(":") != -1) {
				// handle attribute field sort
				column = "@" +  QName.createQName(sort, namespaceService).toString();
			} else {
				// other sort types e.g. TYPE
				column = sort;
			}
			sortMap.put(column, asc);
		}

		return sortMap;

	}
	

	@SuppressWarnings("unchecked")
	protected Map<String, String> extractCriteria(JSONObject jsonObject) throws JSONException {

		Map<String, String> criteriaMap = new HashMap<String, String>();

		Iterator<String> iterator = jsonObject.keys();

		while (iterator.hasNext()) {

			String key = (String) iterator.next();
			String value = jsonObject.getString(key);
			criteriaMap.put(key, value);
		}

		return criteriaMap;

	}

	protected List<NodeRef> doSearch(WebScriptRequest req, Integer maxResults) throws JSONException {

		String query = req.getParameter(PARAM_QUERY);
		String sort = req.getParameter(PARAM_SORT);
		Map<String, Boolean> sortMap = extractSortMap(sort);
		String term = req.getParameter(PARAM_TERM);
		String tag = req.getParameter(PARAM_TAG);
		String siteId = req.getParameter(PARAM_SITE);
		String containerId = req.getParameter(PARAM_CONTAINER);
		String repo = req.getParameter(PARAM_REPOSITORY);
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		String searchQuery = null;
		
		String nodeRef = req.getParameter(PARAM_NODEREF);
		if(nodeRef!=null && !nodeRef.isEmpty()){
			searchQuery += " -TYPE:\"cm:systemfolder\""
					+ " -@cm\\:lockType:READ_ONLY_LOCK"
					+ " -ASPECT:\"bcpg:compositeVersion\" AND -ASPECT:\"ecm:simulationEntityAspect\"";
			searchQuery += " +PATH:\"" + getPath(nodeRef) + "//*\"";
			if (itemType != null && !itemType.isEmpty()) {
				searchQuery += " +TYPE:\"" + itemType + "\"";
			}
		}
		
		QName datatype = null;
		Map<String, String> criteriaMap = null;
		
		boolean isRepo = false;
		if (repo != null && repo.equals("true")) {
			isRepo = true;
		}

		if (query != null && !query.isEmpty()) {
		
			JSONObject jsonObject = new JSONObject(query);
			criteriaMap = extractCriteria(jsonObject);
			datatype =  QName.createQName(jsonObject.getString("datatype"), namespaceService);
			
		}
	
		


		return advSearchService.queryAdvSearch(searchQuery, datatype, term, tag, criteriaMap, isRepo, siteId, containerId,
				sortMap, maxResults!=null ? maxResults : -1);

	}


	private String getPath(String nodeRef) {
		return nodeService.getPath(new NodeRef(nodeRef)).toPrefixString(namespaceService);
	}

}
