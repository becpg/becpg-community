package fr.becpg.repo.cache;


/**
 * 
 * @author matthieu
 *
 */
public interface BeCPGCacheService {

	public <T> T getFromCache(
			String cacheName, String cacheKey , 
			BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack);
	
	public void clearAllCaches();

}
