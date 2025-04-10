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
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.search.AdvSearchService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * <p>Abstract AbstractSearchWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractSearchWebScript extends AbstractWebScript {

	/** Constant <code>PARAM_QUERY="query"</code> */
	protected static final String PARAM_QUERY = "query";

	/** Constant <code>PARAM_SORT="sort"</code> */
	protected static final String PARAM_SORT = "sort";

	/** Constant <code>PARAM_TERM="term"</code> */
	protected static final String PARAM_TERM = "term";

	/** Constant <code>PARAM_TAG="tag"</code> */
	protected static final String PARAM_TAG = "tag";

	/** Constant <code>PARAM_CONTAINER="container"</code> */
	protected static final String PARAM_CONTAINER = "container";

	/** Constant <code>PARAM_SITE="site"</code> */
	protected static final String PARAM_SITE = "site";

	/** Constant <code>PARAM_REPOSITORY="repo"</code> */
	protected static final String PARAM_REPOSITORY = "repo";

	/** Constant <code>PARAM_NODEREF="nodeRef"</code> */
	protected static final String PARAM_NODEREF = "nodeRef";

	/** Constant <code>PARAM_ITEMTYPE="itemType"</code> */
	protected static final String PARAM_ITEMTYPE = "itemType";

	/** Pagination **/

	protected static final String PARAM_PAGE = "page";

	/** Constant <code>PARAM_PAGE_SIZE="pageSize"</code> */
	protected static final String PARAM_PAGE_SIZE = "pageSize";

	/** Constant <code>PARAM_MAX_RESULTS="maxResults"</code> */
	protected static final String PARAM_MAX_RESULTS = "maxResults";

	/** Services **/

	/**
	 * The node service for handling node operations.
	 */
	protected NodeService nodeService;

	/**
	 * The advanced search service for performing complex searches.
	 */
	protected AdvSearchService advSearchService;

	/**
	 * The namespace service for handling namespace operations.
	 */
	protected NamespaceService namespaceService;

	/**
	 * <p>Setter for the field <code>advSearchService</code>.</p>
	 *
	 * @param advSearchService a {@link fr.becpg.repo.search.AdvSearchService} object.
	 */
	public void setAdvSearchService(AdvSearchService advSearchService) {
		this.advSearchService = advSearchService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
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
	 * <p>doSearch.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @param maxResults a {@link java.lang.Integer} object.
	 * @return a {@link java.util.List} object.
	 * @throws org.json.JSONException if any.
	 */
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
			criteriaMap = JsonHelper.extractCriteria(jsonObject);
			datatype = QName.createQName(jsonObject.getString("datatype"), namespaceService);

		}

		if (queryBuilder == null) {
			queryBuilder = advSearchService.createSearchQuery(datatype, term, tag, isRepo, siteId, containerId);
		}

		queryBuilder.andOperator().addSort(sortMap);
		

		return advSearchService.queryAdvSearch(datatype, queryBuilder, criteriaMap, maxResults != null ? maxResults : RepoConsts.MAX_RESULTS_256);

	}

	private String getPath(String nodeRef) {
		return nodeService.getPath(new NodeRef(nodeRef)).toPrefixString(namespaceService);
	}

}
