package fr.becpg.repo.cache;

import java.util.Collection;

/**
 * 
 * @author matthieu Tenant Aware Cache
 */
public interface BeCPGCacheService {

	<T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack);

	void clearAllCaches();

	void removeFromCache(String name, String cacheKey);
	
	Collection<String> getCacheKeys(String cacheName);

	void printCacheInfos();

	<T> T getFromCache(String cacheName, String cacheKey, BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack, boolean deleteOnTxRollback);

	
}
