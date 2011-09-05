package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface BeCPGSearchService {

	List<NodeRef> luceneSearch(String runnedQuery, int searchLimit);

	List<NodeRef> unProtLuceneSearch(String runnedQuery);

	List<NodeRef> unProtLuceneSearch(String runnedQuery, String[] sort);

	List<NodeRef> luceneSearchAll(String runnedQuery, String[] sort);

}
