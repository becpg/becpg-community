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

import fr.becpg.repo.RepoConsts;

/**
 * BeCPG Search Service
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public class BeCPGSearchServiceImpl implements BeCPGSearchService{

	private final int SIZE_UNLIMITED = -1;
	
	private static final String DEFAULT_FIELD_NAME = "keywords";
	
	private static final String QUERY_TEMPLATES = "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT)";
	
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
		sp.setLimit(searchLimit);
		sp.setLimitBy(LimitBy.FINAL_SIZE);
		sp.addLocale(Locale.getDefault());
		sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);
		sp.excludeDataInTheCurrentTransaction(false);
		sp.addSort(SearchParameters.SORT_IN_DOCUMENT_ORDER_DESCENDING);
		ResultSet result = null;
		try {
			result = searchService.query(sp);
			if (result != null) {
				nodes =   new LinkedList<NodeRef>(result.getNodeRefs());
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
		sp.addLocale(Locale.getDefault());
		
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
		ResultSet result = null;
		try {
			result = unProtSearchService.query(sp);
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
						+  nodes.size() );
			}
		}
		return nodes;
	}

	@Override
	public List<NodeRef> suggestSearch(String runnedQuery, Map<String, Boolean> sort) {
		
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
		sp.addLocale(Locale.getDefault());
		
		if (sort != null) {
			for(Map.Entry<String, Boolean> kv : sort.entrySet()){
				logger.debug("Add sort :"+kv.getKey()+" "+ kv.getValue());
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
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(runnedQuery + " executed in  "
						+ watch.getTotalTimeSeconds() + " seconds");
				if(result!=null){
					logger.debug("Found "+result.length()+" results");
				}
			}
			if (result != null) {
				result.close();
			}
			
			
		}
		
		return nodes;
	}

	@Override
	public List<NodeRef> search(String searchQuery, Map<String, Boolean> sort, int maxResults,
			String searchLanguage ) {

			List<NodeRef> nodes = new LinkedList<NodeRef>();
			
			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
			
			SearchParameters sp = new SearchParameters();
	        sp.addStore(RepoConsts.SPACES_STORE);
	        sp.setLanguage(searchLanguage);
	        sp.setQuery(searchQuery);	        
	        
	        if(maxResults == SIZE_UNLIMITED){
				sp.setLimitBy(LimitBy.UNLIMITED);
			}
			else{
				sp.setLimit(maxResults);
				sp.setMaxItems(maxResults);
				sp.setLimitBy(LimitBy.FINAL_SIZE);			
			}
	        sp.addLocale(Locale.getDefault());
	        if(SearchService.LANGUAGE_FTS_ALFRESCO.equals(searchLanguage)){
		        sp.setDefaultFieldName(DEFAULT_FIELD_NAME);
		        sp.addQueryTemplate(DEFAULT_FIELD_NAME, QUERY_TEMPLATES);
	        }
	        

			if (sort != null) {
				for(Map.Entry<String, Boolean> kv : sort.entrySet()){
					logger.debug("Add sort :"+kv.getKey()+" "+ kv.getValue());
					sp.addSort(kv.getKey(), kv.getValue());
				}			
			}
	        
			ResultSet result = null;
			try {
				if(maxResults == SIZE_UNLIMITED){
					 result = unProtSearchService.query(sp);
				} else {
					 result = searchService.query(sp);
				}
				if (result != null) {
					nodes =  new LinkedList<NodeRef>(result.getNodeRefs());
				}
			} finally {
				if (result != null) {
					result.close();
				}
				if (logger.isDebugEnabled()) {
					watch.stop();
					logger.debug(searchQuery + " executed in  "
							+ watch.getTotalTimeSeconds() + " seconds - size results "
							+  nodes.size());
				}
			}
			
			return nodes;
	}

	


	
	
}
