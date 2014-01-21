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
package fr.becpg.repo.web.scripts.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.web.scripts.WebscriptHelper;

public abstract class AbstractSearchWebScript extends AbstractWebScript {

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
		Map<String, Boolean> sortMap = WebscriptHelper.extractSortMap(sort,namespaceService);
		String term = req.getParameter(PARAM_TERM);
		String tag = req.getParameter(PARAM_TAG);
		String siteId = req.getParameter(PARAM_SITE);
		String containerId = req.getParameter(PARAM_CONTAINER);
		String repo = req.getParameter(PARAM_REPOSITORY);
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		String searchQuery = null;
		
		String nodeRef = req.getParameter(PARAM_NODEREF);
		if(nodeRef!=null && !nodeRef.isEmpty()){
			searchQuery += LuceneHelper.DEFAULT_IGNORE_QUERY
					+ " -TYPE:\"bcpg:entityListItem\"";
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
	
		String language = SearchService.LANGUAGE_FTS_ALFRESCO;
		if(searchQuery==null){
			searchQuery = advSearchService.getSearchQueryByProperties(datatype, term, tag, isRepo, siteId, containerId);
		} else {
			language = SearchService.LANGUAGE_LUCENE;
		}


		return advSearchService.queryAdvSearch(searchQuery, language, datatype, criteriaMap, sortMap, maxResults!=null ? maxResults : RepoConsts.MAX_RESULTS_256);

	}

	

	private String getPath(String nodeRef) {
		return nodeService.getPath(new NodeRef(nodeRef)).toPrefixString(namespaceService);
	}

}
