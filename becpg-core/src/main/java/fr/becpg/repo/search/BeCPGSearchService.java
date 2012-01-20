package fr.becpg.repo.search;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.search.permission.BeCPGPermissionFilter;

public interface BeCPGSearchService {


	List<NodeRef> luceneSearch(String runnedQuery, int searchLimit);

	List<NodeRef> unProtLuceneSearch(String runnedQuery);

	List<NodeRef> unProtLuceneSearch(String runnedQuery, Map<String, Boolean> sort, int searchLimit);

	List<NodeRef> suggestSearch(String runnedQuery, Map<String, Boolean> sort, Locale locale);

	List<NodeRef> search(String searchQuery, Map<String, Boolean> sortMap, int maxResults,
			BeCPGPermissionFilter beCPGPermissionFilter);

}
