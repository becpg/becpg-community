package fr.becpg.repo.cache.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * 
 * @author matthieu
 * TODO refactor with new simplecache or simply delete
 */
@Service
public class BeCPGCacheServiceImpl implements  BeCPGCacheService {

	private static Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);
	
	private int maxCacheItems = 500;
	
	
	private TenantService tenantService;

	
	private Map<String,SimpleCache<Serializable, ?>> caches = new ConcurrentHashMap<String,SimpleCache<Serializable, ?>>();



	public void setMaxCacheItems(int maxCacheItems) {
		this.maxCacheItems = maxCacheItems;
	}

	
	
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}


	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFromCache(String cacheName, String cacheKey,
			BeCPGCacheDataProviderCallBack<T> cacheDataProviderCallBack) {
		
		
		cacheKey = computeCacheKey(cacheKey);
		SimpleCache<Serializable, T> cache = (SimpleCache<Serializable, T>) getCache(cacheName);
		T ret = null;
		try {
			logger.debug("Getting values from " + cacheKey);
			ret = cache.get(cacheKey);
			
		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret == null) {
			ret = cacheDataProviderCallBack.getData();
			if(ret!=null){
				logger.debug("Store values to " + cacheKey);
				cache.put(cacheKey, ret);
			}
		}

		return ret;
	}
	
	@Override
	public void removeFromCache(String cacheName, String cacheKey) {
		cacheKey = computeCacheKey(cacheKey);
		SimpleCache<Serializable, ?> cache = getCache(cacheName);
		cache.remove(cacheKey);
		
	}
	

	@Override
	public void clearAllCaches() {
		logger.info("Clear all cache");
		for(SimpleCache< Serializable, ?> cache : caches.values()){
			cache.clear();
		}
		
	}

	

	private String computeCacheKey(String cacheKey) {
		return cacheKey+"@"+tenantService.getCurrentUserDomain();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SimpleCache<Serializable, ?> getCache(String cacheName) {
		if(!caches.containsKey(cacheName)){
			caches.put(cacheName, new DefaultSimpleCache(maxCacheItems, cacheName));
		}
		return caches.get(cacheName);
	}

	
	

}
