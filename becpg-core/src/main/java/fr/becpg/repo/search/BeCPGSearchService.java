package fr.becpg.repo.search;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BeCPGSearchService {

	List<NodeRef> luceneSearch(String runnedQuery);

	List<NodeRef> luceneSearch(String runnedQuery, int maxResults);

	List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort);
	
	List<NodeRef> luceneSearch(String runnedQuery, Map<String, Boolean> sort, int maxResults);

	List<NodeRef> search(String searchQuery, Map<String, Boolean> sortMap, int maxResults,
			String searchLanguage);

	List<NodeRef> searchByPath(NodeRef parentNodeRef, String xPath);

}
