package fr.becpg.repo.cache.impl;

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * 
 * @author matthieu
 */
public class BeCPGCacheServiceImpl implements InitializingBean, DisposableBean, BeCPGCacheService {

	private static Log logger = LogFactory.getLog(BeCPGCacheServiceImpl.class);

	private CacheManager cacheManager;
	private Resource configLocation;
	private TenantService tenantService;


	
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	@Override
	public void afterPropertiesSet() throws IOException, CacheException {
		PropertyCheck.mandatory(this, "configLocation", configLocation);

		logger.debug("Init beCPG Cache");
		cacheManager = new CacheManager(this.configLocation.getURL());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getFromCache(String cacheName, String cacheKey,
			BeCPGCacheDataProviderCallBack<T> sigedCacheDataProviderCallBack) {
		
		cacheKey = computeCacheKey(cacheKey);
		Cache cache = getCache(cacheName);
		T ret = null;
		try {
			
			Element el = cache.get(cacheKey);
			if (el != null) {
				logger.debug("Getting values from " + cacheKey);
				ret = (T) el.getObjectValue();
			}
		} catch (Exception e) {
			logger.error("Cannot get " + cacheKey + " from cache " + cacheName, e);
		}

		if (ret == null) {
			ret = sigedCacheDataProviderCallBack.getData();
			if(ret!=null){
				logger.debug("Store values to " + cacheKey);
				cache.put(new Element(cacheKey, ret));
			}
		}

		return ret;
	}
	
	@Override
	public void removeFromCache(String cacheName, String cacheKey) {
		cacheKey = computeCacheKey(cacheKey);
		Cache cache = getCache(cacheName);
		cache.remove(cacheKey);
		
	}
	
	@Override
	public void destroy() {
		logger.info("Close beCPG cache");
		cacheManager.shutdown();
	}

	@Override
	public void clearAllCaches() {
		logger.info("Clear all cache");
		cacheManager.clearAll();
	}

	

	private String computeCacheKey(String cacheKey) {
		return cacheKey+"@"+tenantService.getCurrentUserDomain();
	}

	private Cache getCache(String cacheName) {
		if(!cacheManager.cacheExists(cacheName)){
			cacheManager.addCache(cacheName);
		}
		return cacheManager.getCache(cacheName);
	}

	
	

}
