package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PaginatedSearchCache {
	
	public List<NodeRef> getSearchResults(String queryId);
	
	public String storeSearchResults(List<NodeRef> results);

}
