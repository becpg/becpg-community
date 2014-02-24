/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.search.impl.QueryBuilderHelper;

/**
 * @author matthieu
 * 
 */
public class BeCPGQueryBuilder {

	private static Log logger = LogFactory.getLog(BeCPGQueryBuilder.class);

	private static final String DEFAULT_FIELD_NAME = "keywords";
	private NamespaceService namespaceService;
	private SearchService searchService;
	private String defaultSearchTemplate;

	private Integer maxResults = RepoConsts.MAX_RESULTS_256;
	private NodeRef parentNodeRef;
	private QName type = null;
	private Set<QName> types = new HashSet<>();
	private String path = null;
	private String membersPath = null;
	private Set<NodeRef> ids = new HashSet<>();
	private Set<NodeRef> notIds = new HashSet<>();
	private Set<QName> notNullProps = new HashSet<>();
	private Set<QName> nullProps = new HashSet<>();
	private Map<QName, String> propQueriesMap = new HashMap<QName, String>();
	private Set<String> ftsQueries = new HashSet<>();

	private BeCPGQueryBuilder() {
		
	}

	public static BeCPGQueryBuilder createQuery() {
		return new BeCPGQueryBuilder();
	}

	public BeCPGQueryBuilder ofType(QName typeQname) {
		if (this.type != null) {
			logger.warn("Type is already set for this query.( old:" + type + " -  new: " + typeQname + ")");
		}
		this.type = typeQname;

		return this;
	}

	public BeCPGQueryBuilder inType(QName typeQname) {

		types.add(typeQname);

		return this;
	}

	public BeCPGQueryBuilder maxResults(int maxResults) {
		this.maxResults = maxResults;
		return this;
	}

	public BeCPGQueryBuilder parent(NodeRef parentNodeRef) {
		if (this.parentNodeRef != null) {
			logger.warn("ParentNodeRef is already set for this query.( old:" + this.parentNodeRef + " -  new: " + parentNodeRef + ")");
		}

		this.parentNodeRef = parentNodeRef;
		return this;
	}

	public BeCPGQueryBuilder members(String path) {
		if (this.membersPath != null) {
			logger.warn("Path is already set for this query.( old:" + this.membersPath + " -  new: " + path + ")");
		}
		this.membersPath = path;

		return this;
	}

	public BeCPGQueryBuilder inPath(String path) {
		if (this.path != null) {
			logger.warn("Path is already set for this query.( old:" + this.path + " -  new: " + path + ")");
		}
		this.path = path;

		return this;
	}

	public BeCPGQueryBuilder inSite(String siteId, String containerId) {
		String path = SiteHelper.SITES_SPACE_QNAME_PATH;
		if (siteId != null && siteId.length() > 0) {
			path += "cm:" + ISO9075.encode(siteId) + "/";
		} else {
			path += "*/";
		}
		if (containerId != null && containerId.length() > 0) {
			path += "cm:" + ISO9075.encode(containerId) + "/";
		} else {
			path += "*/";
		}
		inPath(path);

		return this;
	}

	public BeCPGQueryBuilder andID(NodeRef nodeRef) {
		this.ids.add(nodeRef);
		return this;
	}

	public BeCPGQueryBuilder andNotID(NodeRef nodeRef) {
		this.notIds.add(nodeRef);
		return this;
	}

	Map<String, Boolean> sortProps = new TreeMap<>();

	public BeCPGQueryBuilder addSort(Map<String, Boolean> sortMap) {
		
		this.sortProps.putAll(sortMap);

		return this;
	}

	public BeCPGQueryBuilder addSort(QName propToSort, boolean sortOrder) {
		sortProps.put(QueryBuilderHelper.getSortProp(propToSort), sortOrder);
		return this;
	}

	public BeCPGQueryBuilder isNotNull(QName propQName) {
		notNullProps.add(propQName);
		return this;
	}

	public BeCPGQueryBuilder isNull(QName propQName) {
		nullProps.add(propQName);
		return this;
	}

	public BeCPGQueryBuilder andFTSQuery(String ftsQuery) {
		ftsQueries.add(ftsQuery);
		return this;
	}

	public BeCPGQueryBuilder andProp(QName propQName, String propQuery) {
		propQueriesMap.put(propQName, propQuery);
		return this;
	}

	public BeCPGQueryBuilder andBetween(QName propQName, String start, String end) {
		propQueriesMap.put(propQName, String.format("[%s TO %s]", start, end));
		return this;
	}

	public List<NodeRef> list() {

		String runnedQuery = buildQuery();

		List<NodeRef> refs = new LinkedList<NodeRef>();

		if (RepoConsts.MAX_RESULTS_UNLIMITED == maxResults) {
			int page = 1;

			logger.info("Unlimited results ask -  start pagination");
			List<NodeRef> tmp = search(runnedQuery, QueryBuilderHelper.getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);

			if (tmp != null && !tmp.isEmpty()) {
				logger.info(" - Page 1:" + tmp.size());
				refs = tmp;
			}
			while (tmp != null && tmp.size() == RepoConsts.MAX_RESULTS_256) {
				page++;
				tmp = search(runnedQuery, QueryBuilderHelper.getSort(ContentModel.PROP_MODIFIED, false), page, RepoConsts.MAX_RESULTS_256);
				if (tmp != null && !tmp.isEmpty()) {
					logger.info(" - Page " + page + ":" + tmp.size());
					refs.addAll(tmp);
				}
			}

		} else {
			refs = search(runnedQuery, sortProps ,-1, maxResults);
		}

		return refs;
	}

	private String buildQuery() {
//		private QName type = null;
//		private Set<QName> types = new HashSet<>();
//		private Set<NodeRef> ids = new HashSet<>();
//		private Set<NodeRef> notIds = new HashSet<>();
//		private Set<QName> notNullProps = new HashSet<>();
//		private Set<QName> nullProps = new HashSet<>();
//		private Map<QName, String> propQueriesMap = new HashMap<QName, String>();
//		private Set<String> ftsQueries = new HashSet<>();
		
		StringBuilder runnedQuery = new StringBuilder();
		
		if(parentNodeRef!=null) {
			runnedQuery.append(QueryBuilderHelper.getCondParent(parentNodeRef, QueryBuilderHelper.Operator.AND));
		} else if(membersPath !=null) {
			runnedQuery.append(QueryBuilderHelper.getCondMembers(membersPath, QueryBuilderHelper.Operator.AND));
		} else if(path !=null) {
			runnedQuery.append(QueryBuilderHelper.getCondPath(path, QueryBuilderHelper.Operator.AND));
		} 
		
		if(type!=null) {
			runnedQuery.append(QueryBuilderHelper.getCondType(type));
		} else if(!types.isEmpty()) {
			
		}
		
		
		return null;
	}

	public NodeRef singleValue() {

		this.maxResults = RepoConsts.MAX_RESULTS_SINGLE_VALUE;
		List<NodeRef> ret = list();

		return ret != null && !ret.isEmpty() ? ret.get(0) : null;
	}



	private List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int page, int maxResults) {

		List<NodeRef> nodes = new LinkedList<NodeRef>();

		StopWatch watch = new StopWatch();
		watch.start();

		SearchParameters sp = new SearchParameters();
		sp.addStore(RepoConsts.SPACES_STORE);
		sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
		sp.setQuery(runnedQuery);
		sp.addLocale(Locale.getDefault());
		sp.excludeDataInTheCurrentTransaction(false);
		sp.setDefaultFieldName(DEFAULT_FIELD_NAME);
		sp.addQueryTemplate(DEFAULT_FIELD_NAME, defaultSearchTemplate);
		
		//
// AdVSearch Query should use eventual
		//http://docs.alfresco.com/4.2/index.jsp?topic=%2Fcom.alfresco.enterprise.doc%2Fconcepts%2Fintrans-metadata.html
//	    perform transactional execution of queries;
//	    execute queries transactionally, when possible, and fall back to eventual consistency; or
//	    always execute eventual consistency.
		//
//	    solr.query.cmis.queryConsistency
//	    solr.query.fts.queryConsistency
//		system.metadata-query-indexes.ignored=false

		

		//Force the database use if possible
		sp.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);
		
		if (maxResults == RepoConsts.MAX_RESULTS_UNLIMITED) {
			sp.setLimitBy(LimitBy.UNLIMITED);
		} else {
			sp.setLimit(maxResults);
			sp.setMaxItems(maxResults);
			sp.setLimitBy(LimitBy.FINAL_SIZE);
		}

		if (page > 0) {
			sp.setSkipCount((page - 1) * maxResults);
			sp.setMaxPermissionChecks(page * 1000);
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

	public NodeRef selectNodeByPath(NodeRef parentNodeRef, String xPath) {
		// return searchService.selectNodes(parentNodeRef, xPath, null,
		// namespaceService, false);
		return null;
	}

	/**
	 * @param aspectReportEntity
	 * @return
	 */
	public BeCPGQueryBuilder withAspect(QName aspectReportEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public BeCPGQueryBuilder excludeVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	public static final String DEFAULT_IGNORE_QUERY = " -TYPE:\"systemfolder\" -ASPECT:\"bcpg:entityTplAspect\" " + " -@cm\\:lockType:READ_ONLY_LOCK"
			+ " -ASPECT:\"bcpg:compositeVersion\"" + " -ASPECT:\"bcpg:hiddenFolder\"";

	/**
	 * 
	 */
	public void excludeDefaults() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param classDef
	 */
	public BeCPGQueryBuilder exclude(ClassDefinition classDef) {

		// query += LuceneHelper.exclude(classDef.isAspect() ?
		// LuceneHelper.getCondAspect(classQName) :
		// LuceneHelper.getCondType(classQName));

		return null;
	}
	
	public void excludeSearch() {
		// ftsQuery += " AND -TYPE:\"cm:thumbnail\" " +
		// "AND -TYPE:\"cm:failedThumbnail\" " + "AND -TYPE:\"cm:rating\" " +
		// "AND -TYPE:\"bcpg:entityListItem\" "
		// + "AND -TYPE:\"systemfolder\" " +
		// "AND -TYPE:\"rep:report\" AND -TYPE:\"fm:forum\" AND -TYPE:\"fm:forums\" ";
		//
		// // extract data type for this search - advanced search query is type
		// // specific
		// ftsQuery += " AND -ASPECT:\"bcpg:hiddenFolder\"" +
		// " AND -ASPECT:\"bcpg:compositeVersion\""
		// +
		// " AND -ASPECT:\"bcpg:entityTplAspect\" AND -ASPECT:\"sys:hidden\" ";

	}

}
