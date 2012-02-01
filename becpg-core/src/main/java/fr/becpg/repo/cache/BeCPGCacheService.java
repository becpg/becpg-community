package fr.becpg.repo.cache;


/**
 * 
 * @author matthieu
 *
 */
public interface BeCPGCacheService {

	public <T> T getFromUserCache(
			String cacheName, String cacheKey , 
			BeCPGCacheDataProviderCallBack<T> sigedCacheDataProviderCallBack);
	
	public void clearAllCaches();

}
