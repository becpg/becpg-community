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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.JSONHelper;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.web.scripts.WebscriptHelper;

public abstract class AbstractSearchWebScript extends AbstractWebScript {

	protected static final String PARAM_QUERY = "query";

	protected static final String PARAM_SORT = "sort";

	protected static final String PARAM_TERM = "term";

	protected static final String PARAM_TAG = "tag";

	protected static final String PARAM_CONTAINER = "container";

	protected static final String PARAM_SITE = "site";

	protected static final String PARAM_REPOSITORY = "repo";

	protected static final String PARAM_NODEREF = "nodeRef";

	protected static final String PARAM_ITEMTYPE = "itemType";

	/** Pagination **/

	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";

	protected static final String PARAM_MAX_RESULTS = "maxResults";

	/** Services **/

	protected NodeService nodeService;

	protected AdvSearchService advSearchService;

	protected NamespaceService namespaceService;

	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	protected List<NodeRef> doSearch(WebScriptRequest req, Integer maxResults) throws JSONException {

		String query = req.getParameter(PARAM_QUERY);
		String sort = req.getParameter(PARAM_SORT);
		Map<String, Boolean> sortMap = WebscriptHelper.extractSortMap(sort, namespaceService);
		String term = req.getParameter(PARAM_TERM);
		String tag = req.getParameter(PARAM_TAG);
		String siteId = req.getParameter(PARAM_SITE);
		String containerId = req.getParameter(PARAM_CONTAINER);
		String repo = req.getParameter(PARAM_REPOSITORY);
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		BeCPGQueryBuilder queryBuilder = null;

		String nodeRef = req.getParameter(PARAM_NODEREF);
		if (nodeRef != null && !nodeRef.isEmpty()) {
			queryBuilder = BeCPGQueryBuilder.createQuery().inPath(getPath(nodeRef)).excludeSearch().excludeType(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			if (itemType != null && !itemType.isEmpty()) {
				queryBuilder.ofType(QName.createQName(itemType, namespaceService));
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
			criteriaMap = JSONHelper.extractCriteria(jsonObject);
			datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

		}

		if (queryBuilder == null) {
			queryBuilder = advSearchService.createSearchQuery(datatype, term, tag, isRepo, siteId, containerId);
		}

		queryBuilder.addSort(sortMap);

		return advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, maxResults != null ? maxResults : RepoConsts.MAX_RESULTS_256);

	}

	private String getPath(String nodeRef) {
		return nodeService.getPath(new NodeRef(nodeRef)).toPrefixString(namespaceService);
	}

}
