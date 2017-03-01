package fr.becpg.repo.search.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.search.PaginatedSearchCache;

@Service("paginatedSearchCache")
public class PaginatedSearchCacheImpl  implements PaginatedSearchCache {

		@Autowired
		BeCPGCacheService beCPGCacheService;
	
	
		@Override
		public List<NodeRef> getSearchResults(String queryId) {
			if(queryId!=null){
				return beCPGCacheService.getFromCache(PaginatedSearchCache.class.getName(), queryId);
			}
			
			return null;
		}
		
		@Override
		public String storeSearchResults(List<NodeRef> results) {
			String queryExecutionId = GUID.generate();
			beCPGCacheService.storeInCache(PaginatedSearchCache.class.getName(), queryExecutionId,results);
			return queryExecutionId;
		}
	
}
