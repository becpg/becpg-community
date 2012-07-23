package fr.becpg.repo.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.RepoConsts;

/**
 * BeCPG Search Service
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
@Service
public class BeCPGSearchServiceImpl implements BeCPGSearchService{

	public static final int SIZE_UNLIMITED = -1;
	
	private static final String DEFAULT_FIELD_NAME = "keywords";
	
	private static final String QUERY_TEMPLATES = "%(cm:name cm:title cm:description ia:whatEvent ia:descriptionEvent lnk:title lnk:description TEXT)";
	
	private SearchService searchService;
	
	private NamespaceService namespaceService;


	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	private static Log logger = LogFactory.getLog(BeCPGSearchServiceImpl.class);

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery) {
		return luceneSearch(runnedQuery, null, SIZE_UNLIMITED);
	}
	
	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, int maxResults) {
		return luceneSearch(runnedQuery, null, maxResults);
	}
	

	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort) {
		return luceneSearch(runnedQuery, sort, SIZE_UNLIMITED);
	}
		
	@Override
	public List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort, int maxResults) {
		return search(runnedQuery, sort, maxResults, SearchService.LANGUAGE_LUCENE);
	}

	@Override
	public List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int maxResults,
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
	        sp.setQuery(runnedQuery);	
	        sp.addLocale(Locale.getDefault());
	    	sp.excludeDataInTheCurrentTransaction(false);
	    	//sp.setDefaultOperator(Operator.AND);
	    	
	        if(maxResults == SIZE_UNLIMITED){
				sp.setLimitBy(LimitBy.UNLIMITED);
			}
			else{
				sp.setLimit(maxResults);
				sp.setMaxItems(maxResults);
				sp.setLimitBy(LimitBy.FINAL_SIZE);			
			}
	        
	        
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
				result = searchService.query(sp);
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
	public List<NodeRef> searchByPath(NodeRef parentNodeRef, String xPath) {

		return searchService.selectNodes(parentNodeRef, 
				xPath, null, namespaceService, false);
		
	}

	


	
	
}
