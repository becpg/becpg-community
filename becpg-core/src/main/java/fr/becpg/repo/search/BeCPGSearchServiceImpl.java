package fr.becpg.repo.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.common.RepoConsts;

/**
 * BeCPG Search Service
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public class BeCPGSearchServiceImpl implements BeCPGSearchService{

	private final int SIZE_UNLIMITED = -1;
	
	private SearchService searchService;
	private SearchService unProtSearchService;

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public void setUnProtSearchService(SearchService unProtSearchService) {
		this.unProtSearchService = unProtSearchService;
	}

	private static Log logger = LogFactory.getLog(BeCPGSearchServiceImpl.class);

	/**
	 * @param runnedQuery
	 * @param searchLimit
	 * @return
	 */
	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, int searchLimit) {
		logger.debug("Run query: " + runnedQuery + " limit to "
				+ searchLimit + " results ");
		SearchParameters sp = new SearchParameters();
		sp.addStore(RepoConsts.SPACES_STORE);
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery(runnedQuery);
		sp.setLimit(searchLimit);
		sp.setLimitBy(LimitBy.FINAL_SIZE);
		sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);
		sp.excludeDataInTheCurrentTransaction(false);
		sp.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
		ResultSet result = searchService.query(sp);
		try {
			if (result != null) {
				return new LinkedList<NodeRef>(result.getNodeRefs());
			}
		} finally {
			if (result != null) {
				result.close();
			}
		}
		return new LinkedList<NodeRef>();
	}

	/**
	 * @param runnedQuery
	 * @param searchLimit
	 * @return
	 */
	@Override
	public List<NodeRef> unProtLuceneSearch(String runnedQuery) {
		return unProtLuceneSearch(runnedQuery, null, SIZE_UNLIMITED);
	}

	@Override
	public List<NodeRef> unProtLuceneSearch(String runnedQuery, Map<String, Boolean> sort, int searchLimit) {
		List<NodeRef> nodes = new LinkedList<NodeRef>();
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		SearchParameters sp = new SearchParameters();
		sp.addStore(RepoConsts.SPACES_STORE);
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery(runnedQuery);
		
		if(searchLimit == SIZE_UNLIMITED){
			sp.setLimitBy(LimitBy.UNLIMITED);
		}
		else{
			sp.setLimit(searchLimit);
			sp.setMaxItems(searchLimit);
			sp.setLimitBy(LimitBy.FINAL_SIZE);			
		}
		
		sp.excludeDataInTheCurrentTransaction(false);
		if (sort != null) {
			for(Map.Entry<String, Boolean> kv : sort.entrySet()){
				sp.addSort(kv.getKey(), kv.getValue());
			}
		} 
		ResultSet result = unProtSearchService.query(sp);
		try {
			if (result != null) {
				nodes =  new LinkedList<NodeRef>(result.getNodeRefs());
			}
		} finally {
			if (result != null) {
				result.close();
			}
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(runnedQuery + " executed in  "
						+ watch.getTotalTimeSeconds() + " seconds - size results "
						+ nodes.size());
			}
		}
		return nodes;
	}

	@Override
	public List<NodeRef> suggestSearch(String runnedQuery, Map<String, Boolean> sort, Locale locale) {
		
		List<NodeRef> nodes = new LinkedList<NodeRef>();
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		SearchParameters sp = new SearchParameters();
		sp.addStore(RepoConsts.SPACES_STORE);
		sp.setLanguage(SearchService.LANGUAGE_LUCENE);
		sp.setQuery(runnedQuery);
		sp.setLimitBy(LimitBy.FINAL_SIZE);
	    sp.setLimit(RepoConsts.MAX_SUGGESTIONS);        
	    sp.setMaxItems(RepoConsts.MAX_SUGGESTIONS);
		sp.setDefaultOperator(Operator.AND);
		sp.excludeDataInTheCurrentTransaction(false);
		if(locale!=null){
			sp.addLocale(locale);
		}
		
		if (sort != null) {
			for(Map.Entry<String, Boolean> kv : sort.entrySet()){
				sp.addSort(kv.getKey(), kv.getValue());
			}			
		}

		ResultSet result = searchService.query(sp);
		try {
			if (result != null) {
				nodes = new LinkedList<NodeRef>(result.getNodeRefs());
			}
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(runnedQuery + " executed in  "
						+ watch.getTotalTimeSeconds() + " seconds");
				if(result!=null){
					logger.debug("Found "+result.length()+" results");
				}
				if(locale!=null){
					logger.debug("Locale use for search: "+locale.toString());
				}
			}
			if (result != null) {
				result.close();
			}
			
			
		}
		
		return nodes;
	}

}
