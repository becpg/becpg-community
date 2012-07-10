package fr.becpg.repo.cache;


/**
 * 
 * @author matthieu
 * Tenant Aware Cache
 */
public interface BeCPGCacheService {

	<T> T getFromCache(
			String cacheName, String cacheKey , 
			BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack);
	
	void clearAllCaches();

	void removeFromCache(String name, String aclsCacheKey);

}
