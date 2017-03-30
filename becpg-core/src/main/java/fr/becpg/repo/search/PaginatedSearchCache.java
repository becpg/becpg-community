package fr.becpg.repo.search;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.datalist.data.MultiLevelListData;

public interface PaginatedSearchCache {
	
	public List<NodeRef> getSearchResults(String queryId);
	
	public String storeSearchResults(List<NodeRef> results);

	public MultiLevelListData getSearchMultiLevelResults(String queryExecutionId);

	public String storeMultiLevelSearchResults(MultiLevelListData listData);

}
