package fr.becpg.repo.search.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.search.PaginatedSearchCache;

@Service("paginatedSearchCache")
public class PaginatedSearchCacheImpl  implements PaginatedSearchCache {

		private static final String CACHE_KEY = PaginatedSearchCache.class.getName();
		
		private static final String CACHE_KEY_MULTI_LEVEL = PaginatedSearchCache.class.getName()+".multiLevel";
		
	
		@Autowired
		BeCPGCacheService beCPGCacheService;

	
		@Override
		public List<NodeRef> getSearchResults(String queryId) {
			List<NodeRef> ret = null;
			
			if(queryId!=null){
				ret =  beCPGCacheService.getFromCache(CACHE_KEY, queryId);
				if( ret == null){
					MultiLevelListData data = getSearchMultiLevelResults(queryId);
					if(data!=null){
						ret = extract(data);
					}
				}
			}
			
			return ret;
		}
		
		private List<NodeRef> extract(MultiLevelListData data) {
			List<NodeRef> ret = new LinkedList<>();
			
			for (Entry<NodeRef, MultiLevelListData> entry : data.getTree().entrySet()) {
				NodeRef nodeRef = entry.getKey();
				ret.add(nodeRef);
				ret.addAll(extract(entry.getValue()));
			}
			
			return ret;
		}

		@Override
		public String storeSearchResults(List<NodeRef> results) {
			String queryExecutionId = GUID.generate();
			beCPGCacheService.storeInCache(CACHE_KEY, queryExecutionId,results);
			return queryExecutionId;
		}
		
		

		@Override
		public MultiLevelListData getSearchMultiLevelResults(String queryId) {
			if(queryId!=null){
				return beCPGCacheService.getFromCache(CACHE_KEY_MULTI_LEVEL, queryId);
			}
			return null;
		}

		@Override
		public String storeMultiLevelSearchResults(MultiLevelListData listData) {
			String queryExecutionId = GUID.generate();
			beCPGCacheService.storeInCache(CACHE_KEY_MULTI_LEVEL, queryExecutionId,listData);
			return queryExecutionId;
		}
	
}
