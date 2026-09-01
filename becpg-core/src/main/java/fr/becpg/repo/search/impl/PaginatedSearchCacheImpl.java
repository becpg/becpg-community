package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * <p>PaginatedSearchCacheImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("paginatedSearchCache")
public class PaginatedSearchCacheImpl  implements PaginatedSearchCache {

		private static final String CACHE_KEY = PaginatedSearchCache.class.getName();
		
		private static final String CACHE_KEY_MULTI_LEVEL = PaginatedSearchCache.class.getName()+".multiLevel";
		
	
		@Autowired
		BeCPGCacheService beCPGCacheService;

	
		/** {@inheritDoc} */
		/**
		 * <p>Scopes a query identifier to the user it was issued for.</p>
		 *
		 * The identifier travels to the client and comes back on the next page, so it must not be enough
		 * on its own to read the results back: a cached list is the outcome of a search run with one
		 * user's permissions, and it is not filtered again on the way out.
		 *
		 * @param queryId a {@link java.lang.String} object
		 * @return the cache key for the current user
		 */
		private String cacheKey(String queryId) {
			String userName = AuthenticationUtil.getFullyAuthenticatedUser();

			return ((userName != null) ? userName : AuthenticationUtil.SYSTEM_USER_NAME) + "|" + queryId;
		}

		@Override
		public List<NodeRef> getSearchResults(String queryId) {
			List<NodeRef> ret = null;
			
			if(queryId!=null){
				ret =  beCPGCacheService.getFromCache(CACHE_KEY, cacheKey(queryId));
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
			List<NodeRef> ret = new ArrayList<>();
			
			for (Entry<NodeRef, MultiLevelListData> entry : data.getTree().entrySet()) {
				NodeRef nodeRef = entry.getKey();
				ret.add(nodeRef);
				ret.addAll(extract(entry.getValue()));
			}
			
			return ret;
		}

		/** {@inheritDoc} */
		@Override
		public String storeSearchResults(List<NodeRef> results) {
			String queryExecutionId = GUID.generate();
			beCPGCacheService.storeInCache(CACHE_KEY, cacheKey(queryExecutionId), results);
			return queryExecutionId;
		}
		
		

		/** {@inheritDoc} */
		@Override
		public MultiLevelListData getSearchMultiLevelResults(String queryId) {
			if(queryId!=null){
				return beCPGCacheService.getFromCache(CACHE_KEY_MULTI_LEVEL, cacheKey(queryId));
			}
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public String storeMultiLevelSearchResults(MultiLevelListData listData) {
			String queryExecutionId = GUID.generate();
			beCPGCacheService.storeInCache(CACHE_KEY_MULTI_LEVEL, cacheKey(queryExecutionId), listData);
			return queryExecutionId;
		}
	
}
