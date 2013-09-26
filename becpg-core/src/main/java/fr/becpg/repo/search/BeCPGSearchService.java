package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public interface BeCPGSearchService {

	List<NodeRef> luceneSearch(String runnedQuery);

	List<NodeRef> luceneSearch(String runnedQuery, int maxResults);

	List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort);
	
	List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort, int maxResults);
	
	List<NodeRef> lucenePaginatedSearch(String runnedQuery, Map<String, Boolean> sort, int page, int pageSize);

	List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int maxResults,
			String searchLanguage);
	
	List<NodeRef> search(String runnedQuery, Map<String, Boolean> sort, int page, int maxResults,
			String searchLanguage, StoreRef storeRef);

	List<NodeRef> searchByPath(NodeRef parentNodeRef, String xPath);

	

}
