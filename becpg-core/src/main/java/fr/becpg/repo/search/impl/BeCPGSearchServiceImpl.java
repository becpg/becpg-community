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
package fr.becpg.repo.search.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * BeCPG Search Service
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 * 
 */
public class BeCPGSearchServiceImpl implements BeCPGSearchService {

	private static final String DEFAULT_FIELD_NAME = "keywords";

	private SearchService searchService;

	private String defaultSearchTemplate;

	private NamespaceService namespaceService;

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDefaultSearchTemplate(String defaultSearchTemplate) {
		this.defaultSearchTemplate = defaultSearchTemplate;
	}

	private static Log logger = LogFactory.getLog(BeCPGSearchServiceImpl.class);

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery) {
		return luceneSearch(runnedQuery, null, RepoConsts.MAX_RESULTS_UNLIMITED);
	}

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, int maxResults) {
		return luceneSearch(runnedQuery, null, maxResults);
	}

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort) {
		return luceneSearch(runnedQuery, sort, RepoConsts.MAX_RESULTS_UNLIMITED);
	}

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort, int maxResults) {
		return search(runnedQuery, sort, maxResults, SearchService.LANGUAGE_LUCENE);
	}
	
	@Override
	public List<NodeRef> lucenePaginatedSearch(String runnedQuery, Map<String, Boolean> sort, int page, int pageSize) {
		return search(runnedQuery, sort, page , pageSize, SearchService.LANGUAGE_LUCENE, RepoConsts.SPACES_STORE);
	}

	@Override
	public List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int maxResults, String searchLanguage) {
		return search(runnedQuery, sort, -1, maxResults, searchLanguage, RepoConsts.SPACES_STORE);
	}

	@Override
	public List<NodeRef> searchByPath(NodeRef parentNodeRef, String xPath) {

		return searchService.selectNodes(parentNodeRef, xPath, null, namespaceService, false);

	}
	
	
	

	@Override
	public List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int page,  int maxResults, String searchLanguage, StoreRef storeRef) {

		List<NodeRef> nodes = new LinkedList<NodeRef>();

		StopWatch watch = new StopWatch();
		watch.start();

		SearchParameters sp = new SearchParameters();
		sp.addStore(storeRef);
		sp.setLanguage(searchLanguage);
		sp.setQuery(runnedQuery);
		sp.addLocale(Locale.getDefault());
		sp.excludeDataInTheCurrentTransaction(false);


		if (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED) {
			sp.setLimitBy(LimitBy.UNLIMITED);
		} else {
			sp.setLimit(maxResults);
			sp.setMaxItems(maxResults);
			sp.setLimitBy(LimitBy.FINAL_SIZE);
		}
		
       if(page >0){
			sp.setSkipCount((page-1)*maxResults);
			sp.setMaxPermissionChecks(page*1000);
       } 
		

		if (SearchService.LANGUAGE_FTS_ALFRESCO.equals(searchLanguage)) {
			sp.setDefaultFieldName(DEFAULT_FIELD_NAME);
			sp.addQueryTemplate(DEFAULT_FIELD_NAME, defaultSearchTemplate);
		}

		if (sort != null) {
			for (Map.Entry<String, Boolean> kv : sort.entrySet()) {
				logger.debug("Add sort :" + kv.getKey() + " " + kv.getValue());
				sp.addSort(kv.getKey(), kv.getValue());
			}
		}

		ResultSet result = null;
		try {
			result = searchService.query(sp);
			if (result != null) {
				nodes = new LinkedList<NodeRef>(result.getNodeRefs());
			}
		} finally {
			if (result != null) {
				result.close();
			}
			watch.stop();
			if (watch.getTotalTimeSeconds() > 1) {
				logger.warn("Slow query [" + runnedQuery + "] executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + nodes.size());
			}

			if (logger.isDebugEnabled()) {
				logger.debug(runnedQuery + " executed in  " + watch.getTotalTimeSeconds() + " seconds - size results " + nodes.size());
			}
		}
		return nodes;
	}

}
