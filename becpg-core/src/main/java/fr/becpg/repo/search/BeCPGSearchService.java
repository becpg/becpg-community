package fr.becpg.repo.search;

import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BeCPGSearchService {

	List<NodeRef> luceneSearch(String runnedQuery, int searchLimit);

	List<NodeRef> unProtLuceneSearch(String runnedQuery);

	List<NodeRef> unProtLuceneSearch(String runnedQuery, String[] sort, int searchLimit);

	List<NodeRef> suggestSearch(String runnedQuery, String[] sort, Locale locale);

}
